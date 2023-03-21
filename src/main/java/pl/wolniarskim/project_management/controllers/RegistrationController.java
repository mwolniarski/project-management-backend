package pl.wolniarskim.project_management.controllers;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.wolniarskim.project_management.models.DTO.RegistrationRequest;
import pl.wolniarskim.project_management.services.RegistrationService;

@RestController
@RequestMapping(path = "/api/registration")
@AllArgsConstructor
public class RegistrationController {

    private RegistrationService registrationService;

    @PostMapping
    public String register(@RequestBody RegistrationRequest request){
        return registrationService.register(request);
    }

    @PostMapping("/confirm")
    public ResponseEntity<String> confirm(@RequestParam("token") String token){
        registrationService.confirmToken(token);
        return ResponseEntity.ok().build();
    }
}
