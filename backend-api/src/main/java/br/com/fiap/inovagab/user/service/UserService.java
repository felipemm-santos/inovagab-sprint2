package br.com.fiap.inovagab.user.service;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.fiap.inovagab.shared.exception.ApiException;
import br.com.fiap.inovagab.user.document.UserDocument;
import br.com.fiap.inovagab.user.model.UserRole;
import br.com.fiap.inovagab.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final int BCRYPT_MAXIMUM_PASSWORD_BYTES = 72;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserDocument registerOperator(
            String name,
            String email,
            String rawPassword
    ) {
        return create(name, email, rawPassword, UserRole.OPERADOR);
    }

    public UserDocument create(
            String name,
            String email,
            String rawPassword,
            UserRole role
    ) {
        String normalizedEmail = normalizeEmail(email);

        if (rawPassword.getBytes(StandardCharsets.UTF_8).length
                > BCRYPT_MAXIMUM_PASSWORD_BYTES) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "PASSWORD_TOO_LONG",
                    "Password must contain at most 72 bytes in UTF-8"
            );
        }

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw emailConflict();
        }

        var user = UserDocument.builder()
                .name(name.strip())
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .role(role)
                .active(true)
                .build();

        try {
            return userRepository.save(user);
        } catch (DuplicateKeyException exception) {
            throw emailConflict();
        }
    }

    public UserDocument findActiveById(String id) {
        return userRepository.findById(id)
                .filter(UserDocument::isActive)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.UNAUTHORIZED,
                        "INVALID_TOKEN",
                        "The authenticated user is no longer available"
                ));
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(normalizeEmail(email));
    }

    public static String normalizeEmail(String email) {
        return email == null
                ? null
                : email.strip().toLowerCase(Locale.ROOT);
    }

    private ApiException emailConflict() {
        return new ApiException(
                HttpStatus.CONFLICT,
                "EMAIL_ALREADY_REGISTERED",
                "This email is already registered"
        );
    }
}
