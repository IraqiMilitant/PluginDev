package com.khelm.dungeons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import com.khelm.dungeons.uniqueitems.armour.ArmourSet;

/**
 * Class which sits ontop of the Player class (note does not extend). Holds the player's group, dungeon and rating data
 * 
 * @author IraqiMilitant
 *
 */
public class Raider{

	private static ArrayList<Raider>raiders=new ArrayList<Raider>();
	private static ArrayList<Raider>invaders=new ArrayList<Raider>();
	private static ArrayList<Raider>invaderQueue=new ArrayList<Raider>();

	private static ArrayList<Raider>arbiters=new ArrayList<Raider>();
	private static ArrayList<Raider>arbiterQueue=new ArrayList<Raider>();

	//the corresponding player object
	private Player player;
	//group the player is in, if any
	private RaidGroup group;
	//does this player have a group?
	private boolean hasGroup=false;
	//are they currently in a dungeon
	private boolean inDungeon=false;
	//local and global rating of the player
	private int localRating=0;
	private int globalRating=0;
	//ref to the dungeon the player owns (DM edits)
	private Dungeon ownedDungeon;
	//if the player is ready to attempt a dungeon
	private boolean ready;
	//are they editing a dungeon
	private boolean edits=false;
	//current room the player is editing
	private Room editing;

	private DungeonPerks activePerk;

	private int sin;

	private boolean invading;
	private boolean arbiter;

	private boolean dead;
	
	private int lastHit=0;

	private HashMap<ArmourSet,Integer>armourSets;
	private ArrayList<PotionEffectType> armourEffects;


	/**
	 * Raider constructor, built from the Player object
	 * 
	 * @param p
	 */
	public Raider(Player p){
		player=p;
		dead=false;
		invading=false;
		raiders.add(this);
		this.activePerk=DungeonPerks.NONE;
		armourSets=new HashMap<ArmourSet,Integer>();
		inDungeon=false;
		if(p.getWorld().equals(Bukkit.getWorld(Constants.dunegonW))){
			this.updateArmour();
		}
		sin=0;
	}
	
	public void setLastHit(int id){
		this.lastHit=id;
	}
	
	public int getLastHit(){
		return lastHit;
	}

	/**
	 * used to check if the raider is editing a dungeon
	 * 
	 * @return
	 */
	public boolean getEdits(){
		return edits;
	}

	/**
	 * flag the raider as currently editing a dungeon
	 * 
	 * @param edit
	 */
	public void setEdits(boolean edit){
		if(ready && edit){
			ready=false;
		}
		edits=edit;
	}

	/**
	 * invasion status getter
	 * 
	 * @return
	 */
	public boolean getInvading(){
		return invading;
	}

	public void notifyDamaged(EntityDamageEvent event){

		activePerk.applyEffect(player, event);

	}

	public void notifyTargetted(EntityTargetEvent event){

		activePerk.applyEffect(player, event);


	}

	public DungeonPerks getPerk(){
		return activePerk;
	}

	/**
	 * toggles a given perk name on for the player
	 * 
	 * @param name
	 */
	public void togglePerk(String name){
		try{
		DungeonPerks perk=DungeonPerks.valueOf(name.toUpperCase());
		if(this.localRating>=perk.rating){
			if(perk.equals(activePerk)){
				activePerk.clearEffects(this.player);
				activePerk=DungeonPerks.NONE;
				this.player.sendMessage(ChatColor.YELLOW+perk.getName()+"Perk turned off");
				return;
			}

			perk.applyEffect(this.player);
			activePerk=perk;
			this.player.sendMessage(ChatColor.GREEN+perk.getName()+"Perk turned on");

		}else{
			this.player.sendMessage(ChatColor.RED+"You need "+ChatColor.BLUE+perk.rating+ChatColor.RED+" rating to use that perk");
		}
		}catch(Exception e){
			this.player.sendMessage(ChatColor.RED+"No such perk");
		}
	}

