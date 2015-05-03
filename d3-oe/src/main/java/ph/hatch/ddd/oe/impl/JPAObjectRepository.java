package ph.hatch.ddd.oe.impl;

import org.reflections.ReflectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ph.hatch.ddd.domain.annotations.DomainEntity;
import ph.hatch.ddd.oe.ObjectRegistry;
import ph.hatch.ddd.oe.ObjectRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
@Transactional
public class JPAObjectRepository<entityClass> implements ObjectRepository<entityClass> {

    static Logger log = Logger.getLogger(ObjectRegistry.class.getName());

//    @Autowired(required = true)
//    private SessionFactory sessionFactory;

    @Autowired
    ObjectRegistry objectRegistry;

    protected EntityManager entityManager;

    public EntityManager getEntityManager() {
        return entityManager;
    }

    @PersistenceContext
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }


    public void persist(Object entityInstance) {

//        Session session = this.sessionFactory.getCurrentSession();
//        session.persist(entityInstance);
        entityManager.persist(entityInstance);

    }

    public Object loadByEntityId(Class<DomainEntity> entity, Object entityId, String... fetchMembers) {

        String identityField = objectRegistry.getEntityIdentityFieldname(entity);

        String innerIdentityField = "";

        // try to find the lone field for an EntityIdentity, we will need it when building the query
        try {

            Set<Field> fields = ReflectionUtils.getFields(Class.forName(objectRegistry.getMetaForClass(entity).getFieldMeta(identityField).getClassName()));

            if(fields.size() == 1) {

                Field field = (Field) fields.toArray()[0];
                System.out.println(fields.toArray()[0]);

                if(field.getGenericType() == String.class) {
                    innerIdentityField = field.getName();
                }
            }

        } catch(ClassNotFoundException cnfe) {
            return null;
        }


        if(identityField != null) {

            try {
                Class clazz = Class.forName(entity.getName());

                CriteriaBuilder builder = entityManager.getCriteriaBuilder();

                CriteriaQuery criteriaQuery = builder.createQuery(clazz);
                Root pRoot = criteriaQuery.from(clazz);

                Predicate condition;

                if(innerIdentityField != "") {
                    condition = builder.equal(pRoot.get(entityId.toString()).get(innerIdentityField), entityId.toString());
                } else {
                    condition = builder.equal(pRoot.get(entityId.toString()), entityId.toString());
                }

                criteriaQuery.where(condition);

                for(String fetchMember: fetchMembers) {
                    log.log(Level.FINE, "setting {0} to eager!", fetchMember);
                    pRoot.fetch(fetchMember);
                }


                Query qry = entityManager.createQuery(criteriaQuery);
                List results = qry.getResultList();

                if(!results.isEmpty()){
                    // ignores multiple results
                    return results.get(0);
                }

            } catch(ClassNotFoundException cnf) {
                //TODO: catch this
                cnf.printStackTrace();
            }
        }

        return null;

    }

    public Object load(Class<DomainEntity> entity, Object entityId) {

        String identityField = objectRegistry.getEntityIdentityFieldname(entity);

        String innerIdentityField = "";

        // todo: need to optimize this by moving field lookups in the ObjectRepository
        // try to find the lone field for an EntityIdentity, we will need it when building the query
        try {

            Set<Field> fields = ReflectionUtils.getFields(Class.forName(objectRegistry.getMetaForClass(entity).getFieldMeta(identityField).getClassName()));

            if(fields.size() == 1) {

                Field field = (Field) fields.toArray()[0];

                if(field.getGenericType() == String.class) {
                    innerIdentityField = field.getName();
                }
            }

        } catch(ClassNotFoundException cnfe) {
            return null;
        }


        if(identityField != null) {

            try {
                Class clazz = Class.forName(entity.getName());

                CriteriaBuilder builder = entityManager.getCriteriaBuilder();

                CriteriaQuery criteriaQuery = builder.createQuery(clazz);
                Root pRoot = criteriaQuery.from(clazz);

                Predicate condition;

                if(innerIdentityField != "") {
                    condition = builder.equal(pRoot.get(identityField.toString()).get(innerIdentityField), entityId.toString());
                } else {
                    condition = builder.equal(pRoot.get(identityField.toString()), entityId.toString());
                }

                criteriaQuery.where(condition);

                Query qry = entityManager.createQuery(criteriaQuery);
                List results = qry.getResultList();

                if(!results.isEmpty()){
                    // ignores multiple results
                    return results.get(0);
                }

            } catch(ClassNotFoundException cnf) {
                //TODO: catch this
                cnf.printStackTrace();
            }
        }

        return null;
    }

}
