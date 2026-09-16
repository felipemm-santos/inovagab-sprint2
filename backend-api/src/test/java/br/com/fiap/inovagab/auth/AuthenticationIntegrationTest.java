package br.com.fiap.inovagab.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Locale;
import java.util.regex.Pattern;
import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.web.servlet.MockMvc;

import br.com.fiap.inovagab.MongoIntegrationTestSupport;
import br.com.fiap.inovagab.audit.repository.AuditEventRepository;
import br.com.fiap.inovagab.user.document.UserDocument;
import br.com.fiap.inovagab.user.model.UserRole;
import br.com.fiap.inovagab.user.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationIntegrationTest extends MongoIntegrationTestSupport {

    private static final String RAW_PASSWORD = "000000";
    private static final Pattern ACCESS_TOKEN_PATTERN = Pattern.compile(
            "\\\"accessToken\\\":\\\"([^\\\"]+)\\\""
    );

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditEventRepository auditEventRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Autowired
    private JwtEncoder jwtEncoder;

    @BeforeEach
    void cleanDatabase() {
        auditEventRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void publicRegistrationShouldCreateOnlyOperatorWithBCryptPassword()
            throws Exception {
        mockMvc.perform(post("/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Operador Teste",
                                  "email": "OPERADOR@AGUIABRANCA.COM.BR",
                                  "password": "000000"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.email")
                        .value("operador@aguiabranca.com.br"))
                .andExpect(jsonPath("$.role").value("OPERADOR"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        UserDocument savedUser = userRepository
                .findByEmail("operador@aguiabranca.com.br")
                .orElseThrow();

        assertThat(savedUser.getPasswordHash()).isNotEqualTo(RAW_PASSWORD);
        assertThat(passwordEncoder.matches(
                RAW_PASSWORD,
                savedUser.getPasswordHash()
        )).isTrue();
    }

    @Test
    void duplicateRegistrationShouldReturnConflict() throws Exception {
        createUser("duplicado@aguiabranca.com.br", UserRole.OPERADOR, true);

        mockMvc.perform(post("/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Outro Operador",
                                  "email": "DUPLICADO@AGUIABRANCA.COM.BR",
                                  "password": "000000"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code")
                        .value("EMAIL_ALREADY_REGISTERED"));
    }

    @ParameterizedTest
    @EnumSource(UserRole.class)
    void loginShouldIssueValidJwtForEveryRole(UserRole role) throws Exception {
        String email = role.name().toLowerCase(Locale.ROOT)
                + "@aguiabranca.com.br";
        UserDocument user = createUser(email, role, true);

        String response = mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(email, RAW_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600))
                .andExpect(jsonPath("$.user.id").value(user.getId()))
                .andExpect(jsonPath("$.user.role").value(role.name()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String accessToken = extractAccessToken(response);
        var decodedJwt = jwtDecoder.decode(accessToken);

        assertThat(decodedJwt.getSubject()).isEqualTo(user.getId());
        assertThat(decodedJwt.getClaimAsStringList("roles"))
                .containsExactly(role.name());
        assertThat(decodedJwt.getExpiresAt()).isAfter(decodedJwt.getIssuedAt());
    }

    @Test
    void meShouldReturnAuthenticatedUser() throws Exception {
        UserDocument user = createUser(
                "gestor@aguiabranca.com.br",
                UserRole.GESTOR,
                true
        );
        String accessToken = loginAndGetToken(user.getEmail());

        mockMvc.perform(get("/v1/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.email").value(user.getEmail()))
                .andExpect(jsonPath("$.role").value("GESTOR"));
    }

    @Test
    void invalidCredentialsShouldReturnStandardizedUnauthorizedResponse()
            throws Exception {
        createUser("operador@aguiabranca.com.br", UserRole.OPERADOR, true);

        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(
                                "operador@aguiabranca.com.br",
                                "SenhaIncorreta"
                        )))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
                .andExpect(jsonPath("$.path").value("/v1/auth/login"));
    }

    @Test
    void inactiveUserShouldNotAuthenticate() throws Exception {
        createUser("inativo@aguiabranca.com.br", UserRole.OPERADOR, false);

        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(
                                "inativo@aguiabranca.com.br",
                                RAW_PASSWORD
                        )))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    void missingAndInvalidTokensShouldReturnDifferentErrorCodes()
            throws Exception {
        mockMvc.perform(get("/v1/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code")
                        .value("AUTHENTICATION_REQUIRED"));

        mockMvc.perform(get("/v1/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_TOKEN"));
    }

    @Test
    void expiredTokenShouldBeRejected() throws Exception {
        Instant now = Instant.now();
        var headers = JwsHeader.with(MacAlgorithm.HS256)
                .type("JWT")
                .build();
        var claims = JwtClaimsSet.builder()
                .issuer("inovagab-api-test")
                .subject("expired-user-id")
                .issuedAt(now.minusSeconds(7200))
                .expiresAt(now.minusSeconds(3600))
                .claim("roles", java.util.List.of("OPERADOR"))
                .build();
        String expiredToken = jwtEncoder.encode(
                JwtEncoderParameters.from(headers, claims)
        ).getTokenValue();

        mockMvc.perform(get("/v1/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(expiredToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_TOKEN"));
    }

    @Test
    void onlyLeaderShouldCreateUsersWithPrivilegedRoles() throws Exception {
        UserDocument leader = createUser(
                "lider@aguiabranca.com.br",
                UserRole.LIDER,
                true
        );
        UserDocument manager = createUser(
                "gestor@aguiabranca.com.br",
                UserRole.GESTOR,
                true
        );
        String leaderToken = loginAndGetToken(leader.getEmail());
        String managerToken = loginAndGetToken(manager.getEmail());
        String newUserBody = """
                {
                  "name": "Novo Gestor",
                  "email": "novo.gestor@aguiabranca.com.br",
                  "password": "000000",
                  "role": "GESTOR"
                }
                """;

        mockMvc.perform(post("/v1/users")
                        .header(HttpHeaders.AUTHORIZATION, bearer(managerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newUserBody))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));

        mockMvc.perform(post("/v1/users")
                        .header(HttpHeaders.AUTHORIZATION, bearer(leaderToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newUserBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("GESTOR"));

        UserDocument createdUser = userRepository
                .findByEmail("novo.gestor@aguiabranca.com.br")
                .orElseThrow();
        assertThat(passwordEncoder.matches(
                RAW_PASSWORD,
                createdUser.getPasswordHash()
        )).isTrue();
    }

    private UserDocument createUser(
            String email,
            UserRole role,
            boolean active
    ) {
        return userRepository.save(UserDocument.builder()
                .name("Usuário " + role.name())
                .email(email)
                .passwordHash(passwordEncoder.encode(RAW_PASSWORD))
                .role(role)
                .active(active)
                .build());
    }

    private String loginAndGetToken(String email) throws Exception {
        String response = mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(email, RAW_PASSWORD)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return extractAccessToken(response);
    }

    private String extractAccessToken(String responseBody) {
        var matcher = ACCESS_TOKEN_PATTERN.matcher(responseBody);
        assertThat(matcher.find()).isTrue();
        return matcher.group(1);
    }

    private String loginBody(String email, String password) {
        return """
                {
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(email, password);
    }

    private String bearer(String accessToken) {
        return "Bearer " + accessToken;
    }
}
