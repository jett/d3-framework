package ph.hatch.ddd.oe.test.domain;

import java.io.Serializable;

public class ManagementRole implements Serializable {

    String role;
    PersonId manager;

    public ManagementRole() {
    }

    public ManagementRole(String role, PersonId manager) {
        this.role = role;
        this.manager = manager;
    }
}
