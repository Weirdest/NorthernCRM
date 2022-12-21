package net.northern.crm.persistence.entities;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table
public class SettingEntity {
    @Id
    private String setting;
    @Basic
    @Column(length = 1500)
    private String data;

    public String getSetting() {
        return setting;
    }

    public void setSetting(String setting) {
        this.setting = setting;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SettingEntity that = (SettingEntity) o;
        return Objects.equals(setting, that.setting) && Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(setting, data);
    }
}
