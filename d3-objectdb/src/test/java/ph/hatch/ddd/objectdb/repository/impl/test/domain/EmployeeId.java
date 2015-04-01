package ph.hatch.ddd.objectdb.repository.impl.test.domain;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class EmployeeId implements Serializable {

    @Column(name = "EMP_ID")
    String id;

    public EmployeeId(){
    }

    public EmployeeId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }
}
