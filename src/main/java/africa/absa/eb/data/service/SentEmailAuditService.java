package africa.absa.eb.data.service;

import africa.absa.eb.data.entity.SentEmailAudit;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class SentEmailAuditService {

    private final SentEmailAuditRepository repository;

    @Autowired
    public SentEmailAuditService(SentEmailAuditRepository repository) {
        this.repository = repository;
    }

    public Optional<SentEmailAudit> get(UUID id) {
        return repository.findById(id);
    }

    public SentEmailAudit update(SentEmailAudit entity) {
        return repository.save(entity);
    }

    public void delete(UUID id) {
        repository.deleteById(id);
    }

    public Page<SentEmailAudit> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public int count() {
        return (int) repository.count();
    }

}
