package pl.wolniarskim.project_management.resources;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.wolniarskim.project_management.services.UserService;

import java.io.IOException;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class UserProfileResource {

    private final UserService userService;

    @PostMapping("/profileImage")
    public ResponseEntity uploadProfileImage(@RequestParam("image")MultipartFile file) throws IOException {
        userService.uploadProfileImage(file);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/profileImage")
    public ResponseEntity getProfileImage() {
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("image/png"))
                .body(userService.getUserProfileImage());
    }
}
