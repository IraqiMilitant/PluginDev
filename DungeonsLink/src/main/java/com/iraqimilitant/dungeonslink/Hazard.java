package com.iraqimilitant.dungeonslink;

import java.util.HashMap;
/**
 * Enum holds all defined hazard types (note actual generation is handled by the generation classes)
 * 
 * @author IraqiMilitant
 *
 */
public enum Hazard {
	NONE ("none"),
	LAVA ("lava"),
	VOID ("void"),
	FIRE ("fire");

	//holds hazard difficulty ratings
	private static HashMap<Hazard,Integer>hazardRatings;

	Hazard(String name){
	}

	public static int getRating(Hazard h){
		return hazardRatings.get(h);
	}

	public static void setRating(Hazard h,int rating){
		hazardRatings.put(h, rating);
	}
	
	/**
	 * id if this hazard can do fire damage
	 * 
	 * @return
	 */
	public boolean fireDamage(){
		switch(this){
			case NONE:
				return false;
			case LAVA:
				return true;
			case VOID:
				return false;
			case FIRE:
				return true;
		}
		return false;
	}

}
