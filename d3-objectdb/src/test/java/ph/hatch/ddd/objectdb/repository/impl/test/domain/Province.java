package ph.hatch.ddd.objectdb.repository.impl.test.domain;

import ph.hatch.ddd.domain.annotations.DomainEntity;
import ph.hatch.ddd.domain.annotations.DomainEntityIdentity;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "PROVINCE")
@DomainEntity
public class Province {

    @EmbeddedId
    @DomainEntityIdentity
    ProvinceCode provinceCode;

    @Column(name = "NAME")
    String name;

//    @Embedded
//    @AttributeOverride(name = "countryCode", column = @Column(name="", nullable=true))
    CountryCode countryCode;

    public Province(){}

    public Province(String code, String name) {
        this.provinceCode = new ProvinceCode(code);
        this.name = name;
    }

    public Province(String code, String name, CountryCode countryCode) {
        this.provinceCode = new ProvinceCode(code);
        this.name = name;
        this.countryCode = countryCode;
    }


}
