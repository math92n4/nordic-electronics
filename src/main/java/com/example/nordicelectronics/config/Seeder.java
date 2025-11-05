//package com.example.nordicelectronics.config;
//
//import com.example.nordicelectronics.entity.User;
//import com.example.nordicelectronics.repositories.sql.UserRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDate;
//
//@Component
//@RequiredArgsConstructor
//public class Seeder implements CommandLineRunner {
//
//    private final UserRepository userRepository;
//    private final PasswordEncoder passwordEncoder;
//
//    @Override
//    public void run(String... args) throws Exception {
//        // Create default admin user if not exists
//        if (!userRepository.existsByEmail("admin@nordic.com")) {
//            User admin = User.builder()
//                    .firstName("Admin")
//                    .lastName("User")
//                    .email("admin@nordic.com")
//                    .phoneNumber("+45 12 34 56 78")
//                    .dateOfBirth(LocalDate.of(1990, 1, 1))
//                    .password(passwordEncoder.encode("admin"))
//                    .isAdmin(true)
//                    .build();
//            userRepository.save(admin);
//        }
//
//        // Create default regular user if not exists
//        if (!userRepository.existsByEmail("user@nordic.com")) {
//            User user = User.builder()
//                    .firstName("Regular")
//                    .lastName("User")
//                    .email("user@nordic.com")
//                    .phoneNumber("+45 87 65 43 21")
//                    .dateOfBirth(LocalDate.of(1995, 6, 15))
//                    .password(passwordEncoder.encode("user"))
//                    .isAdmin(false)
//                    .build();
//            userRepository.save(user);
//        }
//    }
//}
