package ddd.oe.test

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.transaction.annotation.Transactional
import ph.hatch.ddd.oe.OESerializers
import ph.hatch.ddd.oe.ObjectRegistry
import ph.hatch.ddd.oe.ObjectRepository
import ph.hatch.ddd.oe.ObjectExplorer
import ph.hatch.ddd.oe.test.domain.Country
import ph.hatch.ddd.oe.test.domain.CountryCode
import ph.hatch.ddd.oe.test.domain.Department
import ph.hatch.ddd.oe.test.domain.DepartmentId
import ph.hatch.ddd.oe.test.domain.Employee
import ph.hatch.ddd.oe.test.domain.PersonId
import ph.hatch.ddd.oe.test.domain.Province
import ph.hatch.ddd.oe.test.domain.ProvinceCode

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(value = "classpath:oe/test-context.xml")
@Transactional
class TestOE {

    @Autowired
    ObjectRepository objectRepository;

    @Autowired
    ObjectRegistry objectRegistry;

    @Before
    public void testPersistence() {

        Gson gson = new GsonBuilder().setDateFormat("MM/dd/yyyy").create()

        objectRepository.persist(new Country("PH", "Philippines"));
        objectRepository.persist(new Province("001", "NCR", new CountryCode("PH")));

        objectRepository.persist(new Employee("PER-001", "Jett", "Gamboa", new ProvinceCode("001")))
        objectRepository.persist(new Employee("PER-002", "Anne", "Gamboa"))
        objectRepository.persist(new Employee("PER-003", "Juan", "dela Cruz"))

        Object result = objectRepository.load(Employee.class, new PersonId("PER-003"))

        Department department = new Department("OPS", "Operations")
        department.addPerson(new PersonId("PER-001"))
        department.addPerson(new PersonId("PER-003"))

        // added to test child classes that are elements of a collection
        department.addManagementRole(new PersonId("PER-001"), "Dummy Role");
        department.addManagementRole(new PersonId("PER-003"), "Another Role");

        objectRepository.persist(department)

        result = objectRepository.load(Department.class, new DepartmentId("OPS"))

    }

    @Test
    public void testExplorer() {

        Gson gson = new Gson()

        Department department = objectRepository.loadByEntityId(Department.class, new DepartmentId("OPS"))
        ObjectExplorer objectExplorer = new ObjectExplorer(objectRegistry, objectRepository);

        objectExplorer.setDateFormat("MM/dd/yyyy")
        objectExplorer.includeNulls(true);

        Map mymap = objectExplorer.explore(department, true)

//        println mymap.personIds.size()

        println gson.toJson(mymap)

        OESerializers oeSerializers = new OESerializers();
        println oeSerializers.maptoxml(mymap, "Department")

        mymap.personIds.each() { employee ->
//            println employee.Employee.firstName +  " " + employee.Employee.lastName  + " " + employee.Employee.birthProvinceCode?.Province?.name
        }
    }

}
