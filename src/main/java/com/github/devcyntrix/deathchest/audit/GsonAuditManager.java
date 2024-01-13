package com.github.devcyntrix.deathchest.audit;

import com.github.devcyntrix.deathchest.api.audit.AuditItem;
import com.github.devcyntrix.deathchest.api.audit.AuditManager;
import com.google.inject.Singleton;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Singleton
public class GsonAuditManager extends Thread implements AuditManager {

    private static final String format = "audit-%s.csv";
    private final File folder;

    private final BlockingQueue<AuditItem> items = new LinkedBlockingQueue<>();

    public GsonAuditManager(File folder) {
        this.folder = folder;
        if (!this.folder.isDirectory() && !this.folder.mkdirs())
            throw new RuntimeException("Cannot create audit folder \"%s\"".formatted(folder));
        start();
    }

    @Override
    public void audit(AuditItem item) {
        boolean offer = items.offer(item);
        if (!offer) {
            throw new IllegalStateException("Cannot offer audit item to the queue " + item);
        }
    }

    @Override
    public void run() {
        try {
            while (!isInterrupted()) {
                AuditItem item = items.take();

                File file = new File(folder, format.formatted(DATE_FORMAT.format(item.date())));
                try (FileWriter writer = new FileWriter(file, true)) {
                    writer.write(item + "\n");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (InterruptedException ignored) {
        }
    }

    @Override
    public void close() {
        interrupt();
    }
}
