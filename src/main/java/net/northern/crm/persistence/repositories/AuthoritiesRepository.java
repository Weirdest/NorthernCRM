package net.northern.crm.persistence.repositories;

import net.northern.crm.persistence.entities.AuthorityEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthoritiesRepository extends CrudRepository<AuthorityEntity, String> {
    AuthorityEntity findAuthoritiesEntityByAuthority(String authority);
}
