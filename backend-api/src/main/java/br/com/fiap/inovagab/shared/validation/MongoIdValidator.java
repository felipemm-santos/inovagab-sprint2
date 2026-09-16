package br.com.fiap.inovagab.shared.validation;

import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;

import br.com.fiap.inovagab.shared.exception.ApiException;

public final class MongoIdValidator {

    private MongoIdValidator() {
    }

    public static void requireValid(String id) {
        if (id == null || !ObjectId.isValid(id)) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_ID",
                    "The resource identifier is invalid"
            );
        }
    }
}
