package com.chelly.backend.models.payload.request;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailRequest {
    @Email(message = "Format email tidak valid")
    @NotBlank(message = "Email tidak boleh kosong")
    @Size(min = 5, message = "Email minimal 5 karakter")
    @Size(max = 100, message = "Email maksimal 100 karakter")
    private String email;
}
