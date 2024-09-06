package sns.snsproject.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    DUPLICATE_USER_NAME(HttpStatus.CONFLICT, "Duplicate User Name"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User Not Found"),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "Invalid Password"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "Token is invalid"),
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "Post Not Found"),
    INVALID_PERMISSION(HttpStatus.UNAUTHORIZED, "Permission is invalid"),
    ALREADY_LIKED(HttpStatus.CONFLICT, "User already liked post"),
    DUPLICATE_FOLLOW(HttpStatus.CONFLICT, "Duplicate Follow"),
    FOLLOW_NOT_FOUND(HttpStatus.NOT_FOUND, "FOLLOW Not Fount")
    ;

    private HttpStatus status;
    private String message;
}
