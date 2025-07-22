package com.chelly.backend.repository;

import com.chelly.backend.models.User;
import com.chelly.backend.models.Voucher;
import com.chelly.backend.models.VoucherRedeem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VoucherRedeemRepository extends JpaRepository<VoucherRedeem, Integer> {
    Boolean existsByUserAndVoucher(User user, Voucher voucher);


}
