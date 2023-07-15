package com.github.devcyntrix.deathchest.controller;

import com.github.devcyntrix.deathchest.DeathChestModel;
import com.github.devcyntrix.deathchest.api.DeathChest;
import com.github.devcyntrix.deathchest.api.DeathChestSnapshot;
import com.github.devcyntrix.deathchest.api.audit.AuditManager;
import com.github.devcyntrix.deathchest.api.storage.DeathChestStorage;
import com.github.devcyntrix.deathchest.util.ChestListener;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

public class DeathChestController implements Closeable {

    private final AuditManager auditManager;
    private final DeathChestStorage storage;

    private final Set<ChestListener> listeners = new HashSet<>();
    protected final Set<DeathChestModel> loadedChests = new CopyOnWriteArraySet<>();

    public DeathChestController(AuditManager auditManager, DeathChestStorage storage) {
        this.auditManager = auditManager;
        this.storage = storage;
    }

    public void subscribe(ChestListener listener) {
        this.listeners.add(listener);
    }

    public void loadChests() {
        // Loading chests...
        // Recreates the deathchests
        Set<DeathChestSnapshot> chests = this.storage.getChests();
        chests.forEach(deathChestSnapshot -> this.loadedChests.add(deathChestSnapshot.createChest(this)));
        getLogger().info(this.deathChests.size() + " death chests loaded.");

        if (this.auditManager != null) {
            this.auditManager.start();
        }

    }

    @Override
    public void close() throws IOException {
        saveChests();

        // Unload audit manager
        if (this.auditManager != null) {
            try {
                auditManager.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        unloadChests();
    }

    private void unloadChests() {
        this.loadedChests.forEach(model -> {
            this.listeners.forEach(listener -> listener.onUnload(model));
            model.cancelTasks();
        });
        this.loadedChests.clear();
    }

    public void saveChests() throws IOException {
        this.storage.save();
        this.storage.update(this.loadedChests.stream().map(DeathChest::createSnapshot).collect(Collectors.toSet()));
    }
}
