package com.chelly.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.chelly.backend.models.Report;
import com.chelly.backend.models.ReportSearchCriteria;
import com.chelly.backend.models.User;
import com.chelly.backend.models.enums.ReportStatus;
import com.chelly.backend.models.exceptions.ResourceNotFoundException;
import com.chelly.backend.models.payload.request.UpdateProfileRequest;
import com.chelly.backend.models.payload.response.ReportStats;
import com.chelly.backend.models.payload.response.UserResponse;
import com.chelly.backend.models.payload.response.UserStats;
import com.chelly.backend.models.specifications.ReportSpecification;
import com.chelly.backend.repository.ReportRepository;
import com.chelly.backend.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final ReportRepository reportRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    private static final Map<Integer, Integer> LEVEL_THRESHOLDS;

    static {
        LEVEL_THRESHOLDS = Map.of(
                1, 0,
                2, 100,
                3, 200,
                4, 400,
                5, 700,
                6, 1000);
    }

    public ReportStats getUserReportStats() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return reportRepository.getReportStats(user.getId());
    }

    public List<Report> searchReportsByUser(ReportSearchCriteria criteria) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        criteria.setUserId(user.getId());

        return reportRepository.findAll(ReportSpecification.getSpecification(criteria));
    }

    public List<Report> findAllByCurrentUser() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return reportRepository.findAllByUser(user);
    }

    public List<Report> getUserReportHistory() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        ReportSearchCriteria criteria = new ReportSearchCriteria();
        criteria.setUserId(user.getId());
        criteria.setStartDate(LocalDateTime.now().minusWeeks(1));
        criteria.setEndDate(LocalDateTime.now());

        return reportRepository.findAll(ReportSpecification.getSpecification(criteria));
    }

    @Transactional
    public UserResponse getCurrentUser() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .address(user.getAddress())
                .birthdate(user.getBirthdate())
                .profilePicture(user.getProfilePicture())
                .roles(user.getRoles())
                .reportStats(reportRepository.getReportStats(user.getId()))
                .stats(UserStats.builder()
                        .level(user.getLevel())
                        .points(user.getPoints())
                        .requiredPoints(getRequiredPoints(user))
                        .requiredPointsPercentage(getRequiredPointsPercentage(user))
                        .build())
                .build();
    }

    public User updateProfile(Integer id, UpdateProfileRequest updateProfileRequest) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("User tidak ditemukan"));

        if (updateProfileRequest.getProfilePicture() != null) {
            user.setProfilePicture(fileStorageService.storeFile(updateProfileRequest.getProfilePicture()));
        }

        // user.setId(id);
        user.setUsername(updateProfileRequest.getUsername());
        user.setFullName(updateProfileRequest.getFullName());
        user.setPhoneNumber(updateProfileRequest.getPhoneNumber());

        return userRepository.save(user);
    }

    public Integer getCurrentUserRank(Integer userId) {
        return userRepository.getUserRank(userId);
    }

    public Boolean canModifyProfile(Integer id) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return user.getId().equals(id);
    }

    @Transactional
    public void updateStats(User user, ReportStatus reportStatus) {
        int pointsToAdd = 0;
        if (reportStatus == ReportStatus.COMPLETED) {
            pointsToAdd = 15;
        }
        user.setPoints(user.getPoints() + pointsToAdd);

        checkAndUpdateUserLevel(user);

        userRepository.save(user);
    }

    private void checkAndUpdateUserLevel(User user) {
        int currentLevel = user.getLevel();
        int currentPoints = user.getPoints();
        int nextLevel = currentLevel + 1;

        log.info("--- Checking Level Up for User: {} ---", user.getUsername());
        log.info("Current Level: {}", currentLevel);
        log.info("Current Points (after addition): {}", currentPoints);
        log.info("Next Level to check: {}", nextLevel);

        boolean leveledUp = false;
        while (LEVEL_THRESHOLDS.containsKey(nextLevel) && currentPoints >= LEVEL_THRESHOLDS.get(nextLevel)) {
            log.info("Threshold for Level {}: {}", nextLevel, LEVEL_THRESHOLDS.get(nextLevel));
            user.setLevel(nextLevel);
            log.info("User {} LEVELED UP to Level {}!", user.getUsername(), nextLevel);
            leveledUp = true;
            nextLevel++; // Lanjut cek level berikutnya
        }

        if (!leveledUp) {
            log.info("User {} did not level up. Still at Level {} (Points: {}). Next level ({}) requires {} points.",
                    user.getUsername(), currentLevel, currentPoints, nextLevel,
                    LEVEL_THRESHOLDS.getOrDefault(nextLevel, 0));
        }
        log.info("--- End Level Up Check ---");
    }

    public Integer getRequiredPoints(User user) {
        int currentLevel = user.getLevel();
        int currentPoints = user.getPoints();

        // Cek level berikutnya
        int nextLevel = currentLevel + 1;
        Integer nextLevelThreshold = LEVEL_THRESHOLDS.get(nextLevel); // Ambil threshold untuk level selanjutnya

        if (nextLevelThreshold == null) {
            // User sudah di level maksimal atau tidak ada level selanjutnya yang
            // didefinisikan
            return 0; // Tidak ada poin yang dibutuhkan lagi
        }

        int required = nextLevelThreshold - currentPoints;
        return Math.max(required, 0); // Pastikan tidak negatif
    }

    public Integer getRequiredPointsPercentage(User user) {
        int currentLevel = user.getLevel();
        int currentPoints = user.getPoints();

        // Cek level berikutnya
        int nextLevel = currentLevel + 1;
        Integer nextLevelThreshold = LEVEL_THRESHOLDS.get(nextLevel);

        // Ambil threshold untuk level saat ini (untuk menghitung basis persentase)
        // Jika user di level 1 (threshold 0), maka targetnya adalah threshold level 2
        // (100)
        Integer currentLevelStartThreshold = LEVEL_THRESHOLDS.getOrDefault(currentLevel, 0);

        if (nextLevelThreshold == null || currentLevelStartThreshold == nextLevelThreshold) {
            // User sudah di level maksimal atau tidak ada progres selanjutnya yang bisa
            // dihitung
            return 100;
        }

        // Hitung poin yang sudah didapat DI ATAS ambang batas level saat ini
        // Misalnya, di Level 2 (start 100 poin), user punya 105 poin. Dia sudah 5 poin
        // di atas ambang batas Level 2.
        int pointsEarnedInCurrentLevelSegment = currentPoints - currentLevelStartThreshold;

        // Hitung total poin yang dibutuhkan untuk segmen level ini (misal dari 100 ke
        // 200 = 100 poin)
        int pointsNeededForNextLevelSegment = nextLevelThreshold - currentLevelStartThreshold;

        if (pointsNeededForNextLevelSegment <= 0) {
            return 100; // Hindari pembagian nol atau negatif
        }

        int percentage = (int) (((double) pointsEarnedInCurrentLevelSegment / pointsNeededForNextLevelSegment) * 100);
        return Math.min(percentage, 100); // Pastikan tidak lebih dari 100%
    }

}
