package pl.wolniarskim.project_management.exceptions;

public class UserAlreadyAssociatedException extends RuntimeException{
    public UserAlreadyAssociatedException(){
        super("User associated with this project");
    }
}
