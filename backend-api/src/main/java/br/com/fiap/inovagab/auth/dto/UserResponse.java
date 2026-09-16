package br.com.fiap.inovagab.auth.dto;

import br.com.fiap.inovagab.user.document.UserDocument;
import br.com.fiap.inovagab.user.model.UserRole;

public record UserResponse(
        String id,
        String name,
        String email,
        UserRole role,
        boolean active
) {

    public static UserResponse from(UserDocument user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.isActive()
        );
    }
}