	/**
	 * iterates over the armour and updates for any specific set effects
	 * not currently used as armour sets are not in the loot tables yet
	 * will likely change to use the better system I made for the event plugin
	 * 
	 */
	public void updateArmour(){
		if(armourEffects instanceof ArrayList<?>){
			for(PotionEffectType pe:armourEffects){
				this.player.removePotionEffect(pe);
			}
			armourEffects.clear();
		}else{
			armourEffects=new ArrayList<PotionEffectType>();
		}
		armourSets.clear();
		ItemStack[] equip=player.getEquipment().getArmorContents();
		for(int i=0;i<equip.length;i++){
			try{
				if(!equip[i].getItemMeta().getDisplayName().contains(" of ")){
					continue;
				}
				String[] name=equip[i].getItemMeta().getDisplayName().split(" of ");
				if(name.length>1){
					ArmourSet as=ArmourSet.valueOf(name[name.length-1].toUpperCase());
					if(as instanceof ArmourSet){
						if(!armourSets.containsKey(as)){
							armourSets.put(as, 1);
						}else{
							int count=armourSets.get(as)+1;
							armourSets.remove(as);
							armourSets.put(as, count>4?4:count);
						}
					}
				}
			}catch (NullPointerException e){
				//e.printStackTrace();
				continue;
			}
		}
		for(ArmourSet a:armourSets.keySet()){
			a.ApplyEffects(this.player,armourSets.get(a));
		}

	}

	/**
	 * allows a armour set to register a potion effect as being from a armour set
	 * 
	 * @param p
	 */
	public void registerArmourEffect(PotionEffectType p){

		armourEffects.add(p);
	}

	/**
	 * armour set getter
	 * 
	 * @return
	 */
	public HashMap<ArmourSet,Integer> getArmourSets(){
		return armourSets;
	}


	/**
	 * sets the raider's SIN value, used on creation of the object and data load from DB
	 * and after any events that affect SIN
	 * 
	 * @param sin
	 * @param fromDatabase
	 */
	public void setSin(int sin,boolean fromDatabase){
		Dungeons.logToFile(this.player.getDisplayName()+"'s sin is now "+sin+", previously "+this.sin);
		this.sin=sin;
		if(!fromDatabase){
			if(sin>0){
				this.player.sendMessage(ChatColor.RED+"Your sin level is now "+sin);
			}else{
				this.player.sendMessage(ChatColor.GREEN+"Your sin level is now "+sin);
			}

			DatabaseWorker.writePlayerInfo(this);
		}
	}

	/**
	 * SIN getter
	 * 
	 * @return
	 */
	public int getSin(){
		return sin;
	}

	/**
	 * Allows changing of the invasion flag to ID when a raider is invading a dungeon
	 * as a 'bad guy'
	 * 
	 * @param i
	 */
	public void setInvading(boolean i){
		Dungeons.logToFile(this.player.getDisplayName()+" has been selected for an invasion");
		invading=i;
		if(invading&&invaderQueue.contains(this)){
			invaderQueue.remove(this);
			invaders.add(this);
			setSin(sin+10,false);
			this.updateArmour();
		}else if(!invading&&invaders.contains(this)){
			invaders.remove(this);
		}
	}

	/**
	 * Allows changing of the arbiter flag which marks when the raider is invading
	 * as an Arbiter
	 * 
	 * @param i
	 */
	public void setArbiter(boolean i){
		Dungeons.logToFile(this.player.getDisplayName()+" has been selected to be an arbiter");
		arbiter=i;
		if(arbiter&&arbiterQueue.contains(this)){
			arbiterQueue.remove(this);
			arbiters.add(this);
			setSin(sin>5?sin-5:0,false);
		}else if(!arbiter && arbiters.contains(this)){
			arbiters.remove(this);
		}
	}

	/**
	 * Arbiter flag getter
	 * 
	 * @return
	 */
	public boolean getArbiter(){
		return arbiter;
	}

