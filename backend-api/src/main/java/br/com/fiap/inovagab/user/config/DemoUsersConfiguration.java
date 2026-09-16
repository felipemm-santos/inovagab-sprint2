package br.com.fiap.inovagab.user.config;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import br.com.fiap.inovagab.user.model.UserRole;
import br.com.fiap.inovagab.user.service.UserService;

@Slf4j
@Profile("local")
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(DemoUsersProperties.class)
public class DemoUsersConfiguration {

    @Bean
    @ConditionalOnProperty(
            prefix = "inovagab.bootstrap.demo-users",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true
    )
    public ApplicationRunner demoUsersInitializer(
            UserService userService,
            DemoUsersProperties properties
    ) {
        return arguments -> {
            var definitions = List.of(
                    new DemoUserDefinition(
                            properties.operador(),
                            UserRole.OPERADOR
                    ),
                    new DemoUserDefinition(
                            properties.gestor(),
                            UserRole.GESTOR
                    ),
                    new DemoUserDefinition(
                            properties.lider(),
                            UserRole.LIDER
                    )
            );

            int createdUsers = 0;
            for (DemoUserDefinition definition : definitions) {
                var user = definition.user();

                if (userService.existsByEmail(user.email())) {
                    continue;
                }

                userService.create(
                        user.name(),
                        user.email(),
                        properties.password(),
                        definition.role()
                );
                createdUsers++;
            }

            log.info(
                    "Demo users verified: {} created, {} already existed",
                    createdUsers,
                    definitions.size() - createdUsers
            );
        };
    }

    private record DemoUserDefinition(
            DemoUsersProperties.DemoUser user,
            UserRole role
    ) {
    }
}
