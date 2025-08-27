package se.moln.ecommerceintegration.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.moln.ecommerceintegration.model.ActivityLog;

import java.util.List;
import java.util.UUID;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, UUID> {
    List<ActivityLog> findTop50ByUserEmailOrderByCreatedAtDesc(String userEmail);
}
