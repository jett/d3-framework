package ph.hatch.ddd.repository.impl;

import org.reflections.ReflectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ph.hatch.ddd.oe.ObjectRegistry;
import ph.hatch.ddd.oe.ObjectRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class GenericDBObjectRepository implements ObjectRepository {

    @Autowired
    ObjectRegistry objectRegistry;

    // Injected database connection:
    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public void persist(Object entityInstance) {
        em.persist(entityInstance);

    }

    @Override
    public Object loadByEntityId(Class entity, Object entityId, String... fetchMembers) {
        return null;
    }

    @Override
    public Object load(Class entity, Object entityId) {

        String identityField = objectRegistry.getEntityIdentityFieldname(entity);

        // get all the fields of the entity identity (by convention there should only be one)
        Set<Field> entityIdentityFields = ReflectionUtils.getAllFields(entityId.getClass());


        if(identityField != null && entityIdentityFields.size() == 1) {

            try {

                Class clazz = Class.forName(entity.getName());
                String entityIdentityFieldName = ((Field)(entityIdentityFields.toArray()[0])).getName();

                CriteriaBuilder cb = em.getCriteriaBuilder();

                CriteriaQuery<Object> q = cb.createQuery(clazz);
                Root<Object> c = q.from(clazz);

                //ParameterExpression p = cb.parameter(entityId.getClass());
                ParameterExpression p = cb.parameter(String.class);
                //q.select(c).where(cb.equal(c.get(identityField).get(entityIdentityFieldName), p));
                q.select(c).where(cb.equal(c.get(identityField).get(entityIdentityFieldName), p));

                TypedQuery query = em.createQuery(q);
                query.setParameter(p, entityId);

                List<Object> results = query.getResultList();

                TypedQuery<String> query2 = em.createQuery(
                        "SELECT name FROM Country where countryCode.code='PH'", String.class);

                List<String> results2 = query2.getResultList();

                System.out.println("hi");

            } catch(Exception e) {
                //TODO: catch this
                e.printStackTrace();
            }
        }

        return null;

    }
}
