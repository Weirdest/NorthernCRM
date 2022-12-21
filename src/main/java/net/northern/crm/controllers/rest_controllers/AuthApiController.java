package net.northern.crm.controllers.rest_controllers;

import net.northern.crm.config.JWTUtil;
import net.northern.crm.persistence.repositories.UsersRepository;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthApiController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;

    private final UsersRepository usersRepository;

    @Autowired
    public AuthApiController(AuthenticationManager authenticationManager, JWTUtil jwtUtil, UsersRepository usersRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.usersRepository = usersRepository;
    }

    @PostMapping("/check")
    public ResponseEntity<?> checkForAuth(@RequestBody AuthResponse jwt) {
        JSONObject jsonObject = new JSONObject();

        logger.info("Received auth check");

         try {
             return ResponseEntity.ok(jsonObject.put("authenticated", jwtUtil.validateToken(jwt.getJwt(), usersRepository.findByUsername(jwtUtil.extractUsername(jwt.getJwt())))).toString());
         } catch (Exception e) {
             logger.info(e.getMessage());
             return ResponseEntity.ok(jsonObject.put("authenticated", false).toString());
         }
    }

    @PostMapping
    public ResponseEntity<?> createAuthToken(@RequestBody AuthRequest authRequest) {

        logger.info("Received Auth Request");

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
            );
        } catch (BadCredentialsException e) {
            logger.info("Bad Creds " + authRequest.getUsername() + ":" + authRequest.getPassword());
            return new ResponseEntity<>("Bad Credentials", HttpStatus.UNAUTHORIZED);
        } catch (AuthenticationException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        logger.info("Authenticated");

        final UserDetails userDetails = usersRepository.findByUsername(authRequest.getUsername());
        final String jwt = jwtUtil.generateToken(userDetails);

        return ResponseEntity.ok(new AuthResponse(jwt));
    }

    public static class AuthRequest {
        private String username;
        private String password;

        public AuthRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }
        public AuthRequest(String username) {
            this.username = username;
        }

        public AuthRequest() {}

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class AuthResponse {
        private String jwt;

        public AuthResponse(String jwt) {
            this.jwt = jwt;
        }

        public AuthResponse() {}

        public String getJwt() {
            return jwt;
        }

        public void setJwt(String jwt) {this.jwt = jwt;}

    }

}
