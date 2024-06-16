package com.github.devcyntrix.deathchest.util;

import org.apache.commons.lang3.time.DurationFormatUtils;

import java.util.function.Function;

public class DurationFormatter implements Function<Long, String> {

    private final String format;

    public DurationFormatter(String format) {
        this.format = format;
    }

    @Override
    public String apply(Long duration) {
        if (duration <= 0) duration = 0L;
        return DurationFormatUtils.formatDuration(duration, format);
    }
}
