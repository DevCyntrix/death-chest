package com.github.devcyntrix.deathchest.report;

import com.github.devcyntrix.deathchest.api.report.Report;
import com.github.devcyntrix.deathchest.api.report.ReportManager;
import com.github.devcyntrix.deathchest.util.adapter.DurationAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.bind.util.ISO8601Utils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GsonReportManager implements ReportManager {

    private static final Logger LOG = Logger.getLogger(GsonReportManager.class.getName());
    private static final String format = "report-%s.json";
    private static final String reportPattern = "^report-(.*).json$";

    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .setDateFormat(DATE_FORMAT_CONFIG, DATE_FORMAT_CONFIG)
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .create();
    private final File folder;

    public GsonReportManager(File folder) {
        this.folder = folder;
        if (!this.folder.isDirectory() && !this.folder.mkdirs())
            throw new RuntimeException("Failed to create report directory");

    }


    @Override
    public void addReport(@NotNull Report report) {
        File file = new File(folder, format.formatted(ISO8601Utils.format(report.date())));
        if (file.exists()) {
            boolean delete = file.delete();
            if (!delete) throw new RuntimeException("Failed to update report file");
        }

        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(report, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @Nullable Report getLatestReport() {
        TreeSet<Date> reportDates = getReportDates();
        if (reportDates.isEmpty())
            return null;

        Date last = reportDates.last();
        File file = new File(folder, format.formatted(ReportManager.formatISO(last)));
        return parseReport(file);
    }

    @Override
    public @NotNull Set<@NotNull Report> getReports() {
        Set<Report> reports = new HashSet<>();
        File[] files = folder.listFiles(file -> file.isFile() && file.getName().matches(reportPattern));
        if (files == null)
            return reports;

        for (File file : files) {
            Report report = parseReport(file);
            if (report == null)
                continue;
            reports.add(report);
        }
        return reports;
    }

    @Override
    public @NotNull TreeSet<Date> getReportDates() {
        TreeSet<Date> list = new TreeSet<>(Date::compareTo);

        File[] files = folder.listFiles(file -> file.isFile() && file.getName().matches(reportPattern));
        if (files == null)
            return list;

        Pattern pattern = Pattern.compile(reportPattern);
        for (File file : files) {
            Matcher matcher = pattern.matcher(file.getName());
            if (!matcher.find())
                continue;
            String group = matcher.group(1);
            Date date = ReportManager.parseISO(group);
            if (date == null) {
                LOG.log(Level.SEVERE, "Failed to parse date format of file name " + group);
                continue;
            }
            list.add(date);
        }

        return list;
    }

    @Override
    public boolean deleteReport(@NotNull Date date) {
        File file = new File(folder, format.formatted(ISO8601Utils.format(date)));
        return file.delete();
    }

    @Override
    public void deleteReports() {
        File[] files = folder.listFiles(file -> file.isFile() && file.getName().matches(reportPattern));
        if (files == null)
            return;

        for (File file : files) {
            if (!file.delete()) {
                LOG.warning("Failed to delete file: " + file);
            }
        }
    }

    private Report parseReport(File file) {
        try (FileReader reader = new FileReader(file)) {
            return gson.fromJson(reader, Report.class);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to read report", e);
        }
        return null;
    }
}
