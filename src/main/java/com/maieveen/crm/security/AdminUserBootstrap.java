package com.maieveen.crm.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminUserBootstrap implements CommandLineRunner {

    private final AdminUserRepository adminUserRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${crm.admin.bootstrap.enabled:true}")
    private boolean enabled;

    @Value("${crm.admin.bootstrap.username:admin}")
    private String username;

    @Value("${crm.admin.bootstrap.password:change-me}")
    private String password;

    public AdminUserBootstrap(AdminUserRepository adminUserRepository,
                              RoleRepository roleRepository,
                              PasswordEncoder passwordEncoder) {
        this.adminUserRepository = adminUserRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!enabled) {
            return;
        }

        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseGet(() -> roleRepository.save(new Role("ADMIN")));

        adminUserRepository.findByUsername(username).ifPresentOrElse(
                adminUser -> {
                    if (adminUser.getRoles().stream().noneMatch(role -> "ADMIN".equals(role.getName()))) {
                        adminUser.getRoles().add(adminRole);
                        adminUserRepository.save(adminUser);
                    }
                },
                () -> {
                    AdminUser adminUser = new AdminUser(username, passwordEncoder.encode(password));
                    adminUser.getRoles().add(adminRole);
                    adminUserRepository.save(adminUser);
                }
        );
    }
}
