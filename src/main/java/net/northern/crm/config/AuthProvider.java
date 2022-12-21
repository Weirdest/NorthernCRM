package net.northern.crm.config;

import net.northern.crm.persistence.entities.UserEntity;
import net.northern.crm.persistence.repositories.UsersRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;

@Component
public class AuthProvider implements AuthenticationProvider {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final PasswordEncoder encoder;
    private final UsersRepository usersRepository;

    @Autowired
    public AuthProvider(UsersRepository usersRepository) {
        this.encoder = new BCryptPasswordEncoder();
        this.usersRepository = usersRepository;
    }

    @Transactional
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = (String) authentication.getPrincipal();
        String password = (String) authentication.getCredentials();

        UserEntity userDetails = usersRepository.findByUsername(username);

        if (userDetails == null) {
            logger.error("Login Failed, username not found: " + username);
            throw new BadCredentialsException("Did not find user");
        }

        if (!encoder.matches(password, userDetails.getPassword())) {
            logger.error("Login Failed, password incorrect: " + username);
            throw new BadCredentialsException("Bad Password");
        }

        if (!userDetails.isEnabled()) {
            logger.error("Login Failed, user is disabled: " + username);
            throw new DisabledException("User Disabled");
        }

        logger.info("User authenticated: " + username);

        return new UsernamePasswordAuthenticationToken(username, password, userDetails.getAuthorities() == null ? new LinkedList<>() : userDetails.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        //return NoOpPasswordEncoder.getInstance();
        return encoder;
    }

}
