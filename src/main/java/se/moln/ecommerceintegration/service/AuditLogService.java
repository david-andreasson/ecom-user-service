package se.moln.ecommerceintegration.service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.moln.ecommerceintegration.model.ActivityLog;
import se.moln.ecommerceintegration.repository.ActivityLogRepository;

@Service
public class AuditLogService {
    private ActivityLogRepository activityLogRepository;
    private final boolean enabled;

    public AuditLogService(ActivityLogRepository activityLogRepository, @Value("${audit.persist.enabled:false}") boolean enabled) {
        this.activityLogRepository = activityLogRepository;
        this.enabled = enabled;
    }

    @Transactional
    public void save(String action, String userEmail, String method, String path, int status, String ip, String userAgent, long durationMs){
        if(!enabled) return;
        activityLogRepository.save(ActivityLog.of(action, userEmail, method, path, status, ip, userAgent, durationMs));


        }
    }

