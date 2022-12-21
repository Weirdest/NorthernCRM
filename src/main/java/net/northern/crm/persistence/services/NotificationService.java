package net.northern.crm.persistence.services;

import net.northern.crm.persistence.dto.NotificationDTO;
import net.northern.crm.persistence.entities.NotificationEntity;
import net.northern.crm.persistence.entities.UserEntity;
import net.northern.crm.persistence.repositories.NotificationsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {

    private final NotificationsRepository repository;

    @Autowired
    public NotificationService(NotificationsRepository repository) {
        this.repository = repository;
    }

    public void createNotification(String title, String summary, String extras, NotificationEntity.NotiType type) {
        NotificationEntity notification = new NotificationEntity(title, summary, extras,type);

        repository.save(notification);
    }

    public List<NotificationDTO> getNotiList() {
        LinkedList<NotificationDTO> returnList = new LinkedList<>();

        repository.findAllByDismissedByIsNullOrderByDateCreated().forEach(
                entity -> returnList.add(new NotificationDTO(entity))
        );

        return returnList;
    }

    public void dismiss(long id, UserEntity user) {
        Optional<NotificationEntity> notificationOptional = repository.findById(id);

        if (notificationOptional.isPresent()) {
            NotificationEntity entity = notificationOptional.get();

            entity.setDismissedBy(user);

            repository.save(entity);
        }
    }
}
