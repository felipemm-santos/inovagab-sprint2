package br.com.fiap.inovagab.user.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import br.com.fiap.inovagab.user.model.UserRole;
import br.com.fiap.inovagab.user.service.UserService;

@Slf4j
@Configuration
@ConditionalOnProperty(
        prefix = "inovagab.bootstrap.leader",
        name = "enabled",
        havingValue = "true"
)
public class InitialLeaderConfiguration {

    @Bean
    public ApplicationRunner initialLeaderRunner(
            UserService userService,
            @Value("${inovagab.bootstrap.leader.name:}") String name,
            @Value("${inovagab.bootstrap.leader.email:}") String email,
            @Value("${inovagab.bootstrap.leader.password:}") String password
    ) {
        return arguments -> {
            if (!StringUtils.hasText(name)
                    || !StringUtils.hasText(email)
                    || !StringUtils.hasText(password)) {
                throw new IllegalStateException(
                        "Initial leader is enabled, but its name, email or password is missing"
                );
            }

            if (userService.existsByEmail(email)) {
                log.info("Initial leader already provisioned");
                return;
            }

            userService.create(name, email, password, UserRole.LIDER);
            log.info("Initial leader provisioned successfully");
        };
    }
}
