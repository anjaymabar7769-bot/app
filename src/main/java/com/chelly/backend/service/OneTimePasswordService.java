package com.chelly.backend.service;

import com.chelly.backend.models.OneTimePassword;
import com.chelly.backend.models.User;
import com.chelly.backend.models.exceptions.ResourceNotFoundException;
import com.chelly.backend.models.payload.request.EmailRequest;
import com.chelly.backend.models.payload.request.OneTimePasswordRequest;
import com.chelly.backend.repository.OneTimePasswordRepository;
import com.chelly.backend.repository.UserRepository;
import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@AllArgsConstructor
public class OneTimePasswordService {
    private final OneTimePasswordRepository oneTimePasswordRepository;
    private final UserRepository userRepository;
    private final MailService mailService;
    private final SpringTemplateEngine templateEngine;

    public OneTimePassword generateOneTimePassword(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("Email tidak terdaftar.")
        );

        List<OneTimePassword> activeOtps = oneTimePasswordRepository.findAllByUserAndAvailableTrue(user);
        for (OneTimePassword otp : activeOtps) {
            otp.setAvailable(false);
        }
        oneTimePasswordRepository.saveAll(activeOtps);

        SecureRandom secureRandom = new SecureRandom();
        String code = String.format("%06d", secureRandom.nextInt(1_000_000));

        return oneTimePasswordRepository.save(OneTimePassword.builder()
                .code(code)
                .available(true)
                .user(user)
                .expiredAt(Instant.now().plus(5, ChronoUnit.MINUTES))
                .build());
    }

    public OneTimePassword findByCode(String code) {
        return oneTimePasswordRepository.findByCode(code).orElseThrow(
                () -> new ResourceNotFoundException("Kode verifikasi tidak valid.")
        );
    }

    public void sendOneTimePassword(EmailRequest emailRequest) throws MessagingException {
        OneTimePassword oneTimePassword = generateOneTimePassword(emailRequest.getEmail());
        mailService.sendMail(emailRequest.getEmail(), "Verify Your Identity", loadOneTimePasswordTemplate(oneTimePassword.getCode()));
    }

    public OneTimePassword verifyOneTimePassword(OneTimePasswordRequest oneTimePasswordRequest) {
        OneTimePassword oneTimePassword = findByCode(oneTimePasswordRequest.getCode());

        if (!oneTimePassword.isAvailable()) {
            throw new RuntimeException("Kode verifikasi tidak valid.");
        }

        if (Instant.now().isAfter(oneTimePassword.getExpiredAt())) {
            oneTimePassword.setAvailable(false);
            oneTimePasswordRepository.save(oneTimePassword);
            throw new RuntimeException("Kode verifikasi tidak valid.");
        }

//        if (!passwordEncoder.matches(oneTimePassword.getCode(), oneTimePasswordRequest.getCode())) {
//            throw new RuntimeException("Invalid one time password.");
//        }

        User user = oneTimePassword.getUser();
        user.setCanChangePassword(true);
        userRepository.save(user);

        oneTimePassword.setAvailable(false);
        oneTimePasswordRepository.save(oneTimePassword);

        return oneTimePassword;
    }

    public String loadOneTimePasswordTemplate(String code) {
        Context context = new Context();
        context.setVariable("otp_code", code);

        return templateEngine.process("otp_template.html", context);
    }
}
