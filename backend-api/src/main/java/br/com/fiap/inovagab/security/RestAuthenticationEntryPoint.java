package br.com.fiap.inovagab.security;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

import br.com.fiap.inovagab.shared.exception.ApiException;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final HandlerExceptionResolver resolver;

    public RestAuthenticationEntryPoint(
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver
    ) {
        this.resolver = resolver;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authenticationException
    ) throws IOException, ServletException {
        boolean tokenWasProvided = request.getHeader(HttpHeaders.AUTHORIZATION) != null;
        String code = tokenWasProvided
                ? "INVALID_TOKEN"
                : "AUTHENTICATION_REQUIRED";
        String message = tokenWasProvided
                ? "The access token is invalid or expired"
                : "Authentication is required to access this resource";

        resolver.resolveException(
                request,
                response,
                null,
                new ApiException(HttpStatus.UNAUTHORIZED, code, message)
        );
    }
}
