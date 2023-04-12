package com.github.devcyntrix.deathchest.api.audit;

public class AuditInfo {

    @Override
    public String toString() {
        return AuditItem.GSON.toJson(this);
    }
}
