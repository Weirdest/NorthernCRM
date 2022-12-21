package net.northern.crm.persistence.repositories;

import net.northern.crm.persistence.entities.NotificationEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationsRepository extends CrudRepository<NotificationEntity, Long> {
    Iterable<NotificationEntity> findAllByDismissedByIsNullOrderByDateCreated();
}
