package com.khelm.dungeons.mobcontrol;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.potion.PotionEffectType;

import com.khelm.dungeons.Constants;
import com.khelm.dungeons.Dungeons;
import com.khelm.dungeons.SerializableLocation;

/**
 * These will be generated on mob group spawn (used for group based abilities etc)
 * 
 * @author IraqiMilitant
 *
 */
public class MobGroup implements Serializable{

	private static final long serialVersionUID = -8407742955857166887L;
	private static ArrayList<MobGroup>resGroups;

	//private static ArrayList<MobGroup>groups=new ArrayList<MobGroup>();
	private transient ArrayList<Mob>mobs;
	private transient MobGroupType type;
	private int diff;
	//private double atk,def;
	private SerializableLocation loc;
	private int taskID;
	private long resTime;
	private int activationRange;
	private int freedom;
	private boolean respawn;
	private String groupName;
	private ArrayList<MobGroupAbilities>abilities;
	private ArrayList<MobAbilities>mobAbilities;
	private transient ArrayList<PotionEffectType>mobEffects;
	//string list used to save potioneffects when the room is saved to file
	private ArrayList<String>effectsSave;
	//identifies if effects have been saved
	private boolean effectsSaved;
	private String name;
	private transient World w;
	private String worldName;


	public MobGroup(MobGroupType type,World w,boolean respawn){
		mobs=new ArrayList<Mob>();
		mobAbilities=new ArrayList<MobAbilities>();
		abilities=new ArrayList<MobGroupAbilities>();
		mobEffects=new ArrayList<PotionEffectType>();
		effectsSaved=false;
		this.type=type;
		groupName=type.getName();
		diff=type.getBaseDiff();
		//groups.add(this);
		this.w=w;
		worldName=w.getName();
		//		atk=1;
		//		def=1;
		resTime=0L;
		this.name=type.getName();
		this.respawn=respawn;
		if(respawn){
			if(!(resGroups instanceof ArrayList<?>)){
				resGroups=new ArrayList<MobGroup>();
			}
			resGroups.add(this);
		}


	}

	//	public void setAtk(double atk){
	//		this.atk=atk;
	//	}
	//
	//	public void setDef(double def){
	//		this.def=def;
	//	}

	public ArrayList<PotionEffectType> getMobEffects(){
		return mobEffects;
	}
	public ArrayList<MobAbilities> getMobAbilities(){
		return mobAbilities;
	}
	public void setResTime(long time){
		resTime=time;
	}

	public void setActivation(int act){
		activationRange=act;
	}

	public void setFreedom(int free){
		freedom=free;
	}

	public void setLoc(Location l){
		loc=new SerializableLocation(l);
	}

	public Location getLoc(){
		return loc.getLocation();
	}

	//	public void activate(){
	//		if(this.respawn){
	//			this.startSpawning();
	//		}else{
	//			this.spawnGroup(loc.getX(), loc.getY(), loc.getZ(), atk, def, false);
	//		}
	//	}

	/**
	 * loads a group object by recovering fields that are not serialized when the object is
	 * saved to file
	 * 
	 * @return
	 */
	public boolean load(){
		this.type=MobGroupType.getByName(groupName);
		mobEffects=new ArrayList<PotionEffectType>();
		mobs=new ArrayList<Mob>();
		if(effectsSaved){//if effects were saved
			for(String s:effectsSave){
				mobEffects.add(PotionEffectType.getByName(s));
			}
		}
		if(!(type instanceof MobGroupType)){
			//groups.remove(this);
			return false;
		}
		w=Bukkit.getWorld(worldName);
		//		if(this.respawn){
		//			this.startSpawning();
		//		}

		return true;
	}

	/**
	 * prep the group to be saved to file
	 */
	public void prepForSave(){
		effectsSave=new ArrayList<String>();
		if(mobEffects.size()>0){//if there are effects save them to string
			for(PotionEffectType p:mobEffects){
				effectsSave.add(p.getName());
				effectsSaved=true;
			}
		}
	}

