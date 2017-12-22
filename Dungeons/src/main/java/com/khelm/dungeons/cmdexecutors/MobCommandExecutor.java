package com.khelm.dungeons.cmdexecutors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import com.khelm.dungeons.Dungeons;
import com.khelm.dungeons.mobcontrol.Mob;
import com.khelm.dungeons.mobcontrol.MobAbilities;
import com.khelm.dungeons.mobcontrol.MobGroup;
import com.khelm.dungeons.mobcontrol.MobGroupType;

public class MobCommandExecutor implements CommandExecutor {

	//	private final Dungeons plugin;//instance of plugin

	private static MobCommandExecutor cmdExec;//this
	private static HashMap<Player,ArrayList<MobGroup>> deleteQueries;

	/**
	 * constructor saves instance of plugin class
	 * @param plugin
	 */
	public MobCommandExecutor(Dungeons plugin){
		//		this.plugin=plugin;
		cmdExec=this;
		deleteQueries=new HashMap<Player,ArrayList<MobGroup>>();
	}

	@SuppressWarnings("deprecation")
	public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
		Player p=null;
		Location l=null;
		if(s instanceof Player){
			p=(Player)s;
			if(!p.isOp()){
				return false;
			}
		}else{
			return false;
		}
		Block b=p.getTargetBlock((Set)null, 100);

		if (args.length>0){
//			if(args[0].equals("findgroups")){
//				ArrayList<MobGroup>res=MobGroup.getGroupByLoc(p.getLocation());
//				
//				if(res.size()>0){
//					if(deleteQueries.containsKey(p)){
//						deleteQueries.remove(p);
//						deleteQueries.put(p, res);
//					}else{
//						deleteQueries.put(p, res);
//					}
//					for(int i=0;i<res.size();i++){
//						p.sendMessage(ChatColor.GREEN+""+i+": "+res.get(i).getName());
//					}
//					
//				}else{
//					p.sendMessage(ChatColor.RED+"No Mob Group Spawns nearby");
//				}
//				
//				return true;
//				
//				
//			}else if(args[0].equals("delete")){
//				if(args.length>1){
//					if(deleteQueries.containsKey(p)){
//						deleteQueries.get(p).get(Integer.parseInt(args[1])).delete();
//						deleteQueries.remove(p);
//					}
//					return true;
//				}
//			}else if(args[0].equals("group")){
//				
//				if(b.getRelative(BlockFace.UP).getType()==Material.AIR){
//				
//					l=b.getRelative(BlockFace.UP).getLocation();
//
//					//command is /mob group <name> <respawntime> <activation range> <freedom> <atk> <def> <effects>
//					if(args.length>=5 && MobGroupType.getByName(args[1]) instanceof MobGroupType){
//					
//						MobGroup mg=new MobGroup(MobGroupType.getByName(args[1]),p.getWorld(),Integer.parseInt(args[2])<100?false:true);
//						mg.setAtk(Double.parseDouble(args[5]));
//						mg.setDef(Double.parseDouble(args[6]));
//						mg.setResTime(Long.parseLong(args[2]));
//						mg.setActivation(Integer.parseInt(args[3]));
//						mg.setFreedom(Integer.parseInt(args[4]));
//						mg.setLoc(l);
//						if(args.length>7){
//							for(int i=7;i<args.length;i++){
//								mg.addAbility(args[i]);
//							}
//						}
//						Dungeons.getPlugin(Dungeons.class).log("activating group");
//						mg.activate();
//						
//					}
//					return true;
//				}
			//}else{
				Dungeons.getPlugin(Dungeons.class).log("no group flag");
				if(b.getRelative(BlockFace.UP).getType()==Material.AIR){
					Dungeons.getPlugin(Dungeons.class).log("target loc good");
					l=b.getRelative(BlockFace.UP).getLocation();

					ArrayList<PotionEffectType>pe=new ArrayList<PotionEffectType>();
					ArrayList<MobAbilities>ma=new ArrayList<MobAbilities>();
					//command is /mob <name> <atk> <def> <effects>
					if(args.length>=3){
						Dungeons.getPlugin(Dungeons.class).log("Args>3");
						if(args.length>3){
							for(int i=3;i<args.length;i++){
								if(PotionEffectType.getByName(args[i]) instanceof PotionEffectType){
									pe.add(PotionEffectType.getByName(args[i]));
								}else if(MobAbilities.valueOf(args[i].toUpperCase()) instanceof MobAbilities){
									ma.add(MobAbilities.valueOf(args[i].toUpperCase()));
								}
							}
						}
						Dungeons.getPlugin(Dungeons.class).log("spawning mob");
						new Mob(args[0],Float.valueOf(args[1]),Float.valueOf(args[2]),pe,ma).spawn(p.getWorld(),l.getBlockX(),l.getBlockY(),l.getBlockZ(),false);
					}
					return true;
				}
			//}
		}
		return false;
	}
	/**
	 * returns the instance used for command execution
	 * 
	 * @return
	 */
	public static MobCommandExecutor getCommandExecutor(){
		return cmdExec;
	}
}
