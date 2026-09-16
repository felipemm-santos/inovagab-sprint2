package br.com.fiap.inovagab.user.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "inovagab.bootstrap.demo-users")
public record DemoUsersProperties(
        boolean enabled,
        @NotBlank @Size(min = 6, max = 72) String password,
        @Valid @NotNull DemoUser operador,
        @Valid @NotNull DemoUser gestor,
        @Valid @NotNull DemoUser lider
) {

    public record DemoUser(
            @NotBlank @Size(max = 120) String name,
            @NotBlank @Email @Size(max = 254) String email
    ) {
    }
}
