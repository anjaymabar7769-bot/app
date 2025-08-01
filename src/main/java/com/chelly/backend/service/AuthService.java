package com.chelly.backend.service;

import com.chelly.backend.models.Role;
import com.chelly.backend.models.User;
import com.chelly.backend.models.UserSession;
import com.chelly.backend.models.exceptions.AuthException;
import com.chelly.backend.models.exceptions.DuplicateElementException;
import com.chelly.backend.models.exceptions.ResourceNotFoundException;
import com.chelly.backend.models.payload.request.LoginRequest;
import com.chelly.backend.models.payload.request.RegisterRequest;
import com.chelly.backend.models.payload.request.UpdatePasswordRequest;
import com.chelly.backend.repository.RoleRepository;
import com.chelly.backend.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class AuthService {
    private UserRepository userRepository;
    private final SessionService sessionService;
    private final RoleRepository roleRepository;
    private final UserDetailsService userDetailsService;

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
                .password(registerRequest.getPassword())
                .birthdate(registerRequest.getBirthDate())
                .canChangePassword(false)
                .roles(roles)
                .level(1)
                .points(0)
                .build());
    }

    public UserSession login(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail()).orElseThrow(
                () -> new ResourceNotFoundException("Email tidak ditemukan."));

        if (!loginRequest.getPassword().equals(user.getPassword())) {
            throw new AuthException("Email atau password salah");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(), userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return sessionService.createSession(user);
    }

    public void logout() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        sessionService.findByUser(user).ifPresent(sessionService::deleteSession);
    }

    public User updatePassword(UpdatePasswordRequest updatePasswordRequest) {
        User user = userRepository.findByEmail(updatePasswordRequest.getEmail()).orElseThrow(
                () -> new ResourceNotFoundException("User tidak ditemukan"));
        if (!user.getCanChangePassword()) {
            throw new ResourceNotFoundException("Can't change password");
        }
        user.setPassword(updatePasswordRequest.getPassword());
        user.setCanChangePassword(false);
        return userRepository.save(user);
    }
}
