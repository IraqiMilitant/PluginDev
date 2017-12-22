package com.khelm.dungeons.generation;

import com.khelm.dungeons.Room;
import com.khelm.dungeons.Theme;

/**
 * interface implemented by all generation algorithm classes
 * 
 * @author IraqiMilitant
 *
 */
public interface GenerationAlgorithm {
	
	/**
	 * Generates the room based on the given theme
	 * 
	 * @param theme
	 * @param r
	 */
	public void Generate(Theme theme, Room r);
	
	/**
	 * adds torches to the room
	 * 
	 * @param t
	 * @param r
	 */
	public void genTorches(Theme t,Room r);
	
	/**
	 * Adds the room's hazard to its' generation
	 * 
	 * @param theme
	 * @param r
	 */
	public boolean Hazard(Theme theme, Room r);
	
}
