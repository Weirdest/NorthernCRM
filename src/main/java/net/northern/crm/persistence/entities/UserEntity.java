package net.northern.crm.persistence.entities;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;

@Entity
@Table
public class UserEntity implements UserDetails {
    @Id
    private String username;
    @Basic
    private String password;
    @Basic
    private boolean enabled;
    @ManyToMany(fetch = FetchType.EAGER)
    private Set<AuthorityEntity> grantedAuthorities;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setAuthorities(Set<AuthorityEntity> authorities) {
        this.grantedAuthorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return grantedAuthorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserEntity user = (UserEntity) o;
        return enabled == user.enabled && username.equals(user.username) && password.equals(user.password) && Objects.equals(grantedAuthorities, user.grantedAuthorities);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, password, enabled, grantedAuthorities);
    }

}