	/**
	 * Used to queue for invasion  
	 * by consuming an invasion token if available
	 * 
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public boolean queueForInvade(){
		if(player.getInventory().contains(Material.IRON_INGOT)){
			ItemStack m = new ItemStack(Material.IRON_INGOT, 1);
			player.getInventory().removeItem(m);
			player.updateInventory();
			Dungeons.logToFile(this.player.getDisplayName()+" has queued to invade");
			invaderQueue.add(this);
			return true;
		}

		return false;

	}

	/**
	 * Used to queue for arbiter invasion using an invasion token
	 * 
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public boolean queueForArbiter(){
		if(player.getInventory().contains(Material.IRON_INGOT)){
			ItemStack m = new ItemStack(Material.IRON_INGOT, 1);
			player.getInventory().removeItem(m);
			player.updateInventory();
			arbiterQueue.add(this);
			Dungeons.logToFile(this.player.getDisplayName()+" has queued to be an arbiter");
			return true;
		}

		return false;

	}

	/**
	 * React to having won an invasion event
	 * 
	 */
	public void notifyInvasionWin(){
		this.setInvading(false);
		this.setArbiter(false);
		player.teleport(Constants.exit);
		if(arbiter){
			Dungeons.logToFile(this.player.getDisplayName()+" has won as arbiter");
			setSin(sin>10?sin-10:0,false);
		}else{
			Dungeons.logToFile(this.player.getDisplayName()+" has won as invader");
			setSin(sin+10,false);
		}
		for(ItemStack is:this.player.getEquipment().getArmorContents()){
			if(is instanceof ItemStack){
				if(is.getType().getMaxDurability()>0){
					is.setDurability((short)0);
				}
			}
		}

		for(ItemStack is:this.player.getInventory()){
			if(is instanceof ItemStack){
				if(is.getType().getMaxDurability()>0){
					is.setDurability((short)0);
				}
			}
		}

	}

	/**
	 * Used to assess the total value of an inventory
	 * used for loot re-rolls
	 * 
	 * @param inv
	 * @return
	 */
	public int assessInventory(Inventory inv){
		int loot=0;
		Map<Enchantment,Integer>enchants;
		if(inv.getContents().length==0){
			return 0;
		}
		for(ItemStack item:inv.getContents()){

			if(item instanceof ItemStack && item.getType()!=null){
				if(item.getItemMeta() instanceof ItemMeta){
					if(item.getItemMeta().hasDisplayName()){
						if(item.getItemMeta().getDisplayName().contains("N00B")){
							continue;
						}
					}
				}
				if(Constants.weapon_Melee.containsKey(item.getType().toString())){
					loot+=Constants.weapon_Melee.get(item.getType().toString());
					enchants=item.getEnchantments();
					if(enchants!=null && !enchants.isEmpty()){
						for(Enchantment e:enchants.keySet()){
							loot+=Constants.enchant_Melee.get(e.getName())*enchants.get(e);
						}
					}

				}else if(Constants.weapon_Ranged.containsKey(item.getType().toString())){
					loot+=Constants.weapon_Ranged.get(item.getType().toString());
					enchants=item.getEnchantments();
					if(enchants!=null && !enchants.isEmpty()){
						for(Enchantment e:enchants.keySet()){
							loot+=Constants.enchant_Ranged.get(e.getName())*enchants.get(e);
						}
					}

				}else if(Constants.armour.containsKey(item.getType().toString())){
					loot+=Constants.armour.get(item.getType().toString());
					enchants=item.getEnchantments();
					if(enchants!=null && !enchants.isEmpty()){
						for(Enchantment e:enchants.keySet()){
							loot+=Constants.enchant_Armour.get(e.getName())*enchants.get(e);
						}
					}

				}


			}	

		}

		if(loot>10){
			return loot;

		}else{

			return 0;
		}
	}

