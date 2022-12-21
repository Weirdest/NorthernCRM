package net.northern.crm.persistence.services;

import net.northern.crm.persistence.entities.AuthorityEntity;
import net.northern.crm.persistence.entities.UserEntity;
import net.northern.crm.persistence.repositories.AuthoritiesRepository;
import net.northern.crm.persistence.repositories.UsersRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Service
public class UsersService {

    public enum Result {
        DENIED_CHANGE,
        SUCCESS,
        UNAVAILABLE,
        NOT_FOUND
    }

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final UsersRepository usersRepository;
    private final AuthoritiesRepository authoritiesRepository;

    @Autowired
    public UsersService(UsersRepository repository, AuthoritiesRepository authoritiesRepository) {
        this.usersRepository = repository;
        this.authoritiesRepository = authoritiesRepository;
    }

    public List<UserEntity> getAllUsers() {
        Iterable<UserEntity> users = usersRepository.findAll();

        LinkedList<UserEntity> userEntityLinkedList = new LinkedList<>();

        users.forEach(userEntityLinkedList::add);

        return userEntityLinkedList;

    }

    public UserEntity getUser(String username) {
        return usersRepository.findByUsername(username);
    }

    public Result createUser(String username) {
        //Check if username is taken
        if (usersRepository.existsByUsername(username)) {
            return Result.UNAVAILABLE;
        }

        UserEntity user = new UserEntity();
        user.setUsername(username);

        //If we made it here then proceed
        usersRepository.save(user);

        return Result.SUCCESS;
    }

    public Result changeUserState(boolean enabled, String username, String requester) {
        UserEntity user = usersRepository.findByUsername(username);
        UserEntity requestingUser = usersRepository.findByUsername(requester);

        Result sanityCheck = checkForSanity(user, requestingUser);

        if (sanityCheck != Result.SUCCESS) {
            return sanityCheck;
        }

        user.setEnabled(enabled);

        usersRepository.save(user);

        return Result.SUCCESS;
    }

    public Result changeUserAuthorities(String[] authorities, String username, String requester) {
        //Get the user
        UserEntity user = usersRepository.findByUsername(username);
        UserEntity requestingUser = usersRepository.findByUsername(requester);

        //Check if this is allowed and user exists
        Result sanityCheck = checkForSanity(user, requestingUser);
        if(sanityCheck != Result.SUCCESS) {
            return sanityCheck;
        }

        Set<AuthorityEntity> authoritiesEntityList = new HashSet<>();

        if(authorities != null) {
            for (String authority : authorities) {
                authoritiesEntityList.add(authoritiesRepository.findAuthoritiesEntityByAuthority(authority));
            }
        }

        user.getAuthorities().forEach(grantedAuthority -> {
            if(grantedAuthority.getAuthority().equals("ADMIN")) {
                authoritiesEntityList.add((AuthorityEntity) grantedAuthority);
            }
        });

        user.setAuthorities(authoritiesEntityList);
        usersRepository.save(user);

        return Result.SUCCESS;
    }

    public Result changeUserPassword(String username, String password, String requester) {
        //Get the user
        UserEntity user = usersRepository.findByUsername(username);

        //Check if this is allowed and user exists
        Result sanityCheck = checkForSanity(user, usersRepository.findByUsername(requester));
        if(sanityCheck != Result.SUCCESS) {
            return sanityCheck;
        }

        user.setPassword(password);

        usersRepository.save(user);

        return Result.SUCCESS;
    }

    private Result checkForSanity(UserEntity user, UserEntity requester) {
        if (user == null) {
            //Change this to include a custom message
            return Result.NOT_FOUND;
        }

        if (!saneSecurity(user, requester)) {
            return Result.DENIED_CHANGE;
        }

        return Result.SUCCESS;
    }

    private boolean saneSecurity(UserEntity toBeChanged, UserEntity requester) {

        if (toBeChanged.getUsername().equals(requester.getUsername())) {

            //Allow admin to change their own info
            return true;
        }

        boolean toBeChangedAdmin = false;
        boolean requesterAdmin = false;


        for(GrantedAuthority authority : toBeChanged.getAuthorities()) {
            if (authority.getAuthority().equals("ADMIN")) {
                toBeChangedAdmin = true;
            }
        }

        for (GrantedAuthority authority : requester.getAuthorities()) {
            if (authority.getAuthority().equals("ADMIN")) {
                requesterAdmin = true;
            }
        }

        //Requester must be admin, and other must not be admin
        return requesterAdmin && !toBeChangedAdmin;
    }
}
