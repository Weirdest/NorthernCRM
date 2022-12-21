package net.northern.crm.persistence.repositories;

import net.northern.crm.persistence.entities.SettingEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SettingsRepository extends CrudRepository<SettingEntity, String> {
}
