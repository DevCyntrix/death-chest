package com.github.devcyntrix.deathchest.api.report;

import com.google.gson.internal.bind.util.ISO8601Utils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

public interface ReportManager {


    int DATE_FORMAT_CONFIG = DateFormat.DEFAULT;

    default void createReport() {
        addReport(Report.create());
    }

    void addReport(@NotNull Report report);

    @Nullable Report getLatestReport();

    @NotNull Set<@NotNull Report> getReports();

    @NotNull TreeSet<Date> getReportDates();

    default boolean deleteReport(@NotNull Report report) {
        return deleteReport(report.date());
    }

    boolean deleteReport(@NotNull Date date);

    void deleteReports();

    static @Nullable Date parseISO(@NotNull String value) {
        try {
            return ISO8601Utils.parse(value, new ParsePosition(0));
        } catch (ParseException e) {
            return null;
        }
    }

    static @NotNull String formatISO(@NotNull Date date) {
        return ISO8601Utils.format(date);
    }
}
