package se.moln.ecommerceintegration.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.moln.ecommerceintegration.model.Entitlement;
import se.moln.ecommerceintegration.repository.EntitlementRepository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EntitlementService {
    private final EntitlementRepository repo;

    public Optional<Entitlement> find(UUID userId, String sku) {
        return repo.findByUserIdAndSku(userId, sku);
    }

    @Transactional
    public Entitlement issue(UUID userId, String sku, int remaining, OffsetDateTime expiresAt) {
        return find(userId, sku)
                .map(e -> {
                    e.setRemaining(e.getRemaining() + Math.max(remaining, 0));
                    return repo.save(e);
                })
                .orElseGet(() -> repo.save(Entitlement.of(userId, sku, remaining, expiresAt)));
    }

    @Transactional
    public boolean consume(UUID userId, String sku, int count) {
        var e = repo.findForUpdate(userId, sku).orElse(null);
        if (e == null) return false;
        if (e.getRemaining() < count) return false;
        e.setRemaining(e.getRemaining() - count);
        repo.save(e);
        return true;
    }
}
