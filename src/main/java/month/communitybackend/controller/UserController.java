package month.communitybackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import month.communitybackend.dto.UserDto;
import month.communitybackend.repository.UserRepository;
import month.communitybackend.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "User Document", description = "사용자 정보 관리 API 문서화")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    @PutMapping("/update")
    @Operation(summary = "사용자 정보 수정", description = "현재 로그인한 사용자의 정보를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "사용자 정보 수정 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    public ResponseEntity<?> updateUser(@RequestBody UserDto.Update updateDto) {
        userService.updateUser(updateDto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete")
    @Operation(summary = "사용자 탈퇴", description = "현재 로그인한 사용자의 계정을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "사용자 계정 삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    public ResponseEntity<?> deleteUser() {
        userService.deleteUser();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/check-username")
    @Operation(summary = "아이디 중복 확인", description = "사용자 아이디의 중복 여부를 확인합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "사용 가능한 아이디"),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 아이디")
    })
    public ResponseEntity<?> checkUsername(@RequestParam("username") String username) {
        if(userRepository.existsByUsername(username)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("해당 아이디가 이미 존재합니다.");
        }
        return ResponseEntity.ok().body("사용 가능한 아이디입니다.");
    }

    @GetMapping("/check-nickname")
    @Operation(summary = "닉네임 중복 확인", description = "사용자 닉네임의 중복 여부를 확인합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "사용 가능한 닉네임"),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 닉네임")
    })
    public ResponseEntity<?> checkNickname(@RequestParam("nickname") String nickname) {
        if(userRepository.existsByNickname(nickname)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("해당 닉네임이 이미 존재합니다.");
        }
        return ResponseEntity.ok().body("사용 가능한 닉네임 입니다.");
    }
}