	/**
	 * spawn the group at a specific location
	 * with fire immunity as needed
	 * 
	 * @param x x coord
	 * @param y y coord
	 * @param z z coord
	 * @param atk attack modifier
	 * @param def defence modifier
	 * @param fire whether or not they need fire immunity
	 */
	public void spawnGroup(int x,int y,int z,double atk,double def,boolean fire){
		Mob m=null;
		mobs.clear();
		for(String mn:type.getMobs()){

			if(mn.equals("wither_skeleton")){
				m=new Mob("skeleton",atk,def,mobEffects,mobAbilities);
				m.setGroup(this);
				m.spawn(w, x, y, z,fire);
				mobs.add(m);
				((Skeleton)m.getEntity()).setSkeletonType(Skeleton.SkeletonType.WITHER);
			}else if(mn.equals("pig_zombie")){
				m=new Mob(mn,atk,def,mobEffects,mobAbilities);
				m.setGroup(this);
				m.spawn(w, x, y, z,fire);
				mobs.add(m);
				((PigZombie)m.getEntity()).setAnger(32767);
				((PigZombie)m.getEntity()).setAngry(true);//piss off the pigman
				((PigZombie)m.getEntity()).setAnger(32767);
			}else{
				m=new Mob(mn,atk,def,mobEffects,mobAbilities);
				m.setGroup(this);
				m.spawn(w, x, y, z,fire);
				mobs.add(m);
			}
		}
	}

	/**
	 * add an ability to the group
	 * 
	 * @param a ability name
	 */
	public void addAbility(String a){
		try{
			if(PotionEffectType.getByName(a.toUpperCase()) instanceof PotionEffectType){
				mobEffects.add(PotionEffectType.getByName(a.toUpperCase()));
				diff+=Constants.mobEffects.get(a.toUpperCase());
				return;
			}
		}catch(IllegalArgumentException ex){

		}
		try{
			if(MobAbilities.valueOf(a.toUpperCase())instanceof MobAbilities){
				mobAbilities.add(MobAbilities.valueOf(a.toUpperCase()));
				diff+=Constants.abilities.get(a.toUpperCase());
				return;
			}
		}catch(IllegalArgumentException ex){

		}
		try{
			if(MobGroupAbilities.valueOf(a.toUpperCase())instanceof MobGroupAbilities){
				abilities.add(MobGroupAbilities.valueOf(a.toUpperCase()));
				diff+=Constants.groupAbilities.get(a.toUpperCase());
				return;
			}
		}catch(IllegalArgumentException ex){

		}
	}

	public ArrayList<MobGroupAbilities> getAbilities(){
		return abilities;
	}

	public void addMob(Mob m){
		mobs.add(m);
	}

	public ArrayList<Mob> getMobs(){
		return mobs;
	}

	public String getName(){
		return name;
	}

	public int getDiff(){
		return diff;
	}

	/**
	 * react to a member of the group attacking a player
	 * @param e
	 */
	public void notifyAttack(EntityDamageByEntityEvent e){
		for(MobGroupAbilities ma:abilities){
			ma.reactAttack(e,this);
		}
	}

	/**
	 * react to a member of the group being damaged
	 * 
	 * @param e
	 */
	public void notifyDamaged(EntityDamageByEntityEvent e){
		Mob mob=null;
		boolean flag=false;
		for(MobGroupAbilities ma:abilities){
			ma.reactDamage(e,this);
		}
		for(Mob m:mobs){
			if(!(m.getEntity().isValid())){
				mob=m;
				flag=true;
			}
		}
		if(flag){
			mobs.remove(mob);
		}
	}

	/**
	 * react to the death of a group member
	 * 
	 * @param e
	 */
	public void notifyDead(EntityDeathEvent e){
		boolean flag=false;
		Mob mob=null;
		for(Mob m:mobs){
			if(m.getEntity().equals(e.getEntity())){
				flag=true;
				mob=m;
			}
		}
		if(flag){
			mobs.remove(mob);
		}
		for(MobGroupAbilities ma:abilities){
			ma.reactDeath(e,this);
		}
	}



