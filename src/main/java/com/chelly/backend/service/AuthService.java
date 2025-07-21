package com.chelly.backend.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.chelly.backend.models.Role;
import com.chelly.backend.models.User;
import com.chelly.backend.models.UserSession;
import com.chelly.backend.models.exceptions.AuthException;
import com.chelly.backend.models.exceptions.DuplicateElementException;
import com.chelly.backend.models.exceptions.ResourceNotFoundException;
import com.chelly.backend.models.payload.request.LoginRequest;
import com.chelly.backend.models.payload.request.RegisterRequest;
import com.chelly.backend.repository.RoleRepository;
import com.chelly.backend.repository.UserRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AuthService {
    private UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionService sessionService;
    private final AuthenticationManager authenticationManager;
    private final RoleRepository roleRepository;

    public User register(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new DuplicateElementException("Email telah digunakan");
        }

        List<Role> roles = new ArrayList<>();
        roles.add(roleRepository.findByName("USER").orElseThrow(
                () -> new ResourceNotFoundException("Role was not found")));

        if (registerRequest.getEmail().equals("admin@gmail.com")) {
            roles.clear();
            roles.add(roleRepository.findByName("ADMIN").orElseThrow(
                    () -> new ResourceNotFoundException("Role was not found")));
        }

        return userRepository.save(User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .birthdate(registerRequest.getBirthDate())
                .roles(roles)
                .level(1)
                .points(100)
                .build());
    }

    public UserSession login(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail()).orElseThrow(
                () -> new ResourceNotFoundException("Email tidak ditemukan."));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new AuthException("Email atau password salah");
        }

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginRequest.getEmail(),
                loginRequest.getPassword()));

        return sessionService.createSession(user);
    }

    public void logout() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        sessionService.findByUser(user).ifPresent(sessionService::deleteSession);
    }

    // public User changePassword(String password) {
    // User user = (User)
    // SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    // user.setPassword = password;
    // return userRepository.save(password);
    // }
}
