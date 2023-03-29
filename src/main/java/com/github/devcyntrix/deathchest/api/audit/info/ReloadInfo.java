package com.github.devcyntrix.deathchest.api.audit.info;

import com.github.devcyntrix.deathchest.api.audit.AuditInfo;
import com.google.gson.annotations.Expose;
import org.bukkit.command.CommandSender;

import java.util.Objects;

public final class ReloadInfo extends AuditInfo {
    @Expose
    private final CommandSender sender;

    public ReloadInfo(CommandSender sender) {
        this.sender = sender;
    }

    public CommandSender sender() {
        return sender;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ReloadInfo) obj;
        return Objects.equals(this.sender, that.sender);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sender);
    }


}
