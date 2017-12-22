package com.khelm.dungeons.mobcontrol;

import java.util.ArrayList;

import com.khelm.dungeons.Dungeons;
/**
 * these will be defined based on config entries
 * 
 * @author IraqiMilitant
 *
 */
public class MobGroupType {
	
	private static ArrayList<MobGroupType>groups=new ArrayList<MobGroupType>();
	private ArrayList<String>mobs;
	private String name;
	private int baseDiff;
	
	public MobGroupType(String name,ArrayList<String>mobs,int baseDiff){
		Dungeons.getPlugin(Dungeons.class).log("mobgrouptype added");
		this.mobs=mobs;
		groups.add(this);
		this.baseDiff=baseDiff;
		this.name=name;
		for(String s:mobs){
			Dungeons.getPlugin(Dungeons.class).log("mob "+s+" added");
		}
		
		
	}
	
	public int getBaseDiff(){
		return baseDiff;
	}
	
	public void addMob(String m){
		mobs.add(m);
	}
	
	public ArrayList<String> getMobs(){
		return mobs;
	}
	
	public void setName(String n){
		name=n;
	}
	
	public String getName(){
		return name;
	}
	
	/**
	 * returns a MobGroupType based on a string name
	 * 
	 * @param name
	 * @return
	 */
	public static MobGroupType getByName(String name){
		for(MobGroupType m:groups){
			if(m.getName().toUpperCase().equals(name.toUpperCase())){
				return m;
			}
		}
		return null;
	}

}
