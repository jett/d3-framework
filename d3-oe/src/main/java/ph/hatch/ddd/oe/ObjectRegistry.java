package ph.hatch.ddd.oe;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.reflections.Configuration;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import ph.hatch.ddd.domain.annotations.DomainEntity;
import ph.hatch.ddd.domain.annotations.DomainEntityIdentity;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.reflections.ReflectionUtils.withAnnotation;

public class ObjectRegistry {

    static final Logger log = Logger.getLogger(ObjectRegistry.class.getName());

    private BiMap entityStore;          // stores all the Entities and identifiers
    private Map identityStore;          // stores names of the entity identity fields
    private Map<String, ObjectMeta> metaStore;    // stores meta for all classes

    private Map entityStore1;
    private Map entityStore2;

    public ObjectRegistry(String... packageNames) {

        entityStore = HashBiMap.create();

        entityStore1 = new HashMap();
        entityStore2 = new HashMap();
        identityStore = new HashMap();

        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        //Configuration config = new ConfigurationBuilder().addUrls(url);

        for(String packageName : packageNames) {

            Set<URL> url = ClasspathHelper.forPackage(packageName);
            configurationBuilder.addUrls(url);

        }

        metaStore = new HashMap();

        Configuration config = configurationBuilder;
        Reflections reflections = new Reflections(config);

        Set<Class<?>> entities = reflections.getTypesAnnotatedWith(DomainEntity.class);

        for(Class entity : entities) {

            // get identity fields
//            try {

               log.log(Level.FINE, "registering {0}", entity.getCanonicalName());

                Set<Field> fields=new HashSet<Field>();

                Class<?> classInEntity = (Class<?>) entity;
                Class targetClass = null;

                try {

                    // add all the fields of the @DomainEntity up the hierarchy
                    // todo: 4/26 i am here
                    while (classInEntity.isAnnotationPresent(DomainEntity.class)) {

                        fields.addAll(ReflectionUtils.getFields(classInEntity, withAnnotation(DomainEntityIdentity.class)));

                        if(!classInEntity.getSuperclass().isAnnotationPresent(DomainEntityIdentity.class)) {
                            targetClass = classInEntity;
                        }

                        classInEntity = (Class<?>) classInEntity.getSuperclass();
                    }

                     //fields = ReflectionUtils.getFields(entity, withAnnotation(DomainEntityIdentity.class));

                    // TODO : experimental fix
                    // Update Jett 5/31 : we do not do this because we always use the EntityIdentity of the base class

                    // added to also store entity identity lookups for subclasses
                    //if(fields == null) {
//                    if(fields.size() == 0) {
//
//                        Set<Class<?>> superClazzes = ReflectionUtils.getAllSuperTypes(entity);
//                        for(Class superClass : superClazzes) {
//                            if(fields == null) {
//                            if(fields.size() == 0) {
//                                fields = ReflectionUtils.getFields(superClass, withAnnotation(DomainEntityIdentity.class));
//                            }
//                        }
//                    }

                } catch(NoClassDefFoundError ncdfe) {

                    log.log(Level.SEVERE, "No class definition found for fields of the class!");

                }

                if(fields != null && fields.size() == 1) {

                    Field identityField =  (Field) fields.toArray()[0];

                    log.log(Level.INFO, "adding: {0} : {1} ", new Object[]{ entity.getCanonicalName() , identityField.getType().getCanonicalName()});

                    // jett 5/2 : store the oldest ancestor with the @DomainEntity attribute (old was entity.)
                    identityStore.put(targetClass, identityField.getName());

                    //entityStore.put(entity.getCanonicalName(), identityField.getName());
                    // TODO, bug here if type is one of the base types

                    if(!identityField.getType().getCanonicalName().equalsIgnoreCase(String.class.getCanonicalName()) ) {

                        if(entityStore.inverse().containsKey(identityField.getType().getCanonicalName())) {

                            // System.out.println("Type " + identityField.getType().getCanonicalName() + " is already the key for another class");
                            log.log(Level.WARNING, "Type {0} for {1} is already the key for another class ({3})", new Object[]{identityField.getType().getCanonicalName(), entity, entityStore.inverse().get(identityField.getType().getCanonicalName())});

                        } else {

                            // jett 5/2 : store the oldest ancestor with the @DomainEntity attribute (old was entity.)
                            entityStore.put(targetClass.getCanonicalName(), identityField.getType().getCanonicalName());
                        }


                    }

                    entityStore1.put(targetClass.getCanonicalName(), identityField.getType().getCanonicalName());

                } else {

                    // TODO: log error, DomainEntity may only have one identity field
                    log.log(Level.SEVERE, "DomainEntity {0} may only have one identity field.", entity.getCanonicalName());

                }

                // get all fields

                ObjectMeta meta = new ObjectMeta(entity, entity.getCanonicalName());


                // don't bother checking if we previously had errors retrieving the fields
                if(fields != null) {

                    fields = ReflectionUtils.getAllFields(entity);

                    for(Field field : fields) {

                        // System.out.println("processing meta for : " + entity.getCanonicalName() + " : " + field.getName() + " > " + field.getType());

                        // check if element is instance of a collection
                        if(Collection.class.isAssignableFrom(field.getType())) {

                            try {
                                // only get parameterized types
                                if(field.getGenericType() instanceof ParameterizedType) {

                                    ParameterizedType objectListType = (ParameterizedType) field.getGenericType();
                                    Class setClass = (Class<?>) objectListType.getActualTypeArguments()[0];

                                    //System.out.println("set : " + field.getType());
                                    //System.out.println("collection of " + setClass);

                                    meta.addCollectionField(field.getType(), setClass, field.getName());

                                }
                            } catch(TypeNotPresentException tnpe) {

                                System.out.println("field not found");
                                log.log(Level.SEVERE, "field type was not found. ");
                            }

                        } else {
                            meta.addField(field.getType(), field.getName());
                        }
                    }

                    metaStore.put(entity.getCanonicalName(), meta);
                }

//            } catch (IllegalStateException ise) {
//
//                log.severe("Error processing " + entity.getCanonicalName() + " caused by:  " + ise.getMessage());
//                log.severe("ignoring!");
//
//            } catch (Exception e) {
//
//                log.severe("Error processing " + entity.getCanonicalName() + " caused by:  " + e.getMessage());
//                log.severe("ignoring!");
//
//            }

        }

    }

    public String getClassForEntityIdentityField(String entityIdentityFieldname) {
        return (String) entityStore.inverse().get(entityIdentityFieldname);
    }

    public String getEntityIdentityFieldname(Class clazz) {
        return (String) identityStore.get(clazz);
    }

    public ObjectMeta getMetaForClass(Class clazz) {
        return metaStore.get(clazz.getCanonicalName());
    }

    public BiMap getEntityStore() {
        return entityStore;
    }

    public Set getEntities() {

        return entityStore.keySet();

    }


}
