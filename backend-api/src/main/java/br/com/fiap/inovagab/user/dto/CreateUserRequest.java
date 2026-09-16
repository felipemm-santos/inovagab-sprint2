package br.com.fiap.inovagab.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import br.com.fiap.inovagab.user.model.UserRole;

public record CreateUserRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Size(min = 6, max = 72) String password,
        @NotNull UserRole role
) {
}
