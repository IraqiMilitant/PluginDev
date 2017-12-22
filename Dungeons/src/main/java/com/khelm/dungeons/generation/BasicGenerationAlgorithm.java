package com.khelm.dungeons.generation;

import java.io.Serializable;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

import com.khelm.dungeons.Constants;
import com.khelm.dungeons.Room;
import com.khelm.dungeons.Theme;

/**
 * Generation algorithm for basic gen
 * 
 * @author IraqiMilitant
 *
 */
public class BasicGenerationAlgorithm implements GenerationAlgorithm, Serializable {


	/**
	 * 
	 */
	private static final long serialVersionUID = -7311021823598560043L;

	public BasicGenerationAlgorithm(){

	}

	/**
	 * generates the room with the given theme
	 * 
	 */
	public void Generate(Theme theme,Room r){
		int x,y,z;
		Location l;
		Location c=r.getCenter();
		Random rand=new Random();
		if(!(theme instanceof Theme)){
			return;
		}

		r.clearRoom(true);


		for(z=r.lowZ;z<=r.highZ;z++){
			for(y=r.lowY;y<=r.highY;y++){
				l=new Location(c.getWorld(),r.lowX,y,z);
				if(l.getBlock().getType()==Material.AIR){
					theme.generateBlock(l, true,false,r);
				}
				l=new Location(c.getWorld(),r.highX,y,z);
				if(l.getBlock().getType()==Material.AIR){
					theme.generateBlock(l, true,false,r);
				}
			}
		}
		//r.highZ and r.lowZ walls
		for(x=r.lowX;x<=r.highX;x++){
			for(y=r.lowY;y<=r.highY;y++){
				l=new Location(c.getWorld(),x,y,r.lowZ);
				if(l.getBlock().getType()==Material.AIR){
					theme.generateBlock(l, true,false,r);
				}
				l=new Location(c.getWorld(),x,y,r.highZ);
				if(l.getBlock().getType()==Material.AIR){
					theme.generateBlock(l, true,false,r);
				}
			}
		}

		//ceiling and floor
		for(x=r.lowX;x<=r.highX;x++){
			for(z=r.lowZ;z<=r.highZ;z++){
				l=new Location(c.getWorld(),x,r.lowY,z);
				if(l.getBlock().getType()==Material.AIR){
					theme.generateBlock(l, false,true,r);
				}
				l=new Location(c.getWorld(),x,r.highY,z);
				if(l.getBlock().getType()==Material.AIR){
					theme.generateBlock(l, false,false,r);
				}
			}
		}

		//second layer on ceiling and floor
		for(x=r.lowX;x<=r.highX;x++){
			for(z=r.lowZ;z<=r.highZ;z++){
				l=new Location(c.getWorld(),x,r.lowY+1,z);
				if(l.getBlock().getType()==Material.AIR&&rand.nextBoolean()){
					theme.generateBlock(l, false,true,r);
				}
				l=new Location(c.getWorld(),x,r.highY-1,z);
				if(l.getBlock().getType()==Material.AIR&&rand.nextBoolean()){
					theme.generateBlock(l, false,false,r);
				}
			}
		}
	}

	public void genTorches(Theme t,Room r){
		Random rand=new Random();
		if(t.getTorches()){
			int y=r.lowY+2;
			for(int x=r.lowX;x<=r.highX;x++){
				for(int z=r.lowZ;z<=r.highZ;z++){
					Location b=new Location(Bukkit.getWorld(Constants.dunegonW),x,y,z);
					if(b.getBlock().getRelative(BlockFace.DOWN).getType().isBlock()&&b.getBlock().getType().equals(Material.AIR) && b.getBlock().getRelative(BlockFace.UP).getType().equals(Material.AIR)){
						if(rand.nextInt(100)<=20){
							b.getBlock().setType(Material.TORCH);
						}
					}
				}
			}

		}
	}

	/**
	 * adds the room hazard to the room gen
	 * 
	 */
	public boolean Hazard(Theme theme, Room r) {
		Random rand=new Random();
		Location l;
		int x,y,z;
		switch(r.getHazard()){
		case NONE:
			break;

		case LAVA:	
			y=r.lowY+1;
			for(x=r.lowX+1;x<r.highX;x++){
				for(z=r.lowZ+1;z<r.highZ;z++){
					l=new Location(r.getCenter().getWorld(),x,y,z);
					if(l.getBlock().getType()==Material.AIR){
						l.getBlock().setType(Material.LAVA);
					}
				}
			}
			return true;

		case FIRE:
			y=r.lowY+1;
			for(x=r.lowX+1;x<r.highX;x++){
				for(z=r.lowZ+1;z<r.highZ;z++){
					l=new Location(r.getCenter().getWorld(),x,y,z);
					if(rand.nextBoolean()){
						if(l.getBlock().getType()==Material.AIR){
							l.getBlock().setType(Material.FIRE);
						}else if(l.getBlock().getType()==Material.NETHERRACK){
							l.getBlock().getRelative(BlockFace.UP).setType(Material.FIRE);
						}else{
							l.getBlock().setType(Material.NETHERRACK);
							l.getBlock().getRelative(BlockFace.UP).setType(Material.FIRE);
						}
					}
				}
			}
			return true;
		case VOID:
			y=r.lowY+1;
			for(x=r.lowX+1;x<r.highX;x++){
				for(z=r.lowZ+1;z<r.highZ;z++){
					l=new Location(r.getCenter().getWorld(),x,y,z);
					if(l.getBlock().getType()==Material.AIR){
						for(y=r.lowY;y>=0;y--){
							l=new Location(r.getCenter().getWorld(),x,y,z);
							l.getBlock().setType(Material.AIR);
						}
					}
				}
			}

			return true;

		}
		return false;
	}


}

