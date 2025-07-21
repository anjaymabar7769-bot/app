package com.chelly.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.context.SecurityContextHolder;
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
    private final CloudinaryService cloudinaryService;
    private final ReportRepository reportRepository;

    private static final Map<Integer, Integer> LEVEL_THRESHOLDS;

    static {
        LEVEL_THRESHOLDS = Map.of(
                1, 100,
                2, 200,
                3, 400,
                4, 700,
                5, 1000);
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
            user.setProfilePicture(cloudinaryService.uploadImage(updateProfileRequest.getProfilePicture()));
        }

        // user.setId(id);
        user.setEmail(updateProfileRequest.getEmail());
        user.setUsername(updateProfileRequest.getUsername());
        user.setBirthdate(updateProfileRequest.getBirthDate());

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
        if (reportStatus.equals(ReportStatus.COMPLETED)) {
            user.setPoints(user.getPoints() + 15);
            checkAndUpdateUserLevel(user);
            userRepository.save(user);
        }
    }

    private Integer getRequiredPointsPercentage(User user) {
        Integer threshold = LEVEL_THRESHOLDS.get(user.getLevel());
        if (threshold == null || threshold == 0)
            return 100;
        return Math.min(100, (user.getPoints() * 100) / threshold);
    }

    private Integer getRequiredPoints(User user) {
        Integer currentLevel = user.getLevel();
        return Math.max(0, LEVEL_THRESHOLDS.get(currentLevel) - user.getPoints());
    }

    private void checkAndUpdateUserLevel(User user) {
        int currentLevel = user.getLevel();
        int currentPoints = user.getPoints();
        int nextLevel = currentLevel + 1;

        while (LEVEL_THRESHOLDS.containsKey(nextLevel) && currentPoints >= LEVEL_THRESHOLDS.get(nextLevel)) {
            user.setLevel(nextLevel);
            System.out.println("User " + user.getUsername() + " leveled up to " + nextLevel + "!");
            nextLevel++;
        }
    }
}
