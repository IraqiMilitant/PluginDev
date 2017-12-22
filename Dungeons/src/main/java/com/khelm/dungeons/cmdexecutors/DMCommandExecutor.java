package com.khelm.dungeons.cmdexecutors;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;

import com.khelm.dungeons.Constants;
import com.khelm.dungeons.Dungeon;
import com.khelm.dungeons.Dungeons;
import com.khelm.dungeons.Hazard;
import com.khelm.dungeons.Raider;
import com.khelm.dungeons.Room;
import com.khelm.dungeons.Theme;
import com.khelm.dungeons.Utility;
import com.khelm.dungeons.mobcontrol.Mob;
import com.khelm.dungeons.mobcontrol.MobAbilities;
import com.khelm.dungeons.mobcontrol.MobGroup;
import com.khelm.dungeons.mobcontrol.MobGroupAbilities;
import com.khelm.dungeons.mobcontrol.MobGroupType;

/**
 * Command executor for all /DM commands
 * 
 * @author IraqiMilitant
 *
 */
public class DMCommandExecutor implements CommandExecutor {

	private Dungeons plugin;//ref to plugin instance
	private static DMCommandExecutor cmdExec;//static ref to executor
	private HashMap<Player,Dungeon>editors;//map of dungeons and their editors
	private HashMap<Player,MobGroup>mobGroupEditing;//map of dungeons and their editors
	private ArrayList<UUID>dmItems;//map of dungeons and their editors


	Dungeon d;//dungeon being worked with

	public DMCommandExecutor(Dungeons plugin){
		this.plugin=plugin;
		editors=new HashMap<Player,Dungeon>();
		mobGroupEditing=new HashMap<Player,MobGroup>();
		cmdExec=this;
		loadDMItems();

	}

