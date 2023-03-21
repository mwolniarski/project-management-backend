package pl.wolniarskim.project_management.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.wolniarskim.project_management.models.DTO.LoginCredentials;

@RestController
public class LoginController {

    @PostMapping("/login")
    public void login(LoginCredentials loginCredentials){
        // it's only created for swagger ui
    }
}
