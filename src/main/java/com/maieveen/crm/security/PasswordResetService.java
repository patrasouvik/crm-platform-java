package com.maieveen.crm.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int MAX_OTP_ATTEMPTS = 5;

    private final AdminUserRepository adminUserRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${crm.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${crm.mail.from:noreply@skp-lab.com}")
    private String from;

    @Value("${crm.mail.otp-expiry-minutes:10}")
    private long otpExpiryMinutes;

    public PasswordResetService(AdminUserRepository adminUserRepository,
                                PasswordResetTokenRepository tokenRepository,
                                PasswordEncoder passwordEncoder,
                                ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.adminUserRepository = adminUserRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSenderProvider = mailSenderProvider;
    }

    @Transactional
    public void requestOtp(String email) {
        if (email == null || email.isBlank()) {
            return;
        }

        Optional<AdminUser> adminOptional = adminUserRepository.findByEmailIgnoreCase(email.trim());
        if (adminOptional.isEmpty() || !adminOptional.get().isEnabled()) {
            return;
        }

        AdminUser adminUser = adminOptional.get();
        tokenRepository.findTopByAdminUserIdAndUsedAtIsNullOrderByCreatedAtDesc(adminUser.getId())
                .ifPresent(existing -> {
                    existing.markUsed();
                    tokenRepository.save(existing);
                });

        String otp = String.format("%06d", RANDOM.nextInt(1_000_000));
        PasswordResetToken token = new PasswordResetToken(
                adminUser,
                passwordEncoder.encode(otp),
                LocalDateTime.now().plusMinutes(otpExpiryMinutes));
        tokenRepository.save(token);

        if (mailEnabled) {
            JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
            if (mailSender == null) {
                throw new IllegalStateException("Mail is enabled but no mail sender is configured.");
            }
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(adminUser.getEmail());
            message.setSubject("SKP Lab CRM password reset OTP");
            message.setText("Your SKP Lab CRM password reset OTP is: " + otp
                    + "\n\nThis OTP expires in " + otpExpiryMinutes + " minutes.\n"
                    + "If you did not request a password reset, you can ignore this email.");
            mailSender.send(message);
        } else {
            log.info("Password reset OTP generated for admin '{}': {} (mail disabled; development mode)",
                    adminUser.getUsername(), otp);
        }
    }

    @Transactional
    public boolean verifyOtp(String email, String otp) {
        if (email == null || email.isBlank() || otp == null || !otp.matches("\\d{6}")) {
            return false;
        }

        Optional<AdminUser> adminOptional = adminUserRepository.findByEmailIgnoreCase(email.trim());
        if (adminOptional.isEmpty()) {
            return false;
        }

        Optional<PasswordResetToken> tokenOptional = tokenRepository
                .findTopByAdminUserIdAndUsedAtIsNullOrderByCreatedAtDesc(adminOptional.get().getId());

        if (tokenOptional.isEmpty()) {
            return false;
        }

        PasswordResetToken token = tokenOptional.get();
        if (token.isExpired() || token.getVerifiedAt() != null || token.getAttempts() >= MAX_OTP_ATTEMPTS) {
            return false;
        }

        if (!passwordEncoder.matches(otp, token.getOtpHash())) {
            token.incrementAttempts();
            if (token.getAttempts() >= MAX_OTP_ATTEMPTS) {
                token.markUsed();
            }
            tokenRepository.save(token);
            return false;
        }

        token.markVerified();
        tokenRepository.save(token);
        return true;
    }

    @Transactional
    public boolean resetPassword(Long adminUserId, String newPassword) {
        if (!isValidNewPassword(newPassword)) {
            return false;
        }

        Optional<PasswordResetToken> tokenOptional = tokenRepository
                .findTopByAdminUserIdAndUsedAtIsNullOrderByCreatedAtDesc(adminUserId);
        if (tokenOptional.isEmpty()) {
            return false;
        }

        PasswordResetToken token = tokenOptional.get();
        if (token.isExpired() || token.getVerifiedAt() == null) {
            return false;
        }

        AdminUser adminUser = adminUserRepository.findById(adminUserId).orElse(null);
        if (adminUser == null || !adminUser.isEnabled()) {
            return false;
        }

        if (passwordEncoder.matches(newPassword, adminUser.getPasswordHash())) {
            return false;
        }

        adminUser.setPasswordHash(passwordEncoder.encode(newPassword));
        adminUserRepository.save(adminUser);
        token.markUsed();
        tokenRepository.save(token);
        return true;
    }

    public boolean isValidNewPassword(String password) {
        return password != null && password.length() >= 12
                && password.matches(".*[A-Za-z].*")
                && password.matches(".*\\d.*")
                && password.matches(".*[^A-Za-z0-9].*");
    }

    @Transactional
    public boolean changePassword(String username, String currentPassword, String newPassword) {
        if (!isValidNewPassword(newPassword) || currentPassword == null) {
            return false;
        }

        AdminUser adminUser = adminUserRepository.findByUsername(username).orElse(null);
        if (adminUser == null || !passwordEncoder.matches(currentPassword, adminUser.getPasswordHash())) {
            return false;
        }

        if (passwordEncoder.matches(newPassword, adminUser.getPasswordHash())) {
            return false;
        }

        adminUser.setPasswordHash(passwordEncoder.encode(newPassword));
        adminUserRepository.save(adminUser);
        return true;
    }
}
