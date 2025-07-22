package com.chelly.backend.controller;

import com.chelly.backend.handler.ResponseHandler;
import com.chelly.backend.models.OneTimePassword;
import com.chelly.backend.models.User;
import com.chelly.backend.models.UserSession;
import com.chelly.backend.models.payload.request.*;
import com.chelly.backend.models.payload.response.SuccessResponse;
import com.chelly.backend.service.AuthService;
import com.chelly.backend.service.OneTimePasswordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
@Slf4j
@Tag(name = "Autentikasi", description = "API untuk proses login, registrasi, dan logout pengguna")
public class AuthController {
        private final AuthService authService;
        private final OneTimePasswordService oneTimePasswordService;

        @PostMapping("/register")
        @Operation(summary = "Registrasi Pengguna Baru", description = "Mendaftarkan akun pengguna baru ke dalam sistem")
        public ResponseEntity<SuccessResponse<User>> register(
                        @RequestBody @Valid RegisterRequest registerRequest) {
                log.info("Percobaan registrasi pengguna dengan email: {}", registerRequest.getEmail());
                return ResponseHandler.buildSuccessResponse(
                                HttpStatus.CREATED,
                                "Pengguna berhasil didaftarkan",
                                authService.register(registerRequest));
        }

        @PostMapping("/login")
        @Operation(summary = "Login Pengguna", description = "Melakukan otentikasi pengguna dan mengembalikan token Autentikasi")
        public ResponseEntity<SuccessResponse<UserSession>> login(
                        @RequestBody @Valid LoginRequest loginRequest) {
                log.info("Percobaan login oleh email: {}", loginRequest.getEmail());
                return ResponseHandler.buildSuccessResponse(
                                HttpStatus.OK,
                                "Login berhasil",
                                authService.login(loginRequest));
        }

        @PostMapping("/logout")
        @Operation(summary = "Logout Pengguna", description = "Keluar dari sistem dan menghapus token autentikasi")
        public ResponseEntity<SuccessResponse<String>> logout() {
                log.info("Permintaan logout oleh pengguna");
                authService.logout();
                return ResponseHandler.buildSuccessResponse(
                                HttpStatus.OK,
                                "Logout berhasil",
                                "Pengguna berhasil logout");
        }

        @PostMapping("/send-otp")
        public ResponseEntity<SuccessResponse<String>> sendOtp(@RequestBody @Valid EmailRequest emailRequest)
                        throws MessagingException {
                oneTimePasswordService.sendOneTimePassword(emailRequest);
                return ResponseHandler.buildSuccessResponse(
                                HttpStatus.OK,
                                "Berhasil Kirim OTP",
                                "Kode verifikasi telah dikirim , silahkan cek email anda");
        }

        @PostMapping("/verify-otp")
        public ResponseEntity<SuccessResponse<Boolean>> verifyOtp(
                        @RequestBody @Valid OneTimePasswordRequest oneTimePasswordRequest) {
                OneTimePassword oneTimePassword = oneTimePasswordService.verifyOneTimePassword(oneTimePasswordRequest);

                if (oneTimePassword == null) {
                        return ResponseHandler.buildSuccessResponse(
                                        HttpStatus.BAD_REQUEST,
                                        "OTP tidak valid atau sudah kedaluwarsa",
                                        false);
                }

                return ResponseHandler.buildSuccessResponse(
                                HttpStatus.OK,
                                "OTP berhasil diverifikasi",
                                true);
        }

        @PutMapping("/update-password")
        @Operation(summary = "Perbarui Password Pengguna", description = "Memperbarui password profil pengguna berdasarkan email")
        public ResponseEntity<SuccessResponse<User>> updatePassword(
                        @RequestBody @Valid UpdatePasswordRequest updatePasswordRequest) {
                return ResponseHandler.buildSuccessResponse(
                                HttpStatus.OK,
                                "Berhasil memperbarui profil pengguna",
                                authService.updatePassword(updatePasswordRequest));
        }

}
