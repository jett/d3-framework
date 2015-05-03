package ph.hatch.ddd.oe.test.domain;

import org.hibernate.annotations.Fetch;
import ph.hatch.ddd.domain.annotations.DomainEntity;
import ph.hatch.ddd.domain.annotations.DomainEntityIdentity;
import ph.hatch.ddd.oe.annotations.ExploredMethod;
import ph.hatch.ddd.oe.annotations.OverrideName;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.*;

@Entity
@Table(name = "DEPARTMENT")
@DomainEntity
public class Department {

    @EmbeddedId
    @DomainEntityIdentity
    DepartmentId departmentId;

    @Column(name = "NAME")
    @OverrideName(name = "assignedDepartment")
    String departmentName;


    @ExploredMethod
    PersonId myBoss() {
        return boss;
    };

    @ExploredMethod
    String dateLoaded() {
        return new Date().toString();
    }

    @Column(name = "BUDGET")
    BigDecimal budget;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable
            (
                    name="DEPT_PERSON",
                    joinColumns=@JoinColumn(name="DEPT_ID")
            )
    @Fetch(value = org.hibernate.annotations.FetchMode.SELECT)
    @Column(name="PERSON_ID")
    Set<PersonId> personIds;

    @ElementCollection
    @CollectionTable(
            name="MANAGEMENT_ROLES",
            joinColumns=@JoinColumn(name="DEPT_ID")
    )
    List<ManagementRole> managementRoles;

    @Embedded
    @Column(name="BOSS_EMP_ID")
    PersonId boss;

    public Department(){

    }

    public Department(String id, String name) {

        departmentId = new DepartmentId(id);
        this.departmentName = name;

        this.personIds = new HashSet<PersonId>();
        this.budget = new BigDecimal(20000000);

    }

    public void addPerson(PersonId personId) {

        personIds.add(personId);

    }

    public void addManagementRole(PersonId employeeId, String role) {

        if(managementRoles == null) {
            managementRoles = new ArrayList<ManagementRole>();
        }

        managementRoles.add(new ManagementRole(role, employeeId));

    }

    public String getDepartmentName() {
        return departmentName;
    }

    public DepartmentId getDepartmentId() {
        return this.departmentId;
    }

    public void setBoss(PersonId boss) {
        this.boss = boss;
    }

    public Set getEmployees() {

        return personIds;

    }

}
