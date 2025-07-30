package com.vb.projects.airBnbApp.security;

import com.vb.projects.airBnbApp.dto.LoginRequestDto;
import com.vb.projects.airBnbApp.dto.UserDto;
import com.vb.projects.airBnbApp.dto.UserRequestDto;
import com.vb.projects.airBnbApp.entity.User;
import com.vb.projects.airBnbApp.enums.Role;
import com.vb.projects.airBnbApp.exception.ResourceNotFoundException;
import com.vb.projects.airBnbApp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;

    public UserDto registerUser(UserRequestDto userRequestDto) {
        User user = userRepository.findByEmail(userRequestDto.getEmail()).orElse(null);
        if(user != null) {
            throw new RuntimeException("User already exists!");
        }
        user = modelMapper.map(userRequestDto, User.class);
        user.setRoles(Set.of(Role.GUEST));
        user.setPassword(passwordEncoder.encode(userRequestDto.getPassword()));
        user = userRepository.save(user);
        return modelMapper.map(user, UserDto.class);
    }

    public String[] loginUser(LoginRequestDto loginRequestDto) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginRequestDto.getEmail(), loginRequestDto.getPassword()
        ));
        User user = (User) authentication.getPrincipal();
        String[] tokens = new String[2];
        tokens[0] = jwtService.generateAccessToken(user);
        tokens[1] = jwtService.generateRefreshToken(user);
        return tokens;
    }

    public String refreshToken(String token) {
        Long id = jwtService.getUserIdFromToken(token);
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found with id: "+id));
        return jwtService.generateAccessToken(user);
    }
}
