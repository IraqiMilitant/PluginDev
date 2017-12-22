package com.khelm.dungeons.mobcontrol;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.khelm.dungeons.Dungeons;

/**
 * Class controls individual custom mobs
 * 
 * @author iraqimilitant
 *
 */
public class Mob {

	//private static ArrayList<Mob> mobs=new ArrayList<Mob>();
	private static ArrayList<Mob> spawnedMobs=new ArrayList<Mob>();
	private static ArrayList<Entity> entities=new ArrayList<Entity>();

	private LivingEntity entity;
	private EntityType type;
	private MobGroup group;
	private double atkMod,defMod;
	private ArrayList<PotionEffectType> effects;
	private ArrayList<MobAbilities>abilities; 

	public Mob(String e,double atk,double def,ArrayList<PotionEffectType>p,ArrayList<MobAbilities>a){
		type=EntityType.valueOf(e.toUpperCase());
		effects=new ArrayList<PotionEffectType>();
		abilities=new ArrayList<MobAbilities>();
		atkMod=atk;
		defMod=def;
		effects=p;
		abilities=a;
		//mobs.add(this);
	}


	/**
	 * returns the assosciated entity
	 * 
	 * @return
	 */
	public LivingEntity getEntity(){
		return entity;
	}

	/**
	 * Assigns this mob to specific mob group
	 * 
	 * @param g
	 */
	public void setGroup(MobGroup g){
		group=g;
	}

	/**
	 * group getter
	 * 
	 * @return
	 */
	public MobGroup getGroup(){
		return group;
	}

	/**
	 * on mob death remove the mob object from list of spawned mobs
	 */
	public void kill(){
		spawnedMobs.remove(this);
	}

	/**
	 * attack modifier getter
	 * @return
	 */
	public double getAtk(){
		return atkMod;
	}

	/**
	 * spawns mob at a location and gives it fire immunity if needed
	 * 
	 * @param w
	 * @param x
	 * @param y
	 * @param z
	 * @param fire
	 */
	public void spawn(World w,int x,int y,int z,boolean fire){
		Location l=new Location(w,x,y,z);
		Entity e=null;
		entities.add(e);
		if(!(w instanceof World)){
			Dungeons.getPlugin(Dungeons.class).log("No World for spawn!!!");
		}
		e=l.getWorld().spawnEntity(l, type);
		if(e instanceof LivingEntity){
			entity=(LivingEntity)e;
		}
		
		final LivingEntity en=entity;
		final Mob mob=this;
		final boolean f=fire;
		
		Bukkit.getServer().getScheduler().runTask(Dungeons.getPlugin(Dungeons.class),new Runnable(){
			public void run() {
				if(MobHealth.valueOf(en.getType().toString()) instanceof MobHealth){
					en.setMaxHealth(MobHealth.valueOf(en.getType().toString()).health*defMod);
					en.setHealth(en.getMaxHealth());
				}else{
					en.setMaxHealth(en.getMaxHealth()*defMod);
					en.setHealth(en.getMaxHealth());
				}

				for(PotionEffectType p:effects){
					en.addPotionEffect(new PotionEffect(p,100000,1));
				}
				if(f){
					en.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE,100000,10000));
				}

				if(abilities instanceof ArrayList){
					for(MobAbilities m:abilities){
						m.reactSpawn(mob);
					}
				}
				en.setRemoveWhenFarAway(false);

				spawnedMobs.add(mob);
			}
			
		});
		
	}

	/**
	 * apply any ability effects for when a mob attacks the player
	 *  
	 * @param e
	 */
	public void notifyAttack(EntityDamageByEntityEvent e){
		for(MobAbilities ma:abilities){
			ma.reactAttack(e);
		}
		if (group instanceof MobGroup){
			group.notifyAttack(e);
		}
	}

	/**
	 * mob reacts to being damaged
	 * 
	 * @param e
	 */
	public void notifyDamaged(EntityDamageByEntityEvent e){
		for(MobAbilities ma:abilities){
			ma.reactDamage(e);
		}
		if (group instanceof MobGroup){
			group.notifyDamaged(e);
		}
	}

	/**
	 * mob reacts to its' own death
	 * 
	 * @param e
	 */
	public void notifyDead(EntityDeathEvent e){
		for(MobAbilities ma:abilities){
			ma.reactDeath(e);
		}
		if (group instanceof MobGroup){
			group.notifyDead(e);
		}
	}

	/**
	 * static method used to kill a mob
	 * 
	 * @param e
	 */
	public static void killMob(LivingEntity e){
		for(Mob m:spawnedMobs){
			if(m.getEntity()==e){
				m.kill();
			}
		}
	}

	/**
	 * returns the Mob object assosciated with a given entity
	 * 
	 * @param e
	 * @return
	 */
	public static Mob dungeonMob(Entity e){
		LivingEntity et;
		if(e instanceof LivingEntity){
			et=(LivingEntity)e;
			for(Mob m:spawnedMobs){
				if(m.getEntity()==et){
					return m;
				}
			}
		}
		return null;
	}

	/**
	 * returns a list of all Mob entities
	 * 
	 * @return
	 */
	public static ArrayList<Entity> getEntities(){
		return entities;
	}
}
