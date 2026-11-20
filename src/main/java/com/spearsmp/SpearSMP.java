package com.spearsmp;

import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import java.util.*;

public final class SpearSMP extends JavaPlugin implements Listener, CommandExecutor {
    private NamespacedKey spearKey, tierKey;
    private final Map<UUID, Long> dashReady = new HashMap<>();
    private static final String[] TIERS = {"Wood", "Stone", "Coal", "Copper", "Redstone", "Iron", "Lapis", "Gold", "Diamond", "Netherite", "God"};
    private static final int[] REQUIRED = {1,1,1,2,2,2,2,3,3,3};
    private static final ChatColor[] COLORS = {ChatColor.GRAY,ChatColor.WHITE,ChatColor.DARK_GRAY,ChatColor.GOLD,ChatColor.RED,ChatColor.WHITE,ChatColor.BLUE,ChatColor.YELLOW,ChatColor.AQUA,ChatColor.DARK_PURPLE,ChatColor.LIGHT_PURPLE};

    @Override public void onEnable() {
        saveDefaultConfig(); spearKey = new NamespacedKey(this, "spear_smp_weapon"); tierKey = new NamespacedKey(this, "spear_smp_tier");
        getServer().getPluginManager().registerEvents(this, this); Objects.requireNonNull(getCommand("spear")).setExecutor(this);
        getLogger().info("SpearSMP progression enabled. Use /spear help");
    }

