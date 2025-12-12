package com.github.devcyntrix.deathchest.feature.blacklist;

import com.google.common.base.Preconditions;
import org.bukkit.inventory.ItemStack;

import java.io.Closeable;
import java.io.IOException;
import java.util.Set;

public class ItemBlacklistService implements Closeable {

    private final ItemBlacklistStore blacklistStore;

    public ItemBlacklistService(ItemBlacklistStore blacklistStore) {
        Preconditions.checkNotNull(blacklistStore);
        this.blacklistStore = blacklistStore;
    }

    public void addItem(ItemStack itemStack) {
        this.blacklistStore.getList().add(itemStack);
    }

    public Set<ItemStack> getItems() {
        return this.blacklistStore.getList();
    }

    @Override
    public void close() throws IOException {
        this.blacklistStore.close();
    }
}
