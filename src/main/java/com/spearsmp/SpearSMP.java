package com.spearsmp;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public final class SpearSMP extends JavaPlugin implements Listener {
    private NamespacedKey spearKey;

    @Override
    public void onEnable() {
        spearKey = new NamespacedKey(this, "spear_smp_weapon");
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("SpearSMP prototype enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("SpearSMP disabled.");
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;
        // Prototype feedback only: full kill-based spear progression is not implemented yet.
        killer.sendMessage(ChatColor.DARK_PURPLE + "✦ " + ChatColor.LIGHT_PURPLE
                + "SpearSMP: kill registered! Progression system is still being built.");
    }

    public ItemStack createStarterSpear() {
        ItemStack item = new ItemStack(Material.WOODEN_SWORD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.DARK_PURPLE + "Wooden Spear");
            meta.getPersistentDataContainer().set(spearKey, PersistentDataType.STRING, "wooden");
            item.setItemMeta(meta);
        }
        return item;
    }
}
