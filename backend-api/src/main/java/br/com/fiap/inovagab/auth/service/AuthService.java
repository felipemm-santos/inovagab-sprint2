package br.com.fiap.inovagab.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import br.com.fiap.inovagab.audit.service.AuthenticationAuditService;
import br.com.fiap.inovagab.auth.dto.AuthResponse;
import br.com.fiap.inovagab.auth.dto.LoginRequest;
import br.com.fiap.inovagab.auth.dto.RegisterRequest;
import br.com.fiap.inovagab.auth.dto.UserResponse;
import br.com.fiap.inovagab.security.JwtTokenService;
import br.com.fiap.inovagab.shared.exception.ApiException;
import br.com.fiap.inovagab.user.document.UserDocument;
import br.com.fiap.inovagab.user.repository.UserRepository;
import br.com.fiap.inovagab.user.service.UserService;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final UserService userService;
    private final JwtTokenService jwtTokenService;
    private final AuthenticationAuditService auditService;

    public UserResponse register(RegisterRequest request) {
        UserDocument user = userService.registerOperator(
                request.name(),
                request.email(),
                request.password()
        );
        return UserResponse.from(user);
    }

    public AuthResponse login(LoginRequest request) {
        String email = UserService.normalizeEmail(request.email());

        try {
            authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(
                            email,
                            request.password()
                    )
            );
        } catch (BadCredentialsException | AccountStatusException exception) {
            auditService.recordFailure();
            throw new ApiException(
                    HttpStatus.UNAUTHORIZED,
                    "INVALID_CREDENTIALS",
                    "Email or password is invalid"
            );
        }

        UserDocument user = userRepository.findByEmail(email)
                .filter(UserDocument::isActive)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.UNAUTHORIZED,
                        "INVALID_CREDENTIALS",
                        "Email or password is invalid"
                ));
        var generatedToken = jwtTokenService.generate(user);
        auditService.recordSuccess(user);

        return new AuthResponse(
                generatedToken.value(),
                "Bearer",
                generatedToken.expiresIn(),
                generatedToken.expiresAt(),
                UserResponse.from(user)
        );
    }

    public UserResponse currentUser(String userId) {
        return UserResponse.from(userService.findActiveById(userId));
    }
}
