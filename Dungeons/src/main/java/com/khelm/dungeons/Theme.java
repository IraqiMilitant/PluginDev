package com.khelm.dungeons;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

import com.khelm.dungeons.generation.GenerationAlgorithm;
import com.khelm.dungeons.generation.GenerationAlgorithms;

/**
 * Instanced for each theme as defined in the config
 * 
 * @author IraqiMilitant
 *
 */
public class Theme implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6689160083215808278L;

	//list of all themes
	private static ArrayList<Theme>themes=new ArrayList<Theme>();

	//list of blocks to make up walls and ceiling
	private ArrayList<ThemeBlock> other;
	//floor specific blocks
	private ArrayList<ThemeBlock> floor;
	
	//hats used for random selection
	private HashMap<Integer,ThemeBlock>otherHat;
	private HashMap<Integer,ThemeBlock>floorHat;
	
	//theme name
	private String name;
	
	//how the theme is generated
	private GenerationAlgorithm gen;
	
	//add torches?
	private boolean torches;

	/**
	 * Theme constructor defines a new theme given a name, lists of blocks, generation algo name, and a bool value
	 * for torch gen
	 * 
	 * @param name
	 * @param other
	 * @param floor
	 * @param gen
	 * @param torches
	 */
	public Theme(String name,ArrayList<ThemeBlock> other,ArrayList<ThemeBlock> floor,String gen,boolean torches){
		this.other=other;
		this.floor=floor;
		this.name=name;
		this.torches=torches;
		this.gen=GenerationAlgorithms.valueOf(gen.toUpperCase()).getAlgorithm();
		if(!(this.gen instanceof GenerationAlgorithm)){
			this.gen=GenerationAlgorithms.BASIC.getAlgorithm();
		}
		Dungeons.getPlugin(Dungeons.class).log("Theme with name: "+name+" created");
		Dungeons.getPlugin(Dungeons.class).log("Floor:");
		for(ThemeBlock t:floor){
			Dungeons.getPlugin(Dungeons.class).log("  "+t.getId()+", "+t.getVein()+", "+t.getWeight());
		}
		Dungeons.getPlugin(Dungeons.class).log("Other:");
		for(ThemeBlock t:other){
			Dungeons.getPlugin(Dungeons.class).log("  "+t.getId()+", "+t.getVein()+", "+t.getWeight());
		}
		fillHat();

	}
	
	/**
	 * get the theme generation algorithm
	 * 
	 * @return
	 */
	public GenerationAlgorithm getGen(){
		return gen;
	}
	
	public boolean getTorches(){
		return torches;
	}
	
	public void setTorches(boolean t){
		torches=t;
	}

	/**
	 * fills the hats for generation
	 */
	private void fillHat(){
		otherHat= new HashMap<Integer,ThemeBlock>();
		floorHat= new HashMap<Integer,ThemeBlock>();
		int i;
		int s;
		for(ThemeBlock t:other){//fills the other hat
			s=otherHat.size();
			for(i=s;i<(t.getWeight()+s);i++){
				otherHat.put(i, t);
			}
		}
		for(ThemeBlock t:floor){//fills floor hat
			s=floorHat.size();
			for(i=s;i<(t.getWeight()+s);i++){
				floorHat.put(i, t);
			}
		}
	}

	/**
	 * adds a theme to the theme list
	 * 
	 * @param t
	 */
	public static void addTheme(Theme t){
		themes.add(t);
	}

	/**
	 * generates a block at the given location
	 * 
	 * @param l
	 * @param wall
	 * @param floor
	 * @param room
	 */
	@SuppressWarnings("deprecation")
	public void generateBlock(Location l,boolean wall,boolean floor, Room room){
		Random r=new Random();
		int rn;
		ThemeBlock block;
		if(floor){
			rn = r.nextInt(floorHat.size());
			block=floorHat.get(rn);
		}else{
			rn = r.nextInt(otherHat.size());
			block=otherHat.get(rn);
		}
		int id=block.getId();
		l.getBlock().setType(Material.getMaterial(id));
		if(!wall){
			if (block.getVein()){

				veinOut(l,block,room);
			}
		}

	}

	/**
	 * used for blocks that generate veins when placed
	 * 
	 * @param l
	 * @param block
	 * @param room
	 */
	@SuppressWarnings("deprecation")	
	private void veinOut(Location l,ThemeBlock block,Room room){
		Random r=new Random();
		int r_int;
		for(BlockFace f:BlockFace.values()){
			if((l.getBlock().getRelative(f).getType()==Material.AIR)&&(room.isInside(l.getBlock().getRelative(f).getLocation()))){
				r_int=r.nextInt((100 - 0) + 1) + 0;//generate a random number
				if(r_int<3){//3% chance of vein generation
					l.getBlock().getRelative(f).setType(Material.getMaterial(block.getId()));
					veinOut(l.getBlock().getRelative(f).getLocation(),block,room);//recursive call
				}else if(r_int<15 && (f.equals(BlockFace.DOWN)||f.equals(BlockFace.UP))){//15% chance if direction is up or down
					l.getBlock().getRelative(f).setType(Material.getMaterial(block.getId()));
					veinOut(l.getBlock().getRelative(f).getLocation(),block,room);
				}
			}
		}
	}

	/**
	 * gets the list of blocks in this theme (floor or other depending on arg)
	 * 
	 * @param floor
	 * @return
	 */
	public ArrayList<ThemeBlock> getBlocks(boolean floor){
		if(floor){
			return this.floor;
		}else{
			return this.other;
		}
	}
	
	/**
	 * gets the theme name
	 *  
	 * @return
	 */
	public String getName(){
		return name;
	}

	/**
	 * gets a theme corresponding to a string name
	 * 
	 * @param name
	 * @return
	 */
	public static Theme getTheme(String name){
		for(Theme t:themes){
			if (t.getName().equals(name)){
				return t;
			}
		}
		return null;
	}

}
