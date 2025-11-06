package com.gearup.userauth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gearup.userauth.model.UserAuditLog;
import com.gearup.userauth.repository.UserAuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class AuditService {

    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);

    private final UserAuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public AuditService(UserAuditLogRepository auditLogRepository, ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void logUserAction(Long userId, String action, String description, 
                             Map<String, Object> oldValues, Map<String, Object> newValues) {
        try {
            UserAuditLog auditLog = new UserAuditLog();
            auditLog.setUserId(userId);
            auditLog.setAction(action);
            auditLog.setDescription(description);
            
            if (oldValues != null) {
                auditLog.setOldValues(objectMapper.writeValueAsString(oldValues));
            }
            
            if (newValues != null) {
                auditLog.setNewValues(objectMapper.writeValueAsString(newValues));
            }
            
            auditLog.setCreatedAt(LocalDateTime.now());
            
            auditLogRepository.save(auditLog);
            logger.debug("Audit log created for user {}: {}", userId, action);
            
        } catch (JsonProcessingException e) {
            logger.error("Error creating audit log: {}", e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<UserAuditLog> getUserAuditHistory(Long userId) {
        return auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public List<UserAuditLog> getUserAuditHistoryByAction(Long userId, String action) {
        return auditLogRepository.findByUserIdAndActionOrderByCreatedAtDesc(userId, action);
    }
}
