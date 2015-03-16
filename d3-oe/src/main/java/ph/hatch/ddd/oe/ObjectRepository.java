package ph.hatch.ddd.oe;

import ph.hatch.ddd.domain.annotations.DomainEntity;

public interface ObjectRepository<entityClass> {

    void persist(Object entityInstance);

    Object loadByEntityId(Class<DomainEntity> entity, Object entityId, String... fetchMembers);

    Object load(Class<DomainEntity> entity, Object entityId);

}
