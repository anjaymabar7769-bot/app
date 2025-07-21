package com.chelly.backend.controller;

import com.chelly.backend.handler.ResponseHandler;
import com.chelly.backend.models.Voucher;
import com.chelly.backend.models.payload.response.SuccessResponse;
import com.chelly.backend.service.VoucherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Voucher", description = "API untuk manajemen dan penukaran voucher")
@RestController
@RequestMapping("/voucher")
@AllArgsConstructor
public class VoucherController {

    private final VoucherService voucherService;

    @Operation(summary = "Ambil semua voucher")
    @GetMapping
    public ResponseEntity<SuccessResponse<List<Voucher>>> getAllVouchers() {
        return ResponseHandler.buildSuccessResponse(
                HttpStatus.OK,
                "Berhasil mengambil semua voucher",
                voucherService.getAllVouchers()
        );
    }

    @Operation(summary = "Ambil detail voucher berdasarkan ID")
    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse<Voucher>> getVoucherById(@PathVariable Integer id) {
        return ResponseHandler.buildSuccessResponse(
                HttpStatus.OK,
                "Voucher ditemukan",
                voucherService.getVoucherById(id)
        );
    }

    @Operation(summary = "Buat voucher baru (admin)")
    public ResponseEntity<SuccessResponse<Voucher>> createVoucher(@RequestBody Voucher voucher) {
        return ResponseHandler.buildSuccessResponse(
                HttpStatus.CREATED,
                "Voucher berhasil dibuat",
                voucherService.createVoucher(voucher)
        );
    }

    @Operation(summary = "Tukar voucher oleh user login")
    @PostMapping("/{id}/redeem")
    public ResponseEntity<SuccessResponse<Voucher>> redeemVoucher(@PathVariable Integer id) {
        return ResponseHandler.buildSuccessResponse(
                HttpStatus.OK,
                "Voucher berhasil ditukar",
                voucherService.redeem(id)
        );
    }
}
