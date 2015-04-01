package ph.hatch.ddd.objectdb.repository.impl.test;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ph.hatch.ddd.objectdb.repository.impl.test.domain.*;
import ph.hatch.ddd.oe.ObjectMeta;
import ph.hatch.ddd.oe.ObjectRegistry;
import ph.hatch.ddd.oe.ObjectRepository;
import ph.hatch.ddd.repository.impl.GenericDBObjectRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="classpath:test-context.xml")
public class TestObjectDBObjectRepository {

    @Autowired
    ObjectRepository objectRepository;

    @Autowired
    ObjectRegistry objectRegistry;

    @PersistenceContext
    private EntityManager em;


    @Before
    public void dbSetup() {

    }

    @Test
    public void testPersistence() {

        objectRepository.persist(new Country("PH", "Philippines"));
        objectRepository.persist(new Province("001", "NCR", new CountryCode("PH")));

        objectRepository.persist(new Employee("PER-001", "Jett", "Gamboa", new ProvinceCode("001")));
        objectRepository.persist(new Employee("PER-002", "Anne", "Gamboa"));
        objectRepository.persist(new Employee("PER-003", "Juan", "dela Cruz"));

        Object result = objectRepository.load(Employee.class, new PersonId("PER-003"));

        Department department = new Department("OPS", "Operations");
        department.addPerson(new PersonId("PER-001"));
        department.addPerson(new PersonId("PER-003"));

    }

    @Test
    public void testLoadEntity() {

        System.out.println("loadentity");
        Object result = objectRepository.load(Country.class, new CountryCode("PH"));

        TypedQuery<String> query = em.createQuery(
                "SELECT c.name FROM Country AS c where c.countryCode.code='PH'", String.class);

        List<String> results = query.getResultList();

        System.out.println("shite");
    }

    private class queryCriteria() {


    }

    public Object queryBuilder(Class object, String...params) {

        ObjectMeta meta = objectRegistry.getMetaForClass(object);

        String jpqlQuery = "SELECT FROM " + meta.getClassName();

        TypedQuery<Object> query = em.createQuery(jpqlQuery, Object.class);
        List<Object> results = query.getResultList();

        System.out.println("done");

        return null;


    }


    @Test
    public void testPrototypeFinder() {

        queryBuilder(Country.class);


    }
}
