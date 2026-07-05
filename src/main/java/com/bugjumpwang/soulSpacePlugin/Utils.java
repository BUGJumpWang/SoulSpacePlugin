package com.bugjumpwang.soulSpacePlugin;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Utils {

    private static final Gson gson = new Gson();
    private static final Type listType = new TypeToken<List<Map<String, Object>>>() {}.getType();

    public static String serializeItems(ItemStack[] items) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (ItemStack item : items) {
            if (item == null) {
                list.add(null);
            } else {
                list.add(item.serialize());
            }
        }
        return gson.toJson(list);
    }

    public static ItemStack[] deserializeItems(String json) {
        if (json == null || json.isEmpty()) {
            return new ItemStack[54];
        }
        List<Map<String, Object>> list = gson.fromJson(json, listType);
        ItemStack[] items = new ItemStack[54];
        for (int i = 0; i < list.size() && i < 54; i++) {
            Map<String, Object> map = list.get(i);
            if (map == null) {
                items[i] = null;
            } else {
                items[i] = ItemStack.deserialize(map);
            }
        }
        return items;
    }
}
