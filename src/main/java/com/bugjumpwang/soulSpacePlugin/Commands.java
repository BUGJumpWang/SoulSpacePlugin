package com.bugjumpwang.soulSpacePlugin;

import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class Commands implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(msg(sender, "command-usage"));
            return true;
        }

        String subCmd = args[0].toLowerCase();

        switch (subCmd) {
            case "getbags":
                return handleGetBags(sender);
            case "open":
                return handleOpen(sender);
            case "reload":
                return handleReload(sender);
            case "lang":
                return handleLang(sender, args);
            default:
                sender.sendMessage(msg(sender, "unknown-subcommand", "/ssp " + subCmd));
                sender.sendMessage(msg(sender, "available-subcommands"));
                return true;
        }
    }

    private boolean handleLang(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(msg(sender, "player-only"));
            return true;
        }
        Player player = (Player) sender;
        SoulSpacePlugin plugin = SoulSpacePlugin.getInstance();
        if (args.length < 2) {
            player.sendMessage(msg(player, "lang-usage"));
            return true;
        }
        String code = args[1].toLowerCase();
        if (!plugin.getLanguageManager().isSupported(code)) {
            player.sendMessage(msg(player, "lang-unknown", code));
            return true;
        }
        PlayerData data = plugin.getDatabaseManager().loadPlayerData(player.getUniqueId());
        if (data == null) {
            data = new PlayerData(player.getUniqueId(), false, 1, code);
        } else {
            data.setLanguage(code);
        }
        plugin.getDatabaseManager().savePlayerData(data);
        plugin.getDatabaseManager().updatePlayerLanguage(player.getUniqueId(), code);
        player.sendMessage(msg(player, "lang-changed", code));
        return true;
    }

    private boolean handleGetBags(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(msg(sender, "player-only"));
            return true;
        }
        Player player = (Player) sender;
        SoulSpacePlugin plugin = SoulSpacePlugin.getInstance();
        PlayerData data = plugin.getDatabaseManager().loadPlayerData(player.getUniqueId());
        if (data == null) {
            data = new PlayerData(player.getUniqueId(), false, 1);
        }

        int cost = plugin.getConfigManager().getFirstCost();

        if (!data.isHasPaid()) {
            if (plugin.getEconomy().getBalance(player) < cost) {
                player.sendMessage(msg(player, "insufficient-funds", String.valueOf(cost)));
                return true;
            }

            EconomyResponse response = plugin.getEconomy().withdrawPlayer(player, cost);
            if (!response.transactionSuccess()) {
                player.sendMessage(msg(player, "deduct-failed"));
                return true;
            }
            data.setHasPaid(true);
            plugin.getDatabaseManager().savePlayerData(data);
        }

        ItemStack feather = createSoulFeather(player, data.getLevel());
        player.getInventory().addItem(feather);
        player.sendMessage(msg(player, "got-feather", plugin.getLanguageManager().getSoulSpaceDisplayName(player)));
        return true;
    }

    private boolean handleOpen(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(msg(sender, "player-only"));
            return true;
        }
        Player player = (Player) sender;
        SoulSpaceGUI.open(player);
        return true;
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("soulspace.reload")) {
            sender.sendMessage(msg(sender, "no-permission"));
            return true;
        }
        SoulSpacePlugin plugin = SoulSpacePlugin.getInstance();
        plugin.getConfigManager().reload();
        plugin.getLanguageManager().reload();
        sender.sendMessage(msg(sender, "config-reloaded"));
        return true;
    }

    private String msg(CommandSender sender, String key, String... args) {
        SoulSpacePlugin plugin = SoulSpacePlugin.getInstance();
        if (sender instanceof Player) {
            return plugin.getLanguageManager().getMessage((Player) sender, key, args);
        }
        return plugin.getLanguageManager().getMessage(plugin.getLanguageManager().getDefaultLanguage(), key, args);
    }

    private ItemStack createSoulFeather(Player player, int level) {
        SoulSpacePlugin plugin = SoulSpacePlugin.getInstance();
        ItemStack feather = new ItemStack(Material.FEATHER);
        ItemMeta meta = feather.getItemMeta();
        meta.setDisplayName(plugin.getLanguageManager().getSoulSpaceDisplayName(player));
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.setLore(Arrays.asList(
                plugin.getLanguageManager().getMessage(player, "feather-player-id", player.getName()),
                plugin.getLanguageManager().getMessage(player, "feather-space-level", String.valueOf(level))
        ));
        feather.setItemMeta(meta);
        SoulSpaceGUI.markSoulFeather(feather);
        return feather;
    }
}