	/**
	 * Generate loot based on a rating value for either dungeon completion or
	 * loot re-roll
	 * 
	 * @param dungeonRating
	 * @param reroll
	 */
	@SuppressWarnings("deprecation")
	public void generateLoot(int dungeonRating, boolean reroll){

		Integer loot;
		if(reroll){
			Dungeons.logToFile("Generating re-roll loot for "+this.player.getDisplayName()+" rating of "+dungeonRating);
			loot=dungeonRating;
		}else{
			Dungeons.logToFile("Generating dunegon loot for "+this.player.getDisplayName()+" rating of "+dungeonRating);
			double scale=0;
			if(this.localRating>=(dungeonRating+15)){
				scale=((this.localRating-dungeonRating)/15)+1;
			}else{
				scale=1;
			}

			Constants.eco.depositPlayer(this.player.getName(), Constants.cashReward/scale);
			Dungeons.logToFile(this.player.getName()+" Has been awarded $"+Constants.cashReward/scale);
			this.player.sendMessage(ChatColor.GREEN+"You have been awarded $"+ChatColor.GOLD+Constants.cashReward/scale+ChatColor.GREEN+ " for Dungeon completion");
			loot=dungeonRating>5?dungeonRating:5;
		}
		if(this.sin>0){
			this.setSin(this.sin-1, false);
		}
		Inventory lootInv=Bukkit.createInventory(null, 54,"Loot");
		String item;
		int strength,counter;
		ItemStack input;
		int potionCount=5;
		int itemLimit=5;
		boolean hasBow=false;
		boolean hasMelee=false;
		String mod;
		boolean decider;
		Random r=new Random();//r.nextInt(High-Low) + Low;
		while(loot>0){
			int rRes;
			if(loot<5){
				if(itemLimit==0){
					this.player.openInventory(lootInv);
					return;
				}
				rRes=4;
			}else if(itemLimit==0 && potionCount>0){
				rRes=3;
			}else if(itemLimit==0 && potionCount==0){
				this.player.openInventory(lootInv);
				return;
			}else{
				rRes=r.nextInt(4-1) + 1;
			}
			//reroll?(loot>this.localRating?this.localRating:loot):loot
			//generate a unique?
			//			if(r.nextInt(100)<=10){
			//				rRes=0;
			//			}
			item=null;
			counter=0;
			switch(rRes){
			case 0://needs work before use
				if(itemLimit!=0){
					item=getRand(reroll?(loot>this.localRating?this.localRating:loot):loot,0,Constants.unique_Armour);
					if(item!=null){
						input=ArmourSet.getItem(item);
						while(loot>6){
							mod=null;
							if(r.nextBoolean()||r.nextBoolean()){
								mod=getEnchant(input.getType().toString(),reroll?(loot>this.localRating?this.localRating:loot):loot,0,Constants.enchant_Armour);
							}
							if(mod!=null){
								strength=enchantStrength(mod,reroll?(loot>this.localRating?this.localRating:loot):loot,Constants.enchant_Armour.get(mod));
								loot=loot-Constants.enchant_Armour.get(mod)*(strength);
								input.addUnsafeEnchantment(Enchantment.getByName(mod), strength);
							}else{
								break;
							}
						}

						itemLimit--;
						lootInv.addItem(input.clone());
						break;
					}
				}
				counter++;

			case 1:

				if(itemLimit>0&&!(hasBow&&hasMelee)){
					item=null;
					decider=false;
					if((r.nextBoolean()||hasBow)&&!hasMelee){
						item=getRand(loot>this.localRating?this.localRating:loot,0,Constants.weapon_Melee);//:loot,0,Constants.weapon_Melee);//loot>=10?loot/2:0
						decider=true;
					}else if(!hasBow){
						item=getRand(loot>this.localRating?this.localRating:loot,0,Constants.weapon_Ranged);
						decider=false;
					}
					if(item!=null){

						input=new ItemStack(Material.getMaterial(item));
						hasMelee=true;
						if(decider){
							loot=loot-Constants.weapon_Melee.get(item);
							while(loot>6){
								mod=null;
								if(r.nextBoolean()||r.nextBoolean()){
									mod=getEnchant(item,reroll?(loot>this.localRating?this.localRating:loot):loot,0,Constants.enchant_Melee);
								}
								if(mod!=null){
									strength=enchantStrength(mod,reroll?(loot>this.localRating?this.localRating:loot):loot,Constants.enchant_Melee.get(mod));
									loot=loot-Constants.enchant_Melee.get(mod)*(strength);
									input.addUnsafeEnchantment(Enchantment.getByName(mod), strength);
								}else{
									break;
								}
							}
						}else{
							loot=loot-Constants.weapon_Ranged.get(item);
							hasBow=true;
							while(loot>6){
								mod=null;
								if(r.nextBoolean()||r.nextBoolean()){
									mod=getEnchant(item,reroll?(loot>this.localRating?this.localRating:loot):loot,0,Constants.enchant_Ranged);
								}
								if(mod!=null){
									strength=enchantStrength(mod,reroll?(loot>this.localRating?this.localRating:loot):loot,Constants.enchant_Ranged.get(mod));
									loot=loot-Constants.enchant_Ranged.get(mod)*(strength);
									input.addUnsafeEnchantment(Enchantment.getByName(mod), strength);
								}else{
									break;
								}
							}
						}
						itemLimit--;
						lootInv.addItem(input.clone());
						counter++;
						break;
					}else{
						counter++;
					}
				}
			case 2:
				if(itemLimit>0){
					item=getRand(loot>this.localRating?this.localRating:loot,0,Constants.armour);
					if(item!=null){
						loot=loot-Constants.armour.get(item);
						input=new ItemStack(Material.getMaterial(item));
						while(loot>6){
							mod=null;
							if(r.nextBoolean()||r.nextBoolean()){
								mod=getEnchant(item,reroll?(loot>this.localRating?this.localRating:loot):loot,0,Constants.enchant_Armour);
							}
							if(mod!=null){
								strength=enchantStrength(mod,reroll?(loot>this.localRating?this.localRating:loot):loot,Constants.enchant_Armour.get(mod));
								loot=loot-Constants.enchant_Armour.get(mod)*(strength);
								input.addUnsafeEnchantment(Enchantment.getByName(mod), strength);
							}else{
								break;
							}
						}
						itemLimit--;
						lootInv.addItem(input.clone());
						counter++;
						break;
					}else{
						counter++;
					}
				}
			case 3:
				if(potionCount>0){
					if(r.nextBoolean()){
						item=getRand(loot,0,Constants.potion_Normal);
						decider=true;
					}else{
						item=getRand(loot,0,Constants.potion_Splash);
						decider=false;
					}
					if(item!=null && PotionEffectType.getByName(item)!=null){
						int[]pMod=new int[]{1,0};


						if(decider){
							loot=loot-Constants.potion_Normal.get(item);
							if(loot>Constants.potion_Normal.get(item)){
								pMod=potionStrength(item,reroll?(loot>this.localRating?this.localRating:loot):loot,Constants.potion_Normal.get(item));
								loot=loot-((Constants.potion_Normal.get(item)*(pMod[0]-1))+(Constants.potion_Normal.get(item)*pMod[1]));
							}
							input=new ItemStack(Material.POTION,1);
						}else{
							loot=loot-Constants.potion_Splash.get(item);
							if(loot>Constants.potion_Splash.get(item)){
								pMod=potionStrength(item,reroll?(loot>this.localRating?this.localRating:loot):loot,Constants.potion_Splash.get(item));
								loot=loot-((Constants.potion_Splash.get(item)*(pMod[0]-1))+(Constants.potion_Splash.get(item)*pMod[1]));
							}
							input=new ItemStack(Material.SPLASH_POTION,1);
						}

						//Potion p=new Potion(PotionType.valueOf(item),pMod[0]);
						PotionEffect pe=new PotionEffect(PotionEffectType.getByName(item),pMod[0],pMod[1]);
						PotionMeta meta=(PotionMeta)input.getItemMeta();
						meta.addCustomEffect(pe, true);
						input.setItemMeta(meta);
						
//						
//						if(pMod[1]==1){
//							//p.extend();
//						}
//						if(!decider){
//							//p.setSplash(true);
//						}
						//input=p.toItemStack(1);
						potionCount--;
						lootInv.addItem(input.clone());
						counter++;
						break;
					}else{
						counter++;
					}
				}
			case 4:
				if(itemLimit>0){
					item=getRand(reroll?(loot>this.localRating?this.localRating:loot):loot,0,Constants.item_Misc);
					if(item!=null){//&& !(item.equals("IRON_INGOT")&&reroll))
						loot=loot-Constants.item_Misc.get(item);

						if(loot<5 && loot>=1){
							input=new ItemStack(Material.getMaterial(item),1+loot);
							if(item.equals("IRON_INGOT")){
								ItemMeta meta=input.getItemMeta();
								meta.setDisplayName("Invasion Token");
								input.setItemMeta(meta);
							}
							lootInv.addItem(input.clone());
							this.player.openInventory(lootInv);
							return;

						}else{
							input=new ItemStack(Material.getMaterial(item),1);
						}
						itemLimit--;
						if(item.equals("IRON_INGOT")){
							ItemMeta meta=input.getItemMeta();
							meta.setDisplayName("Invasion Token");
							input.setItemMeta(meta);
						}
						lootInv.addItem(input.clone());
						break;
					}else{
						counter++;
					}
				}
			}
			if(counter>=4){
				this.player.openInventory(lootInv);
				return;
			}
		}

		this.player.openInventory(lootInv);
	}

