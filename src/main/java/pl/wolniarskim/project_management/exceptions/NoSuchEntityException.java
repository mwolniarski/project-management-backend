package pl.wolniarskim.project_management.exceptions;

public class NoSuchEntityException extends RuntimeException{

    public NoSuchEntityException(){
        super("No entity with given id");
    }
}
