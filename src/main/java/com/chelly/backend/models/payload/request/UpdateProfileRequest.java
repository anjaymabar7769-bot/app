package com.chelly.backend.models.payload.request;

import java.time.LocalDateTime;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProfileRequest {
    @NotBlank(message = "Username tidak boleh kosong")
    @Size(min = 5, message = "Username minimal 5 karakter")
    @Size(max = 25, message = "Username maksimal 25 karakter")
    private String username;
    private String fullName;
    private String phoneNumber;
    private MultipartFile profilePicture;
}
