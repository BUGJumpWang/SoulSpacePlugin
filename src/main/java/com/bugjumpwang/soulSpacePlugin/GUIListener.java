package com.bugjumpwang.soulSpacePlugin;

import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class GUIListener implements Listener {

    private final Map<UUID, RenameContext> renameContexts = new HashMap<>();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.getAction().name().contains("RIGHT")) return;
        ItemStack item = event.getItem();
        if (item == null || !SoulSpaceGUI.isSoulFeather(item)) return;

        event.setCancelled(true);
        Player player = event.getPlayer();
        SoulSpaceGUI.open(player);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        Inventory inv = event.getInventory();
        if (inv == null) return;

        if (inv.getHolder() instanceof SoulSpaceGUI.SoulSpaceHolder) {
            int slot = event.getRawSlot();
            if (slot >= 45 && slot < 54) {
                event.setCancelled(true);
                SoulSpaceGUI.SoulSpaceHolder holder = (SoulSpaceGUI.SoulSpaceHolder) inv.getHolder();
                switch (slot) {
                    case 45:
                        player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_DISPENSE, 1.0f, 1.0f);
                        player.closeInventory();
                        break;
                    case 47:
                        player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_DISPENSE, 1.0f, 1.0f);
                        if (holder.getCurrentPage() > 1) {
                            SoulSpaceGUI.savePage(inv);
                            SoulSpaceGUI.openPage(player, holder.getCurrentPage() - 1, holder.getTotalPages());
                        }
                        break;
                    case 48:
                        player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_DISPENSE, 1.0f, 1.0f);
                        handleUpgradeClick(player, holder);
                        break;
                    case 49:
                        player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_DISPENSE, 1.0f, 1.0f);
                        if (holder.getCurrentPage() < holder.getTotalPages()) {
                            SoulSpaceGUI.savePage(inv);
                            SoulSpaceGUI.openPage(player, holder.getCurrentPage() + 1, holder.getTotalPages());
                        }
                        break;
                    case 51:
                        player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_DISPENSE, 1.0f, 1.0f);
                        handleRenameClick(player, holder);
                        break;
                    case 52:
                        player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_DISPENSE, 1.0f, 1.0f);
                        handleTraverseClick(player);
                        break;
                    case 53:
                        player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_DISPENSE, 1.0f, 1.0f);
                        SoulSpaceGUI.sortPage(inv);
                        player.sendMessage(SoulSpacePlugin.getInstance().getLanguageManager().getMessage(player, "sort-done"));
                        break;
                    default:
                        break;
                }
            }
            return;
        }

        if (event.getView().getTitle().equals(SoulSpacePlugin.getInstance().getLanguageManager().getMessage(player, "gui-confirm-title"))) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            if (slot == 11) {
                player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_DISPENSE, 1.0f, 1.0f);
                SoulSpacePlugin plugin = SoulSpacePlugin.getInstance();
                PlayerData data = plugin.getDatabaseManager().loadPlayerData(player.getUniqueId());
                if (data == null) return;
                int currentLevel = data.getLevel();
                int maxLevel = plugin.getConfigManager().getMaxLevel();
                if (currentLevel >= maxLevel) {
                    player.sendMessage(plugin.getLanguageManager().getMessage(player, "max-level-reached"));
                    player.closeInventory();
                    return;
                }
                int cost = plugin.getConfigManager().getUpgradeCost();
                if (plugin.getEconomy().getBalance(player) < cost) {
                    player.sendMessage(plugin.getLanguageManager().getMessage(player, "insufficient-upgrade-funds", String.valueOf(cost)));
                    player.closeInventory();
                    return;
                }
                EconomyResponse resp = plugin.getEconomy().withdrawPlayer(player, cost);
                if (!resp.transactionSuccess()) {
                    player.sendMessage(plugin.getLanguageManager().getMessage(player, "deduct-failed"));
                    return;
                }
                data.setLevel(currentLevel + 1);
                plugin.getDatabaseManager().savePlayerData(data);
                updateFeatherLore(player, data.getLevel());
                player.sendMessage(plugin.getLanguageManager().getMessage(player, "upgrade-success", String.valueOf(data.getLevel())));
                player.closeInventory();
                SoulSpaceGUI.open(player);
            } else if (slot == 15) {
                player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_DISPENSE, 1.0f, 1.0f);
                player.closeInventory();
                SoulSpaceGUI.open(player);
            }
            return;
        }

        if (inv.getHolder() instanceof SoulSpaceGUI.TraverseHolder) {
            event.setCancelled(true);
            SoulSpaceGUI.TraverseHolder holder = (SoulSpaceGUI.TraverseHolder) inv.getHolder();
            int slot = event.getRawSlot();

            if (slot == 45) {
                player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_DISPENSE, 1.0f, 1.0f);
                player.closeInventory();
                SoulSpaceGUI.open(player);
                return;
            } else if (slot == 47) {
                player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_DISPENSE, 1.0f, 1.0f);
                if (holder.getListPage() > 1) {
                    SoulSpaceGUI.openTraverseGUI(player, holder.getListPage() - 1, holder.getSelectedPage());
                }
                return;
            } else if (slot == 48) {
                player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_DISPENSE, 1.0f, 1.0f);
                int maxListPage = (int) Math.ceil((double) holder.getTotalLevel() / 21.0);
                if (maxListPage < 1) maxListPage = 1;
                if (holder.getListPage() < maxListPage) {
                    SoulSpaceGUI.openTraverseGUI(player, holder.getListPage() + 1, holder.getSelectedPage());
                }
                return;
            } else if (slot == 51) {
                player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_DISPENSE, 1.0f, 1.0f);
                int selected = holder.getSelectedPage();
                if (selected > 0 && selected <= holder.getTotalLevel()) {
                    player.closeInventory();
                    SoulSpaceGUI.openPage(player, selected, holder.getTotalLevel());
                }
                return;
            } else if (slot == 52) {
                player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_DISPENSE, 1.0f, 1.0f);
                int selected = holder.getSelectedPage();
                if (selected > 0 && selected <= holder.getTotalLevel()) {
                    SoulSpaceGUI.openIconSelectGUI(player, selected, holder.getListPage(), holder.getSelectedPage());
                }
                return;
            }

            int[] entrySlots = {10,11,12,13,14,15,16, 19,20,21,22,23,24,25, 28,29,30,31,32,33,34};
            for (int i = 0; i < entrySlots.length; i++) {
                if (entrySlots[i] == slot) {
                    player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_DISPENSE, 1.0f, 1.0f);
                    int startIndex = (holder.getListPage() - 1) * 21;
                    int pageNum = startIndex + i + 1;
                    if (pageNum <= holder.getTotalLevel()) {
                        SoulSpaceGUI.openTraverseGUI(player, holder.getListPage(), pageNum);
                    }
                    break;
                }
            }
            return;
        }

        if (inv.getHolder() instanceof SoulSpaceGUI.IconSelectHolder) {
            event.setCancelled(true);
            SoulSpaceGUI.IconSelectHolder holder = (SoulSpaceGUI.IconSelectHolder) inv.getHolder();
            int slot = event.getRawSlot();

            if (slot == 18) {
                player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_DISPENSE, 1.0f, 1.0f);
                player.closeInventory();
                SoulSpaceGUI.openTraverseGUI(player, holder.getListPage(), holder.getSelectedPage());
                return;
            }

            if ((slot >= 0 && slot < 9) || (slot >= 9 && slot < 16)) {
                ItemStack clicked = inv.getItem(slot);
                if (clicked != null && clicked.getType() != Material.AIR) {
                    player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_DISPENSE, 1.0f, 1.0f);
                    String iconName = clicked.getType().name();
                    SoulSpacePlugin.getInstance().getDatabaseManager().updatePageIcon(
                            holder.getUuid(), holder.getPageToModify(), iconName);
                    player.closeInventory();
                    SoulSpaceGUI.openTraverseGUI(player, holder.getListPage(), holder.getSelectedPage());
                }
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        Inventory inv = event.getInventory();
        if (inv == null) return;
        if (inv.getHolder() instanceof SoulSpaceGUI.SoulSpaceHolder) {
            for (int slot : event.getRawSlots()) {
                if (slot >= 45 && slot < 54) {
                    event.setCancelled(true);
                    return;
                }
            }
        } else if (inv.getHolder() instanceof SoulSpaceGUI.TraverseHolder ||
                inv.getHolder() instanceof SoulSpaceGUI.IconSelectHolder ||
                (event.getWhoClicked() instanceof Player &&
                        event.getView().getTitle().equals(SoulSpacePlugin.getInstance().getLanguageManager().getMessage(
                                (Player) event.getWhoClicked(), "gui-confirm-title")))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Inventory inv = event.getInventory();
        if (inv.getHolder() instanceof SoulSpaceGUI.SoulSpaceHolder) {
            SoulSpaceGUI.savePage(inv);
        }
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (!renameContexts.containsKey(uuid)) return;

        event.setCancelled(true);
        RenameContext context = renameContexts.remove(uuid);
        String newName = event.getMessage();
        if (newName.length() > 30) newName = newName.substring(0, 30);

        SoulSpacePlugin.getInstance().getDatabaseManager().updatePageName(uuid, context.page, newName);
        Bukkit.getScheduler().runTask(SoulSpacePlugin.getInstance(), () -> {
            SoulSpaceGUI.openPage(player, context.page, context.totalPages);
        });
        player.sendMessage(SoulSpacePlugin.getInstance().getLanguageManager().getMessage(player, "page-renamed"));
    }

    private void handleUpgradeClick(Player player, SoulSpaceGUI.SoulSpaceHolder holder) {
        PlayerData data = SoulSpacePlugin.getInstance().getDatabaseManager().loadPlayerData(player.getUniqueId());
        if (data == null) return;
        int currentLevel = data.getLevel();
        int maxLevel = SoulSpacePlugin.getInstance().getConfigManager().getMaxLevel();
        if (currentLevel >= maxLevel) {
            player.sendMessage(SoulSpacePlugin.getInstance().getLanguageManager().getMessage(player, "max-level-reached"));
            return;
        }
        int cost = SoulSpacePlugin.getInstance().getConfigManager().getUpgradeCost();
        if (SoulSpacePlugin.getInstance().getEconomy().getBalance(player) < cost) {
            player.sendMessage(SoulSpacePlugin.getInstance().getLanguageManager().getMessage(player, "insufficient-upgrade-funds", String.valueOf(cost)));
            return;
        }
        SoulSpaceGUI.openUpgradeConfirm(player, holder.getCurrentPage(), holder.getTotalPages());
    }

    private void handleRenameClick(Player player, SoulSpaceGUI.SoulSpaceHolder holder) {
        Inventory inv = player.getOpenInventory().getTopInventory();
        if (inv != null) SoulSpaceGUI.savePage(inv);
        player.closeInventory();
        renameContexts.put(player.getUniqueId(), new RenameContext(holder.getCurrentPage(), holder.getTotalPages()));
        player.sendMessage(SoulSpacePlugin.getInstance().getLanguageManager().getMessage(player, "rename-prompt"));
    }

    private void handleTraverseClick(Player player) {
        Inventory inv = player.getOpenInventory().getTopInventory();
        if (inv != null && inv.getHolder() instanceof SoulSpaceGUI.SoulSpaceHolder) {
            SoulSpaceGUI.savePage(inv);
        }
        player.closeInventory();
        SoulSpaceGUI.openTraverseGUI(player, 1, -1);
    }

    private void updateFeatherLore(Player player, int newLevel) {
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item != null && SoulSpaceGUI.isSoulFeather(item)) {
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(SoulSpacePlugin.getInstance().getLanguageManager().getSoulSpaceDisplayName(player));
                List<String> lore = new ArrayList<>();
                lore.add(SoulSpacePlugin.getInstance().getLanguageManager().getMessage(player, "feather-player-id", player.getName()));
                lore.add(SoulSpacePlugin.getInstance().getLanguageManager().getMessage(player, "feather-space-level", String.valueOf(newLevel)));
                lore.add(SoulSpacePlugin.getInstance().getLanguageManager().getMessage(player, "feather-upgrade-cost", String.valueOf(SoulSpacePlugin.getInstance().getConfigManager().getUpgradeCost())));
                lore.add(SoulSpacePlugin.getInstance().getLanguageManager().getMessage(player, "feather-click-upgrade"));
                meta.setLore(lore);
                item.setItemMeta(meta);
                SoulSpaceGUI.markSoulFeather(item);
                contents[i] = item;
            }
        }
        player.getInventory().setContents(contents);
    }

    private static class RenameContext {
        final int page;
        final int totalPages;
        RenameContext(int page, int totalPages) {
            this.page = page;
            this.totalPages = totalPages;
        }
    }
}