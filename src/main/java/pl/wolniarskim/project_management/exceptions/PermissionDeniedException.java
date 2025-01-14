package pl.wolniarskim.project_management.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.FORBIDDEN)
public class PermissionDeniedException extends RuntimeException{

    public PermissionDeniedException(){
        super("Permission denied");
    }
}