	//	public void startSpawning(){
	//
	//		if(this.loc instanceof SerializableLocation){
	//
	//			taskID=Dungeons.getPlugin(Dungeons.class).getServer().getScheduler().scheduleSyncRepeatingTask(Dungeons.getPlugin(Dungeons.class), new Runnable(){
	//
	//				public void run() {
	//					
	//					if(!loc.getLocation().getChunk().isLoaded()){
	//						return;
	//					}
	//					
	//
	//					if(!(mobs instanceof ArrayList<?>)){
	//						mobs=new ArrayList<Mob>();
	//					}
	//					boolean flag=false;
	//					for(Player p:Bukkit.getOnlinePlayers()){
	//						if(p.getWorld().equals(loc.getLocation().getWorld())){
	//							if(p.getLocation().distance(loc.getLocation())<activationRange){
	//								flag=true;
	//							}
	//						}
	//					}
	//					if(mobs.size()>0){
	//						for(Mob m:mobs){
	//							if(m.getEntity().getLocation().distance(loc.getLocation())>freedom){
	//								m.getEntity().teleport(loc.getLocation());
	//								m.getEntity().setHealth(m.getEntity().getMaxHealth());
	//							}
	//							if(m.getEntity().isValid()){
	//								
	//								flag=false;
	//							}
	//						}
	//					}
	//
	//					if(flag||mobs.size()<=(type.getMobs().size()-2)){
	//						if(mobs.size()<=type.getMobs().size()-2){
	//							for(Mob m:mobs){
	//								m.getEntity().remove();
	//							}
	//							mobs.clear();
	//						}
	//						spawnGroup(loc.getX(),loc.getY(),loc.getZ(),atk,def,false);
	//					}
	//
	//
	//				}
	//
	//			}, 0L, resTime<100?0L:resTime);
	//
	//
	//		}
	//	}
	//
	//	public void delete(){
	//		if(resGroups.contains(this)){
	//			Bukkit.getScheduler().cancelTask(taskID);
	//			resGroups.remove(this);
	//		}
	//	}


	//	public static void saveResGroups(){
	//		Dungeons.getPlugin(Dungeons.class).log("Saving Dungeon");
	//		File mobGroupList=new File(Constants.path+Constants.MOBGROUPS);
	//
	//		for(MobGroup mg:resGroups){
	//			mg.prepForSave();
	//		}
	//
	//		try {//save the list to file
	//			if(!mobGroupList.exists()){
	//				mobGroupList.createNewFile();
	//			}
	//			FileOutputStream f_out=new FileOutputStream(Constants.path+Constants.MOBGROUPS);
	//			ObjectOutputStream obj_out=new ObjectOutputStream(f_out);
	//			obj_out.writeObject(resGroups);
	//			obj_out.close();
	//		} catch (FileNotFoundException e) {
	//			// TODO Auto-generated catch block
	//			e.printStackTrace();
	//		} catch (IOException i) {
	//			// TODO Auto-generated catch block
	//			i.printStackTrace();
	//		}
	//	}
	//
	//	public static void loadResGroups(){
	//		Dungeons.getPlugin(Dungeons.class).log("loading DM Item List");
	//		resGroups=new ArrayList<MobGroup>();
	//
	//		File mobGroupList=new File(Constants.path+Constants.MOBGROUPS);
	//		ObjectInputStream obj_in;
	//		FileInputStream f_in;
	//		Object obj;
	//		try {
	//			if(mobGroupList.exists()){//get dungeon list from file
	//				f_in=new FileInputStream(Constants.path+Constants.MOBGROUPS);
	//				obj_in=new ObjectInputStream(f_in);
	//				obj=obj_in.readObject();
	//				//if((ArrayList<?>)obj instanceof ArrayList<?>){
	//				resGroups=(ArrayList<MobGroup>)obj;//set the dungeonnames from the dungeonlist file
	//				//}
	//
	//				for(MobGroup mg:resGroups){
	//					mg.load();
	//				}
	//
	//			}
	//		} catch (FileNotFoundException e) {
	//			// TODO Auto-generated catch block
	//			e.printStackTrace();
	//		} catch (IOException i) {
	//			// TODO Auto-generated catch block
	//			i.printStackTrace();
	//		} catch (ClassNotFoundException o) {
	//			// TODO Auto-generated catch block
	//			o.printStackTrace();
	//		}
	//	}
	//
	//	public static ArrayList<MobGroup> getGroupByLoc(Location l){
	//		ArrayList<MobGroup>results=new ArrayList<MobGroup>();
	//
	//		for(MobGroup mg:resGroups){
	//			if(mg.getLoc().distance(l)<=10){
	//				results.add(mg);
	//			}
	//		}
	//
	//		return results;
	//	}

}
