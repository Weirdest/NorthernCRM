package net.northern.crm.persistence.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.intuit.ipp.data.Item;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class QBItemLite {
    @Basic
    @JsonProperty("qbId")
    @Column(columnDefinition = "varchar(250)")
    public String qbId;

    @JsonProperty("name")
    @Basic
    @Column(columnDefinition = "varchar(250)")
    public String name;

    @JsonProperty("sku")
    @Basic
    @Column(columnDefinition = "varchar(250)")
    public String sku;

    @JsonProperty("description")
    @Basic
    @Column(columnDefinition = "varchar(750)")
    public String description;

    @JsonProperty("active")
    @Basic
    @Column(columnDefinition = "boolean")
    public Boolean active;

    @JsonProperty("fqn")
    @Basic
    @Column(columnDefinition = "varchar(250)")
    private String fqn;

    @JsonProperty("type")
    @Basic
    @Column(columnDefinition = "varchar(250)")
    private String type;


    public QBItemLite(Item entity) {
        this.qbId = entity.getId();
        this.name = entity.getName();
        this.sku = entity.getSku();
        this.description = entity.getDescription();
        this.active = entity.isActive();
        this.fqn = entity.getFullyQualifiedName();
        this.type = entity.getType().value();
    }

    public QBItemLite() {
    }

    public String getQbId() {
        return qbId;
    }

    public void setQbId(String qb_id) {
        this.qbId = qb_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getFqn() {
        return fqn;
    }

    public void setFqn(String fqn) {
        this.fqn = fqn;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}