package ph.hatch.ddd.oe;

import com.google.gson.Gson;
import org.reflections.ReflectionUtils;
import ph.hatch.ddd.domain.annotations.DomainEntity;
import ph.hatch.ddd.domain.annotations.DomainEntityIdentity;
import ph.hatch.ddd.oe.annotations.ExploredMethod;

import java.lang.reflect.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.reflections.ReflectionUtils.withAnnotation;

/**
 * Created by jett on 4/26/15.
 */
public class ObjectExplorer {

    static final Logger log = Logger.getLogger(ObjectRegistry.class.getName());

    ObjectRegistry objectRegistry;
    ObjectRepository objectRepository;

    private String dateFormat = "MM/dd/yyyy";
    private boolean areNullsIncluded = false;

    private Gson gson;

    public ObjectExplorer(ObjectRegistry objectRegistry, ObjectRepository objectRepository) {

        this.objectRegistry = objectRegistry;
        this.objectRepository = objectRepository;

    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public void includeNulls(Boolean includeNulls) {
        this.areNullsIncluded = includeNulls;
    }

    // this is just for display purposes
    private static String repeat(String str, int times) {
        return new String(new char[times]).replace("\0", str);
    }

    public Map explore(Object object, Boolean includeNull) {

        HashSet visited = new HashSet();        // track visited objects in a cycle

        log.log(Level.FINE, "start expanding");
        log.log(Level.FINE, "%s\n", object.getClass().getSimpleName());
        //hierarchy.put(object.getClass().getSimpleName(), expandHierarchy(object, visited, 1));

        return expandHierarchy(object, visited, 1);
    }

    private Map expandHierarchy(Object object, HashSet visited, Integer level) {

        Map hierarchy = new HashMap<>();

        Set<Field> fields=new HashSet<Field>();
        Set<Method> methods = new HashSet<Method>();

        Class classToExpand = object.getClass();

        // get all the fields for this class (including those belonging to the superclass(es))
        while (classToExpand != Object.class) {
            fields.addAll(ReflectionUtils.getFields(classToExpand));
            methods.addAll(ReflectionUtils.getAllMethods(classToExpand, withAnnotation(ExploredMethod.class)));

            classToExpand = classToExpand.getSuperclass();
        }

        for(Method method : methods) {

            String methodReturnType = method.getReturnType().getCanonicalName();
            String entityClassForReturnType = objectRegistry.getClassForEntityIdentityField(methodReturnType);

            String fieldName = method.getName();

            try {
                method.setAccessible(true);
                Object methodReturn = method.invoke(object);

                System.out.printf("%s+- %s(m) : %s\n", repeat("\t", level), fieldName, methodReturn);

                Object result = null;

                // if it is not an entity identity, return as is
                if(entityClassForReturnType == null) {

                    hierarchy.put(fieldName, methodReturn);

                } else {

                    result = objectRepository.load((Class<DomainEntity>) Class.forName(entityClassForReturnType), methodReturn);

                    // if a matching record was found, go dig for it
                    if (result == null) {
                        if (areNullsIncluded) {
                            hierarchy.put(fieldName, "");
                        }
                    } else {
                        hierarchy.put(fieldName, expandHierarchy(result, visited, level + 1));
                    }
                }


            } catch(InvocationTargetException | IllegalAccessException | ClassNotFoundException e) {
                log.log(Level.SEVERE, "could not invoke ExploredProperty method");
            }

        }

        for(Field field : fields) {

            String fieldName = field.getName();

            try {

                Field targetField = field;
                targetField.setAccessible(true);

                Object objectValue = targetField.get(object);

                System.out.printf("%s+- %s : %s\n", repeat("\t", level), targetField.getName(), objectValue);

                // check if field is the object's entity identity, if it is, store it in our map and carry on
                if (targetField.isAnnotationPresent(DomainEntityIdentity.class)) {

                    hierarchy.put(fieldName, objectValue.toString());

                } else if (objectRegistry.getClassForEntityIdentityField(targetField.getType().getCanonicalName()) != null) {

                    // check if the field has an associated entity
                    if (objectValue == null) {

                        if(areNullsIncluded) {
                            hierarchy.put(fieldName, "");
                        }

                    } else {

                        String otherFieldEntityIdentity = objectRegistry.getClassForEntityIdentityField(targetField.getType().getCanonicalName());

                        Class<?> cl = Class.forName(targetField.getType().getCanonicalName());
                        Constructor<?> cons = cl.getConstructor(String.class);

                        Object o = cons.newInstance(objectValue.toString());

                        Object result = null;

                        String identityField = objectRegistry.getEntityIdentityFieldname(Class.forName(otherFieldEntityIdentity));
                        result = objectRepository.load((Class<DomainEntity>) Class.forName(otherFieldEntityIdentity), o);

                        // if a matching record was found, go dig for it
                        if (result == null) {
                            if(areNullsIncluded) {
                                hierarchy.put(fieldName, "");
                            }
                        } else {
                            hierarchy.put(fieldName, expandHierarchy(result, visited, level + 1));
                        }
                    }


                } else if (Collection.class.isAssignableFrom(field.getType())) {

                    // if field is a collection
                    try {
                        // only get parameterized types
                        if (field.getGenericType() instanceof ParameterizedType) {

                            ParameterizedType objectListType = (ParameterizedType) field.getGenericType();
                            Class setClass = (Class<?>) objectListType.getActualTypeArguments()[0];

                            // if the collection is a set of DomainEntityIdentities
                            if(objectRegistry.getClassForEntityIdentityField(setClass.getCanonicalName()) != null) {

                                Map setMap = new HashMap<>();

                                if(objectValue != null) {

                                    // loop through items in the set (if collection is not empty)
                                    for (Object identityObject : (Collection) objectValue) {

                                        String otherFieldEntityIdentity = objectRegistry.getClassForEntityIdentityField(setClass.getCanonicalName());
                                        Object result = objectRepository.load((Class<DomainEntity>) Class.forName(otherFieldEntityIdentity), identityObject);

                                        if (result == null) {
                                            setMap.put(identityObject, "");
                                        } else {
                                            setMap.put(identityObject, expandHierarchy(result, visited, level + 1));
                                        }
                                    }
                                }

                                hierarchy.put(fieldName, setMap);

                            } else {

                                // if the setClass was not an DomainEntityIdentity, then it must be a different
                                // class

                                Map setMap = new HashMap<>();

                                Integer elementCount = 1;

                                // loop through items in the set
                                for(Object genericSetObject: (Collection) objectValue) {

                                    setMap.put(elementCount, expandHierarchy(genericSetObject, visited, level + 1));
                                    elementCount++;

                                }

                                hierarchy.put(fieldName, setMap);

                            }
                        }

                    } catch (TypeNotPresentException tnpe) {

                        log.log(Level.SEVERE, "field type was not found.");
                    }


                } else {

                    // just a "normal" field, add to our Map and carry on
                    hierarchy.put(fieldName, objectValue == null ? "" : objectValue.toString());

                }

            } catch(IllegalAccessException iae) {
                // todo: add exception handling
                log.log(Level.SEVERE, "Illegal Access Exception");
            } catch(ClassNotFoundException cnfe ) {
                // todo: add exception handling
                log.log(Level.SEVERE, "Class not found Exception");
            }  catch(NoSuchMethodException nsme) {
                // todo: add exception handling
                log.log(Level.SEVERE, "Method not Found Exceptions");
            } catch(InstantiationException ie) {
                // todo: add exception handling
                log.log(Level.SEVERE, "Instantiation Exception");
            } catch(InvocationTargetException ite) {
                // todo: add exception handling
                log.log(Level.SEVERE, "InvocationTarget Exception");
            }

        }

        return hierarchy;
    }

}
