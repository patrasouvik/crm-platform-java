package com.maieveen.crm.web;

import com.maieveen.crm.audit.AuditService;
import com.maieveen.crm.security.AdminUser;
import com.maieveen.crm.security.AdminUserRepository;
import com.maieveen.crm.security.PasswordResetService;
import com.maieveen.crm.user.CrmUser;
import com.maieveen.crm.user.CrmUserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/crm")
public class CrmController {

    private static final String RESET_ADMIN_ID = "CRM_RESET_ADMIN_ID";
    private static final String RESET_EMAIL = "CRM_RESET_EMAIL";

    private final CrmUserRepository userRepository;
    private final AuditService auditService;
    private final AdminUserRepository adminUserRepository;
    private final PasswordResetService passwordResetService;

    public CrmController(CrmUserRepository userRepository,
                         AuditService auditService,
                         AdminUserRepository adminUserRepository,
                         PasswordResetService passwordResetService) {
        this.userRepository = userRepository;
        this.auditService = auditService;
        this.adminUserRepository = adminUserRepository;
        this.passwordResetService = passwordResetService;
    }

    @GetMapping
    public String landing() { return "crm/landing"; }

    @GetMapping("/login")
    public String login() { return "crm/login"; }

    @GetMapping("/admin")
    public String admin() { return "crm/admin"; }

    @GetMapping("/admin/change-password")
    public String changePassword() { return "crm/change-password"; }

