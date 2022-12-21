package net.northern.crm.persistence.dto;

import net.northern.crm.persistence.entities.NotificationEntity;

import java.text.DateFormat;
import java.util.Date;

public class NotificationDTO {

    private final long notificationId;
    private final String title;
    private final String summary;
    private final String dismissedBy;
    private final NotificationEntity.NotiType type;
    private final String extras;
    private final Date dateCreated;
    private final Date dateDismissed;
    private final DateFormat dateFormat = DateFormat.getDateTimeInstance();


    public NotificationDTO(NotificationEntity entity) {
        this.notificationId = entity.getNotificationId();
        this.title = entity.getTitle();
        this.summary = entity.getSummary();
        this.type = entity.getType();
        this.extras = entity.getExtras();
        this.dateCreated = entity.getDateCreated();
        this.dismissedBy = entity.getDismissedBy() != null ? entity.getDismissedBy().getUsername() : null;
        this.dateDismissed = entity.getDismissedOn();
    }

    public long getNotificationId() {
        return notificationId;
    }

    public String getTitle() {
        return title;
    }

    public String getSummary() {
        return summary;
    }

    public String getDismissedBy() {
        return dismissedBy;
    }

    public NotificationEntity.NotiType getType() {
        return type;
    }

    public String getExtras() {
        return extras;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public String getFormattedCreatedDate() {
        return dateFormat.format(dateCreated);
    }

    public Date getDateDismissed() {
        return dateDismissed;
    }
}
