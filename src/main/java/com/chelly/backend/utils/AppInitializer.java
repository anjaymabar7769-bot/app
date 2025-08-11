package com.chelly.backend.utils;

import com.chelly.backend.models.Category;
import com.chelly.backend.models.Role;
import com.chelly.backend.models.Voucher;
import com.chelly.backend.models.payload.request.RegisterRequest;
import com.chelly.backend.repository.CategoryRepository;
import com.chelly.backend.repository.RoleRepository;
import com.chelly.backend.repository.VoucherRepository;
import com.chelly.backend.service.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
public class AppInitializer implements CommandLineRunner {
    private final RoleRepository roleRepository;
    private final AuthService authService;
    private final VoucherRepository voucherRepository;
    private final CategoryRepository categoryRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        initializeRoles();
        initUser();
        initVouchers();
        initializeCategory();
//        exportAllTablesToCsv("src/main/resources/exports");
    }

    public void exportAllTablesToCsv(String outputDir) {
        List<String> tables = jdbcTemplate.queryForList("SELECT table_name FROM user_tables", String.class);

        for (String table : tables) {
            exportTableToCsv(table, outputDir);
        }
    }

    private void exportTableToCsv(String tableName, String outputDir) {
        List<String> columns = jdbcTemplate.queryForList(
                "SELECT column_name FROM user_tab_columns WHERE table_name = ?",
                String.class,
                tableName.toUpperCase());

        String sql = "SELECT * FROM " + tableName;

        Path filePath = Paths.get(outputDir, tableName + ".csv");

        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(filePath))) {
            writer.println(String.join(",", columns));

            jdbcTemplate.query(sql, rs -> {
                do {
                    List<String> row = new ArrayList<>();
                    for (String col : columns) {
                        row.add(sanitize(rs.getString(col)));
                    }
                    writer.println(String.join(",", row));
                } while (rs.next());
            });

            System.out.println("Exported table: " + tableName);

        } catch (IOException e) {
            System.err.println("Gagal export table " + tableName + ": " + e.getMessage());
        }
    }

    private String sanitize(String value) {
        if (value == null) return "";
        return value.replace(",", " ");
    }

    void initializeCategory() {
        List<Category> categories = List.of(
                Category.builder()
                        .name("Parkir Sembarangan")
                        .build(),
                Category.builder()
                        .name("Melanggar Rambu Lalu Lintas")
                        .build(),
                Category.builder()
                        .name("Berkendara Melawan Arus")
                        .build(),
                Category.builder()
                        .name("Tidak Menggunakan Helm")
                        .build(),
                Category.builder()
                        .name("Menerobos Lampu Merah")
                        .build(),
                Category.builder()
                        .name("Menggunakan HP Saat Berkendara")
                        .build(),
                Category.builder()
                        .name("Tidak Menggunakan Sabuk Pengaman")
                        .build(),
                Category.builder()
                        .name("Lainnya")
                        .build()
        );

        categoryRepository.saveAll(categories);
    }

    void initVouchers() {
        Voucher voucher1 = Voucher.builder()
                .title("Voucher Pulsa Telkomsel 10.000")
                .description("Tukar 10.000 poin untuk pulsa Telkomsel senilai Rp10.000")
                .minimumPoints(50)
                .build();

        Voucher voucher2 = Voucher.builder()
                .title("Voucher Diskon Grab 20%")
                .description("Dapatkan diskon 20% untuk layanan GrabCar atau GrabFood")
                .minimumPoints(7500)
                .build();

        Voucher voucher3 = Voucher.builder()
                .title("Voucher GoPay Cashback Rp5.000")
                .description("Cashback GoPay senilai Rp5.000 untuk transaksi min. Rp25.000")
                .minimumPoints(5000)
                .build();

        voucherRepository.saveAll(List.of(voucher1, voucher2, voucher3));
    }

    void initUser() {
        RegisterRequest admin = RegisterRequest.builder()
                .username("admin")
                .email("admin@gmail.com")
                .password("admin123")
                .birthDate(LocalDateTime.now().minusDays(12))
                .build();

        RegisterRequest user = RegisterRequest.builder()
                .username("username")
                .email("user@gmail.com")
                .password("user123")
                .birthDate(LocalDateTime.now().minusDays(12))
                .build();

        authService.register(admin);
        authService.register(user);
    }

    void initializeRoles() {
        List<Role> roles = new ArrayList<>();
        roles.add(Role.builder()
                .name("USER")
                .build());
        roles.add(Role.builder()
                .name("ADMIN")
                .build());
        roleRepository.saveAll(roles);
    }
}
