package com.chelly.backend.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.chelly.backend.models.User;
import com.chelly.backend.models.Voucher;
import com.chelly.backend.models.VoucherRedeem;
import com.chelly.backend.models.exceptions.RedeemException;
import com.chelly.backend.models.exceptions.ResourceNotFoundException;
import com.chelly.backend.repository.UserRepository;
import com.chelly.backend.repository.VoucherRedeemRepository;
import com.chelly.backend.repository.VoucherRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class VoucherService {
    private final VoucherRepository voucherRepository;
    private final VoucherRedeemRepository voucherRedeemRepository;
    private final UserRepository userRepository;

    public List<Voucher> getAllVouchers() {
        return voucherRepository.findAll();
    }

    public Voucher getVoucherById(int id) {
        return voucherRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Voucher not found with id: " + id));
    }

    public Voucher createVoucher(Voucher voucher) {
        return voucherRepository.save(voucher);
    }

    public Voucher redeem(Integer id) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Voucher voucher = getVoucherById(id);
        if (voucherRedeemRepository.existsByUserAndVoucher(user, voucher)) {
            throw new RedeemException("Voucher sudah di klaim");
        }

        if (user.getPoints() < voucher.getMinimumPoints()) {
            throw new RedeemException("Point anda tidak cukup");
        }

        user.setPoints(user.getPoints() - voucher.getMinimumPoints());
        userRepository.save(user);

        voucherRedeemRepository.save(VoucherRedeem.builder()
                .voucher(voucher)
                .user(user)
                .redeemedAt(LocalDateTime.now())
                .build());

        return voucher;
    }
}
