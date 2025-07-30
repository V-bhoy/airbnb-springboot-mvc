package com.vb.projects.airBnbApp.controller;

import com.vb.projects.airBnbApp.dto.LoginRequestDto;
import com.vb.projects.airBnbApp.dto.LoginResponseDto;
import com.vb.projects.airBnbApp.dto.UserDto;
import com.vb.projects.airBnbApp.dto.UserRequestDto;
import com.vb.projects.airBnbApp.exception.ResourceNotFoundException;
import com.vb.projects.airBnbApp.security.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<UserDto> signup(@RequestBody UserRequestDto userRequestDto) {
        return new ResponseEntity<>(authService.registerUser(userRequestDto), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequestDto, HttpServletResponse httpServletResponse) {
        String[] tokens = authService.loginUser(loginRequestDto);
        Cookie cookie = new Cookie("refresh_token", tokens[1]);
        cookie.setHttpOnly(true);
        httpServletResponse.addCookie(cookie);
        return ResponseEntity.ok(new LoginResponseDto(tokens[0]));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDto> refreshToken(HttpServletRequest request) {
        String refreshToken = Arrays.stream(request.getCookies()).filter(c->c.getName().equals("refresh_token"))
                .findFirst()
                .map(   Cookie::getValue)
                .orElseThrow(() -> new AuthenticationServiceException("Refresh token not found"));
        String token = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(new LoginResponseDto(token));
    }
}
