package com.github.devcyntrix.deathchest.util.update;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;

public interface UpdateChecker {

    @Nullable String getLatestRelease();

    @Nullable InputStream download(@NotNull String version);
}
