package com.chelly.backend.models.payload.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "Username tidak boleh kosong")
    @Size(min = 5, message = "Username minimal 5 karakter")
    @Size(max = 25, message = "Username maksimal 25 karakter")
    private String username;

    @Email(message = "Format email tidak valid")
    @NotBlank(message = "Email tidak boleh kosong")
    @Size(min = 5, message = "Email minimal 5 karakter")
    @Size(max = 100, message = "Email maksimal 100 karakter")
    private String email;

    @PastOrPresent
    private LocalDateTime birthDate;

    @NotBlank(message = "Password tidak boleh kosong")
    @Size(min = 6, message = "Password minimal 6 karakter")
    @Size(max = 100, message = "Password maksimal 100 karakter")
    private String password;
}

