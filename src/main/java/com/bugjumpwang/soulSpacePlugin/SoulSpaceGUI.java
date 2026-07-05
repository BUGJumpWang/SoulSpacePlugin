package com.bugjumpwang.soulSpacePlugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class SoulSpaceGUI {

    private static final String TITLE_PREFIX = ChatColor.AQUA + "灵魂空间 第";
    private static final String TITLE_MID = "页/共";
    private static final String TITLE_SUFFIX = "页";
    public static final String CONFIRM_TITLE = ChatColor.AQUA + "是否升级？";
    public static final String TRAVERSE_TITLE = ChatColor.AQUA + "页面遍历";
    public static final String ICON_SELECT_TITLE = ChatColor.AQUA + "选择图标";
    private static final int SIZE = 54;

    public static void open(Player player) {
        SoulSpacePlugin plugin = SoulSpacePlugin.getInstance();
        PlayerData data = plugin.getDatabaseManager().loadPlayerData(player.getUniqueId());
        if (data == null) {
            data = new PlayerData(player.getUniqueId(), false, 1);
            plugin.getDatabaseManager().savePlayerData(data);
        }
        openPage(player, 1, data.getLevel());
    }

    public static void openPage(Player player, int page, int totalPages) {
        SoulSpacePlugin plugin = SoulSpacePlugin.getInstance();
        UUID uuid = player.getUniqueId();

        DatabaseManager.PageInfo info = plugin.getDatabaseManager().loadPageInfo(uuid, page);
        String json = (info != null) ? info.getContents() : null;
        ItemStack[] contents = Utils.deserializeItems(json);
        String pageName = (info != null) ? info.getName() : "";

        String title = TITLE_PREFIX + page + TITLE_MID + totalPages + TITLE_SUFFIX;
        if (!pageName.isEmpty()) {
            title += " " + ChatColor.translateAlternateColorCodes('&', pageName);
        }

        SoulSpaceHolder holder = new SoulSpaceHolder(uuid, page, totalPages);
        Inventory inv = Bukkit.createInventory(holder, SIZE, title);
        inv.setContents(contents);

        ItemStack close = createGlass(Material.RED_STAINED_GLASS_PANE, ChatColor.RED + "✘关闭空间", ChatColor.RED + "点击后关闭灵魂空间");
        inv.setItem(45, close);

        ItemStack gray = createGrayGlass();
        inv.setItem(46, gray);

        ItemStack prev;
        if (page > 1) {
            prev = createGlass(Material.LIME_STAINED_GLASS_PANE, ChatColor.GREEN + "上一页", ChatColor.AQUA + "←点我进入上一页");
        } else {
            prev = createGlass(Material.RED_STAINED_GLASS_PANE, ChatColor.RED + "已经是第一页！", ChatColor.RED + "已经是第一页！");
        }
        inv.setItem(47, prev);

        PlayerData data = plugin.getDatabaseManager().loadPlayerData(uuid);
        if (data == null) data = new PlayerData(uuid, false, 1);
        ItemStack feather = createFeather(player, data);
        inv.setItem(48, feather);

        ItemStack next;
        if (page < totalPages) {
            next = createGlass(Material.LIME_STAINED_GLASS_PANE, ChatColor.GREEN + "下一页", ChatColor.AQUA + "点我进入下一页→");
        } else {
            next = createGlass(Material.RED_STAINED_GLASS_PANE, ChatColor.RED + "已经是最后一页！", ChatColor.RED + "已经是最后一页！");
        }
        inv.setItem(49, next);

        inv.setItem(50, gray);

        ItemStack nameTag = new ItemStack(Material.NAME_TAG);
        ItemMeta nameMeta = nameTag.getItemMeta();
        nameMeta.setDisplayName(ChatColor.AQUA + "更改当页名字");
        nameMeta.setLore(Arrays.asList(ChatColor.GRAY + "点击后输入新名字", ChatColor.GRAY + "支持颜色代码 &"));
        nameTag.setItemMeta(nameMeta);
        inv.setItem(51, nameTag);

        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta compMeta = compass.getItemMeta();
        compMeta.setDisplayName(ChatColor.AQUA + "遍历");
        compMeta.setLore(Arrays.asList(ChatColor.GRAY + "查看所有页面"));
        compass.setItemMeta(compMeta);
        inv.setItem(52, compass);

        inv.setItem(53, gray);

        player.openInventory(inv);
    }

    private static ItemStack createGrayGlass() {
        ItemStack gray = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = gray.getItemMeta();
        meta.setDisplayName(" ");
        gray.setItemMeta(meta);
        return gray;
    }

    private static ItemStack createGlass(Material material, String name, String lore) {
        ItemStack glass = new ItemStack(material);
        ItemMeta meta = glass.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        glass.setItemMeta(meta);
        return glass;
    }

    private static ItemStack createFeather(Player player, PlayerData data) {
        SoulSpacePlugin plugin = SoulSpacePlugin.getInstance();
        ItemStack feather = new ItemStack(Material.FEATHER);
        ItemMeta meta = feather.getItemMeta();
        meta.setDisplayName(plugin.getConfigManager().getDisplayName());
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.AQUA + "玩家ID：" + player.getName());
        lore.add(ChatColor.GREEN + "空间等级：" + ChatColor.RED + data.getLevel() + "级");
        lore.add(ChatColor.GOLD + "升级下一级花费：" + plugin.getConfigManager().getUpgradeCost());
        lore.add(ChatColor.AQUA + "点我升级空间→");
        meta.setLore(lore);
        feather.setItemMeta(meta);
        return feather;
    }

    public static void savePage(Inventory inv) {
        if (!(inv.getHolder() instanceof SoulSpaceHolder)) return;
        SoulSpaceHolder holder = (SoulSpaceHolder) inv.getHolder();
        UUID uuid = holder.getPlayerUUID();
        int page = holder.getCurrentPage();

        ItemStack[] contents = inv.getContents();
        ItemStack[] copy = new ItemStack[contents.length];
        System.arraycopy(contents, 0, copy, 0, contents.length);
        String json = Utils.serializeItems(copy);
        SoulSpacePlugin.getInstance().getDatabaseManager().savePage(uuid, page, json);
    }

    public static void close(Player player) {
        player.closeInventory();
    }

    public static void openUpgradeConfirm(Player player, int currentPage, int totalPages) {
        Inventory inv = Bukkit.createInventory(null, 27, CONFIRM_TITLE);
        ItemStack gray = createGrayGlass();
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, gray);
            inv.setItem(18 + i, gray);
        }
        ItemStack confirm = createGlass(Material.LIME_STAINED_GLASS_PANE, ChatColor.GREEN + "确认升级", ChatColor.GREEN + "点击确认升级");
        inv.setItem(11, confirm);
        ItemStack cancel = createGlass(Material.RED_STAINED_GLASS_PANE, ChatColor.RED + "暂不升级", ChatColor.RED + "点击暂不升级");
        inv.setItem(15, cancel);
        player.openInventory(inv);
    }

    public static void openTraverseGUI(Player player, int listPage, int selectedPage) {
        SoulSpacePlugin plugin = SoulSpacePlugin.getInstance();
        UUID uuid = player.getUniqueId();
        PlayerData data = plugin.getDatabaseManager().loadPlayerData(uuid);
        if (data == null) return;
        int totalLevel = data.getLevel();

        int entriesPerPage = 21;
        int maxListPage = (int) Math.ceil((double) totalLevel / entriesPerPage);
        if (maxListPage < 1) maxListPage = 1;
        if (listPage < 1) listPage = 1;
        if (listPage > maxListPage) listPage = maxListPage;

        TraverseHolder holder = new TraverseHolder(uuid, listPage, selectedPage, totalLevel);
        Inventory inv = Bukkit.createInventory(holder, SIZE, TRAVERSE_TITLE);

        int[] graySlots = {0,1,2,3,4,5,6,7,8, 9, 17, 18, 26, 27, 35, 36, 44, 45};
        for (int slot : graySlots) {
            inv.setItem(slot, createGrayGlass());
        }

        ItemStack back = createGlass(Material.RED_STAINED_GLASS_PANE, ChatColor.RED + "←返回上一页", ChatColor.RED + "返回主空间");
        inv.setItem(45, back);

        inv.setItem(46, createGrayGlass());

        ItemStack prevList;
        if (listPage > 1) {
            prevList = createGlass(Material.LIME_STAINED_GLASS_PANE, ChatColor.GREEN + "上一页", ChatColor.AQUA + "←上一页列表");
        } else {
            prevList = createGlass(Material.RED_STAINED_GLASS_PANE, ChatColor.RED + "已经是第一页！", ChatColor.RED + "已经是第一页！");
        }
        inv.setItem(47, prevList);

        ItemStack nextList;
        if (listPage < maxListPage) {
            nextList = createGlass(Material.LIME_STAINED_GLASS_PANE, ChatColor.GREEN + "下一页", ChatColor.AQUA + "下一页列表→");
        } else {
            nextList = createGlass(Material.RED_STAINED_GLASS_PANE, ChatColor.RED + "已经是最后一页！", ChatColor.RED + "已经是最后一页！");
        }
        inv.setItem(48, nextList);

        inv.setItem(49, createGrayGlass());
        inv.setItem(50, createGrayGlass());
        inv.setItem(53, createGrayGlass());

        inv.setItem(51, createGrayGlass());
        inv.setItem(52, createGrayGlass());

        int[] entrySlots = {10,11,12,13,14,15,16, 19,20,21,22,23,24,25, 28,29,30,31,32,33,34};
        int startIndex = (listPage - 1) * entriesPerPage;
        int endIndex = Math.min(startIndex + entriesPerPage, totalLevel);
        for (int i = startIndex; i < endIndex; i++) {
            int pageNum = i + 1;
            DatabaseManager.PageInfo info = plugin.getDatabaseManager().loadPageInfo(uuid, pageNum);
            String pageName = (info != null && !info.getName().isEmpty()) ? info.getName() : "未命名";
            String iconMat = (info != null && info.getIcon() != null) ? info.getIcon() : "STONE";
            Material mat = Material.getMaterial(iconMat);
            if (mat == null) mat = Material.STONE;

            ItemStack display = new ItemStack(mat);
            ItemMeta meta = display.getItemMeta();
            String displayName = ChatColor.GREEN + "第" + pageNum + "页 " + ChatColor.translateAlternateColorCodes('&', pageName);
            meta.setDisplayName(displayName);
            if (pageNum == selectedPage) {
                meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            display.setItemMeta(meta);
            int slotIndex = i - startIndex;
            inv.setItem(entrySlots[slotIndex], display);
        }

        if (selectedPage > 0 && selectedPage <= totalLevel) {
            ItemStack door = new ItemStack(Material.OAK_DOOR);
            ItemMeta doorMeta = door.getItemMeta();
            doorMeta.setDisplayName(ChatColor.GREEN + "进入该储存页");
            doorMeta.setLore(Arrays.asList(ChatColor.GRAY + "点击跳转到此页"));
            door.setItemMeta(doorMeta);
            inv.setItem(51, door);

            ItemStack redstone = new ItemStack(Material.REDSTONE);
            ItemMeta redMeta = redstone.getItemMeta();
            redMeta.setDisplayName(ChatColor.AQUA + "更改图标");
            redMeta.setLore(Arrays.asList(ChatColor.GREEN + "为自己的页面设置图标"));
            redstone.setItemMeta(redMeta);
            inv.setItem(52, redstone);
        }

        player.openInventory(inv);
    }

    public static void openIconSelectGUI(Player player, int pageToModify, int listPage, int selectedPage) {
        IconSelectHolder holder = new IconSelectHolder(player.getUniqueId(), pageToModify, listPage, selectedPage);
        Inventory inv = Bukkit.createInventory(holder, 27, ICON_SELECT_TITLE);

        Material[] row1 = {Material.DIAMOND_PICKAXE, Material.BUCKET, Material.DIAMOND_CHESTPLATE,
                Material.REDSTONE, Material.IRON_INGOT, Material.REDSTONE_TORCH,
                Material.ANVIL, Material.DIAMOND_BLOCK, Material.RAIL};
        for (int i = 0; i < 9; i++) {
            ItemStack item = new ItemStack(row1[i]);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.WHITE + item.getType().toString());
            item.setItemMeta(meta);
            inv.setItem(i, item);
        }

        Material[] row2 = {Material.ENCHANTED_BOOK, Material.BREAD, Material.MUSIC_DISC_OTHERSIDE,
                Material.GRASS_BLOCK, Material.BLUE_WOOL, Material.GLASS, Material.SHULKER_BOX};
        for (int i = 0; i < 7; i++) {
            ItemStack item = new ItemStack(row2[i]);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.WHITE + item.getType().toString());
            item.setItemMeta(meta);
            inv.setItem(9 + i, item);
        }

        ItemStack back = createGlass(Material.RED_STAINED_GLASS_PANE, ChatColor.RED + "←返回到上一页", ChatColor.RED + "返回遍历列表");
        inv.setItem(18, back);
        for (int i = 19; i < 27; i++) {
            inv.setItem(i, createGrayGlass());
        }

        player.openInventory(inv);
    }

    public static class SoulSpaceHolder implements InventoryHolder {
        private final UUID uuid;
        private final int currentPage;
        private final int totalPages;

        public SoulSpaceHolder(UUID uuid, int currentPage, int totalPages) {
            this.uuid = uuid;
            this.currentPage = currentPage;
            this.totalPages = totalPages;
        }

        public UUID getPlayerUUID() { return uuid; }
        public int getCurrentPage() { return currentPage; }
        public int getTotalPages() { return totalPages; }

        @Override
        public Inventory getInventory() { return null; }
    }

    public static class TraverseHolder implements InventoryHolder {
        private final UUID uuid;
        private final int listPage;
        private final int selectedPage;
        private final int totalLevel;

        public TraverseHolder(UUID uuid, int listPage, int selectedPage, int totalLevel) {
            this.uuid = uuid;
            this.listPage = listPage;
            this.selectedPage = selectedPage;
            this.totalLevel = totalLevel;
        }

        public UUID getUuid() { return uuid; }
        public int getListPage() { return listPage; }
        public int getSelectedPage() { return selectedPage; }
        public int getTotalLevel() { return totalLevel; }

        @Override
        public Inventory getInventory() { return null; }
    }

    public static class IconSelectHolder implements InventoryHolder {
        private final UUID uuid;
        private final int pageToModify;
        private final int listPage;
        private final int selectedPage;

        public IconSelectHolder(UUID uuid, int pageToModify, int listPage, int selectedPage) {
            this.uuid = uuid;
            this.pageToModify = pageToModify;
            this.listPage = listPage;
            this.selectedPage = selectedPage;
        }

        public UUID getUuid() { return uuid; }
        public int getPageToModify() { return pageToModify; }
        public int getListPage() { return listPage; }
        public int getSelectedPage() { return selectedPage; }

        @Override
        public Inventory getInventory() { return null; }
    }
}