package africa.absa.eb.data.service;

import africa.absa.eb.data.entity.SentEmailAudit;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SentEmailAuditRepository extends JpaRepository<SentEmailAudit, UUID> {

}