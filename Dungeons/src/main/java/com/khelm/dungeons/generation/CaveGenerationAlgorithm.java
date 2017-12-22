package com.khelm.dungeons.generation;

import java.io.Serializable;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.material.Torch;

import com.khelm.dungeons.Constants;
import com.khelm.dungeons.Room;
import com.khelm.dungeons.Theme;
import com.khelm.dungeons.ThemeBlock;

/**
 * Generation Algorithm for a more true-cave style
 * 
 * @author IraqiMilitant
 *
 */
public class CaveGenerationAlgorithm implements GenerationAlgorithm, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8169008090279501665L;

	public CaveGenerationAlgorithm(){

	}

	@SuppressWarnings("deprecation")
	public void Generate(Theme t,Room r){
		int min=4;
		int max=9;
		boolean flag;
		Location c=r.getCenter();
		Location ex=r.getExit();
		Location en=r.getEntry();
		//first fill room with highest weight block from theme
		ThemeBlock fill=null;
		for(ThemeBlock tb:t.getBlocks(false)){
			if(fill instanceof ThemeBlock){
				if(tb.getWeight()>fill.getWeight()){
					fill=tb;
				}
			}else{
				fill=tb;
			}
		}
		Location l;
		for(int x=r.lowX;x<=r.highX;x++){
			for(int y=r.lowY-1;y<=r.highY;y++){
				for(int z=r.lowZ;z<=r.highZ;z++){
					l=new Location(c.getWorld(),x,y,z);
					flag=true;
					for (Room rm:r.getDungeon().getRooms()){
						if(!rm.equals(r)){
							if(rm.isInside(l)){
								if(rm.getRoomNum()<r.getRoomNum()){
									flag=false;
								}
							}
						}
					}
					if(flag){//test me!!
						if(l.getBlockX()==r.highX||l.getBlockY()==r.lowY||l.getBlockY()==r.highY||l.getBlockZ()==r.lowZ||l.getBlockZ()==r.highZ){

							t.generateBlock(l, true,false,r);

						}else{

							l.getBlock().setType(Material.getMaterial(fill.getId()));
						}
					}


				}
			}
		}


		caveIterator(en,c,max,min,r,t);
		caveIterator(c,ex,max,min,r,t);

	}

	/**
	 * Iterates through the roomfrom start through the center to stop triggering slice generation
	 * 
	 * @param start
	 * @param stop
	 * @param axisX
	 * @param max
	 * @param min
	 * @param r
	 * @param t
	 */
	private void caveIterator(Location start,Location stop,int max,int min,Room r,Theme t){
		Random rand=new Random();
		int decider;
		int actual= rand.nextInt((max - min) + 1) + min;
		Location l=new Location(start.getWorld(),start.getX(),start.getY(),start.getZ());
		do{
			caveSlice(l,t,r,actual);
			decider=rand.nextInt((max - min) + 1) + min;
			if(decider<actual){
				actual-=2;
			}else if(decider>actual){
				actual+=2;
			}
			if(l.getBlockY()<stop.getBlockY()){
				l.setY(l.getBlockY()+1);
			}else if(l.getBlockY()>stop.getBlockY()){
				l.setY(l.getBlockY()-1);
			}

			if(l.getBlockZ()<stop.getBlockZ()){
				l.setZ(l.getBlockZ()+1);
			}else if(l.getBlockZ()>stop.getBlockZ()){
				l.setZ(l.getBlockZ()-1);
			}
			if(l.getBlockX()<stop.getBlockX()){
				l.setX(l.getBlockX()+1);
			}else if(l.getBlockX()>stop.getBlockX()){
				l.setX(l.getBlockX()-1);
			}
		}while(l.distance(stop)>1.9);
	}


	/**
	 * generates a slice around a given location
	 * 
	 * @param l location around which to generate
	 * @param t theme being used for generation
	 * @param r room that contains this location
	 * @param actual current radius to guarantee air around the location
	 */
	private void caveSlice(Location l,Theme t,Room r,int actual){
		boolean place;
		int xBoundL,yBoundL,zBoundL;
		int xBoundH,yBoundH,zBoundH;
		Location b;
		int x,y,z;
		//low bounds
		xBoundL=l.getBlockX()-actual-1;
		if(xBoundL<r.lowX){
			xBoundL=r.lowX;
		}
		yBoundL=l.getBlockY()-actual-1;
		if(yBoundL<r.lowY){
			yBoundL=r.lowY;
		}
		zBoundL=l.getBlockZ()-actual-1;
		if(zBoundL<r.lowZ){
			zBoundL=r.lowZ;
		}
		//high bounds
		xBoundH=l.getBlockX()+actual+1;
		if(xBoundH>r.highX){
			xBoundH=r.highX;
		}
		yBoundH=l.getBlockY()+actual+1;
		if(yBoundH>r.highY){
			yBoundH=r.highY;
		}
		zBoundH=l.getBlockZ()+actual+1;
		if(zBoundH>r.highZ){
			zBoundH=r.highZ;
		}

		for(x=xBoundL;x<=xBoundH;x++){
			for(y=yBoundL;y<=yBoundH;y++){
				for(z=zBoundL;z<zBoundH;z++){
					b=new Location(l.getWorld(),x,y,z);

					place=false;
					if(!(b.getBlock().getType()==Material.AIR)){
						if((b.distance(l)<actual || b.distance(r.getCenter())<actual)|| (b.distance(r.getExit())<actual)|| (b.distance(r.getEntry())<actual)){
							if(!(b.getBlockX()==r.lowX||b.getBlockX()==r.highX||b.getBlockY()==r.lowY||b.getBlockY()==r.highY||b.getBlockZ()==r.lowZ||b.getBlockZ()==r.highZ)){
								b.getBlock().setType(Material.AIR);
							}else{
								place=true;
							}
						}else if(b.distance(l)>actual){
							place=true;
						}

					}else if((b.getBlockX()==r.lowX||b.getBlockX()==r.highX||b.getBlockY()==r.lowY||b.getBlockY()==r.highY||b.getBlockZ()==r.lowZ||b.getBlockZ()==r.highZ)){
						place=true;
						//break;
					}else{
						place=false;
					}
					if(place||b.getBlockX()==r.lowX||b.getBlockX()==r.highX||b.getBlockY()==r.lowY||b.getBlockY()==r.highY||b.getBlockZ()==r.lowZ||b.getBlockZ()==r.highZ){
						if(!(b.getBlockX()<r.lowX||b.getBlockX()>r.highX||b.getBlockY()<r.lowY||b.getBlockY()>r.highY||b.getBlockZ()<r.lowZ||b.getBlockZ()>r.highZ)){

							t.generateBlock(b, true,false,r);


						}
					}

				}

			}
		}
	}

	public void genTorches(Theme t,Room r){
		Random rand=new Random();
		if(t.getTorches()){
			int y=r.getCenter().getBlockY();
			for(int x=r.lowX;x<=r.highX;x++){
				for(int z=r.lowZ;z<=r.highZ;z++){
					Location b=new Location(Bukkit.getWorld(Constants.dunegonW),x,y,z);
					if(b.getBlock().getType().equals(Material.AIR) && 
							(b.getBlock().getRelative(BlockFace.UP).getType().equals(Material.AIR))&&
							(b.getBlock().getRelative(BlockFace.NORTH).getType().equals(Material.AIR)||
									b.getBlock().getRelative(BlockFace.SOUTH).getType().equals(Material.AIR)||
									b.getBlock().getRelative(BlockFace.EAST).getType().equals(Material.AIR)||
									b.getBlock().getRelative(BlockFace.WEST).getType().equals(Material.AIR))){
						if(rand.nextBoolean()){
							Torch to=new Torch(); 

							if(!b.getBlock().getRelative(BlockFace.DOWN).getType().equals(Material.AIR)){
								b.getBlock().setType(Material.TORCH);
							}else if(!b.getBlock().getRelative(BlockFace.EAST).getType().equals(Material.AIR)){
								to.setFacingDirection(BlockFace.WEST);
								b.getBlock().setTypeIdAndData(Material.TORCH.getId(),to.getData(), false);

							}else if(!b.getBlock().getRelative(BlockFace.WEST).getType().equals(Material.AIR)){
								to.setFacingDirection(BlockFace.EAST);
								b.getBlock().setTypeIdAndData(Material.TORCH.getId(),to.getData(), false);

							}else if(!b.getBlock().getRelative(BlockFace.NORTH).getType().equals(Material.AIR)){
								to.setFacingDirection(BlockFace.SOUTH);
								b.getBlock().setTypeIdAndData(Material.TORCH.getId(),to.getData(), false);

							}else if(!b.getBlock().getRelative(BlockFace.SOUTH).getType().equals(Material.AIR)){
								to.setFacingDirection(BlockFace.NORTH);
								b.getBlock().setTypeIdAndData(Material.TORCH.getId(),to.getData(), false);

							}

						}
					}
				}
			}

		}
	}

	public boolean Hazard(Theme theme, Room r) {
		Random rand=new Random();
		Location l;
		int x,y,z;
		switch(r.getHazard()){
		case NONE:
			break;

		case LAVA:	
			for(y=r.lowY;y<r.lowY+3;y++){
				for(x=r.lowX+1;x<r.highX;x++){
					for(z=r.lowZ+1;z<r.highZ;z++){
						l=new Location(r.getCenter().getWorld(),x,y,z);
						if((l.getBlock().getRelative(BlockFace.UP).getType().equals(Material.AIR))
								&&!(l.getBlock().getRelative(BlockFace.WEST).getType().equals(Material.AIR))
								&&!(l.getBlock().getRelative(BlockFace.NORTH).getType().equals(Material.AIR))
								&&!(l.getBlock().getRelative(BlockFace.SOUTH).getType().equals(Material.AIR))
								&&!(l.getBlock().getRelative(BlockFace.EAST).getType().equals(Material.AIR))){
							if(rand.nextBoolean()){
								l.getBlock().setType(Material.LAVA);
							}
						}
					}
				}
			}
			return true;

		case FIRE:
			for(y=r.lowY;y<r.lowY+3;y++){
				for(x=r.lowX+1;x<r.highX;x++){
					for(z=r.lowZ+1;z<r.highZ;z++){
						l=new Location(r.getCenter().getWorld(),x,y,z);
						if(!((l.getBlock().getType().equals(Material.AIR))&& !(l.getBlock().getType().equals(Material.FIRE)))
								&&(l.getBlock().getRelative(BlockFace.UP).getType().equals(Material.AIR))
								&&(!(l.getBlock().getRelative(BlockFace.WEST).getType().equals(Material.AIR))&&!(l.getBlock().getRelative(BlockFace.WEST).getType().equals(Material.FIRE)))
								&&(!(l.getBlock().getRelative(BlockFace.NORTH).getType().equals(Material.AIR))&&!(l.getBlock().getRelative(BlockFace.NORTH).getType().equals(Material.FIRE)))
								&&(!(l.getBlock().getRelative(BlockFace.SOUTH).getType().equals(Material.AIR))&&!(l.getBlock().getRelative(BlockFace.SOUTH).getType().equals(Material.FIRE)))
								&&(!(l.getBlock().getRelative(BlockFace.EAST).getType().equals(Material.AIR))&&!(l.getBlock().getRelative(BlockFace.EAST).getType().equals(Material.FIRE)))){
							if(rand.nextBoolean()){
								if(l.getBlock().getType()==Material.NETHERRACK){
									l.getBlock().getRelative(BlockFace.UP).setType(Material.FIRE);
								}else{
									l.getBlock().setType(Material.NETHERRACK);
									l.getBlock().getRelative(BlockFace.UP).setType(Material.FIRE);
								}
							}
						}
//						if(rand.nextBoolean()){
//							if(l.getBlock().getType()==Material.AIR){
//								l.getBlock().setType(Material.FIRE);
//							}else if(l.getBlock().getType()==Material.NETHERRACK){
//								l.getBlock().getRelative(BlockFace.UP).setType(Material.FIRE);
//							}else{
//								l.getBlock().setType(Material.NETHERRACK);
//								l.getBlock().getRelative(BlockFace.UP).setType(Material.FIRE);
//							}
//						}
					}
				}
			}
			return true;
		case VOID:

			return false;

		}
		return false;
	}

}
