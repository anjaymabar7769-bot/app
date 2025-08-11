package com.chelly.backend.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "vouchers")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Voucher {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "voucher_id_generator")
    @SequenceGenerator(name = "voucher_id_generator", sequenceName = "voucher_id_seq", allocationSize = 1)
    @Column(length = 10)
    private Integer id;

    @Column(nullable = false , length = 50)
    private String title;

    @Column(nullable = false , length = 100)
    private String description;

    @Column(nullable = false)
    private Integer minimumPoints;

    @CreationTimestamp
    @Column(updatable = false)
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss", timezone = "Asia/Jakarta")
    @JsonIgnore
    private Date createdAt;

    @UpdateTimestamp
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss", timezone = "Asia/Jakarta")
    @JsonIgnore
    private Date updatedAt;
}
