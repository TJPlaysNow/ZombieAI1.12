package com.pzg.www.zombienotstupid.main;

import org.bukkit.Location;

import net.minecraft.server.v1_12_R1.EntityCreature;
import net.minecraft.server.v1_12_R1.Navigation;
import net.minecraft.server.v1_12_R1.PathEntity;
import net.minecraft.server.v1_12_R1.PathfinderGoalTarget;

public class PathfinderGoalWalkToLoc extends PathfinderGoalTarget{
	
	private double speed;

	private EntityCreature entity;

	private Location loc;

	private Navigation navigation;

	public PathfinderGoalWalkToLoc(EntityCreature entity, Location loc, double speed)   {
		 super(entity, true);
		 
	     this.entity = entity;
	     this.loc = loc;
	     this.navigation = (Navigation) this.entity.getNavigation();
	     this.speed = speed;
	     
	}   
	
	public void setTargetLocation(Location loc){
		this.loc = loc;
	}
	
	public boolean a(){
		return loc != null;
	}
	
	public void c(){
		PathEntity pathEntity = this.navigation.a(loc.getX(), loc.getY(), loc.getZ());
		
		if (pathEntity == null){
			return;
		}

		this.navigation.a(pathEntity, speed);
		
	}
}
