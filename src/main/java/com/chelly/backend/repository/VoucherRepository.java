package com.chelly.backend.repository;

import com.chelly.backend.models.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Integer> {
    @Query("SELECT v FROM Voucher v WHERE v.id NOT IN " +
            "(SELECT vr.voucher.id FROM VoucherRedeem vr WHERE vr.user.id = :userId)")
    List<Voucher> findVouchersNotRedeemedByUser(@Param("userId") Integer userId);
}