	/**
	 * Will return an enchantment name for an item based on a min and max value and the item
	 * 
	 * @param item
	 * @param max
	 * @param min
	 * @param list
	 * @return
	 */
	private String getEnchant(String item,int max,int min,HashMap<String,Integer>list){
		Random r=new Random();
		ArrayList<String>options=new ArrayList<String>();
		for(String k:list.keySet()){
			if(list.get(k)<=max &&list.get(k)>=min && Enchantment.getByName(k).canEnchantItem(new ItemStack(Material.getMaterial(item)))){
				options.add(k);
			}
		}
		if(options.size()==0){
			return null;
		}else{
			return options.get(r.nextInt(options.size()));
		}
	}


	/**
	 * determines strength of a potion
	 * 
	 * @param name
	 * @param lootVal
	 * @param rating
	 * @return
	 */
	private int[] potionStrength(String name,int lootVal,int rating){
		Random r=new Random();
		int loot=lootVal;
		boolean extend=false;
		int[]i=new int[]{1,0};
		while(true){
			if(loot>rating){
				if(r.nextBoolean()){
					loot-=rating;
					if(r.nextBoolean()&&PotionType.valueOf(name).isUpgradeable()&&PotionType.valueOf(name).getMaxLevel()>i[0]){
						i[0]++;
					}else if(!PotionType.valueOf(name).isInstant()&&PotionType.valueOf(name).isExtendable()&&!extend){
						extend=true;
						i[1]++;
					}else{
						return i;
					}

				}else{
					return i;
				}
			}else{
				return i;
			}
		}


	}

