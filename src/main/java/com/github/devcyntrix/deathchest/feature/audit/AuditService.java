package com.github.devcyntrix.deathchest.feature.audit;

import com.github.devcyntrix.deathchest.api.audit.AuditItem;
import com.github.devcyntrix.deathchest.api.audit.AuditStore;

import java.io.Closeable;
import java.io.IOException;

public class AuditService implements Closeable {

    private final AuditStore auditStore;

    public AuditService(AuditStore auditStore) {
        this.auditStore = auditStore;
    }

    public void log(AuditItem item) {
        auditStore.audit(item);
    }

    @Override
    public void close() throws IOException {
        this.auditStore.close();
    }
}
