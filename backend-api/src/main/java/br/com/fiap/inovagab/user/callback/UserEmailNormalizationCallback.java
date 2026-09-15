package br.com.fiap.inovagab.user.callback;

import org.springframework.data.mongodb.core.mapping.event.BeforeConvertCallback;
import org.springframework.stereotype.Component;

import br.com.fiap.inovagab.user.document.UserDocument;

@Component
public class UserEmailNormalizationCallback implements BeforeConvertCallback<UserDocument> {

    @Override
    public UserDocument onBeforeConvert(UserDocument user, String collection) {
        user.setEmail(user.getEmail());
        return user;
    }
}