	/**
	 * Determine strength of a given enchant based on total rating and how many points
	 * can be used for this enchant
	 * 
	 * @param name
	 * @param lootVal
	 * @param rating
	 * @return
	 */
	private int enchantStrength(String name,int lootVal,int rating){
		int counter=1;
		int loot=lootVal-rating;
		Random r=new Random();
		while(true){
			if(loot>rating){
				if(r.nextBoolean()){//&&counter<Enchantment.getByName(name).getMaxLevel()
					counter++;
					loot-=rating;
				}else{
					return counter;
				}
			}else{
				return counter;
			}
		}
	}

	/**
	 * Gets a random item within a value range from a given HashMap
	 * 
	 * @param max
	 * @param min
	 * @param list
	 * @return
	 */
	private String getRand(int max,int min,HashMap<String,Integer>list){
		Random r=new Random();
		ArrayList<String>options=new ArrayList<String>();
		for(String k:list.keySet()){
			if(list.get(k)<=max &&list.get(k)>=min){
				options.add(k);
			}
		}
		if(options.size()==0){
			return null;
		}else{
			return options.get(r.nextInt(options.size()));
		}
	}

	/**
	 * Dead getter
	 * 
	 * @return
	 */
	public boolean getDead(){
		return dead;
	}

	/**
	 * Dead setter
	 * 
	 * @param dead
	 */
	public void setDead(boolean dead){
		this.dead=dead;
	}

	/**
	 * gets the player's Local rating
	 * 
	 * @return
	 */
	public int getLocalRating(){
		return localRating;
	}

	/**
	 * gets the player's Global rating
	 * 
	 * @return
	 */
	public int getGlobalRating(){
		return globalRating;
	}

	/**
	 * set the current room the player is editing
	 * 
	 * @param r
	 */
	public void setEditing(Room r){
		editing=r;
		if(r instanceof Room){
			r.clearRoom(false);
		}
	}

	/**
	 * get the room the player is editing if any
	 * 
	 * @return
	 */
	public Room getEditing(){
		return editing;
	}

	/**
	 * get the corresponding player object
	 * 
	 * @return
	 */
	public Player getPlayer(){
		return player;
	}

	/**
	 * get the player's group
	 * 
	 * @return
	 */
	public RaidGroup getGroup(){
		return group;
	}

	/**
	 * check if the player has a group
	 * 
	 * @return
	 */
	public boolean hasGroup(){
		return hasGroup;
	}

	/**
	 * Get the player's name
	 * 
	 * @return
	 */
	public String getName(){
		return player.getName();
	}

	/**
	 * set the player's group
	 * 
	 * @param group
	 */
	public void setGroup(RaidGroup group){
		this.group=group;
		ready=false;
		hasGroup=true;
	}

	/**
	 * player leaves current group
	 * 
	 */
	public void leaveGroup(){
		if(hasGroup){
			group.leaveGroup(this);
			this.group=null;
			if(inDungeon){
				exitDungeon();
			}
			hasGroup=false;
		}
	}

	/**
	 * toggle player ready status
	 */
	public void setReady(){
		ready=!ready;
	}

	/**
	 * get the player's ready status
	 * 
	 * @return
	 */
	public boolean getReady(){
		return ready;
	}

