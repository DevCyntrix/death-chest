package com.github.devcyntrix.deathchest.api.audit.info;

import com.github.devcyntrix.deathchest.DeathChestModel;
import com.github.devcyntrix.deathchest.api.audit.AuditInfo;
import com.google.gson.annotations.Expose;

import java.util.Map;

public final class DestroyChestInfo extends AuditInfo {

    @Expose
    private final DeathChestModel chest;
    @Expose
    private final DestroyReason reason;
    @Expose
    private final Map<String, Object> extra;

    public DestroyChestInfo(DeathChestModel chest, DestroyReason reason, Map<String, Object> extra) {
        this.chest = chest;
        this.reason = reason;
        this.extra = extra;
    }

    public DeathChestModel getChest() {
        return chest;
    }

    public DestroyReason getReason() {
        return reason;
    }

    public Map<String, Object> getExtra() {
        return extra;
    }
}
