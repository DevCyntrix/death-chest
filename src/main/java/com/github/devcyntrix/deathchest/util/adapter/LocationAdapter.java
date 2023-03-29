package com.github.devcyntrix.deathchest.util.adapter;

import com.google.gson.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.lang.reflect.Type;

public class LocationAdapter implements JsonSerializer<Location>, JsonDeserializer<Location> {

    @Override
    public Location deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (!json.isJsonObject())
            return null;
        JsonObject object = json.getAsJsonObject();

        World world = null;
        if (object.has("world")) {
            String worldName = object.get("world").getAsString();
            world = Bukkit.getWorld(worldName);
        }

        double x = object.get("x").getAsDouble();
        double y = object.get("y").getAsDouble();
        double z = object.get("z").getAsDouble();
        float yaw = 0F;
        if (object.has("yaw")) {
            yaw = object.get("yaw").getAsFloat();
        }
        float pitch = 0F;
        if (object.has("pitch")) {
            pitch = object.get("pitch").getAsFloat();
        }

        return new Location(world, x, y, z, yaw, pitch);
    }

    @Override
    public JsonElement serialize(Location src, Type typeOfSrc, JsonSerializationContext context) {
        if (src == null)
            return JsonNull.INSTANCE;

        JsonObject object = new JsonObject();
        if (src.getWorld() != null)
            object.addProperty("world", src.getWorld().getName());

        object.addProperty("x", src.getX());
        object.addProperty("y", src.getY());
        object.addProperty("z", src.getZ());

        if (src.getYaw() != 0F) {
            object.addProperty("yaw", src.getYaw());
        }

        if (src.getPitch() != 0F) {
            object.addProperty("pitch", src.getPitch());
        }

        return object;
    }
}
