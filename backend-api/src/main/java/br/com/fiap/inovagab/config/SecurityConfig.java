package br.com.fiap.inovagab.config;

import java.util.Base64;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import br.com.fiap.inovagab.security.JwtProperties;
import br.com.fiap.inovagab.security.RestAccessDeniedHandler;
import br.com.fiap.inovagab.security.RestAuthenticationEntryPoint;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    private static final int BCRYPT_STRENGTH = 12;
    private static final int MINIMUM_HS256_KEY_BYTES = 32;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            RestAuthenticationEntryPoint authenticationEntryPoint,
            RestAccessDeniedHandler accessDeniedHandler,
            JwtAuthenticationConverter jwtAuthenticationConverter
    ) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.POST,
                                "/v1/auth/login",
                                "/v1/auth/register"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/auth/me")
                        .authenticated()
                        .requestMatchers(HttpMethod.POST, "/v1/users")
                        .hasRole("LIDER")
                        .requestMatchers(HttpMethod.POST, "/v1/guidelines")
                        .hasRole("LIDER")
                        .requestMatchers(HttpMethod.PUT, "/v1/guidelines/*")
                        .hasRole("LIDER")
                        .requestMatchers(HttpMethod.DELETE, "/v1/guidelines/*")
                        .hasRole("LIDER")
                        .requestMatchers(HttpMethod.GET, "/v1/guidelines/**")
                        .authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/v1/ideas/*/priority")
                        .hasRole("GESTOR")
                        .requestMatchers(
                                HttpMethod.POST,
                                "/v1/ideas/*/approve",
                                "/v1/ideas/*/reject"
                        ).hasRole("GESTOR")
                        .requestMatchers(HttpMethod.GET, "/v1/ideas/mine")
                        .hasRole("OPERADOR")
                        .requestMatchers(HttpMethod.GET, "/v1/ideas")
                        .hasRole("GESTOR")
                        .requestMatchers(HttpMethod.GET, "/v1/ideas/*")
                        .hasAnyRole("OPERADOR", "GESTOR")
                        .requestMatchers(HttpMethod.POST, "/v1/ideas")
                        .hasRole("OPERADOR")
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/v1/ideas/*"
                        ).hasRole("OPERADOR")
                        .requestMatchers(HttpMethod.DELETE, "/v1/ideas/*")
                        .hasRole("OPERADOR")
                        .requestMatchers(HttpMethod.GET, "/v1/projects/**")
                        .hasAnyRole("GESTOR", "LIDER")
                        .requestMatchers(HttpMethod.POST, "/v1/projects")
                        .hasRole("GESTOR")
                        .requestMatchers(HttpMethod.PUT, "/v1/projects/*")
                        .hasRole("GESTOR")
                        .requestMatchers(HttpMethod.DELETE, "/v1/projects/*")
                        .hasRole("GESTOR")
                        .requestMatchers(HttpMethod.GET, "/v1/dashboard/**")
                        .hasRole("LIDER")
                        .requestMatchers("/actuator/**")
                        .hasRole("LIDER")
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .anyRequest().denyAll())
                .oauth2ResourceServer(resourceServer -> resourceServer
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter)))
                .cors(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(BCRYPT_STRENGTH);
    }

    @Bean
    public AuthenticationProvider authenticationProvider(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder
    ) {
        var provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationProvider authenticationProvider
    ) {
        return new ProviderManager(List.of(authenticationProvider));
    }

    @Bean
    public SecretKey jwtSecretKey(JwtProperties properties) {
        byte[] decodedSecret;

        try {
            decodedSecret = Base64.getDecoder().decode(properties.secret());
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("JWT_SECRET must be a valid Base64 value", exception);
        }

        if (decodedSecret.length < MINIMUM_HS256_KEY_BYTES) {
            throw new IllegalStateException("JWT_SECRET must contain at least 32 bytes");
        }

        return new SecretKeySpec(decodedSecret, "HmacSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
        return NimbusJwtEncoder.withSecretKey(jwtSecretKey)
                .algorithm(MacAlgorithm.HS256)
                .build();
    }

    @Bean
    public JwtDecoder jwtDecoder(
            SecretKey jwtSecretKey,
            JwtProperties properties
    ) {
        var decoder = NimbusJwtDecoder.withSecretKey(jwtSecretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        decoder.setJwtValidator(
                JwtValidators.createDefaultWithIssuer(properties.issuer())
        );
        return decoder;
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        var authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthoritiesClaimName("roles");
        authoritiesConverter.setAuthorityPrefix("ROLE_");

        var authenticationConverter = new JwtAuthenticationConverter();
        authenticationConverter.setJwtGrantedAuthoritiesConverter(
                authoritiesConverter
        );
        return authenticationConverter;
    }
}
