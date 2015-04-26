package ph.hatch.ddd.oe.test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import ph.hatch.ddd.oe.ObjectExplorer;
import ph.hatch.ddd.oe.ObjectRegistry;
import ph.hatch.ddd.oe.ObjectRepository;
import ph.hatch.ddd.oe.test.domain.*;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(value = "classpath:oe/test-context.xml")
@Transactional
public class TestOENew {

    @Autowired
    ObjectRepository objectRepository;

    @Autowired
    ObjectRegistry objectRegistry;

    @Before
    public void testPersistence() {

        Gson gson = new GsonBuilder().setDateFormat("MM/dd/yyyy").create();

        objectRepository.persist(new Country("PH", "Philippines"));
        objectRepository.persist(new Province("001", "NCR", new CountryCode("PH")));

        objectRepository.persist(new Employee("PER-001", "Jett", "Gamboa", new ProvinceCode("001")));
        objectRepository.persist(new Employee("PER-002", "Anne", "Gamboa"));
        objectRepository.persist(new Employee("PER-003", "Juan", "dela Cruz"));

        Object result = objectRepository.load(Employee.class, new PersonId("PER-003"));

        Department department = new Department("OPS", "Operations");
        department.addPerson(new PersonId("PER-001"));
        department.addPerson(new PersonId("PER-003"));

        department.setBoss(new PersonId("PER-001"));

        // added to test child classes that are elements of a collection
        department.addManagementRole(new PersonId("PER-001"), "Dummy Role");
        department.addManagementRole(new PersonId("PER-003"), "Another Role");

        objectRepository.persist(department);

        result = objectRepository.load(Department.class, new DepartmentId("OPS"));

    }

    @Test
    public void testNewSerializer() {

        Department department = (Department) objectRepository.loadByEntityId(Department.class, new DepartmentId("OPS"));

        ObjectExplorer oe = new ObjectExplorer(objectRegistry, objectRepository);
        Map result = oe.explore(department, true);

        System.out.println(result);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.println(gson.toJson(result));


//        Gson gson = new GsonBuilder().setPrettyPrinting().create();
//        String departmentJson = gson.toJson(department);
//        System.out.println(departmentJson);
//        OESerializers oeSerializers = new OESerializers();


        // working XML serialization
//        JSONObject json = new JSONObject(departmentJson);
//        String xml = "<Department>" + XML.toString(json) + "</Department>";
//
//        System.out.println(xml);
//        System.out.println(prettyFormat(xml));


//        // create a new xstream object w/json provider
//        XStream xstreamForJson = new XStream(new JettisonMappedXmlDriver());
////        xstreamForJson.setMode(XStream.NO_REFERENCES);
////        xstreamForJson.alias("status", Status.class);
//        Status status = (Status) xstreamForJson.fromXML(departmentJson);
//
//        // create xstream object for reading xml
//        XStream xstream = new XStream();
//        xstream.setMode(XStream.NO_REFERENCES);
//
//        System.out.println(xstream.toXML(status));

    }


    // http://stackoverflow.com/questions/139076/how-to-pretty-print-xml-from-java
    public static String prettyFormat(String input, int indent) {
        try {
            Source xmlInput = new StreamSource(new StringReader(input));
            StringWriter stringWriter = new StringWriter();
            StreamResult xmlOutput = new StreamResult(stringWriter);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setAttribute("indent-number", indent);
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(xmlInput, xmlOutput);
            return xmlOutput.getWriter().toString();
        } catch (Exception e) {
            throw new RuntimeException(e); // simple exception handling, please review it
        }
    }

    public static String prettyFormat(String input) {
        return prettyFormat(input, 2);
    }


}
