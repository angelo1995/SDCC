package com.example.demo.configurations;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig {
	
	@Bean
    SecurityFilterChain resourceServerFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
        	.requestMatchers(antMatcher(HttpMethod.OPTIONS)).permitAll()
            .requestMatchers(antMatcher("/book/view/meeting")).permitAll()
            .requestMatchers(antMatcher("/message/*")).permitAll()
            .anyRequest()
            .authenticated());
        
        http.oauth2ResourceServer((oauth2ResourceServer) -> oauth2ResourceServer
				.jwt((jwt) -> jwt.jwtAuthenticationConverter(converter)));
        
        http
        	.cors(cors -> cors.disable()) 
        	.csrf(csrf -> csrf.disable());
        
        return http.build();
    }

    private static final String GROUPS = "groups";
    private static final String REALM_ACCESS_CLAIM = "realm_access";
    private static final String ROLES_CLAIM = "roles";

    @Bean
    protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());
    }
    
    @Autowired
  	private JwtAuthenticationConverter converter;

    @SuppressWarnings("unchecked")
	@Bean
    GrantedAuthoritiesMapper userAuthoritiesMapperForKeycloak() {
        return authorities -> {
            Set<GrantedAuthority> mappedAuthorities = new HashSet<>();
            var authority = authorities.iterator().next();
            boolean isOidc = authority instanceof OidcUserAuthority;

            if (isOidc) {
                var oidcUserAuthority = (OidcUserAuthority) authority;
                var userInfo = oidcUserAuthority.getUserInfo();

                // Tokens can be configured to return roles under
                // Groups or REALM ACCESS hence have to check both
                if (userInfo.hasClaim(REALM_ACCESS_CLAIM)) {
                    var realmAccess = userInfo.getClaimAsMap(REALM_ACCESS_CLAIM);
                    var roles = (Collection<String>) realmAccess.get(ROLES_CLAIM);
                    Collection<GrantedAuthority> ciao = generateAuthoritiesFromClaim(roles);
                    System.out.println("ciao" + ciao);
                    mappedAuthorities.addAll(ciao);
                } else if (userInfo.hasClaim(GROUPS)) {
                    Collection<String> roles = (Collection<String>) userInfo.getClaim(GROUPS);
                    mappedAuthorities.addAll(generateAuthoritiesFromClaim(roles));
                }
            } else {
                var oauth2UserAuthority = (OAuth2UserAuthority) authority;
                Map<String, Object> userAttributes = oauth2UserAuthority.getAttributes();

                if (userAttributes.containsKey(REALM_ACCESS_CLAIM)) {
                    Map<String, Object> realmAccess = (Map<String, Object>) userAttributes.get(REALM_ACCESS_CLAIM);
                    Collection<String> roles = (Collection<String>) realmAccess.get(ROLES_CLAIM);
                    mappedAuthorities.addAll(generateAuthoritiesFromClaim(roles));
                }
            }
            return mappedAuthorities;
        };
    }

    Collection<GrantedAuthority> generateAuthoritiesFromClaim(Collection<String> roles) {
        return roles.stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role)).collect(
            Collectors.toList());
    }
}