package com.pzg.www.zombienotstupid.main;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import net.minecraft.server.v1_12_R1.Entity;
import net.minecraft.server.v1_12_R1.EntityTypes;
import net.minecraft.server.v1_12_R1.EntityZombie;
import net.minecraft.server.v1_12_R1.MinecraftKey;

public class PluginMain extends JavaPlugin implements Listener {
	
	public static boolean useCustomAI = true;
	public static Plugin plugin;
	
	protected PluginDescriptionFile pdfFile = getDescription();
	
	@Override
	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(this, this);
		CustomEntities.registerEntities();
	}
	
	@Override
	public void onDisable() {
		CustomEntities.unregisterEntities();
	}
	
	@EventHandler
	public void zombieSpawn(EntitySpawnEvent e) {
		Bukkit.getLogger().info("Entity spawned");
		
		if (e.getEntityType().equals(EntityType.ZOMBIE)) {
			Bukkit.getLogger().info("Replacing vanilla zombie with special zombie.");
			e.getEntity().remove();
			
			final CustomZombie zombie = new CustomZombie(((CraftWorld) ((World) e.getLocation().getWorld())).getHandle(), 1.2);
			CustomEntities.spawnEntity(zombie, e.getLocation());
			
			new BukkitRunnable() {
				public void run() {
					Bukkit.getLogger().info("updating zombie walk");
					updateZombieTarget((Zombie) zombie, true);
				}
			}.runTaskLater(this, 100);
		}
	}
	
	public void updateZombieTarget(Zombie zombie, boolean ignoreCurrentTarget){
		Player targetPlayer = getClosestPlayer(zombie);
		if(targetPlayer != null){
			if(!ignoreCurrentTarget){
				if(zombie.getTarget() != null){
					return;
				}
			}
			zombie.setTarget(targetPlayer);
		}else{
			zombie.setTarget(targetPlayer);
		}
	}
	
	public Player getClosestPlayer(LivingEntity enemy){
		Player targetPlayer = null;
		for(Player player : Bukkit.getOnlinePlayers()){
			if(targetPlayer == null){
				targetPlayer = player;
			}else{
				Location zombieLocation = enemy.getLocation();
				if(zombieLocation.distanceSquared(player.getLocation()) < zombieLocation.distanceSquared((targetPlayer.getLocation()))){
					targetPlayer = player;
				}
			}
		}
		return targetPlayer;
	}
	
	public enum CustomEntities {
		CUSTOM_ZOMBIE("Zombie", 54, EntityType.ZOMBIE, EntityZombie.class, CustomZombie.class);

	    private String name;
	    private int id;
	    private EntityType entityType;
	    private Class<? extends Entity> nmsClass;
	    private Class<? extends Entity> customClass;
	    private MinecraftKey key;
	    private MinecraftKey oldKey;

		public static void spawnEntity(Entity entity, Location loc) {
			entity.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
			((CraftWorld)loc.getWorld()).getHandle().addEntity(entity, SpawnReason.CUSTOM);
		}
	    
		private CustomEntities(String name, int id, EntityType entityType, Class<? extends Entity> nmsClass, Class<? extends Entity> customClass) {
	        this.name = name;
	        this.id = id;
	        this.entityType = entityType;
	        this.nmsClass = nmsClass;
	        this.customClass = customClass;
	        this.key = new MinecraftKey(name);
	        this.oldKey = EntityTypes.b.b(nmsClass);
	    }

	    public static void registerEntities() { for (CustomEntities ce : CustomEntities.values()) ce.register(); }
	    public static void unregisterEntities() { for (CustomEntities ce : CustomEntities.values()) ce.unregister(); }

	    private void register() {
	        EntityTypes.d.add(key);
	        EntityTypes.b.a(id, key, customClass);
	    }

	    private void unregister() {
	        EntityTypes.d.remove(key);
	        EntityTypes.b.a(id, oldKey, nmsClass);
	    }

	    public String getName() {
	        return name;
	    }

	    public int getID() {
	        return id;
	    }

	    public EntityType getEntityType() {
	        return entityType;
	    }

	    public Class<?> getCustomClass() {
	        return customClass;
	    }
	}
}