	/**
	 * called whenever a DM command is run
	 */
	public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
		Player p;
		Raider r;
		Hazard h;
		if (s instanceof Player){//check if CommandSender is a player
			p=(Player)s;
			r=Raider.getRaider(p);

		}else{//if not give an error
			if(args.length==2){
				if (args[0].equals("item")){
					Player pl=Bukkit.getPlayer(args[1]);
					if(pl instanceof Player){
						if(pl.getInventory().firstEmpty()!=-1){
							ItemStack item=new ItemStack(Material.WOOD_SWORD,1);
							ItemMeta meta=item.getItemMeta();
							ArrayList<String>lore=new ArrayList<String>();
							UUID id=UUID.randomUUID();
							lore.add(Utility.packHiddenText("@"+id.toString()));
							meta.setLore(lore);
							meta.setDisplayName("Dungeon Master Wand");
							item.setItemMeta(meta);
							pl.getInventory().addItem(item);
							dmItems.add(id);
							this.saveDMItems();
							s.sendMessage("DM item added to player inventory");
							Dungeons.logToFile(pl.getDisplayName()+" has been given a DM item");
						}
					}else{
						s.sendMessage("No player by that name online");
					}
					return true;
				}
			}

			s.sendMessage("You must be a player to run that command");

			return true;
		}
		if (args.length>0){
			if(args[0].equals("info")){
				if(!editors.containsKey(p)){//check if player is in editor map
					p.sendMessage(ChatColor.RED+"You are not currently editing a dungeon.");
					return true;
				}
				p.sendMessage(ChatColor.GREEN+"--------------DUNGEON INFO--------------");
				p.sendMessage(ChatColor.GREEN+"Dungeon Name: "+ChatColor.YELLOW+editors.get(p).getName());
				p.sendMessage(ChatColor.GREEN+"Dungeon Difficulty: "+ChatColor.YELLOW+editors.get(p).evalDiff());
				if(editors.get(p).getSelectedRoom() instanceof Room){//check if they have selected a room
					p.sendMessage(ChatColor.GREEN+"---------------ROOM INFO---------------");
					editors.get(p).getSelectedRoom().printRoomInfo(p);
				}
				p.sendMessage(ChatColor.GREEN+"----------------------------------------");
				return true;

			}else if(args[0].equals("mobinfo")){
				if(!editors.containsKey(p)){//check if player is in editor map
					p.sendMessage(ChatColor.RED+"You are not currently editing a dungeon.");
					return true;
				}else if(!editors.get(p).selectedRoom()){
					p.sendMessage(ChatColor.RED+"You have not selected a room yet");
				}
				p.sendMessage(ChatColor.GREEN+"------------DETAILED MOB INFO-----------");
				for(MobGroup mg:r.getEditing().getMobGroups()){
					p.sendMessage(ChatColor.GREEN+mg.getName());
					for(MobGroupAbilities mga:mg.getAbilities()){
						p.sendMessage(ChatColor.GREEN+"    "+mga.toString());
					}
					for(MobAbilities ma:mg.getMobAbilities()){
						p.sendMessage(ChatColor.GREEN+"    "+ma.name());
					}
					for(PotionEffectType pe:mg.getMobEffects()){
						p.sendMessage(ChatColor.GREEN+"    "+pe.getName());
					}
					p.sendMessage(" ");

				}
				p.sendMessage(ChatColor.GREEN+"----------------------------------------");
				return true;
			}else if (args[0].equals("getdungeon")){//assign a dungeon
				if(p.isOp()){
					if(!editors.containsKey(p)){
						d=Dungeon.assignDungeon(p);
						if(d instanceof Dungeon){//check if assignment was successful
							d.setReady(false);//set dunegon to not ready
							p.sendMessage("Dungeon assigned");
							r.setEdits(true);
							editors.put(p, d);//if it was put the player in the map as an editor of the dungeon
							plugin.log("dungeon assigned");
						}
					}else{
						p.sendMessage(ChatColor.RED+"You are already editing a dungeon!");
					}
					return true;
				}
			}else if (args[0].equals("room")){//room selection
				if(editors.containsKey(p)){//first check if the player running the command is in the editor map
					if(args.length==2){//if the req amount of args are present
						if(args[1].equals("info")){//return list of theme, mobs, effects

						}else{//set room to edit
							//TODO: add a check for out of bounds 
							plugin.log("room selected for edit");
							p.setAllowFlight(true);
							editors.get(p).setEditing(Integer.parseInt(args[1]));
							r.setEditing(editors.get(p).getSelectedRoom());
							p.teleport(editors.get(p).getSelectedRoom().getCenter());
							if(Bukkit.getAllowFlight()){
								p.setFlying(true);
							}


						}
						return true;
					}
				}

			}else if (args[0].equals("set")){//apply edits
				if(!editors.containsKey(p)){//check if player is in editor map
					return false;
				}
				if(!(editors.get(p).getSelectedRoom() instanceof Room)){//check if they have selected a room
					return false;
				}
				if(args.length==3){//confirm args length is proper
					if(editors.get(p).selectedRoom()){
						if(args[1].equals("theme")){//set the theme

							Theme t=Theme.getTheme(args[2]);//get the theme based on the string input
							if(t instanceof Theme){//confirm if the theme exists
								plugin.log("setting theme");
								r.getEditing().setTheme(t);//set the room theme
							}else{
								p.sendMessage("That Theme does not exist");
							}
							return true;
						}else if(args[1].equals("hazard")){//set hazard
							h=Hazard.valueOf(args[2].toUpperCase());
							if(h instanceof Hazard){
								r.getEditing().setHazard(h);
							}else{
								p.sendMessage("No such hazard");
							}
							return true;
						}
					}
				}

			}else if (args[0].equals("add")){//for adding mobs and effects
				if(args.length>=3&&(editors.get(p) instanceof Dungeon)&&(editors.get(p).selectedRoom())){
					if(args[1].equals("mobs")){
						if(args[2].equals("ability")){
							if(mobGroupEditing.containsKey(p)&&args.length==4){
								mobGroupEditing.get(p).addAbility(args[3]);
								p.sendMessage("Mob ability added");
							}
						}else{
							MobGroupType mt=MobGroupType.getByName(args[2]);	
							if(mt instanceof MobGroupType){
								if(mobGroupEditing.containsKey(p)){
									mobGroupEditing.remove(p);
								}
								mobGroupEditing.put(p, new MobGroup(mt,Bukkit.getWorld(Constants.dunegonW),false));
								p.sendMessage("mob group added");
								r.getEditing().addMobGroup(mobGroupEditing.get(p));
							}
						}

					}else if(args[1].equals("effect")){//add an effect
						if(PotionEffectType.getByName(args[2]) instanceof PotionEffectType){//check if potioneffect exists based on input name
							p.sendMessage("adding effect to room: "+r.getEditing().getRoomNum());
							r.getEditing().addEffect(PotionEffectType.getByName(args[2]));//add the effect to the room
						}else{
							p.sendMessage("invalid effect");
						}

					}
					return true;
				}

			}else if (args[0].equals("remove")){
				if(args.length>=3&&(editors.get(p) instanceof Dungeon)&&(editors.get(p).selectedRoom())){
					if(args[1].equals("mobs")){
						if(args[2].toLowerCase().equals("all")){
							editors.get(p).getSelectedRoom().clearMobGroups();
							p.sendMessage("removing all mobs from room: "+r.getEditing().getRoomNum());
						}else{
							editors.get(p).getSelectedRoom().removeMobGroup(args[2]);
							p.sendMessage("removing mob from room: "+r.getEditing().getRoomNum());
						}

					}else if(args[1].equals("effect")){//remove an effect
						if(args[2].toLowerCase().equals("all")){
							editors.get(p).getSelectedRoom().clearRoomEffects();
							p.sendMessage("removing all effects from room: "+r.getEditing().getRoomNum());
						}else{
							if(PotionEffectType.getByName(args[2]) instanceof PotionEffectType){//if the name exists

								if(editors.get(p).getSelectedRoom().removeEffect(PotionEffectType.getByName(args[2]))){//try to remove the effect
									p.sendMessage("removing effect from room: "+r.getEditing().getRoomNum());
								}else{
									p.sendMessage("room "+r.getEditing().getRoomNum()+" does not have that effect assigned to it");						
								}
							}else{
								p.sendMessage("invalid effect");
							}
						}
					}
					return true;
				}
			}else if (args[0].equals("rename")){//rename the room if name not taken
				if((editors.get(p) instanceof Dungeon)&&(args.length==2)){

					editors.get(p).rename(args[1],p);
					return true;
				}
			}else if (args[0].equals("finalize")){//finalize the room and set it ready for raiders
				if(editors.get(p) instanceof Dungeon){
					if(editors.get(p).validate(p)){
						Dungeons.logToFile(p.getDisplayName()+" has finalized edits to Dungeon "+editors.get(p).getName());
						editors.get(p).setReady(true);
						editors.get(p).evalDiff();
						editors.remove(p);
						r.setEdits(false);
						Raider.getRaider(p).setEditing(null);
						p.teleport(Constants.exit);
						p.setFlying(false);
						p.setAllowFlight(false);

					}else{
						p.sendMessage(ChatColor.RED+"All rooms are not yet configured");
					}
					return true;
				}

			}else if(args[0].equals("adminassign")){
				if(p.isOp()){
					if(args.length==3){
						Player target=Bukkit.getPlayer(args[2]);
						if(target instanceof Player && !editors.containsKey(target)){
							Dungeon td=Dungeon.getDungeon(args[1]);
							if(td instanceof Dungeon){
								Dungeons.logToFile(p.getDisplayName()+" attempting to force-assign dungeon "+td.getName()+" to "+target.getDisplayName());
								td.assign(target);
								editors.put(target, td);
								target.sendMessage(ChatColor.GREEN+"You have been assigned dungeon: "+td.getName());
							}else{
								p.sendMessage(ChatColor.RED+"That dungeon does not exist!");
							}
						}else{
							p.sendMessage(ChatColor.RED+"no player by that name online! or they are already editing a dungeon");
						}
						return true;
					}
				}

			}
		}
		return false;
	}

	public boolean itemInit(Player p,ItemStack i){
		ItemMeta meta=i.getItemMeta();
		if(Raider.getRaider(p).isInDungeon()){
			return false;
		}
		if(Raider.getRaider(p).hasGroup()){
			Raider.getRaider(p).leaveGroup();
		}
		if(meta.hasDisplayName() && meta.getDisplayName().equals("Dungeon Master Wand")){
			if(meta.hasLore()&&meta.getLore().get(0).contains(ChatColor.COLOR_CHAR+"@") && dmItems.contains(UUID.fromString(Utility.unpackHiddenText(meta.getLore().get(0)).substring(1)))){
				Dungeon d=Dungeon.assignDungeon(p);
				if(d instanceof Dungeon){
					d.setReady(false);//set dunegon to not ready
					editors.put(p, d);//if it was put the player in the map as an editor of the dungeon
					plugin.log("dungeon assigned");
					p.getInventory().remove(i);
					Raider.getRaider(p).setEdits(true);
					p.sendMessage(ChatColor.GREEN+"Use /dmmenu to open the Dungeon Master menu");
					dmItems.remove(UUID.fromString(Utility.unpackHiddenText(meta.getLore().get(0)).substring(1)));
					return true;
				}
			}
		}



		return false;
	}

	/**
	 * remove a player from the editors map
	 * @param p
	 */
	public void remove(Player p){
		if(editors.containsKey(p)){
			editors.remove(p);
		}
	}


	public void saveDMItems(){
		Dungeons.getPlugin(Dungeons.class).log("Saving Dungeon");
		File dmItemList=new File(Constants.path+Constants.DMITEMS);

		try {//save the list to file
			if(!dmItemList.exists()){
				dmItemList.createNewFile();
			}
			FileOutputStream f_out=new FileOutputStream(Constants.path+Constants.DMITEMS);
			ObjectOutputStream obj_out=new ObjectOutputStream(f_out);
			obj_out.writeObject(dmItems);
			obj_out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException i) {
			// TODO Auto-generated catch block
			i.printStackTrace();
		}
	}

	private void loadDMItems(){
		Dungeons.getPlugin(Dungeons.class).log("loading DM Item List");
		dmItems=new ArrayList<UUID>();

		File dmItemList=new File(Constants.path+Constants.DMITEMS);
		ObjectInputStream obj_in;
		FileInputStream f_in;
		Object obj;
		try {
			if(dmItemList.exists()){//get dungeon list from file
				f_in=new FileInputStream(Constants.path+Constants.DMITEMS);
				obj_in=new ObjectInputStream(f_in);
				obj=obj_in.readObject();
				//if((ArrayList<?>)obj instanceof ArrayList<?>){
				dmItems=(ArrayList<UUID>)obj;//set the dungeonnames from the dungeonlist file
				//}

				for(UUID u:dmItems){
					Dungeons.getPlugin(Dungeons.class).log(u.toString());
				}

			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException i) {
			// TODO Auto-generated catch block
			i.printStackTrace();
		} catch (ClassNotFoundException o) {
			// TODO Auto-generated catch block
			o.printStackTrace();
		}
	}



	/**
	 * return the executor object
	 * 
	 * @return
	 */
	public static DMCommandExecutor getExecutor(){
		return cmdExec;
	}



}
