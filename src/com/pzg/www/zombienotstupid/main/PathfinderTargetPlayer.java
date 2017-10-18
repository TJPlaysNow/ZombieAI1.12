package com.pzg.www.zombienotstupid.main;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

import com.pzg.www.zombienotstupid.pathfinding.AStar;
import com.pzg.www.zombienotstupid.pathfinding.AStar.InvalidPathException;
import com.pzg.www.zombienotstupid.pathfinding.PathingResult;
import com.pzg.www.zombienotstupid.pathfinding.Tile;

import net.minecraft.server.v1_12_R1.EntityCreature;
import net.minecraft.server.v1_12_R1.Navigation;
import net.minecraft.server.v1_12_R1.PathEntity;
import net.minecraft.server.v1_12_R1.PathfinderGoalTarget;

public class PathfinderTargetPlayer extends PathfinderGoalTarget{
	
	EntityCreature entity;
	Navigation navigation;
	
	Location start;
	
	double speed;
	
	Entity target;
	
	public PathfinderTargetPlayer(EntityCreature entity, double speed){
		 super(entity, true);
		 
	     this.entity = entity;
	     this.navigation = (Navigation) this.entity.getNavigation();
	     this.speed = speed;
	     
	}
	
	public void setTarget(Entity target){
		this.target = target;
		
	}
	
	public int errors;
	
	@Override
	public boolean a() {
		if(target == null || !PluginMain.useCustomAI){
			return false;
		}
		
		return true;
	}
	
	List<Tile> walkNodes = new ArrayList<Tile>();
	
	boolean update = true;
	public void updatePath(){
		
		if(!update){
			return;
		}
		if(errors > 10){
			update = false;
			
			new BukkitRunnable(){
				public void run(){
					update = true;
					
					if(errors > 25){
						errors = 0;
					}
				}
			}.runTaskLaterAsynchronously(PluginMain.plugin, errors * 10);
		}
		
		new BukkitRunnable() {
			public void run() {
				if(target == null){
					this.cancel();
					return;
				}
				
				start = entity.getBukkitEntity().getLocation();
				
				Location targetLoc = target.getLocation();
				
				try {
					AStar astarPath = new AStar(start, targetLoc, 80);
					walkNodes = astarPath.iterate();
					PathingResult result = astarPath.getPathingResult();
					
					if(result != PathingResult.SUCCESS || walkNodes == null){
						errors++;
						Bukkit.getLogger().info("Unable to find path! ("+errors+")");
						this.cancel();
						return;
					}
				} catch (InvalidPathException e1) {
					errors++;
					Bukkit.getLogger().info("Invalid path exception ("+errors+")");
					//start or end is air
					this.cancel();
					return;
				}
			}
		}.runTaskAsynchronously(PluginMain.plugin);
		
		return;
	}
	
	int nodesRemoved = 0;
	
	@Override
	public void c(){	
		updatePath();
		
		if(walkNodes == null){
			return;
		}
		
		List<Tile> localNodes = new ArrayList<Tile>(this.walkNodes);
		if(localNodes == null || localNodes.isEmpty()){
			return;
		}
		
		Location currentLoc = this.entity.getBukkitEntity().getLocation();
		
		int i = 0;
		
		Tile tile = localNodes.get(i);
		Location loc  = tile.getLocation(start);
		PathEntity pathEntity; 
		
		while(localNodes.size()-1 > i){
			Tile next = localNodes.get(i+1);
			
			loc  = tile.getLocation(start);
			
			double distanceToNext = currentLoc.distanceSquared(next.getLocation(start));
			double distanceToCurrent = currentLoc.distanceSquared(loc);
			
			if(distanceToNext < distanceToCurrent
				|| distanceToCurrent < 1.5 
				|| (distanceToNext < 10 
					&& (pathEntity = this.navigation.a(loc.getX(), loc.getY(), loc.getZ())) == null)){
				
				tile = next;
				i++;
			}else{
				break;
			}
		}
		
		pathEntity = this.navigation.a(loc.getX(), loc.getY(), loc.getZ());
		
		/*
		Bukkit.getLogger().info("Using walknode "+i+" / "+walkNodes.size());
		for(int t = 0; t < i && t < walkNodes.size(); t++){
			walkNodes.remove(t);
			nodesRemoved++;
		}
		*/
		
		if(target instanceof LivingEntity){
			if(target.getLocation().getBlock().getLocation().add(0.5, 0, 0.5).distanceSquared(currentLoc.getBlock().getLocation().add(0.5, 0, 0.5)) <= 2){
				
				LivingEntity le = (LivingEntity) target;
				le.damage(1, entity.getBukkitEntity());
			}
		}
		
		if (pathEntity == null){
			return;
		}

		this.navigation.a(pathEntity, speed);
		
	}
	
}
