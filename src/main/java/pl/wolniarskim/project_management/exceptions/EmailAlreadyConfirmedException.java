package pl.wolniarskim.project_management.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class EmailAlreadyConfirmedException extends RuntimeException {

    public EmailAlreadyConfirmedException(){
        super("Email already confirmed");
    }
}
