package com.github.devcyntrix.deathchest.api.audit;

import com.github.devcyntrix.deathchest.util.adapter.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.bind.util.ISO8601Utils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Date;

public record AuditItem(Date date, AuditAction action, AuditInfo info) {

    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(ItemStack.class, new ItemStackAdapter())
            .registerTypeAdapter(Location.class, new LocationAdapter())
            .registerTypeAdapter(Inventory.class, new InventoryAdapter())
            .registerTypeHierarchyAdapter(ItemMeta.class, new ItemMetaAdapter())
            .registerTypeHierarchyAdapter(LivingEntity.class, new LivingEntityAdapter())
            .registerTypeHierarchyAdapter(CommandSender.class, new CommandSenderAdapter())
            //.registerTypeHierarchyAdapter(Entity.class, new EntityAdapter())
            .excludeFieldsWithoutExposeAnnotation().create();

    @Override
    public String toString() {
        return "\"" + String.join("\";\"", ISO8601Utils.format(date), action.name(), info.toString()) + "\"";
    }
}