    private ItemStack makeSpear(int tier) {
        tier = Math.max(0, Math.min(10, tier)); ItemStack item = new ItemStack(Material.TRIDENT); ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(COLORS[tier] + "✦ " + TIERS[tier] + " Spear" + (tier == 10 ? " ✦" : ""));
        meta.setCustomModelData(1001 + tier); meta.getPersistentDataContainer().set(spearKey, PersistentDataType.BYTE, (byte)1); meta.getPersistentDataContainer().set(tierKey, PersistentDataType.INTEGER, tier);
        List<String> lore = new ArrayList<>(); lore.add(ChatColor.DARK_PURPLE + "SpearSMP • Cosmic Relic"); lore.add(ChatColor.GRAY + "Tier: " + COLORS[tier] + TIERS[tier]); lore.add(ChatColor.YELLOW + "Kills to next tier: " + (tier == 10 ? "God's path" : REQUIRED[tier])); lore.add(ChatColor.DARK_GRAY + "Right-click: Void Dash"); meta.setLore(lore);
        addEnchant(meta, "sharpness", Math.min(10, new int[]{0,1,2,3,4,5,6,7,8,9,7}[tier]));
        int fire = new int[]{0,0,1,2,2,2,2,3,3,4,5}[tier]; addEnchant(meta,"fire_aspect",fire);
        item.setItemMeta(meta); return item;
    }
    private void addEnchant(ItemMeta meta, String key, int level) { if(level <= 0) return; Enchantment e = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(key)); if(e != null) meta.addEnchant(e, level, true); }
    private boolean isSpear(ItemStack item) { return item != null && item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(spearKey, PersistentDataType.BYTE); }
    private int getTier(ItemStack item) { return item.getItemMeta().getPersistentDataContainer().getOrDefault(tierKey, PersistentDataType.INTEGER, 0); }
    private ItemStack findSpear(Player p) { if(isSpear(p.getInventory().getItemInMainHand())) return p.getInventory().getItemInMainHand(); if(isSpear(p.getInventory().getItemInOffHand())) return p.getInventory().getItemInOffHand(); for(ItemStack it:p.getInventory().getContents()) if(isSpear(it)) return it; return null; }
    private int kills(UUID id) { return getConfig().getInt("players."+id+".kills",0); }
    private int progress(UUID id) { return getConfig().getInt("players."+id+".progress",0); }
    private void setStats(UUID id,int k,int p) { getConfig().set("players."+id+".kills",k); getConfig().set("players."+id+".progress",p); saveConfig(); }

    @EventHandler public void onKill(PlayerDeathEvent event) {
        Player killer = event.getEntity().getKiller(); if(killer == null) return; ItemStack spear = findSpear(killer); if(spear == null) return;
        int tier = getTier(spear), total = kills(killer.getUniqueId()) + 1, prog = progress(killer.getUniqueId()) + 1; setStats(killer.getUniqueId(),total,prog);
        killer.sendMessage(ChatColor.LIGHT_PURPLE + "✦ Spear kill " + ChatColor.GRAY + "("+total+" total)");
        if(tier < 10 && prog >= REQUIRED[tier]) { setStats(killer.getUniqueId(),total,0); ItemStack upgraded = makeSpear(tier+1); replaceSpear(killer,spear,upgraded); killer.sendTitle(COLORS[tier+1]+"✦ "+TIERS[tier+1]+"!",ChatColor.GRAY+"Your spear has evolved",10,45,15); killer.playSound(killer.getLocation(),Sound.UI_TOAST_CHALLENGE_COMPLETE,0.8f,1.2f); killer.getWorld().spawnParticle(Particle.END_ROD,killer.getLocation().add(0,1,0),45,.5,.7,.5,.03); }
        else if(tier == 10) { ItemMeta meta=spear.getItemMeta(); int sharp=Math.min(10,meta.getEnchantLevel(Objects.requireNonNull(Registry.ENCHANTMENT.get(NamespacedKey.minecraft("sharpness"))))+1); addEnchant(meta,"sharpness",sharp); meta.setDisplayName(ChatColor.LIGHT_PURPLE+"✦ God Spear • Sharpness "+sharp); spear.setItemMeta(meta); killer.sendMessage(ChatColor.LIGHT_PURPLE+"God Spear grows stronger: Sharpness "+sharp+"/10"); }
    }
    private void replaceSpear(Player p, ItemStack old, ItemStack replacement) { int slot=p.getInventory().first(old); if(slot>=0) p.getInventory().setItem(slot,replacement); else if(p.getInventory().getItemInOffHand().equals(old)) p.getInventory().setItemInOffHand(replacement); else p.getInventory().setItemInMainHand(replacement); }
    @EventHandler public void onDrop(PlayerDropItemEvent e) { if(isSpear(e.getItemDrop().getItemStack())) { e.setCancelled(true); e.getPlayer().sendMessage(ChatColor.RED+"Your bound spear cannot be dropped."); } }
    @EventHandler public void onUse(PlayerInteractEvent e) { Player p=e.getPlayer(); if(!isSpear(p.getInventory().getItemInMainHand())) return; if(e.getAction().isRightClick()) { long now=System.currentTimeMillis(); long ready=dashReady.getOrDefault(p.getUniqueId(),0L); if(now<ready) return; int cd=getConfig().getInt("abilities.dash-cooldown-seconds",5); dashReady.put(p.getUniqueId(),now+cd*1000L); double strength=getConfig().getDouble("abilities.dash-strength",1.45); Vector v=p.getLocation().getDirection().multiply(strength).setY(.35); p.setVelocity(v); p.getWorld().spawnParticle(Particle.PORTAL,p.getLocation().add(0,1,0),35,.35,.45,.35,.2); p.getWorld().playSound(p.getLocation(),Sound.ENTITY_ENDERMAN_TELEPORT,.7f,1.4f); } }

    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length==0 || args[0].equalsIgnoreCase("help")) { sender.sendMessage(ChatColor.LIGHT_PURPLE+"SpearSMP commands: /spear start | stats | give <player> <tier> | reload"); return true; }
        if(args[0].equalsIgnoreCase("start")) { if(!(sender instanceof Player p)){sender.sendMessage("Players only.");return true;} if(findSpear(p)!=null){p.sendMessage(ChatColor.YELLOW+"You already have a SpearSMP spear.");return true;} p.getInventory().addItem(makeSpear(0)); p.sendMessage(ChatColor.GREEN+"Your Wooden Spear has been granted. Get kills to evolve it!"); return true; }
        if(args[0].equalsIgnoreCase("stats")) { if(!(sender instanceof Player p)){sender.sendMessage("Players only.");return true;} ItemStack s=findSpear(p); if(s==null){p.sendMessage(ChatColor.RED+"You don't have a SpearSMP spear. Use /spear start.");return true;} int t=getTier(s); p.sendMessage(ChatColor.LIGHT_PURPLE+"Spear: "+COLORS[t]+TIERS[t]+ChatColor.GRAY+" | Total kills: "+kills(p.getUniqueId())+" | Progress: "+(t==10?"God tier":progress(p.getUniqueId())+"/"+REQUIRED[t])); return true; }
        if(args[0].equalsIgnoreCase("give")) { if(!sender.hasPermission("spearsmp.admin")){sender.sendMessage(ChatColor.RED+"No permission.");return true;} if(args.length<3){sender.sendMessage("Usage: /spear give <player> <wood|stone|coal|copper|redstone|iron|lapis|gold|diamond|netherite|god>");return true;} Player target=Bukkit.getPlayerExact(args[1]); if(target==null){sender.sendMessage("Player not online.");return true;} int t=-1; for(int i=0;i<TIERS.length;i++)if(TIERS[i].equalsIgnoreCase(args[2]))t=i; if(t<0){sender.sendMessage("Unknown tier.");return true;} target.getInventory().addItem(makeSpear(t)); sender.sendMessage("Gave "+TIERS[t]+" spear to "+target.getName()+"."); return true; }
        if(args[0].equalsIgnoreCase("reload")){if(!sender.hasPermission("spearsmp.admin")){sender.sendMessage(ChatColor.RED+"No permission.");return true;}reloadConfig();sender.sendMessage(ChatColor.GREEN+"SpearSMP config reloaded.");return true;}
        sender.sendMessage(ChatColor.RED+"Unknown command. /spear help"); return true;
    }
}
