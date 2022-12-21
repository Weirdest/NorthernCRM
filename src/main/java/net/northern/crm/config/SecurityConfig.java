package net.northern.crm.config;

import net.northern.crm.persistence.repositories.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@EnableWebSecurity
public class SecurityConfig {

    final UsersRepository usersRepository;
    final AuthProvider authProvider;

    @Autowired
    public SecurityConfig(UsersRepository usersRepository, AuthProvider authProvider) {
        this.usersRepository = usersRepository;
        this.authProvider = authProvider;
    }

    //TODO Make this proper
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web -> web.ignoring().antMatchers("/css/**", "/js/**", "/images/**"));
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity httpSecurity) throws Exception {
        AuthenticationManagerBuilder builder = httpSecurity.getSharedObject(AuthenticationManagerBuilder.class);

        builder.authenticationProvider(authProvider);

        return builder.build();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .antMatcher("/api/**")
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().hasAuthority("API")
                )
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and().addFilterBefore(new JWTFilter(new JWTUtil(), usersRepository), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    @Order(-1)
    public SecurityFilterChain authFilterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .antMatcher("/auth/**")
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().permitAll()
                ).anonymous();

        return http.build();
    }

    @Bean
    public SecurityFilterChain formLoginFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().hasAuthority("ADMIN")
                )
                .formLogin()
                .loginPage("/login").defaultSuccessUrl("/dashboard").permitAll()
                .and().logout().permitAll()
                .and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.ALWAYS);
        return http.build();
    }

}
