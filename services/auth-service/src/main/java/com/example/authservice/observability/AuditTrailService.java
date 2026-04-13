package com.example.authservice.observability;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditTrailService {

    private static final Logger log = LoggerFactory.getLogger(AuditTrailService.class);

    private final JdbcTemplate jdbcTemplate;

    public AuditTrailService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String action, String actorUsername, String subjectType, String subjectId, String details) {
        try {
            jdbcTemplate.update(
                    """
                    INSERT INTO audit_log (action, actor_username, subject_type, subject_id, details, correlation_id)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """,
                    action,
                    actorUsername,
                    subjectType,
                    subjectId,
                    details,
                    MDC.get(CorrelationIdFilter.MDC_KEY));
        } catch (Exception e) {
            log.warn("Audit write failed for action={}", action, e);
        }
    }
}
