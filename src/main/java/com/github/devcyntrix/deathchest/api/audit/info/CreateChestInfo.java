package com.github.devcyntrix.deathchest.api.audit.info;

import com.github.devcyntrix.deathchest.api.DeathChest;
import com.github.devcyntrix.deathchest.api.audit.AuditInfo;
import com.google.gson.annotations.Expose;

public class CreateChestInfo extends AuditInfo {

    @Expose
    private DeathChest chest;

    public CreateChestInfo(DeathChest chest) {
        this.chest = chest;
    }

    public DeathChest getChest() {
        return chest;
    }
}
