package com.github.devcyntrix.deathchest.api.report;

import com.github.devcyntrix.deathchest.config.DeathChestConfig;
import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public record Report(
        @SerializedName("date")
        Date date,
        @SerializedName("plugins")
        Set<PluginInfo> plugins,
        @SerializedName("config")
        DeathChestConfig config,
        @SerializedName("extra")
        Map<String, Object> extra
) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Report report = (Report) o;
        return Objects.equals(date, report.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date);
    }
}
