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

public class GetBagsCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "该命令只能由玩家执行！");
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
                player.sendMessage(ChatColor.RED + "你的申领资金不足！需要 " + cost);
                return true;
            }

            EconomyResponse response = plugin.getEconomy().withdrawPlayer(player, cost);

            if (!response.transactionSuccess()) {
                player.sendMessage(ChatColor.RED + "扣款失败，请重试！");
                return true;
            }
            data.setHasPaid(true);
            plugin.getDatabaseManager().savePlayerData(data);
        }

        ItemStack feather = createSoulFeather(player, data.getLevel());
        player.getInventory().addItem(feather);
        player.sendMessage(ChatColor.AQUA + "你获得了" + plugin.getConfigManager().getDisplayName() + "！");
        return true;
    }

    private ItemStack createSoulFeather(Player player, int level) {
        SoulSpacePlugin plugin = SoulSpacePlugin.getInstance();
        ItemStack feather = new ItemStack(Material.FEATHER);
        ItemMeta meta = feather.getItemMeta();
        meta.setDisplayName(plugin.getConfigManager().getDisplayName());
        meta.addEnchant(Enchantment.UNBREAKING,1 , true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.setLore(Arrays.asList(
                ChatColor.AQUA + "玩家ID：" + player.getName(),   // 新增青色玩家ID
                ChatColor.GREEN + "空间等级：" + ChatColor.RED + level + "级"
        ));
        feather.setItemMeta(meta);
        return feather;
    }
}