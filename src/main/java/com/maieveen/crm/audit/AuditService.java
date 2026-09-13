package com.maieveen.crm.audit;

import com.maieveen.crm.security.AdminUser;
import com.maieveen.crm.security.AdminUserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final AdminUserRepository adminUserRepository;

    public AuditService(AuditLogRepository auditLogRepository, AdminUserRepository adminUserRepository) {
        this.auditLogRepository = auditLogRepository;
        this.adminUserRepository = adminUserRepository;
    }

    public void record(Authentication authentication, String action, String entityType, Long entityId, String details) {
        if (authentication == null || authentication.getName() == null) {
            return;
        }

        adminUserRepository.findByUsername(authentication.getName()).ifPresent(adminUser ->
                record(adminUser, action, entityType, entityId, details)
        );
    }

    public void record(AdminUser adminUser, String action, String entityType, Long entityId, String details) {
        if (adminUser == null) {
            return;
        }
        auditLogRepository.save(new AuditLog(adminUser, action, entityType, entityId, details));
    }
}
