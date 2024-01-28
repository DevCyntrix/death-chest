package com.github.devcyntrix.deathchest.util.adapter;

import com.google.gson.*;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;

public class ItemStackAdapter implements JsonDeserializer<ItemStack>, JsonSerializer<ItemStack> {

    @Override
    public ItemStack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Map<String, Object> deserialize = context.deserialize(json, Map.class);
        deserialize.computeIfPresent("meta", (s, o) -> {
            Map<String, Object> meta = (Map<String, Object>) o;
            meta.replaceAll((s1, o1) -> {
                if (o1 instanceof Number)
                    return ((Number) o1).intValue();
                return o1;
            });
            //meta.computeIfPresent("Damage", (property, value) -> ((Number)value).intValue()); // Converts the damage to an integer
            meta.computeIfPresent("enchants", (s1, enchantsMap) -> {
                Map<String, Object> enchants = (Map<String, Object>) enchantsMap;
                enchants.replaceAll((s2, o1) -> ((Number) o1).intValue()); // Converts the enchantment levels to integer
                return enchantsMap;
            });
            Material material = Material.getMaterial(deserialize.get("type").toString());
            ItemStack stack = new ItemStack(material);

            Class<?> aClass = Arrays.stream(stack.getItemMeta().getClass().getClasses()).filter(memberClass -> memberClass.getSimpleName().equals("SerializableMeta")).findFirst().orElse(null);
            try {
                Method deserializeMethod = aClass.getMethod("deserialize", Map.class);
                return deserializeMethod.invoke(null, meta);

            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });

        return ItemStack.deserialize(deserialize);
    }

    @Override
    public JsonElement serialize(ItemStack src, Type typeOfSrc, JsonSerializationContext context) {
        Map<String, Object> serialize = src.serialize();
        return context.serialize(serialize, Map.class);
    }
}
