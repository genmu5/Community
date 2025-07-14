package month.communitybackend.controller;

import lombok.RequiredArgsConstructor;
import month.communitybackend.dto.UserDto;
import month.communitybackend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PutMapping("/update")
    public ResponseEntity<?> updateUser(@RequestBody UserDto.Update updateDto) {
        userService.updateUser(updateDto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteUser() {
        userService.deleteUser();
        return ResponseEntity.ok().build();
    }
}
