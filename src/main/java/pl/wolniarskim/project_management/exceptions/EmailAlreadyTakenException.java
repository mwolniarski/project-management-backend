package pl.wolniarskim.project_management.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class EmailAlreadyTakenException extends RuntimeException{

    public EmailAlreadyTakenException(){
        super("Email already taken");
    }
}
