package com.khelm.dungeons.cmdexecutors;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.khelm.dungeons.Constants;
import com.khelm.dungeons.Dungeons;
import com.khelm.dungeons.Raider;

public class GearCommandExecutor implements CommandExecutor{
	
	private static HashMap<Inventory,Player>lootRollInvs;
	private static HashMap<Player,ItemStack>repairQueries;
	public GearCommandExecutor(Dungeons plugin){
		//this.plugin=plugin;
		lootRollInvs=new HashMap<Inventory,Player>();
		repairQueries=new HashMap<Player,ItemStack>();
	}

	/**
	 * runs for every /gear command
	 */
	@SuppressWarnings("deprecation")
	public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
		
		Player p=null;
		if (s instanceof Player){//confirm sender is player
			p=(Player)s;
			if(!p.getWorld().equals(Bukkit.getWorld(Constants.dunegonW))){
				s.sendMessage(ChatColor.RED+"You can only run that command in the Dungeon World");
				return true;
			}

		}else{
			s.sendMessage("You must be a player to run that command");
			return false;
		}
		
		if(args.length>0){
			if(args[0].equals("noob")){
				ItemStack item=new ItemStack(Material.WOOD_SWORD,1);
				item.addEnchantment(Enchantment.DURABILITY, 3);
				ItemMeta meta=item.getItemMeta();
				meta.setDisplayName("N00B Sword");
				item.setItemMeta(meta);
				p.getInventory().addItem(item);
				return true;
				
			}else if(args[0].equals("roll")){
				Inventory inv=Bukkit.createInventory(null, 54,"Loot Re-Roll");
				lootRollInvs.put(inv, p);
				p.openInventory(inv);
				return true;
			}else if(args[0].equals("repair")){
				if(args.length>1){
					if(args[1].equals("confirm")){
						if(repairQueries.containsKey(p)){
							Constants.eco.withdrawPlayer(p.getName(), repairAppraisal(repairQueries.get(p)));
							ItemStack i=repairQueries.get(p);
							i.setDurability((short)0);
							repairQueries.remove(p);
						}
						
					}
					return true;
				}
				if(Raider.getRaider(p).isInDungeon()){
					p.sendMessage(ChatColor.RED+"You cannot repair items while in a dungeon");
					return true;
				}
				ItemStack item=p.getItemInHand();
				if(item instanceof ItemStack){
					String name=item.getType().toString();
					if(Constants.weapon_Melee.containsKey(name)||Constants.weapon_Ranged.containsKey(name)||Constants.armour.containsKey(name)){
						int quote=repairAppraisal(item);
						if(Constants.eco.getBalance(p.getDisplayName())>quote){
							if(repairQueries.containsKey(p)){
								repairQueries.remove(p);
							}
							repairQueries.put(p, item);
							p.sendMessage(ChatColor.GREEN+"To repair that item it will cost: "+ChatColor.GOLD+quote);
							p.sendMessage(ChatColor.GREEN+"Use "+ChatColor.BLUE+"/gear repair confirm"+ChatColor.GREEN+" to confirm the repair");
						}else{
							if(repairQueries.containsKey(p)){
								repairQueries.remove(p);
							}
							p.sendMessage(ChatColor.RED+"Repairing that item would cost $"+ChatColor.GOLD+quote+ChatColor.RED+". You do not have enough");
						}
						
					}else{
						if(repairQueries.containsKey(p)){
							repairQueries.remove(p);
						}
						p.sendMessage(ChatColor.RED+"That item cannot be repaired");
					}
				}else{
					if(repairQueries.containsKey(p)){
						repairQueries.remove(p);
					}
					p.sendMessage(ChatColor.RED+"You are not holding anything");
				}
				
				return true;
			}else if(args[0].equals("trash")){
				Inventory inv=Bukkit.createInventory(null, 54,"Trash");
				p.openInventory(inv);
				return true;
			}
		}
		
		
		return false;
		
	}
	
	private int repairAppraisal(ItemStack item){
		int value=0;
	
		Map<Enchantment,Integer>enchants;
			if(item instanceof ItemStack && item.getType()!=null){
				if(Constants.weapon_Melee.containsKey(item.getType().toString())){
					value+=Constants.weapon_Melee.get(item.getType().toString());
					enchants=item.getEnchantments();
					if(enchants!=null && !enchants.isEmpty()){
						for(Enchantment e:enchants.keySet()){
							value+=Constants.enchant_Melee.get(e.getName())*enchants.get(e);
						}
					}

				}else if(Constants.weapon_Ranged.containsKey(item.getType().toString())){
					value+=Constants.weapon_Ranged.get(item.getType().toString());
					enchants=item.getEnchantments();
					if(enchants!=null && !enchants.isEmpty()){
						for(Enchantment e:enchants.keySet()){
							value+=Constants.enchant_Ranged.get(e.getName())*enchants.get(e);
						}
					}

				}else if(Constants.armour.containsKey(item.getType().toString())){
					value+=Constants.armour.get(item.getType().toString());
					enchants=item.getEnchantments();
					if(enchants!=null && !enchants.isEmpty()){
						for(Enchantment e:enchants.keySet()){
							value+=Constants.enchant_Armour.get(e.getName())*enchants.get(e);
						}
					}

				}
		}
		return value;
	}
	
	public static HashMap<Inventory,Player> getLootRolls(){
		return lootRollInvs;
	}
	
	public static void removeInv(Inventory inv){
		if(lootRollInvs.containsKey(inv)){
			lootRollInvs.remove(inv);
		}
	}
	
	public static void playerMove(Player p){
		if(repairQueries.containsKey(p)){
			repairQueries.remove(p);
		}
	}


}
