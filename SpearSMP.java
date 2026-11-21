package com.spearsmp;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import java.lang.reflect.Method;
import java.util.*;

public final class SpearSMP extends JavaPlugin implements Listener, CommandExecutor {
    private NamespacedKey spearKey, tierKey;
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final String[] TIERS = {"Wood","Stone","Coal","Copper","Redstone","Iron","Lapis","Gold","Diamond","Netherite","God"};
    private static final String[] KEYS = {"wood","stone","coal","copper","redstone","iron","lapis","gold","diamond","netherite","god"};
    private static final int[] REQUIRED = {1,1,1,2,2,2,2,3,3,3};
    private static final ChatColor[] COLORS = {ChatColor.GRAY,ChatColor.WHITE,ChatColor.DARK_GRAY,ChatColor.GOLD,ChatColor.RED,ChatColor.WHITE,ChatColor.BLUE,ChatColor.YELLOW,ChatColor.AQUA,ChatColor.DARK_PURPLE,ChatColor.LIGHT_PURPLE};
    private static final String[] ABILITIES = {"Nature's Grip","Earthquake","Shadow Burst","Static Surge","Overcharge","Iron Guard","Arcane Pulse","Solar Flare","Crystal Storm","Void Rift","Thunder I"};
    private static final int[] MODEL = {1001,1002,1003,1004,1005,1006,1007,1008,1009,1010,1011};

    @Override public void onEnable() {
        saveDefaultConfig(); spearKey=new NamespacedKey(this,"spear_smp_weapon"); tierKey=new NamespacedKey(this,"spear_smp_tier");
        Bukkit.getPluginManager().registerEvents(this,this);
        Objects.requireNonNull(getCommand("spear")).setExecutor(this);
        Objects.requireNonNull(getCommand("player")).setExecutor(this);
        getLogger().info("SpearSMP 1.1.0 enabled. /spear help");
    }

    private ItemStack makeSpear(int tier) {
        tier=Math.max(0,Math.min(10,tier)); ItemStack item=new ItemStack(Material.TRIDENT); ItemMeta meta=item.getItemMeta();
        meta.setDisplayName(COLORS[tier]+"✦ "+TIERS[tier]+" Spear"+(tier==10?" ✦":""));
        meta.setCustomModelData(MODEL[tier]); meta.setUnbreakable(true);
        meta.getPersistentDataContainer().set(spearKey,PersistentDataType.BYTE,(byte)1);
        meta.getPersistentDataContainer().set(tierKey,PersistentDataType.INTEGER,tier);
        List<String> lore=new ArrayList<>(); lore.add(ChatColor.DARK_PURPLE+"SpearSMP • Cosmic Relic");
        lore.add(ChatColor.GRAY+"Tier: "+COLORS[tier]+TIERS[tier]); lore.add(ChatColor.LIGHT_PURPLE+"Ability: "+ABILITIES[tier]);
        lore.add(ChatColor.YELLOW+"Right-click: use ability"); lore.add(ChatColor.DARK_GRAY+"Bound • Unbreakable"); meta.setLore(lore);
        addEnchant(meta,"sharpness",Math.min(5,tier/2)); item.setItemMeta(meta); return item;
    }
    private void addEnchant(ItemMeta meta,String key,int level) { if(level<=0)return; Enchantment e=Registry.ENCHANTMENT.get(NamespacedKey.minecraft(key)); if(e!=null)meta.addEnchant(e,level,true); }
    private boolean isSpear(ItemStack i){return i!=null&&i.getType()==Material.TRIDENT&&i.hasItemMeta()&&i.getItemMeta().getPersistentDataContainer().has(spearKey,PersistentDataType.BYTE);}
    private int tier(ItemStack i){return i.getItemMeta().getPersistentDataContainer().getOrDefault(tierKey,PersistentDataType.INTEGER,0);}
    private ItemStack findSpear(Player p){if(isSpear(p.getInventory().getItemInMainHand()))return p.getInventory().getItemInMainHand();if(isSpear(p.getInventory().getItemInOffHand()))return p.getInventory().getItemInOffHand();for(ItemStack i:p.getInventory().getContents())if(isSpear(i))return i;return null;}
    private int kills(UUID id){return getConfig().getInt("players."+id+".kills",0);} private int progress(UUID id){return getConfig().getInt("players."+id+".progress",0);}
    private void stats(UUID id,int k,int pr){getConfig().set("players."+id+".kills",k);getConfig().set("players."+id+".progress",pr);saveConfig();}
    private void awardKill(Player killer){ItemStack spear=findSpear(killer);if(spear==null)return;int t=tier(spear),total=kills(killer.getUniqueId())+1,pr=progress(killer.getUniqueId())+1;stats(killer.getUniqueId(),total,pr);killer.sendMessage(ChatColor.LIGHT_PURPLE+"✦ Spear kill registered "+ChatColor.GRAY+"("+total+" total)");
        if(t<10&&pr>=REQUIRED[t]){stats(killer.getUniqueId(),total,0);ItemStack next=makeSpear(t+1);replaceSpear(killer,spear,next);killer.sendTitle(COLORS[t+1]+"✦ "+TIERS[t+1]+"!",ChatColor.GRAY+"Your spear evolved • "+ABILITIES[t+1],10,45,15);killer.playSound(killer,Sound.UI_TOAST_CHALLENGE_COMPLETE,.9f,1.15f);killer.getWorld().spawnParticle(Particle.END_ROD,killer.getLocation().add(0,1,0),50,.5,.7,.5,.04);}
        else if(t==10){killer.sendMessage(ChatColor.LIGHT_PURPLE+"God-tier kill recorded. Thunder I remains active.");}
    }
    private void replaceSpear(Player p,ItemStack old,ItemStack replacement){int slot=p.getInventory().first(old);if(slot>=0)p.getInventory().setItem(slot,replacement);else if(p.getInventory().getItemInOffHand().equals(old))p.getInventory().setItemInOffHand(replacement);else p.getInventory().setItemInMainHand(replacement);}