    @PostMapping("/admin/change-password")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 Authentication authentication,
                                 Model model) {
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "New password and confirmation do not match.");
            return "crm/change-password";
        }

        if (!passwordResetService.isValidNewPassword(newPassword)) {
            model.addAttribute("error", "Password must be at least 12 characters and include a letter, number and special character.");
            return "crm/change-password";
        }

        if (!passwordResetService.changePassword(authentication.getName(), currentPassword, newPassword)) {
            model.addAttribute("error", "Current password is incorrect, or the new password cannot be used.");
            return "crm/change-password";
        }

        auditService.record(authentication, "CHANGE_PASSWORD", "ADMIN_USER", null, "Changed admin password");
        return "redirect:/crm/login?passwordChanged";
    }

    @GetMapping("/forgot-password")
    public String forgotPassword() { return "crm/forgot-password"; }

    @PostMapping("/forgot-password")
    public String requestPasswordReset(@RequestParam String email, HttpSession session) {
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase();
        session.setAttribute(RESET_EMAIL, normalizedEmail);
        passwordResetService.requestOtp(normalizedEmail);
        return "redirect:/crm/verify-otp";
    }

    @GetMapping("/verify-otp")
    public String verifyOtpPage(HttpSession session, Model model) {
        if (session.getAttribute(RESET_EMAIL) == null) {
            return "redirect:/crm/forgot-password";
        }
        return "crm/verify-otp";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam String otp, HttpSession session, Model model) {
        String email = (String) session.getAttribute(RESET_EMAIL);
        if (email == null || !passwordResetService.verifyOtp(email, otp.trim())) {
            model.addAttribute("error", "The OTP is invalid or has expired.");
            return "crm/verify-otp";
        }

        AdminUser adminUser = adminUserRepository.findByEmailIgnoreCase(email).orElseThrow();
        session.setAttribute(RESET_ADMIN_ID, adminUser.getId());
        return "redirect:/crm/reset-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(HttpSession session) {
        return session.getAttribute(RESET_ADMIN_ID) == null
                ? "redirect:/crm/forgot-password"
                : "crm/reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String newPassword,
                                @RequestParam String confirmPassword,
                                HttpSession session,
                                Model model) {
        Long adminUserId = (Long) session.getAttribute(RESET_ADMIN_ID);
        if (adminUserId == null) {
            return "redirect:/crm/forgot-password";
        }

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "New password and confirmation do not match.");
            return "crm/reset-password";
        }

        if (!passwordResetService.isValidNewPassword(newPassword)) {
            model.addAttribute("error", "Password must be at least 12 characters and include a letter, number and special character.");
            return "crm/reset-password";
        }

        if (!passwordResetService.resetPassword(adminUserId, newPassword)) {
            model.addAttribute("error", "The password reset session is invalid or expired. Please request a new OTP.");
            return "crm/reset-password";
        }

        AdminUser adminUser = adminUserRepository.findById(adminUserId).orElse(null);
        auditService.record(adminUser, "RESET_PASSWORD", "ADMIN_USER", adminUserId, "Reset admin password using OTP");
        session.invalidate();
        return "redirect:/crm/login?passwordChanged";
    }

    @GetMapping("/admin/new-user")
    public String newUser(Model model) {
        model.addAttribute("user", new CrmUser());
        return "crm/new-user";
    }

    @PostMapping("/admin/new-user")
    public String saveUser(@ModelAttribute("user") CrmUser user, Model model, Authentication authentication) {
        user.setEmail(user.getEmail() == null ? null : user.getEmail().trim().toLowerCase());
        user.setPhone(user.getPhone() == null || user.getPhone().isBlank() ? null : user.getPhone().trim());

        if (user.getEmail() != null && userRepository.existsByEmailIgnoreCase(user.getEmail())) {
            model.addAttribute("error", "A user with this email address already exists.");
            return "crm/new-user";
        }

        if (user.getPhone() != null && userRepository.existsByPhone(user.getPhone())) {
            model.addAttribute("error", "A user with this phone number already exists.");
            return "crm/new-user";
        }

        userRepository.save(user);
        auditService.record(authentication, "CREATE", "CRM_USER", user.getId(), "Created CRM user");
        return "redirect:/crm/admin?saved";
    }

    @GetMapping("/admin/users")
    public String existingUsers(@RequestParam(value = "q", required = false, defaultValue = "") String query,
                                Model model) {
        model.addAttribute("query", query);
        model.addAttribute("users", query.isBlank()
                ? userRepository.findAll()
                : userRepository.search(query.trim()));
        return "crm/users";
    }

    @GetMapping("/admin/users/{id}")
    public String userDetails(@PathVariable Long id, Model model) {
        CrmUser user = userRepository.findById(id).orElseThrow();
        model.addAttribute("user", user);
        return "crm/user-details";
    }

    @GetMapping("/admin/users/{id}/edit")
    public String editUser(@PathVariable Long id, Model model) {
        CrmUser user = userRepository.findById(id).orElseThrow();
        model.addAttribute("user", user);
        return "crm/edit-user";
    }

    @PostMapping("/admin/users/{id}/edit")
    public String updateUser(@PathVariable Long id, @ModelAttribute("user") CrmUser updatedUser, Model model, Authentication authentication) {
        CrmUser user = userRepository.findById(id).orElseThrow();
        updatedUser.setEmail(updatedUser.getEmail() == null ? null : updatedUser.getEmail().trim().toLowerCase());
        updatedUser.setPhone(updatedUser.getPhone() == null || updatedUser.getPhone().isBlank() ? null : updatedUser.getPhone().trim());

        if (updatedUser.getEmail() != null && userRepository.existsByEmailIgnoreCaseAndIdNot(updatedUser.getEmail(), id)) {
            model.addAttribute("error", "A user with this email address already exists.");
            model.addAttribute("user", updatedUser);
            return "crm/edit-user";
        }

        if (updatedUser.getPhone() != null && userRepository.existsByPhoneAndIdNot(updatedUser.getPhone(), id)) {
            model.addAttribute("error", "A user with this phone number already exists.");
            model.addAttribute("user", updatedUser);
            return "crm/edit-user";
        }

        user.setFirstName(updatedUser.getFirstName());
        user.setLastName(updatedUser.getLastName());
        user.setEmail(updatedUser.getEmail());
        user.setPhone(updatedUser.getPhone());
        user.setDateOfBirth(updatedUser.getDateOfBirth());
        user.setAddress(updatedUser.getAddress());
        user.setSuburb(updatedUser.getSuburb());
        user.setState(updatedUser.getState());
        user.setPostcode(updatedUser.getPostcode());
        userRepository.save(user);
        auditService.record(authentication, "UPDATE", "CRM_USER", user.getId(), "Updated CRM user");
        return "redirect:/crm/admin/users/{id}";
    }

    @PostMapping("/admin/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, Authentication authentication) {
        if (userRepository.existsById(id)) {
            auditService.record(authentication, "DELETE", "CRM_USER", id, "Deleted CRM user");
            userRepository.deleteById(id);
        }
        return "redirect:/crm/admin/users?deleted";
    }
}
