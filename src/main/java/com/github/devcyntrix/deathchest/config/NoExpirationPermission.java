package com.github.devcyntrix.deathchest.config;

import com.google.gson.annotations.SerializedName;

public record NoExpirationPermission(
        @SerializedName("enabled") boolean enabled,
        @SerializedName("permission") String permission) {
}
