package com.pzg.www.zombienotstupid.main;

import java.lang.reflect.Field;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;

import net.minecraft.server.v1_12_R1.EntityCreature;
import net.minecraft.server.v1_12_R1.EntityHuman;
import net.minecraft.server.v1_12_R1.EntityLiving;
import net.minecraft.server.v1_12_R1.EntityZombie;
import net.minecraft.server.v1_12_R1.GenericAttributes;
import net.minecraft.server.v1_12_R1.PathfinderGoalFloat;
import net.minecraft.server.v1_12_R1.PathfinderGoalHurtByTarget;
import net.minecraft.server.v1_12_R1.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_12_R1.PathfinderGoalMoveTowardsRestriction;
import net.minecraft.server.v1_12_R1.PathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_12_R1.PathfinderGoalRandomLookaround;
import net.minecraft.server.v1_12_R1.PathfinderGoalRandomStroll;
import net.minecraft.server.v1_12_R1.PathfinderGoalSelector;
import net.minecraft.server.v1_12_R1.PathfinderGoalZombieAttack;
import net.minecraft.server.v1_12_R1.World;

public class CustomZombie extends EntityZombie {

	private double speed;

	public PathfinderGoalWalkToLoc pathfinderGoalWalkToLoc;
	public PathfinderTargetPlayer pathfinderTargetPlayer;

	public CustomZombie(World world, double speed){
		super(world);
//		this.speed = speed * (PluginMain.useCustomAI ? 1.22:1);
//		
//		HashSet<?> goalB =   (HashSet<?>)getPrivateField("b", PathfinderGoalSelector.class, goalSelector);   goalB.clear();
//		HashSet<?> goalC =   (HashSet<?>)getPrivateField("c", PathfinderGoalSelector.class, goalSelector);   goalC.clear();
//		HashSet<?> targetB = (HashSet<?>)getPrivateField("b", PathfinderGoalSelector.class, targetSelector); targetB.clear();
//		HashSet<?> targetC = (HashSet<?>)getPrivateField("c", PathfinderGoalSelector.class, targetSelector); targetC.clear();
//		
//		r();
	}

	@Override
	protected void r() {
		this.goalSelector.a(0, new PathfinderGoalFloat(this));
		this.goalSelector.a(2, new PathfinderGoalZombieAttack(this, 1.0D, false));
		this.goalSelector.a(5, new PathfinderGoalMoveTowardsRestriction(this, 1.0D));
		this.goalSelector.a(7, new PathfinderGoalRandomStroll(this, 1.0D));
		this.goalSelector.a(8, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
		this.goalSelector.a(8, new PathfinderGoalRandomLookaround(this));

		this.pathfinderGoalWalkToLoc = new PathfinderGoalWalkToLoc((EntityCreature) this, null, speed);
		this.pathfinderTargetPlayer = new PathfinderTargetPlayer((EntityCreature) this, speed);

		this.targetSelector.a(1, this.pathfinderGoalWalkToLoc);
		this.targetSelector.a(3, new PathfinderGoalHurtByTarget(this, true));

		if(PluginMain.useCustomAI){
			this.targetSelector.a(2, this.pathfinderTargetPlayer);
		}else{
			this.targetSelector.a(4, new PathfinderGoalNearestAttackableTarget<EntityHuman>(this, EntityHuman.class, true));

		}
	}

	@Override
	protected void initAttributes(){
		super.initAttributes();
		getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(100.0D);
		getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.2300000041723251D);
		getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(3.0D);
		getAttributeInstance(GenericAttributes.h).setValue(2.0D);
	}

	public void setTargetLocation(Location loc){
		if(loc != null){
			this.setGoalTarget(null);
		}
		this.pathfinderGoalWalkToLoc.setTargetLocation(loc);

	}

	public void setTarget(LivingEntity craftEntity){
		if(PluginMain.useCustomAI){
			this.pathfinderTargetPlayer.setTarget(craftEntity);
		}else{
			this.getBukkitZombie().setTarget(craftEntity);
		}
	}

	@Override
	public void setGoalTarget(EntityLiving entityLiving){
		if(entityLiving != null && PluginMain.useCustomAI){

			this.setTarget((LivingEntity) entityLiving.getBukkitEntity());

		}

		super.setGoalTarget(entityLiving);

	}
	@Override
	public boolean setGoalTarget(EntityLiving entityLiving, TargetReason reason, boolean fireEvent){
		if(entityLiving != null && PluginMain.useCustomAI){

			this.setTarget((LivingEntity) entityLiving.getBukkitEntity());

		}
		super.setGoalTarget(entityLiving, reason, false);

		return true;
	}

	public Zombie getBukkitZombie(){
		return this.getBukkitEntity() instanceof Zombie ? (Zombie) this.getBukkitEntity():null;
	}

	public static Object getPrivateField(String fieldName, Class<PathfinderGoalSelector> clazz, Object object)
	{
		Field field;
		Object o = null;

		try
		{
			field = clazz.getDeclaredField(fieldName);

			field.setAccessible(true);

			o = field.get(object);
		}
		catch(NoSuchFieldException e)
		{
			e.printStackTrace();
		}
		catch(IllegalAccessException e)
		{
			e.printStackTrace();
		}

		return o;
	}
}
