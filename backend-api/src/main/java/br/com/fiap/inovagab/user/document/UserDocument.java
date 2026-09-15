package br.com.fiap.inovagab.user.document;

import java.util.Locale;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

import br.com.fiap.inovagab.shared.document.AuditableDocument;
import br.com.fiap.inovagab.user.model.UserRole;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class UserDocument extends AuditableDocument {

    @MongoId(FieldType.OBJECT_ID)
    private String id;

    @Field("name")
    private String name;

    @Indexed(name = "ux_users_email", unique = true)
    @Field("email")
    private String email;

    @Field("passwordHash")
    private String passwordHash;

    @Field("role")
    private UserRole role;

    @Builder.Default
    @Field("active")
    private boolean active = true;

    public void setEmail(String email) {
        this.email = email == null ? null : email.strip().toLowerCase(Locale.ROOT);
    }
}

