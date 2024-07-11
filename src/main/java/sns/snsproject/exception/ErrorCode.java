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
    ;

    private HttpStatus status;
    private String message;
}
