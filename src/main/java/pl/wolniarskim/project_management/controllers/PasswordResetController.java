package pl.wolniarskim.project_management.controllers;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pl.wolniarskim.project_management.services.UserService;

@RestController
@RequestMapping(path = "/api/password/reset")
@AllArgsConstructor
public class PasswordResetController {

    private final UserService userService;

    @PostMapping("/{token}")
    public void changePassword(@RequestBody String password, @PathVariable String token){
        userService.changePassword(password, token);
    }

    @PostMapping
    public void resetPassword(@RequestParam String email){
        userService.createResetToken(email);
    }
}
