package se.moln.ecommerceintegration.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import se.moln.ecommerceintegration.model.Entitlement;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EntitlementRepository extends JpaRepository<Entitlement, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Entitlement e where e.userId = ?1 and e.sku = ?2")
    Optional<Entitlement> findForUpdate(UUID userId, String sku);

    Optional<Entitlement> findByUserIdAndSku(UUID userId, String sku);
}
