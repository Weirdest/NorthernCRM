package net.northern.crm.persistence.entities;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

@Entity
@Table
public class NotificationEntity {

    public enum NotiType {
        BAD_PO,
        EXTRA_PO_ITEMS
    }

    @GeneratedValue
    @Id
    private long notificationId;
    @Basic
    private String title;
    @Basic
    private String summary;
    @ManyToOne
    private UserEntity dismissedBy;
    @Basic
    private NotiType type;
    @Basic
    private String extras;

    @Basic
    private Date dateCreated;

    @Basic
    private Date dismissedOn;

    public NotificationEntity() {}
    public NotificationEntity(String title, String summary, String extras, NotiType type) {
        this.title = title;
        this.summary = summary;
        this.extras = extras;
        this.type = type;

        this.dateCreated = new Date();
    }

    public long getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(long id) {
        this.notificationId = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public UserEntity getDismissedBy() {
        return dismissedBy;
    }

    public void setDismissedBy(UserEntity dismissedBy) {
        this.dismissedBy = dismissedBy;
        this.dismissedOn = new Date();
    }

    public NotiType getType() {
        return type;
    }

    public void setType(NotiType type) {
        this.type = type;
    }

    public String getExtras() {
        return extras;
    }

    public void setExtras(String extras) {
        this.extras = extras;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated() {
        this.dateCreated = new Date();
    }

    public Date getDismissedOn() {
        return dismissedOn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotificationEntity that = (NotificationEntity) o;
        return notificationId == that.notificationId && Objects.equals(title, that.title) && Objects.equals(summary, that.summary) && Objects.equals(dismissedBy, that.dismissedBy) && Objects.equals(type, that.type) && Objects.equals(extras, that.extras);
    }

    @Override
    public int hashCode() {
        return Objects.hash(notificationId, title, summary, dismissedBy, type, extras);
    }
}
