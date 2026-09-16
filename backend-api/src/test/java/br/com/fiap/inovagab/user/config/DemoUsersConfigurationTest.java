package br.com.fiap.inovagab.user.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;

import br.com.fiap.inovagab.user.model.UserRole;
import br.com.fiap.inovagab.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class DemoUsersConfigurationTest {

    @Mock
    private UserService userService;

    @Test
    void shouldCreateTheThreeStandardAcademicUsersWhenTheyDoNotExist()
            throws Exception {
        var runner = new DemoUsersConfiguration()
                .demoUsersInitializer(userService, defaultProperties());

        runner.run(new DefaultApplicationArguments());

        verify(userService).create(
                "Operador Base",
                "operador@aguiabranca.com.br",
                "000000",
                UserRole.OPERADOR
        );
        verify(userService).create(
                "Gestor Tático",
                "gestor@aguiabranca.com.br",
                "000000",
                UserRole.GESTOR
        );
        verify(userService).create(
                "Líder Executivo",
                "lider@aguiabranca.com.br",
                "000000",
                UserRole.LIDER
        );
    }

    @Test
    void shouldNotOverwriteAcademicUsersThatAlreadyExist() throws Exception {
        when(userService.existsByEmail(anyString())).thenReturn(true);
        var runner = new DemoUsersConfiguration()
                .demoUsersInitializer(userService, defaultProperties());

        runner.run(new DefaultApplicationArguments());

        verify(userService, never()).create(
                anyString(),
                anyString(),
                anyString(),
                any(UserRole.class)
        );
    }

    private DemoUsersProperties defaultProperties() {
        return new DemoUsersProperties(
                true,
                "000000",
                new DemoUsersProperties.DemoUser(
                        "Operador Base",
                        "operador@aguiabranca.com.br"
                ),
                new DemoUsersProperties.DemoUser(
                        "Gestor Tático",
                        "gestor@aguiabranca.com.br"
                ),
                new DemoUsersProperties.DemoUser(
                        "Líder Executivo",
                        "lider@aguiabranca.com.br"
                )
        );
    }
}
