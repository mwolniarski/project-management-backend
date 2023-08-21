package pl.wolniarskim.project_management.resources;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.wolniarskim.project_management.models.DTO.ProfileDetails;
import pl.wolniarskim.project_management.models.DTO.ProfileDetailsWriteModel;
import pl.wolniarskim.project_management.services.UserService;

import java.io.IOException;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class UserProfileResource {

    private final UserService userService;

    @PostMapping("/profile-image")
    public ResponseEntity<ProfileDetails> uploadProfileImage(@RequestParam("image") MultipartFile file) throws IOException {
        return ResponseEntity.ok().body(userService.uploadProfileImage(file));
    }

    @GetMapping("/profile-details")
    public ResponseEntity<ProfileDetails> getProfileDetails() {
        return ResponseEntity.ok()
                .body(userService.getProfileDetails());
    }

    @PostMapping("/profile-details")
    public ResponseEntity<Void> saveProfileDetails(@RequestBody ProfileDetailsWriteModel profileDetailsWriteModel) {
        userService.saveUserProfile(profileDetailsWriteModel);
        return ResponseEntity.ok().build();
    }
}
