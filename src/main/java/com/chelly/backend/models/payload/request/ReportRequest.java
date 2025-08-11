package com.chelly.backend.models.payload.request;

import java.time.LocalDateTime;

import org.springframework.web.multipart.MultipartFile;

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
public class ReportRequest {
    private Integer categoryId;

    @NotBlank(message = "Deskripsi laporan tidak boleh kosong")
    @Size(min = 10, max = 200, message = "Deskripsi laporan harus antara 10 sampai 200 karakter")
    private String description;

    @NotBlank(message = "Alamat laporan tidak boleh kosong")
    @Size(min = 5, max = 100, message = "Alamat laporan harus antara 5 sampai 100 karakter")
    private String location;

    // @PastOrPresent(message = "Tanggal dan waktu laporan tidak boleh di m asa
    // depan")
    private LocalDateTime reportDate;

    private MultipartFile image;
}
