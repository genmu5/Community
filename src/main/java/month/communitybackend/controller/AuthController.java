package month.communitybackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import month.communitybackend.domain.Role;
import month.communitybackend.domain.User;
import month.communitybackend.dto.UserDto;
import month.communitybackend.security.JwtTokenProvider;
import month.communitybackend.service.UserService;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/register")
    public ResponseEntity<UserDto.Response> register(
            @Valid @RequestBody UserDto.signupRequest dto
    ) {
        User created = userService.register(dto);

        UserDto.Response res = UserDto.Response.builder()
                .id(created.getId())
                .username(created.getUsername())
                .email(created.getEmail())
                .nickname(created.getNickname())
                .roles(created.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet()))
                .build();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(res);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String,String>> login(
            @Valid @RequestBody UserDto.LoginRequest dto
    ) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        dto.getUsername(), dto.getPassword()
                )
        );
        String token = jwtTokenProvider.createToken(
                auth.getName(),
                auth.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList()
        );
        return ResponseEntity.ok(Map.of("token", token));
    }
}
