package com.chelly.backend.models;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "one_time_passwords")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class OneTimePassword {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "otp_generator")
    @SequenceGenerator(name = "otp_generator", sequenceName = "otp_id_seq", allocationSize = 1)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String code;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @Column(nullable = false)
    private boolean available = true;

    @Column(nullable = false)
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss", timezone = "Asia/Jakarta")
    private Instant expiredAt;
}