    @EventHandler public void onPlayerKill(PlayerDeathEvent e){Player killer=e.getEntity().getKiller();if(killer!=null)awardKill(killer);}
    @EventHandler public void onDamage(EntityDamageByEntityEvent e){if(!(e.getDamager() instanceof Player p))return;ItemStack item=p.getInventory().getItemInMainHand();if(!isSpear(item))return;int t=tier(item);double[] damage=getConfig().getDoubleList("combat.damage").stream().mapToDouble(Double::doubleValue).toArray();double d=t<damage.length?damage[t]:4.0;e.setDamage(d);
        if(t==10&&e.getEntity() instanceof LivingEntity target){Location at=target.getLocation().add(0,1,0);target.getWorld().strikeLightningEffect(at);target.getWorld().spawnParticle(Particle.ELECTRIC_SPARK,at,28,.35,.55,.35,.08);target.getWorld().playSound(at,Sound.ENTITY_LIGHTNING_BOLT_THUNDER,.5f,1.65f);}
    }
    @EventHandler public void onDrop(PlayerDropItemEvent e){if(isSpear(e.getItemDrop().getItemStack())){e.setCancelled(true);e.getPlayer().sendMessage(ChatColor.RED+"Your bound spear cannot be dropped.");}}
    @EventHandler public void onUse(PlayerInteractEvent e){Player p=e.getPlayer();ItemStack item=p.getInventory().getItemInMainHand();if(!isSpear(item)||!e.getAction().isRightClick())return;e.setCancelled(true);int t=tier(item);long now=System.currentTimeMillis(),ready=cooldowns.getOrDefault(p.getUniqueId(),0L);if(now<ready){long left=(ready-now+999)/1000;p.sendActionBar(ChatColor.RED+ABILITIES[t]+" cooldown: "+left+"s");return;}int cd=getConfig().getInt("abilities."+KEYS[t]+"-cooldown",12);cooldowns.put(p.getUniqueId(),now+cd*1000L);useAbility(p,t);showCooldown(p,t,cd);}
    private void showCooldown(Player p,int tier,int seconds){new BukkitRunnable(){int left=seconds;public void run(){if(!p.isOnline()){cancel();return;}if(left<=0){p.sendActionBar(ChatColor.GREEN+ABILITIES[tier]+" ready!");cancel();return;}p.sendActionBar(ChatColor.LIGHT_PURPLE+ABILITIES[tier]+ChatColor.GRAY+" • "+left+"s");left--;}}.runTaskTimer(this,0L,20L);}
    private void useAbility(Player p,int t){Location l=p.getLocation();World w=p.getWorld();LivingEntity target=nearestTarget(p,7);
        switch(t){
            case 0 -> {p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION,100,0));w.spawnParticle(Particle.HAPPY_VILLAGER,l.add(0,1,0),24,.6,.8,.6,.05);w.playSound(p,Sound.BLOCK_AZALEA_LEAVES_BREAK,.8f,1.2f);}
            case 1 -> {w.spawnParticle(Particle.BLOCK,l,45,.8,.15,.8,.2,Material.STONE.createBlockData());w.playSound(p,Sound.ENTITY_GENERIC_EXPLODE,.65f,.7f);if(target!=null)target.damage(3,p);}
            case 2 -> {w.spawnParticle(Particle.SMOKE,l.add(0,1,0),45,.7,.8,.7,.06);w.playSound(p,Sound.ENTITY_ENDERMAN_STARE,.8f,.8f);if(target!=null)target.damage(2,p);}
            case 3 -> {w.spawnParticle(Particle.ELECTRIC_SPARK,l.add(0,1,0),35,.6,.7,.6,.1);w.playSound(p,Sound.BLOCK_COPPER_BULB_TURN_ON,1,1.4f);if(target!=null){target.damage(3,p);w.strikeLightningEffect(target.getLocation());}}
            case 4 -> {p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,160,1));w.spawnParticle(Particle.DUST,l.add(0,1,0),35,.5,.6,.5,.1,new Particle.DustOptions(Color.RED,1.4f));w.playSound(p,Sound.BLOCK_BEACON_POWER_SELECT,.8f,1.5f);}
            case 5 -> {p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE,120,1));w.spawnParticle(Particle.CRIT,l.add(0,1,0),30,.5,.6,.5,.1);w.playSound(p,Sound.ITEM_SHIELD_BLOCK,1,.8f);}
            case 6 -> {w.spawnParticle(Particle.ENCHANT,l.add(0,1,0),55,.8,.8,.8,1);w.playSound(p,Sound.BLOCK_ENCHANTMENT_TABLE_USE,1,1.4f);if(target!=null){target.damage(2,p);target.setVelocity(target.getVelocity().add(new Vector(0,.8,0)));}}
            case 7 -> {w.spawnParticle(Particle.FLAME,l.add(0,1,0),45,.7,.8,.7,.03);w.playSound(p,Sound.ITEM_FIRECHARGE_USE,.8f,1.4f);if(target!=null)target.damage(4,p);}
            case 8 -> {w.spawnParticle(Particle.END_ROD,l.add(0,1,0),65,.8,.8,.8,.08);w.playSound(p,Sound.BLOCK_AMETHYST_BLOCK_CHIME,1,1.6f);if(target!=null)target.damage(5,p);}
            case 9 -> {Vector dir=p.getLocation().getDirection();Location dest=p.getLocation().add(dir.multiply(4));if(dest.getBlock().isPassable()&&dest.clone().add(0,1,0).getBlock().isPassable())p.teleport(dest);w.spawnParticle(Particle.PORTAL,l.add(0,1,0),55,.5,.8,.5,.4);w.playSound(p,Sound.ENTITY_ENDERMAN_TELEPORT,1,.8f);}
            case 10 -> {w.spawnParticle(Particle.ELECTRIC_SPARK,l.add(0,1,0),90,.9,1,.9,.15);w.strikeLightningEffect(p.getTargetBlockExact(12)==null?l:p.getTargetBlockExact(12).getLocation());w.playSound(p,Sound.ENTITY_LIGHTNING_BOLT_IMPACT,1,1.2f);}
        }
    }
    private LivingEntity nearestTarget(Player p,double range){LivingEntity best=null;double dist=range*range;for(Entity e:p.getNearbyEntities(range,range,range)){if(e instanceof LivingEntity le&&e!=p&&!(e instanceof ArmorStand)){double d=e.getLocation().distanceSquared(p.getLocation());if(d<dist&&p.hasLineOfSight(e)){dist=d;best=le;}}}return best;}

    @Override public boolean onCommand(CommandSender s,Command c,String label,String[] a){
        if(c.getName().equalsIgnoreCase("player")){if(!(s instanceof Player p)){s.sendMessage("Players only.");return true;}if(!s.hasPermission("spearsmp.admin")){s.sendMessage(ChatColor.RED+"You need spearsmp.admin.");return true;}if(Bukkit.getPluginManager().getPlugin("Citizens")==null){s.sendMessage(ChatColor.RED+"Install the Citizens plugin first. /player uses a real player NPC.");return true;}String name=a.length>0?a[0]:"SpearDummy";try{Class<?> api=Class.forName("net.citizensnpcs.api.CitizensAPI");Object registry=api.getMethod("getNPCRegistry").invoke(null);Method create=null;for(Method m:registry.getClass().getMethods())if(m.getName().equals("createNPC")&&m.getParameterCount()==2){create=m;break;}if(create==null)throw new NoSuchMethodException("createNPC");Object npc=create.invoke(registry,EntityType.PLAYER,name);npc.getClass().getMethod("spawn",Location.class).invoke(npc,p.getLocation());p.sendMessage(ChatColor.GREEN+"Spawned test player "+name+". Kill it while holding your spear to test progression.");}catch(Exception ex){getLogger().warning("Could not spawn Citizens NPC: "+ex.getMessage());p.sendMessage(ChatColor.RED+"NPC spawn failed. Check Citizens version and console.");}return true;}
        if(a.length==0||a[0].equalsIgnoreCase("help")){s.sendMessage(ChatColor.LIGHT_PURPLE+"SpearSMP: /spear start | stats | give <player> <tier> | reload");s.sendMessage(ChatColor.GRAY+"Each tier has its own right-click ability and cooldown. God: Thunder I on every living target hit.");return true;}
        if(a[0].equalsIgnoreCase("start")){if(!(s instanceof Player p)){s.sendMessage("Players only.");return true;}if(findSpear(p)!=null){p.sendMessage(ChatColor.YELLOW+"You already have a SpearSMP spear.");return true;}p.getInventory().addItem(makeSpear(0));p.sendMessage(ChatColor.GREEN+"Wooden Spear granted. Get kills to evolve it!");return true;}
        if(a[0].equalsIgnoreCase("stats")){if(!(s instanceof Player p)){s.sendMessage("Players only.");return true;}ItemStack i=findSpear(p);if(i==null){p.sendMessage(ChatColor.RED+"No spear found. Use /spear start.");return true;}int t=tier(i);p.sendMessage(ChatColor.LIGHT_PURPLE+"Spear: "+COLORS[t]+TIERS[t]+ChatColor.GRAY+" | Kills: "+kills(p.getUniqueId())+" | Progress: "+(t==10?"MAX":progress(p.getUniqueId())+"/"+REQUIRED[t]));return true;}
        if(a[0].equalsIgnoreCase("give")){if(!s.hasPermission("spearsmp.admin")){s.sendMessage(ChatColor.RED+"No permission.");return true;}if(a.length<3){s.sendMessage("Usage: /spear give <player> <tier>");return true;}Player target=Bukkit.getPlayerExact(a[1]);if(target==null){s.sendMessage("Player not online.");return true;}int t=-1;for(int n=0;n<11;n++)if(TIERS[n].equalsIgnoreCase(a[2]))t=n;if(t<0){s.sendMessage("Unknown tier.");return true;}target.getInventory().addItem(makeSpear(t));s.sendMessage("Gave "+TIERS[t]+" spear to "+target.getName());return true;}
        if(a[0].equalsIgnoreCase("reload")){if(!s.hasPermission("spearsmp.admin")){s.sendMessage(ChatColor.RED+"No permission.");return true;}reloadConfig();s.sendMessage(ChatColor.GREEN+"SpearSMP config reloaded.");return true;}
        s.sendMessage(ChatColor.RED+"Unknown command. /spear help");return true;
    }
}