	/**
	 *gives the player a ref to the dungeon assigned them through the DM system
	 * 
	 * @param dungeon
	 */
	public void giveDungeon(Dungeon dungeon){
		Dungeons.logToFile(this.player.getDisplayName()+" has been assigned dungeon "+dungeon.getName());
		ownedDungeon=dungeon;
	}

	/**
	 * gets the owned dungeon
	 * 
	 * @return
	 */
	public Dungeon dungeonOwned(){
		return ownedDungeon;
	}

	/**
	 * sets the local rating of the player, and saves the change to the database
	 * 
	 * @param lr
	 * @param fromDB
	 */
	public void setLocalRating(int lr, boolean fromDB){
		Dungeons.logToFile(this.player.getDisplayName()+"'s rating set to "+lr+", previously "+this.localRating);
		localRating=lr;
		if(hasGroup){
			group.updateRating();
		}
		if(!fromDB){
			this.player.sendMessage(ChatColor.GREEN+" Your rating is now "+ChatColor.GOLD+localRating);
			DatabaseWorker.writePlayerInfo(this);
		}
	}

	/**
	 * set the global rating of the player and write the change to database
	 * 
	 * @param gr
	 * @param fromDB
	 */
	public void setGlobalRating(int gr,boolean fromDB){
		globalRating=gr;
		if(!fromDB){
			DatabaseWorker.writePlayerInfo(this);
		}
	}

	/**
	 * return whether or not the player is in a dungeon
	 * 
	 * @return
	 */
	public boolean isInDungeon(){
		return inDungeon;
	}

	/**
	 * set the player in a Dungeon
	 */
	public void enterDungeon(){

		this.getPlayer().sendMessage("You are entering a dungeon");
		Dungeons.logToFile(this.player.getDisplayName()+" is entering a dungeon");
		if(invaderQueue.contains(this)){
			invaderQueue.remove(this);
		}
		invading=false;
		inDungeon=true;
	}

	public boolean deQueue(){
		if(arbiterQueue.contains(this)){
			arbiterQueue.remove(this);
			Dungeons.logToFile(this.player.getDisplayName()+" is no longer queued to be an arbiter");
			return true;
		}
		if (invaderQueue.contains(this)){
			Dungeons.logToFile(this.player.getDisplayName()+" is no longer queued to be an arbiter");
			invaderQueue.remove(this);
			return true;
		}
		return false;
	}

	/**
	 * remove player from a dungeon by setting bool and
	 * teleporting them to the hub
	 */
	public void exitDungeon(){
		Dungeons.logToFile(this.player.getDisplayName()+" is leaving a dungeon");
		this.getPlayer().teleport(Constants.exit);
		this.getPlayer().setHealth(20);
		this.getPlayer().setFoodLevel(20);
		inDungeon=false;
	}

	/**
	 * when the player logs off ref to them is removed from the Raider list
	 */
	public void logOff(){
		raiders.remove(this);
		if(invaders.contains(this)){
			invaders.remove(this);
		}

		if(invaderQueue.contains(this)){
			invaderQueue.remove(this);
		}
		if(arbiterQueue.contains(this)){
			arbiterQueue.remove(this);
		}

	}

	/**************************************
	 * 
	 * STATIC METHODS
	 * 
	 **************************************/

	/**
	 * get a raider based on a player object
	 * 
	 * @param p
	 * @return
	 */
	public static Raider getRaider(Player p){
		for(Raider r: raiders){
			if(r.getPlayer()==p){
				return r;
			}
		}

		return null;
	}

	/**
	 * Get the raider list
	 * 
	 * @return
	 */
	public static ArrayList<Raider> getRaiders(){
		return raiders;
	}

	/**
	 * Invader list getter
	 * 
	 * @return
	 */
	public static ArrayList<Raider> getInvaders(){
		return invaders;
	}

	/**
	 * Arbiter list getter
	 * 
	 * @return
	 */
	public static ArrayList<Raider> getArbiters(){
		return arbiters;
	}

	/**
	 * Invader Queue getter
	 * 
	 * @return
	 */
	public static ArrayList<Raider> getInvaderQueue(){
		return invaderQueue;
	}

	/**
	 * Arbiter queue getter
	 * 
	 * @return
	 */
	public static ArrayList<Raider> getArbiterQueue(){
		return arbiterQueue;
	}

}
