package net.northern.crm.persistence.dto;

import net.northern.crm.persistence.entities.UserEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.LinkedList;

public class UserDTO {
    private String username;
    private boolean enabled;

    @JsonIgnore
    private boolean isComplete = true;
    private final LinkedList<String> grantedAuthorities = new LinkedList<>();

    public UserDTO(UserEntity entity) {
        this.username = entity.getUsername();
        this.enabled = entity.isEnabled();

        entity.getAuthorities().forEach(grantedAuthority ->
                grantedAuthorities.add(grantedAuthority.getAuthority()));
    }

    public UserDTO(String username) {
        this.username = username;
        this.isComplete = false;
    }

    public UserDTO(){}

    public boolean isComplete() {
        return isComplete;
    }

    @JsonValue
    public String getUsername() {
        return username;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public LinkedList<String> getGrantedAuthorities() {
        return grantedAuthorities;
    }
}
