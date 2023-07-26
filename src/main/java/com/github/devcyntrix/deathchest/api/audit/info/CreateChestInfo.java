package com.github.devcyntrix.deathchest.api.audit.info;

import com.github.devcyntrix.deathchest.DeathChestModel;
import com.github.devcyntrix.deathchest.api.audit.AuditInfo;
import com.google.gson.annotations.Expose;

public class CreateChestInfo extends AuditInfo {

    @Expose
    private DeathChestModel chest;

    public CreateChestInfo(DeathChestModel chest) {
        this.chest = chest;
    }

    public DeathChestModel getChest() {
        return chest;
    }
}
