package vanillaamplified.combat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Animals;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Player;
import org.bukkit.entity.Shulker;
import org.bukkit.entity.Silverfish;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.SkeletonHorse;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Snowman;
import org.bukkit.entity.Wither;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import vanillaamplified.main.VanillaMain;

public class CombatMain implements Listener{

	static Random random = new Random();
	static VanillaMain main = VanillaMain.instance;
	public static int attackWait = 500;
	
	static List<AttackInstance> attackInstances = new ArrayList<AttackInstance>();
	public static HashMap<String, Integer> clicksLastSecond = new HashMap<String, Integer>();
	public static HashMap<String, Integer> clicksThisSecond = new HashMap<String, Integer>();
	
	public static HashMap<String, BlockInstance> lastBlock = new HashMap<String, BlockInstance>();
	static HashMap<String, ChatColor> colors = new HashMap<String, ChatColor>();
	
	 // finish getting hit mobs based on weapon, what happens if hit, and what happens if blocked
	// Add trident attack method!!!!
	
	static List<Player> blockingPlayers = new ArrayList<Player>();
	
	public static void understandCommand(CommandSender sender, Command command, String label, String[] args) {
		if(sender instanceof Player) {
			Player p = (Player) sender;
			String arg1 = args[0];
			if(arg1.equals("changeblockcolor")) {
				if(args.length > 1) {
					String color = args[1];
					ChatColor c = getColor(color);
					if(c != null) {
						colors.put(p.getName(), c);
						p.sendMessage(ChatColor.GRAY + "" + ChatColor.BOLD + "Changed blocking text color to : " + ChatColor.RESET + "" + c + "" + color);
					}
					else {
						p.sendMessage(ChatColor.BOLD + "" + ChatColor.RED + "That color does not exist!");
						p.sendMessage(ChatColor.GRAY + "Possible Colors : dark_purple, light_purple, gold, red, green, yellow,");
						p.sendMessage(ChatColor.GRAY + "aqua, black, blue, dark_aqua, dark_blue, dark_gray, dark_green, white,");
						p.sendMessage(ChatColor.GRAY + "dark_red, gray.");
					}
				}
			}
		}
	}
	
	public static ChatColor getColor(String c) {
		if(c.toLowerCase().equals("aqua")) {
			return ChatColor.AQUA;
		}
		else if(c.toLowerCase().equals("black")) {
			return ChatColor.BLACK;
		}
		else if(c.toLowerCase().equals("blue")) {
			return ChatColor.BLUE;
		}
		else if(c.toLowerCase().equals("dark_aqua")) {
			return ChatColor.DARK_AQUA;
		}
		else if(c.toLowerCase().equals("dark_blue")) {
			return ChatColor.DARK_BLUE;
		}
		else if(c.toLowerCase().equals("dark_gray")) {
			return ChatColor.DARK_GRAY;
		}
		else if(c.toLowerCase().equals("dark_green")) {
			return ChatColor.DARK_GREEN;
		}
		else if(c.toLowerCase().equals("dark_purple")) {
			return ChatColor.DARK_PURPLE;
		}
		else if(c.toLowerCase().equals("dark_red")) {
			return ChatColor.DARK_RED;
		}
		else if(c.toLowerCase().equals("gold")) {
			return ChatColor.GOLD;
		}
		else if(c.toLowerCase().equals("gray")) {
			return ChatColor.GRAY;
		}
		else if(c.toLowerCase().equals("green")) {
			return ChatColor.GREEN;
		}
		else if(c.toLowerCase().equals("light_purple")) {
			return ChatColor.LIGHT_PURPLE;
		}
		else if(c.toLowerCase().equals("red")) {
			return ChatColor.RED;
		}
		else if(c.toLowerCase().equals("white")) {
			return ChatColor.WHITE;
		}
		else if(c.toLowerCase().equals("yellow")) {
			return ChatColor.YELLOW;
		}
		return null;
	}
	
	public static boolean noFlag(Location l, String flag) {
		if(main.wgm != null) {
			return main.wgm.noFlag(l, flag);
		}
		return true;
	}
	
	public static boolean isBlocking(Player p) {
		ItemStack itemInHand = p.getInventory().getItemInMainHand();
		if(itemInHand != null) {
			String type = itemInHand.getType().name().toLowerCase();
			if(type.contains("sword")||type.contains("shield")||type.contains("axe")||type.contains("shovel")||type.contains("hoe")||type.contains("stick")||type.contains("rod")) {
				if(lastBlock.containsKey(p.getName())) {
					BlockInstance current = lastBlock.get(p.getName());
					Long startBlock = current.blockTime;
					long currentTime = System.currentTimeMillis();
					if(currentTime - startBlock < 280) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public static void showBlocking() {
		List<Player> blockingPlayersC = new ArrayList<Player>(blockingPlayers);
		for(Player p : blockingPlayersC) {
			if(isBlocking(p)) {
				if(colors.containsKey(p.getName())) {
					p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.BOLD + "" + colors.get(p.getName()) + "Blocking..."));
				}
				else {
					p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.BOLD + "" + ChatColor.GRAY + "Blocking..."));
				}
			}
			else {
				removeBlockingPlayer(p);
			}
		}
	}
	
	public static void removeBlockingPlayer(Player p) {
		p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(""));
		blockingPlayers.remove(p);
	}
	
	public static void runQuickly() {
		iterateThroughAttacks();
		checkNanos();
		showBlocking();
	}
	
	@EventHandler
	public void onPlayerSwipe(PlayerInteractEvent e) {
		/*if(e.getAction() == Action.LEFT_CLICK_AIR) {
			if(lastBlock.containsKey(e.getPlayer().getName())) {
				lastBlock.get(e.getPlayer().getName()).blockAmmount = 0;
				lastBlock.get(e.getPlayer().getName()).blockTime = 0;
			}
			String playerName = e.getPlayer().getName();
			if(clicksThisSecond.containsKey(playerName)) {
				int clicks = clicksThisSecond.get(playerName) + 1;
				clicksThisSecond.put(playerName, clicks);
			}
			else {
				clicksThisSecond.put(playerName, 1);
			}
		}*/
		if(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			ItemStack itemInHand = e.getPlayer().getInventory().getItemInMainHand();
			if(itemInHand != null) {
				String type = itemInHand.getType().name().toLowerCase();
				if(type.contains("sword")||type.contains("shield")||type.contains("axe")||type.contains("shovel")||type.contains("hoe")||type.contains("stick")||type.contains("rod")) {
					if(lastBlock.containsKey(e.getPlayer().getName())) {
						BlockInstance current = lastBlock.get(e.getPlayer().getName());
						Long startBlock = current.blockTime;
						long currentTime = System.currentTimeMillis();
						if(currentTime - startBlock < 280) {
							current.blockTime = currentTime;
							current.blockAmmount = current.blockAmmount + 1;
						}
						else {
							current.blockTime = currentTime;
							current.blockAmmount = 0;
							if(type.contains("wood") || type.contains("stick")) {
								e.getPlayer().getWorld().playSound(e.getPlayer().getLocation(), Sound.ITEM_ARMOR_EQUIP_CHAIN, 1, 2);
							}
							else if(type.contains("stone")) {
								e.getPlayer().getWorld().playSound(e.getPlayer().getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1, 2);
							}
							else if(!type.contains("shield")){
								e.getPlayer().getWorld().playSound(e.getPlayer().getLocation(), Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1, 2);
							}
							if(!blockingPlayers.contains(e.getPlayer())) {
								blockingPlayers.add(e.getPlayer());
							}
						}
					}
					else {
						BlockInstance instance = new BlockInstance(e.getPlayer());
						lastBlock.put(e.getPlayer().getName(), instance);
						if(!blockingPlayers.contains(e.getPlayer())) {
							blockingPlayers.add(e.getPlayer());
						}
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerMoveHand(PlayerInteractEvent e) {
		if(e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) {
			AttackInstance at = new AttackInstance(e.getPlayer(), null);
			at.e2 = e;
			nanoInstances.add(at);
		}
	}
	
	@EventHandler
	public void onPlayerHith(EntityDamageByEntityEvent e) {
		if(e.isCancelled() == false) {
		if(e.getDamage() <= 0) {
			if(e.getDamage() == 0) {
				
			}
			else {
				if(e.getDamager() instanceof Player) {
					e.setDamage(e.getDamage()*-1);
				}
			}
		}
		else {
		Entity damager = e.getDamager();
		Entity damaged = e.getEntity();
		if((damager instanceof Player) && (damaged instanceof LivingEntity)) {
			e.setCancelled(true);
			AttackInstance at = new AttackInstance(((Player) damager), (LivingEntity) damaged);
			at.e1 = e;
			nanoInstances.add(at);
		}
		}
		}
	}
	
	static List<AttackInstance> nanoInstances = new ArrayList<AttackInstance>();
	
	public static void checkNanos() {
		List<AttackInstance> nanos = new ArrayList<AttackInstance>(nanoInstances);
		List<AttackInstance> nanosFull = new ArrayList<AttackInstance>(nanoInstances);
		List<AttackInstance> removed = new ArrayList<AttackInstance>();
		nanoInstances.clear();
		for(AttackInstance at : nanos) {
			long currentTime = System.currentTimeMillis();
			for(AttackInstance at2 : nanos) {
				if(at2 != at && (!(removed.contains(at2)))) {
					if(at.attacker == at2.attacker) {
						long atTime = currentTime - at.timeStarted;
						long at2Time = currentTime - at2.timeStarted;
						if(atTime <= 30 && at2Time <= 30) {
							nanosFull.remove(at);
							removed.add(at);
						}
					}
				}
			}
		}
		for(AttackInstance at : nanosFull) {
			leftClickEvent(at, at.e1, at.e2);
		}
	}
	
	public static void leftClickEvent(AttackInstance at, EntityDamageByEntityEvent e1, PlayerInteractEvent e2) {
		if(lastBlock.containsKey(at.attacker.getName())) {
			lastBlock.get(at.attacker.getName()).blockAmmount = 0;
			lastBlock.get(at.attacker.getName()).blockTime = 0;
			if(blockingPlayers.contains(at.attacker)) {
				blockingPlayers.remove(at.attacker);
				removeBlockingPlayer(at.attacker);
			}
		}
		String playerName = at.attacker.getName();
		if(clicksThisSecond.containsKey(playerName)) {
			int clicks = clicksThisSecond.get(playerName) + 1;
			clicksThisSecond.put(playerName, clicks);
		}
		else {
			clicksThisSecond.put(playerName, 1);
		}
		int clicks = 1;
		if(clicksLastSecond.containsKey(playerName)) {
			clicks = clicksLastSecond.get(playerName);
		}
		at.clicks = clicks;
		HashMap<LivingEntity, AttackInstance> hitEntities = getHitEntities(at);
		createAttackInstances(hitEntities, at.attacker);
	}

	public static void calculateClicks() {
		HashMap<String, Integer> clicksThisSecondTemp = new HashMap<String, Integer>(clicksThisSecond);
		clicksThisSecond.clear();
		for(String name : clicksThisSecondTemp.keySet()) {
			int clicks = clicksThisSecondTemp.get(name);
			clicksLastSecond.clear();
			clicksLastSecond.put(name, clicks);
		}
	}
	
	public int getLastClicks(String playerName) {
		if(clicksLastSecond.containsKey(playerName)) {
			return clicksLastSecond.get(playerName);
		}
		return 0;
	}
	
	public static void createAttackInstances(HashMap<LivingEntity, AttackInstance> entities, Player attacker) {
		int clicks = 1;
		if(clicksLastSecond.containsKey(attacker.getName())) {
			clicks = clicksLastSecond.get(attacker.getName());
		}
		int chance = (10+(5*(clicks - 5)))/5;//(((int) (100-((clicks*clicks)/2.5))) / 10);
		if(clicks <= 4) {
			chance = 1;
		}
		else if(chance <= 0) {
			chance = 90000;
		}
		if(random.nextInt(chance)==0) {
			for(LivingEntity le : entities.keySet()) {
				if(le instanceof Player) {
					if(noFlag(le.getLocation(), "PVP")) {
						AttackInstance instance = new AttackInstance(attacker, (Player) le);
						instance.damage = entities.get(le).damage;
						instance.bodyPartHit = entities.get(le).bodyPartHit;
						instance.heightHit = entities.get(le).heightHit;
						attackInstances.add(instance);
					}
				}
				else {
					boolean damageEntity = true;
					if(le instanceof Animals) {
						if(!noFlag(le.getLocation(), "DAMAGE_ANIMALS")) {
							damageEntity = false;
						}
					}
					if(damageEntity) {
						wasHit(entities.get(le), entities.get(le).attacker, le);
					}
				}
			}
		}
	}
	
	public static void iterateThroughAttacks() {
		List<AttackInstance> attackInstancesCopies = new ArrayList<AttackInstance>(attackInstances);
		for(AttackInstance at : attackInstancesCopies) {
			if(at.attacked instanceof Player) {
			long currentTime = System.currentTimeMillis();
			long lastBlockTime = lastBlock.get(at.attacked.getName()).blockTime;
			
			boolean blocked = false;
			if(currentTime - at.timeStarted >= attackWait) {
				attackInstances.remove(at);
				wasHit(at, at.attacker, at.attacked);
			}
			else {
			if((currentTime - lastBlockTime <= 300) || ((HumanEntity) at.attacked).isBlocking()) {
				if((lastBlock.get(at.attacked.getName()).blockAmmount >= 1) || ((HumanEntity) at.attacked).isBlocking()) {
					if(wasBlocked(at.attacker, (Player) at.attacked)) {
						blocked = true;
						attackInstances.remove(at);
						runBlock(at, at.attacker, (Player) at.attacked, (((HumanEntity) at.attacked).isBlocking()));
					}
				}
			}
			}
			}
		}
	}
	
	public static boolean wasBlocked(Player hitter, Player blocker) {
		Location attacker = hitter.getLocation().clone();
		Location attacked = blocker.getLocation().clone();
		attacker.setY(0);
		attacked.setY(0);
		Vector v = attacked.getDirection().setY(0);
		Vector vector = attacker.toVector().subtract(attacked.toVector());
		float angle = attacker.toVector().subtract(attacked.toVector()).angle(v);
		if(angle < 1.6) {
			return true;
		}
		return false;
	}
	
	public static void runBlock(AttackInstance at, Player attacker, Player attacked, boolean shield) {
		applyKnockback(at, attacked, .2, .1, at.damage, attacker);
		if(shield) {
			attacked.getWorld().playSound(attacked.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1, 1);
		}
		else {
			attacked.getWorld().playSound(attacked.getLocation(), Sound.BLOCK_ANVIL_HIT, 1, 2);
		}
	}

	public static HashMap<LivingEntity, AttackInstance> getHitEntities(AttackInstance at){
		HashMap<LivingEntity, AttackInstance> hitEntities = new HashMap<LivingEntity, AttackInstance>();
		List<LivingEntity> entitiesNear = new ArrayList<LivingEntity>(); 
		ItemStack held = at.attackerWeapon;
		boolean fists = false;
		if(held != null) {
			String type = held.getType().name().toLowerCase();
			if(type.contains("sword")||type.contains("axe")||type.contains("shovel")||type.contains("hoe")) {
				if(type.contains("hoe")) {
					hitEntities = getHoeEntities(at.attacker, at);
				}
				else if(type.contains("pickaxe")) {
					hitEntities = getPickaxeEntities(at.attacker, at);
				}
				else if(type.contains("axe")) {
					hitEntities = getAxeEntities(at.attacker, at);
				}
				else if(type.contains("shovel")) {
					hitEntities = getShovelEntities(at.attacker, at);
				}
				else if(type.contains("sword")) {
					hitEntities = getSwordEntities(at.attacker, at);
				}
			}
			else {
				fists = true;
			}
		}
		else {
			fists = true;
		}

		if(fists) {
			hitEntities = getFistEntities(at.attacker, at);
		}
		
		// find damages for each entity
		for(LivingEntity le : hitEntities.keySet()) {
			hitEntities.get(le).clicks = at.clicks;
		}
		
		return hitEntities;
	}
	
	public static boolean canPassThrough(Location l1, Location l2, int distance){
		boolean empty = true;
		List<Block> blocks = new ArrayList<Block>();
		Vector v = l1.clone().add(0, 1, 0).toVector().subtract(l2.clone().add(0, 1, 0).toVector()).normalize();
		final BlockIterator bit = new BlockIterator(l2.getWorld(), l2.clone().add(0, 1, 0).toVector(), v, 0, (int) distance);//distance + 1

		while (bit.hasNext()) {
			Block b = bit.next();
			if(b != null) {
				if(!b.isPassable() && (!(b.getType().name().toLowerCase().contains("air")))) {
					return false;
				}
			}
		}
		return empty;
	}
	
	
	
	
	
	//to do
	// finish block method. /
	// make indicator for player when blocking /
	// in get damage method, reduce or increase damage based on mob type weakness to weapon used.
	// in wasHit method, work on increasing or decreasing chance of effect based on weapon used.
	// see if hoe entity detection can be improved.
	// improve performance and optimization.
	// add impact, block, and slash sounds
	// knockback based on damage dealt to mob /
	// add blood effects on hits /
		//- Make only hit body part bleed /
		//- Make bleed if damage is high only or if using sharp weapon /
	
	public static void applyKnockback(AttackInstance at, LivingEntity le, double knockbackXAmmount, double knockbackYAmmount, double damage, LivingEntity hitter) {
		double fullDamage = 2;
		if(at.attackerWeapon != null) {
			fullDamage = getDamageItem(at.attackerWeapon.getType().name().toLowerCase());
		}
		else {
			fullDamage = 2;
		}
		double multiplier = (damage / fullDamage);
		Location l1 = le.getLocation().clone();
		Location l2 = at.attackerLocation.clone();
		Vector knockback = new Vector();
		knockback = l1.toVector().subtract(l2.toVector()).normalize();
		knockback.multiply((.20*(multiplier)) + knockbackXAmmount).add(new Vector(0, (0.08*(multiplier))+knockbackYAmmount, 0));
		knockback.add(le.getVelocity());
		le.setVelocity(knockback);
	}
	
	public static void damageEvent(AttackInstance at, LivingEntity le, Player p, double damage, int bodyPartHit) {
		applyKnockback(at, le, 0, 0, damage, p);
		double fullDamage = 2;
		String type = "fist";
		if(at.attackerWeapon != null) {
			type = at.attackerWeapon.getType().name().toLowerCase();
			fullDamage = getDamageItem(type);
		}
		else {
			fullDamage = 2;
		}
		if((damage > (fullDamage + 1)) || type.contains("sword") || type.contains("hoe") || type.contains("shovel")) {
			bleedEntity(le, at.bodyPartHit);
		}
	}
	
	public static void bleedEntity(LivingEntity le, int bodyPart) {
		// 1==head 2==torso 3==legs
		BoundingBox bb = le.getBoundingBox();
		double boundY = (bb.getCenterY() + .148) - (bb.getHeight()/2.0);
		EntityType type = le.getType();
		
		if(le instanceof Zombie) {
			if(((Zombie) le).isBaby()) {
				le.getWorld().playSound(le.getLocation(), Sound.BLOCK_LAVA_POP, 1, 2);
				double heightAdd = bb.getHeight() / 2.0;
				double width = (bb.getWidthX() + bb.getWidthZ()) / 2.0;
				BlockData bd = getBlood(le);
				le.getWorld().spawnParticle(Particle.BLOCK_CRACK, le.getLocation().add(0, heightAdd, 0), 25, width/3.0, heightAdd/2.0, width/3.0, 0.005, bd);	
				return;
			}
		}
		
		Location bottom = le.getLocation().clone();  bottom.setY(boundY);
		double heightAdd = 0;
		double heightDiff = 0;
		if(type == EntityType.ZOMBIE || type == EntityType.SKELETON || type == EntityType.STRAY) {
			double headHeight = 1.55;
			double torsoHeight = .9;
			if(bodyPart == 1) {
				heightDiff = ((bb.getHeight() - headHeight) / 2.0);
				bottom.add(0, (headHeight + ((bb.getHeight() - headHeight) / 2.0)), 0);
			}
			else if(bodyPart == 2) {
				heightDiff = ((headHeight - torsoHeight) / 2.0);
				bottom.add(0, (((headHeight - torsoHeight) / 2.0) + torsoHeight), 0);
			}
			else if(bodyPart == 3) {
				heightDiff = (torsoHeight / 2.0);
				bottom.add(0, (torsoHeight / 2.0), 0);
			}
		}
		else if(type == EntityType.IRON_GOLEM) {
			double headHeight = 2.2;
			double torsoHeight = 1.2;
			if(bodyPart == 1) {
				heightDiff = ((bb.getHeight() - headHeight) / 2.0);
				bottom.add(0, (headHeight + ((bb.getHeight() - headHeight) / 2.0)), 0);
			}
			else if(bodyPart == 2) {
				heightDiff = ((headHeight - torsoHeight) / 2.0);
				bottom.add(0, (((headHeight - torsoHeight) / 2.0) + torsoHeight), 0);
			}
			else if(bodyPart == 3) {
				heightDiff = (torsoHeight / 2.0);
				bottom.add(0, (torsoHeight / 2.0), 0);
			}
		}
		else if(type == EntityType.DROWNED) {
			double headHeight = 1.45;
			double torsoHeight = .81;
			if(bodyPart == 1) {
				heightDiff = ((bb.getHeight() - headHeight) / 2.0);
				bottom.add(0, (headHeight + ((bb.getHeight() - headHeight) / 2.0)), 0);
			}
			else if(bodyPart == 2) {
				heightDiff = ((headHeight - torsoHeight) / 2.0);
				bottom.add(0, (((headHeight - torsoHeight) / 2.0) + torsoHeight), 0);
			}
			else if(bodyPart == 3) {
				heightDiff = (torsoHeight / 2.0);
				bottom.add(0, (torsoHeight / 2.0), 0);
			}
		}
		else if(type == EntityType.SNOWMAN) {
			double headHeight = 1.35;
			double torsoHeight = .1;
			if(bodyPart == 1) {
				heightDiff = ((bb.getHeight() - headHeight) / 2.0);
				bottom.add(0, (headHeight + ((bb.getHeight() - headHeight) / 2.0)), 0);
			}
			else if(bodyPart == 2) {
				heightDiff = ((headHeight - torsoHeight) / 2.0);
				bottom.add(0, (((headHeight - torsoHeight) / 2.0) + torsoHeight), 0);
			}
		}
		else if(type == EntityType.HUSK || type == EntityType.PIG_ZOMBIE) {
			double headHeight = 1.6;
			double torsoHeight = .9;
			if(bodyPart == 1) {
				heightDiff = ((bb.getHeight() - headHeight) / 2.0);
				bottom.add(0, (headHeight + ((bb.getHeight() - headHeight) / 2.0)), 0);
			}
			else if(bodyPart == 2) {
				heightDiff = ((headHeight - torsoHeight) / 2.0);
				bottom.add(0, (((headHeight - torsoHeight) / 2.0) + torsoHeight), 0);
			}
			else if(bodyPart == 3) {
				heightDiff = (torsoHeight / 2.0);
				bottom.add(0, (torsoHeight / 2.0), 0);
			}
		}
		else if(le instanceof Player) {
			double headHeight = 1.4;
			double torsoHeight = .75;
			if(bodyPart == 1) {
				heightDiff = ((bb.getHeight() - headHeight) / 2.0);
				bottom.add(0, (headHeight + ((bb.getHeight() - headHeight) / 2.0)), 0);
			}
			else if(bodyPart == 2) {
				heightDiff = ((headHeight - torsoHeight) / 2.0);
				bottom.add(0, (((headHeight - torsoHeight) / 2.0) + torsoHeight), 0);
			}
			else if(bodyPart == 3) {
				heightDiff = (torsoHeight / 2.0);
				bottom.add(0, (torsoHeight / 2.0), 0);
			}
		}
		else if(type == EntityType.BLAZE) {
			double headHeight = 1.2;
			double torsoHeight = .01;
			if(bodyPart == 1) {
				heightDiff = ((bb.getHeight() - headHeight) / 2.0);
				bottom.add(0, (headHeight + ((bb.getHeight() - headHeight) / 2.0)), 0);
			}
			else if(bodyPart == 2) {
				heightDiff = ((headHeight - torsoHeight) / 2.0);
				bottom.add(0, (((headHeight - torsoHeight) / 2.0) + torsoHeight), 0);
			}
		}
		else if(type == EntityType.CREEPER) {
			double headHeight = 1.2;
			double torsoHeight = .5;
			if(bodyPart == 1) {
				heightDiff = ((bb.getHeight() - headHeight) / 2.0);
				bottom.add(0, (headHeight + ((bb.getHeight() - headHeight) / 2.0)), 0);
			}
			else if(bodyPart == 2) {
				heightDiff = ((headHeight - torsoHeight) / 2.0);
				bottom.add(0, (((headHeight - torsoHeight) / 2.0) + torsoHeight), 0);
			}
			else if(bodyPart == 3) {
				heightDiff = (torsoHeight / 2.0);
				bottom.add(0, (torsoHeight / 2.0), 0);
			}
		}
		else if(type == EntityType.ENDERMAN) {
			double headHeight = 2.3;
			double torsoHeight = 1.7;
			if(bodyPart == 1) {
				heightDiff = ((bb.getHeight() - headHeight) / 2.0);
				bottom.add(0, (headHeight + ((bb.getHeight() - headHeight) / 2.0)), 0);
			}
			else if(bodyPart == 2) {
				heightDiff = ((headHeight - torsoHeight) / 2.0);
				bottom.add(0, (((headHeight - torsoHeight) / 2.0) + torsoHeight), 0);
			}
			else if(bodyPart == 3) {
				heightDiff = (torsoHeight / 2.0);
				bottom.add(0, (torsoHeight / 2.0), 0);
			}
		}
		else if(type == EntityType.WITHER_SKELETON) {
			double headHeight = 1.8;
			double torsoHeight = 1.1;
			if(bodyPart == 1) {
				heightDiff = ((bb.getHeight() - headHeight) / 2.0);
				bottom.add(0, (headHeight + ((bb.getHeight() - headHeight) / 2.0)), 0);
			}
			else if(bodyPart == 2) {
				heightDiff = ((headHeight - torsoHeight) / 2.0);
				bottom.add(0, (((headHeight - torsoHeight) / 2.0) + torsoHeight), 0);
			}
			else if(bodyPart == 3) {
				heightDiff = (torsoHeight / 2.0);
				bottom.add(0, (torsoHeight / 2.0), 0);
			}
		}
		else if(type == EntityType.EVOKER || type == EntityType.WITCH || type == EntityType.VILLAGER || type == EntityType.WANDERING_TRADER || type == EntityType.PILLAGER
				|| type == EntityType.ILLUSIONER || type == EntityType.ZOMBIE_VILLAGER || type == EntityType.VINDICATOR) {
			double headHeight = 1.2;
			double torsoHeight = .5;
			if(bodyPart == 1) {
				heightDiff = ((bb.getHeight() - headHeight) / 2.0);
				bottom.add(0, (headHeight + ((bb.getHeight() - headHeight) / 2.0)), 0);
			}
			else if(bodyPart == 2) {
				heightDiff = ((headHeight - torsoHeight) / 2.0);
				bottom.add(0, (((headHeight - torsoHeight) / 2.0) + torsoHeight), 0);
			}
			else if(bodyPart == 3) {
				heightDiff = (torsoHeight / 2.0);
				bottom.add(0, (torsoHeight / 2.0), 0);
			}
		}
		else {
			le.getWorld().playSound(le.getLocation(), Sound.BLOCK_LAVA_POP, 1, 2);
			double heightAdd2 = bb.getHeight() / 2.0;
			double width = (bb.getWidthX() + bb.getWidthZ()) / 2.0;
			BlockData bd = getBlood(le);
			le.getWorld().spawnParticle(Particle.BLOCK_CRACK, le.getLocation().add(0, heightAdd2, 0), 25, width/3.0, heightAdd2/2.0, width/3.0, 0.005, bd);	
			return;
		}
		le.getWorld().playSound(le.getLocation(), Sound.BLOCK_LAVA_POP, 1, 2);
		double width = (bb.getWidthX() + bb.getWidthZ()) / 2.0;
		BlockData bd = getBlood(le);
		le.getWorld().spawnParticle(Particle.BLOCK_CRACK, bottom, 25, width/2.7, heightDiff, width/2.7, 0.005, bd);
	}
	
	public static int getBodyPart(AttackInstance at, LivingEntity le) {
		// 1==head 2==torso 3==legs
		int part = 2;
		BoundingBox bb = at.bb.clone();
		double boundY = (bb.getCenterY() + .148) - (bb.getHeight()/2.0);
		double yValue = at.heightHit - boundY;
		if(yValue <= 0) {
			return 3;
		}
		if(le instanceof Zombie) {
			if(((Zombie) le).isBaby()) {
				return 2;
			}
		}
		EntityType type = le.getType();
		if(type == EntityType.ZOMBIE || type == EntityType.SKELETON || type == EntityType.STRAY) {
			if(yValue > 1.55) {
				return 1;
			}
			else if(yValue > .9) {
				return 2;
			}
			else {
				return 3;
			}
		}
		else if(type == EntityType.DROWNED) {
			if(yValue > 1.45) {
				return 1;
			}
			else if(yValue > .81) {
				return 2;
			}
			else {
				return 3;
			}
		}
		else if(type == EntityType.IRON_GOLEM) {
			if(yValue > 2.2) {
				return 1;
			}
			else if(yValue > 1.2) {
				return 2;
			}
			else {
				return 3;
			}
		}
		else if(type == EntityType.SNOWMAN) {
			if(yValue > 1.35) {
				return 1;
			}
			else {
				return 2;
			}
		}
		else if(type == EntityType.HUSK || type == EntityType.PIG_ZOMBIE) {
			if(yValue > 1.6) {
				return 1;
			}
			else if(yValue > .9) {
				return 2;
			}
			else {
				return 3;
			}
		}
		else if(le instanceof Player) {
			if(yValue > 1.4) {
				return 1;
			}
			else if(yValue > .75) {
				return 2;
			}
			else {
				return 3;
			}
		}
		else if(type == EntityType.BLAZE) {
			if(yValue > 1.2) {
				return 1;
			}
			else {
				return 2;
			}
		}
		else if(type == EntityType.CREEPER) {
			if(yValue > 1.2) {
				return 1;
			}
			else if(yValue > .5) {
				return 2;
			}
			else {
				return 3;
			}
		}
		else if(type == EntityType.ENDERMAN) {
			if(yValue > 2.3) {
				return 1;
			}
			else if(yValue > 1.7) {
				return 2;
			}
			else {
				return 3;
			}
		}
		else if(type == EntityType.WITHER_SKELETON) {
			if(yValue > 1.8) {
				return 1;
			}
			else if(yValue > 1.1) {
				return 2;
			}
			else {
				return 3;
			}
		}
		else if(type == EntityType.EVOKER || type == EntityType.WITCH || type == EntityType.VILLAGER || type == EntityType.WANDERING_TRADER || type == EntityType.PILLAGER
				|| type == EntityType.ILLUSIONER || type == EntityType.ZOMBIE_VILLAGER || type == EntityType.VINDICATOR) {
			if(yValue > 1.2) {
				return 1;
			}
			else if(yValue > .5) {
				return 2;
			}
			else {
				return 3;
			}
		}
		return part;
	}
	
	public static BlockData getBlood(LivingEntity le) {
		BlockData bd = Material.NETHER_WART_BLOCK.createBlockData();
		if(le instanceof Creeper) {
			bd = Material.LIME_CONCRETE_POWDER.createBlockData();
		}
		else if(le instanceof WitherSkeleton || le instanceof Wither) {
			bd = Material.COAL_BLOCK.createBlockData();
		}
		else if(le instanceof Skeleton || le instanceof SkeletonHorse) {
			bd = Material.BONE_BLOCK.createBlockData();
		}
		else if(le instanceof MagmaCube) {
			bd = Material.MAGMA_BLOCK.createBlockData();
		}
		else if(le instanceof Blaze) {
			bd = Material.FIRE.createBlockData();
		}
		else if(le instanceof Slime) {
			bd = Material.SLIME_BLOCK.createBlockData();
		}
		else if(le instanceof Shulker) {
			bd = Material.SHULKER_BOX.createBlockData();
		}
		else if(le instanceof IronGolem) {
			bd = Material.IRON_BLOCK.createBlockData();
		}
		else if(le instanceof Snowman) {
			bd = Material.SNOW_BLOCK.createBlockData();
		}
		return bd;
	}
	
	@EventHandler
	public void onMobSpawn(EntitySpawnEvent e) {
		if(e.getEntity() instanceof LivingEntity) {
			((LivingEntity) e.getEntity()).getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(50);
		}
	}
	
	@EventHandler
	public void respawnEvent(PlayerRespawnEvent e) {
		if(e.getPlayer() != null) {
			e.getPlayer().getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(50);
		}
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		if(e.getPlayer() != null) {
			e.getPlayer().getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(50);
		}
	}
	
	public static HashMap<LivingEntity, AttackInstance> getShovelEntities(Player p, AttackInstance atC) {
		Location pLoc = p.getLocation().clone();
		Location pEyeLoc = p.getEyeLocation().clone();
		if(atC.attacker != null) {
			pLoc = atC.attackerLocation.clone();
			pEyeLoc = atC.attackerEyeLocation.clone();
		}
		HashMap<LivingEntity, AttackInstance> nearestEntity = new HashMap<LivingEntity, AttackInstance>();
		
		for(Entity e : p.getNearbyEntities(6, 6, 6)) {
			if(e instanceof LivingEntity) {
				LivingEntity le = (LivingEntity) e;
				double distance = Math.sqrt(e.getLocation().distanceSquared(pLoc.clone().add(0, (26*0.06), 0)));
				if(distance <= 5.6) {
					Location eye = pEyeLoc.clone();
					Vector toEntity = le.getEyeLocation().toVector().subtract(eye.toVector());
					double dot = toEntity.normalize().dot(eye.getDirection());
					double fov = .50;
					//check if in hoe's fov radius
					if(dot > fov) {
						BoundingBox bb = le.getBoundingBox();
						double height = bb.getHeight();
						double width = bb.getWidthX() + .2;
						double length = bb.getWidthZ() + .2;
						Location center = new Location(le.getWorld(), bb.getCenterX(), bb.getCenterY() + .148, bb.getCenterZ());
						Location lowerLocation = center.clone().subtract(width/2.0, height/2.0, length/2.0);
						Location higherLocation = lowerLocation.clone().add(width, height, length);
						
						Vector v = pLoc.getDirection();
						for (double d = 0; d < (distance + 1); d += 0.05) {
							final double dd = d;
							Location current = pEyeLoc.clone().add(0, .2, 0).add(v.clone().multiply(dd));	
							if(Math.max(lowerLocation.getX(), higherLocation.getX()) >= current.getX() && Math.min(lowerLocation.getX(), higherLocation.getX()) <= current.getX()) {
								if(Math.max(lowerLocation.getY(), higherLocation.getY()) >= current.getY() && Math.min(lowerLocation.getY(), higherLocation.getY()) <= current.getY()) {
									if(Math.max(lowerLocation.getZ(), higherLocation.getZ()) >= current.getZ() && Math.min(lowerLocation.getZ(), higherLocation.getZ()) <= current.getZ()) {
								Location modifiedLe = current.clone();
								if(!blockInWay(pEyeLoc.clone(), modifiedLe)) {
									AttackInstance at = new AttackInstance(p, le);
									at.heightHit = current.getY();
									at.clicks = atC.clicks;
									at.damage = getFullDamage(at, p, le);
									nearestEntity.put(le, at);
								}
								break;
							}
							}
						}
					}
				}
			}
		}
		}
		return nearestEntity;
	}
		
	public static HashMap<LivingEntity, AttackInstance> getAxeEntities(Player p, AttackInstance atC) {
		Location pLoc = atC.attackerLocation.clone();
		Location pEyeLoc = atC.attackerEyeLocation.clone();
		if(pLoc == null) {
			pLoc = p.getLocation().clone();
			pEyeLoc = p.getEyeLocation().clone();
		}
		HashMap<LivingEntity, AttackInstance> nearestEntity = new HashMap<LivingEntity, AttackInstance>();
		LivingEntity nearest = null;
		double nearestLength = 999999;
		double yValue = 0;
		
		for(Entity e : p.getNearbyEntities(6, 6, 6)) {
			if(e instanceof LivingEntity) {
				LivingEntity le = (LivingEntity) e;
				double distance = Math.sqrt(e.getLocation().distanceSquared(pLoc.clone().add(0, (26*0.06), 0)));
				if(distance <= 5.6) {
					Location eye = pEyeLoc.clone();
					Vector toEntity = le.getEyeLocation().toVector().subtract(eye.toVector());
					double dot = toEntity.normalize().dot(eye.getDirection());
					double fov = .50;
					//check if in hoe's fov radius
					if(dot > fov) {
						BoundingBox bb = le.getBoundingBox();
						double height = bb.getHeight();
						double width = bb.getWidthX() + .2;
						double length = bb.getWidthZ() + .2;
						Location center = new Location(le.getWorld(), bb.getCenterX(), bb.getCenterY() + .148, bb.getCenterZ());
						Location lowerLocation = center.clone().subtract(width/2.0, height/2.0, length/2.0);
						Location higherLocation = lowerLocation.clone().add(width, height, length);
						
						Vector v = pLoc.getDirection();
						for (double d = 0; d < (distance + 1); d += 0.05) {
							final double dd = d;
							Location current = pEyeLoc.clone().add(0, .2, 0).clone().add(v.clone().multiply(dd));	
							if(Math.max(lowerLocation.getX(), higherLocation.getX()) >= current.getX() && Math.min(lowerLocation.getX(), higherLocation.getX()) <= current.getX()) {
								if(Math.max(lowerLocation.getY(), higherLocation.getY()) >= current.getY() && Math.min(lowerLocation.getY(), higherLocation.getY()) <= current.getY()) {
									if(Math.max(lowerLocation.getZ(), higherLocation.getZ()) >= current.getZ() && Math.min(lowerLocation.getZ(), higherLocation.getZ()) <= current.getZ()) {
								Location modifiedLe = current.clone();
								if(!blockInWay(pEyeLoc.clone(), modifiedLe)) {
									if(distance < nearestLength) {
										nearest = le;
										nearestLength = distance;
										yValue = current.getY();
									}
								}
								break;
							}
							}
						}
					}
				}
			}
		}
		}
		if(nearest != null) {
			AttackInstance at = new AttackInstance(p, nearest);
			at.heightHit = yValue;
			at.clicks = atC.clicks;
			at.damage = getFullDamage(at, p, nearest);
			nearestEntity.put(nearest, at);
		}
		return nearestEntity;
	}
	
	public static HashMap<LivingEntity, AttackInstance> getSwordEntities(Player p, AttackInstance atC) {
		Location pLoc = atC.attackerLocation.clone();
		Location pEyeLoc = atC.attackerEyeLocation.clone();
		if(pLoc == null) {
			pLoc = p.getLocation().clone();
			pEyeLoc = p.getEyeLocation().clone();
		}
		HashMap<LivingEntity, AttackInstance> nearestEntity = new HashMap<LivingEntity, AttackInstance>();
		LivingEntity nearest = null;
		double nearestLength = 999999;
		double yValue = 0;
		
		for(Entity e : p.getNearbyEntities(6, 6, 6)) {
			if(e instanceof LivingEntity) {
				LivingEntity le = (LivingEntity) e;
				double distance = Math.sqrt(e.getLocation().distanceSquared(pLoc.clone().add(0, (26*0.06), 0)));
				if(distance <= 5.8) {
					Location eye = pEyeLoc.clone();
					Vector toEntity = le.getEyeLocation().toVector().subtract(eye.toVector());
					double dot = toEntity.normalize().dot(eye.getDirection());
					double fov = .50;
					//check if in hoe's fov radius
					if(dot > fov) {
						BoundingBox bb = le.getBoundingBox();
						double height = bb.getHeight();
						double width = bb.getWidthX() + .2;
						double length = bb.getWidthZ() + .2;
						Location center = new Location(le.getWorld(), bb.getCenterX(), bb.getCenterY() + .148, bb.getCenterZ());
						Location lowerLocation = center.clone().subtract(width/2.0, height/2.0, length/2.0);
						Location higherLocation = lowerLocation.clone().add(width, height, length);
						
						Vector v = pLoc.getDirection();
						for (double d = 0; d < (distance + 1); d += 0.05) {
							final double dd = d;
							Location current = pEyeLoc.clone().add(0, .2, 0).clone().add(v.clone().multiply(dd));	
							if(Math.max(lowerLocation.getX(), higherLocation.getX()) >= current.getX() && Math.min(lowerLocation.getX(), higherLocation.getX()) <= current.getX()) {
								if(Math.max(lowerLocation.getY(), higherLocation.getY()) >= current.getY() && Math.min(lowerLocation.getY(), higherLocation.getY()) <= current.getY()) {
									if(Math.max(lowerLocation.getZ(), higherLocation.getZ()) >= current.getZ() && Math.min(lowerLocation.getZ(), higherLocation.getZ()) <= current.getZ()) {
										Location modifiedLe = current.clone();
										if(!blockInWay(pEyeLoc.clone(), modifiedLe)) {
											if(distance < nearestLength) {
												nearest = le;
												nearestLength = distance;
												yValue = current.getY();
											}
										}
										break;
									}
								}
							}
							}
						}
					}
				}
			}
		if(nearest != null) {
			AttackInstance at = new AttackInstance(p, nearest);
			at.heightHit = yValue;
			at.clicks = atC.clicks;
			at.damage = getFullDamage(at, p, nearest);
			nearestEntity.put(nearest, at);
		}
		return nearestEntity;
	}
	
	public static HashMap<LivingEntity, AttackInstance> getPickaxeEntities(Player p, AttackInstance atC) {
		Location pLoc = atC.attackerLocation.clone();
		Location pEyeLoc = atC.attackerEyeLocation.clone();
		if(pLoc == null) {
			pLoc = p.getLocation().clone();
			pEyeLoc = p.getEyeLocation().clone();
		}
		HashMap<LivingEntity, AttackInstance> nearestEntity = new HashMap<LivingEntity, AttackInstance>();
		LivingEntity nearest = null;
		double nearestLength = 999999;
		double yValue = 0;
		
		for(Entity e : p.getNearbyEntities(5, 5, 5)) {
			if(e instanceof LivingEntity) {
				LivingEntity le = (LivingEntity) e;
				double distance = Math.sqrt(e.getLocation().distanceSquared(pLoc.clone().add(0, (26*0.06), 0)));
				if(distance <= 5.2) {
					Location eye = pEyeLoc.clone();
					Vector toEntity = le.getEyeLocation().toVector().subtract(eye.toVector());
					double dot = toEntity.normalize().dot(eye.getDirection());
					double fov = .40;
					//check if in hoe's fov radius
					if(dot > fov) {
						BoundingBox bb = le.getBoundingBox();
						double height = bb.getHeight();
						double width = bb.getWidthX() + .2;
						double length = bb.getWidthZ() + .2;
						Location center = new Location(le.getWorld(), bb.getCenterX(), bb.getCenterY() + .148, bb.getCenterZ());
						Location lowerLocation = center.clone().subtract(width/2.0, height/2.0, length/2.0);
						Location higherLocation = lowerLocation.clone().add(width, height, length);
						
						Vector v = pLoc.getDirection();
						for (double d = 0; d < (distance + 1); d += 0.05) {
							final double dd = d;
							Location current = pEyeLoc.clone().add(0, .2, 0).clone().add(v.clone().multiply(dd));	
							if(Math.max(lowerLocation.getX(), higherLocation.getX()) >= current.getX() && Math.min(lowerLocation.getX(), higherLocation.getX()) <= current.getX()) {
									if(Math.max(lowerLocation.getZ(), higherLocation.getZ()) >= current.getZ() && Math.min(lowerLocation.getZ(), higherLocation.getZ()) <= current.getZ()) {
								Location modifiedLe = current.clone();
								if(!blockInWay(pEyeLoc.clone(), modifiedLe)) {
									if(distance < nearestLength) {
										nearest = le;
										nearestLength = distance;
										yValue = current.getY();
									}
								}
								break;
							}
							
							}
						}
					}
				}
			}
		}
		if(nearest != null) {
			AttackInstance at = new AttackInstance(p, nearest);
			at.heightHit = yValue;
			at.clicks = atC.clicks;
			at.damage = getFullDamage(at, p, nearest);
			nearestEntity.put(nearest, at);
		}
		return nearestEntity;
	}
	
	public static HashMap<LivingEntity, AttackInstance> getFistEntities(Player p, AttackInstance atC) {
		Location pLoc = atC.attackerLocation.clone();
		Location pEyeLoc = atC.attackerEyeLocation.clone();
		if(pLoc == null) {
			pLoc = p.getLocation().clone();
			pEyeLoc = p.getEyeLocation().clone();
		}
		HashMap<LivingEntity, AttackInstance> nearestEntity = new HashMap<LivingEntity, AttackInstance>();
		LivingEntity nearest = null;
		double nearestLength = 999999;
		double yValue = 0;
		
		for(Entity e : p.getNearbyEntities(3, 3, 3)) {
			if(e instanceof LivingEntity) {
				LivingEntity le = (LivingEntity) e;
				double distance = Math.sqrt(e.getLocation().distanceSquared(pLoc.clone().add(0, (26*0.06), 0)));
				if(distance <= 3) {
					Location eye = pEyeLoc.clone();
					Vector toEntity = le.getEyeLocation().toVector().subtract(eye.toVector());
					double dot = toEntity.normalize().dot(eye.getDirection());
					double fov = .50;
					//check if in hoe's fov radius
					if(dot > fov) {
						BoundingBox bb = le.getBoundingBox();
						double height = bb.getHeight();
						double width = bb.getWidthX() + .2;
						double length = bb.getWidthZ() + .2;
						Location center = new Location(le.getWorld(), bb.getCenterX(), bb.getCenterY() + .148, bb.getCenterZ());
						Location lowerLocation = center.clone().subtract(width/2.0, height/2.0, length/2.0);
						Location higherLocation = lowerLocation.clone().add(width, height, length);
						
						Vector v = pLoc.getDirection();
						for (double d = 0; d < (distance + 1); d += 0.05) {
							final double dd = d;
							Location current = pEyeLoc.clone().add(0, .2, 0).clone().add(v.clone().multiply(dd));	
							if(Math.max(lowerLocation.getX(), higherLocation.getX()) >= current.getX() && Math.min(lowerLocation.getX(), higherLocation.getX()) <= current.getX()) {
								if(Math.max(lowerLocation.getY(), higherLocation.getY()) >= current.getY() && Math.min(lowerLocation.getY(), higherLocation.getY()) <= current.getY()) {
									if(Math.max(lowerLocation.getZ(), higherLocation.getZ()) >= current.getZ() && Math.min(lowerLocation.getZ(), higherLocation.getZ()) <= current.getZ()) {
								Location modifiedLe = current.clone();
								if(!blockInWay(pEyeLoc.clone(), modifiedLe)) {
									if(distance < nearestLength) {
										nearest = le;
										nearestLength = distance;
										yValue = current.getY();
									}
								}
								break;
							}
							}
							}
						}
					}
				}
			}
		}
		if(nearest != null) {
			AttackInstance at = new AttackInstance(p, nearest);
			at.heightHit = yValue;
			at.clicks = atC.clicks;
			at.damage = getFullDamage(at, p, nearest);
			nearestEntity.put(nearest, at);
		}
		return nearestEntity;
	}
	
	public static HashMap<LivingEntity, AttackInstance> getHoeEntities(Player p, AttackInstance atC) {
		Location pLoc = atC.attackerLocation.clone();
		Location pEyeLoc = atC.attackerEyeLocation.clone();
		if(pLoc == null) {
			pLoc = p.getLocation().clone();
			pEyeLoc = p.getEyeLocation().clone();
		}
		List<LivingEntity> livingNear = new ArrayList<LivingEntity>();
		LivingEntity nearest = null;
		double nearestLength = 999999;
		double yValue = 0;
		HashMap<LivingEntity, AttackInstance> chosenNear = new HashMap<LivingEntity, AttackInstance>();
		
		for(Entity e : p.getNearbyEntities(8, 8, 8)) {
			if(e instanceof LivingEntity) {
				LivingEntity le = (LivingEntity) e;
				double distance = Math.sqrt(e.getLocation().distanceSquared(pLoc.clone().add(0, (26*0.06), 0)));
				if(distance <= 8.5 && distance > 3) {
					Location eye = pEyeLoc;
					Vector toEntity = le.getEyeLocation().toVector().subtract(eye.toVector());
					double dot = toEntity.normalize().dot(eye.getDirection());
					double fov = .37;
					if(distance > 1 && distance < 5) {
						fov += ((distance-1) * .16);
					}
					else if(distance > 5) {
						fov = .80 + (distance * .022);
					}
					//check if in hoe's fov radius
					if(dot > fov) {
						BoundingBox bb = le.getBoundingBox();
						double height = bb.getHeight();
						double width = bb.getWidthX() + .2;
						double length = bb.getWidthZ() + .2;
						Location center = new Location(le.getWorld(), bb.getCenterX(), bb.getCenterY() + .148, bb.getCenterZ());
						Location lowerLocation = center.clone().subtract(width/2.0, height/2.0, length/2.0);
						Location higherLocation = lowerLocation.clone().add(width, height, length);
						
						Vector v = pLoc.getDirection();
						for (double d = 0; d < (distance + 1); d += 0.05) {
							final double dd = d;
							Location current = pEyeLoc.clone().add(0, .2, 0).add(v.clone().multiply(dd));	
							if(Math.max(lowerLocation.getY(), higherLocation.getY()) >= current.getY() && Math.min(lowerLocation.getY(), higherLocation.getY()) <= current.getY()) {
								if(d >= distance) {
									Location modifiedLe = le.getLocation().clone();
									modifiedLe.setY(current.getY());
									if(!blockInWay(pEyeLoc.clone(), modifiedLe)) {
										if(distance < nearestLength) {
											if(nearest != null) {
												livingNear.add(nearest);
											}
											nearest = le;
											nearestLength = distance;
											yValue = current.getY();
										}
									}
									break;
								}
							}
						}
					}
				}
			}
		}
		if(nearest != null) {
			for(LivingEntity le : livingNear) {
				double distance = le.getLocation().distanceSquared(pLoc.clone().add(0, (26*0.06), 0));
				if((distance >= nearestLength-.7) && (distance <= nearestLength+.7)) {
					Location yValueLocation = le.getLocation().clone();
					BoundingBox bb = le.getBoundingBox();
					if((le.getLocation().getY() <= yValue) && ((le.getLocation().getY() + bb.getHeight()) >= yValue)) {
						Location modifiedLoc = yValueLocation.clone();
						modifiedLoc.setY(yValue);
						if(!blockInWay(pEyeLoc.clone(), modifiedLoc)) {
							AttackInstance at = new AttackInstance(p, le);
							at.heightHit = yValue;
							at.clicks = atC.clicks;
							at.damage = getFullDamage(at, p, le);
							chosenNear.put(le, at);
						}
					}
				}
			}
			AttackInstance at2 = new AttackInstance(p, nearest);
			at2.heightHit = yValue;
			at2.clicks = atC.clicks;
			at2.damage = getFullDamage(at2, p, nearest);
			chosenNear.put(nearest, at2);
		}
		return chosenNear;
	}
	
	public static  boolean blockInWay(Location start, Location end) {
		Vector v = end.toVector().subtract(start.toVector()).normalize();
		for (double d = .1; d < (Math.sqrt(start.distanceSquared(end))); d += 0.1) {
			final double dd = d;
			Location current = start.clone().add(v.clone().multiply(dd));	
			if((current.getBlock().isPassable() == false) && (current.getBlock().getType().name().toLowerCase().contains("air") == false)) {
				return true;
			}
		}
		return false;
	}

	public static double getDamageItem(String name) {
		if(name.contains("diamond")) {
			if(name.contains("sword")) {
				return 7;
			}
			else if(name.contains("pickaxe")) {
				return 5;
			}
			else if(name.contains("axe")) {
				return 6;
			}
			else if(name.contains("shovel")) {
				return 5;
			}
			else if(name.contains("hoe")) {
				return 4;
			}
		}
		else if(name.contains("gold")) {
			if(name.contains("sword")) {
				return 6;
			}
			else if(name.contains("pickaxe")) {
				return 4;
			}
			else if(name.contains("axe")) {
				return 5;
			}
			else if(name.contains("shovel")) {
				return 4;
			}
			else if(name.contains("hoe")) {
				return 3;
			}
		}
		else if(name.contains("iron")) {
			if(name.contains("sword")) {
				return 6;
			}
			else if(name.contains("pickaxe")) {
				return 3;
			}
			else if(name.contains("axe")) {
				return 5;
			}
			else if(name.contains("shovel")) {
				return 4;
			}
			else if(name.contains("hoe")) {
				return 3;
			}
		}
		else if(name.contains("stone")) {
			if(name.contains("sword")) {
				return 5;
			}
			else if(name.contains("pickaxe")) {
				return 2;
			}
			else if(name.contains("axe")) {
				return 4;
			}
			else if(name.contains("shovel")) {
				return 3;
			}
			else if(name.contains("hoe")) {
				return 2;
			}
		}
		else if(name.contains("wood")) {
			if(name.contains("sword")) {
				return 4;
			}
			else if(name.contains("pickaxe")) {
				return 1;
			}
			else if(name.contains("axe")) {
				return 3;
			}
			else if(name.contains("shovel")) {
				return 2;
			}
			else if(name.contains("hoe")) {
				return 1;
			}
		}
		else if(name.contains("air")) {
			return 2;
		}
		return 2;
	}
	
	public static double getSubtractorWeapon(ItemStack item) {
		String type = "";
		if(item == null) {
			type = "fist";
		}
		else {
			type = item.getType().name().toLowerCase();
			if(type.contains("air")) {
				type = "fist";
			}
		}
		if(type.contains("fist")) {
			return 3;
		}
		else if(type.contains("axe") && (!type.contains("pick"))) {
			return 1;
		}
		return 0;
	}
	
	public static void wasHit(AttackInstance at, Player attacker, LivingEntity attacked) {
		//to do
		//decide on chance of effects based on weapon
		//finish blocked method
		decreaseHoldingDurability(at.attackerWeaponO, attacker);
		int clicks = at.clicks;
		if(clicks == 0) {
			if(clicksLastSecond.containsKey(attacker.getName())) {
				clicks = clicksLastSecond.get(attacker.getName());
			}
			else {
				clicks = 1;
			}
		}
		int chance = (10 + (5 * (clicks - 5))) / 5;
		if(clicks <= 4) {
			chance = 1;
		}
		if(chance <= 0) {
			chance = 99999;
		}
		if(attacked.getNoDamageTicks() > 0) {
			if(random.nextInt(chance)==0) {
			attacked.setNoDamageTicks((int) (0));
			}
		}
		if(attacked.getNoDamageTicks() <= 0) {
			attacked.damage(at.damage*-1, attacker);
			damageEvent(at, attacked, attacker, at.damage, at.bodyPartHit);
			int bodyPartHit = at.bodyPartHit;
			if(bodyPartHit == 1) {
				if(random.nextInt((int) (chance + (4 - getSubtractorWeapon(at.attackerWeapon))))==0) {
				if(attacked instanceof Player) {
					addPotionEffectBetter(attacked, PotionEffectType.CONFUSION, 80, 15, false, false, false);
					addPotionEffectBetter(attacked, PotionEffectType.BLINDNESS, random.nextInt(9)+1, 0, false, false, false);
					((Player) attacked).playSound(attacked.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASEDRUM, (float) .04, 2);
					Bukkit.getScheduler().runTaskLater(main, () -> ((Player) attacked).playSound(attacked.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASEDRUM, (float) .08, 2), 4);
					Bukkit.getScheduler().runTaskLater(main, () -> addPotionEffectBetter(attacked, PotionEffectType.BLINDNESS, random.nextInt(9)+1, 0, false, false, false), 4);
				}
				}
			}
			else if(bodyPartHit == 3) {
				if(random.nextInt((int) (chance + (4 - getSubtractorWeapon(at.attackerWeapon))))==0) {
					if(attacked instanceof Player) {
						((Player) attacked).setSprinting(false);
					}
					if(!attacked.hasPotionEffect(PotionEffectType.SLOW)) {
						attacked.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 5, 5, false, false));
					}
				}
			}
			if(at.attackerWeapon != null) {
				if(at.attackerWeapon.containsEnchantment(Enchantment.FIRE_ASPECT)) {
					attacked.setFireTicks((at.attackerWeapon.getEnchantmentLevel(Enchantment.FIRE_ASPECT))*80);
				}
			}
			
			int fullPower = 0;
			if(attacked.getEquipment() != null) {
				for(ItemStack armorItem : attacked.getEquipment().getArmorContents()) {
					if(armorItem != null) {
						if(armorItem.containsEnchantment(Enchantment.THORNS)) {
							int power = (armorItem.getEnchantmentLevel(Enchantment.THORNS));
							fullPower += power;
						}
					}
				}
			}
			if(fullPower > 0) {
				int thornChance = 15 + fullPower;
				if((random.nextInt(thornChance) + 1) > 15) {
					attacker.damage(-1*(random.nextInt(4) + 1), attacked);
				}
			}
		}
		//apply effects / knockback
	}
	
	public static double getFullDamage(AttackInstance at, Player p, LivingEntity le) {
		//see if weapon is useful against mob - needs done
		double damage = 1;
		ItemStack itemUsed = null;
		String type = "";
		if(at.attackerWeapon != null) {
			itemUsed = at.attackerWeapon;
			type = itemUsed.getType().name().toLowerCase();
			damage = getDamageItem(type);
			
			Map<Enchantment, Integer> enchants = itemUsed.getEnchantments();
			if(enchants.size() > 0) {
				for(Enchantment e : enchants.keySet()) {
					int power = (enchants.get(e));
					if(e.getName().equals(Enchantment.DAMAGE_ALL.getName())) {
						damage += (1 * power);
					}
					else if(e.getName().equals(Enchantment.DAMAGE_ARTHROPODS.getName())) {
						String leType = le.getType().name().toLowerCase();
						if(leType.contains("spider")) {
							damage += (3 * power);
						}
					}
					else if(e.getName().equals(Enchantment.DAMAGE_UNDEAD.getName())) {
						String leType = le.getType().name().toLowerCase();
						if(leType.contains("zombie")||leType.contains("wither")||leType.contains("skeleton")||
								leType.contains("husk")||leType.contains("stray")||leType.contains("drown")||leType.contains("phantom")) {
							damage += (3 * power);
						}
					}
				}
			}
		}
		else {
			damage = 2;
		}
		
		int bodyPart = getBodyPart(at, le);
		at.bodyPartHit = bodyPart;
		if(bodyPart == 1) {
			damage += 2;
		}
		else if(bodyPart == 3) {
			damage -= 1;
		}
		
		//check if potions are applied to damage in entitydamagebyentity event, if so, remove these methods.
		if(p.hasPotionEffect(PotionEffectType.WEAKNESS)) {
			int amp = (p.getPotionEffect(PotionEffectType.WEAKNESS).getAmplifier() + 1);
			damage -= (amp * 4);
		}
		if(p.hasPotionEffect(PotionEffectType.INCREASE_DAMAGE)) {
			int amp = (p.getPotionEffect(PotionEffectType.INCREASE_DAMAGE).getAmplifier() + 1);
			damage += (amp * 3);
		}
		
		//calculate clicks per second damage reducer
		int clicks = at.clicks;
		if(clicks == 0) {
			if(clicksLastSecond.containsKey(p.getName())) {
				clicks = clicksLastSecond.get(p.getName());
			}
			else {
				clicks = 1;
			}
		}
		damage = damage * getPercentageOff(clicks);
		
		double armor = le.getAttribute(Attribute.GENERIC_ARMOR).getValue();
		double reducedArmorDamage = (damage - (damage * ((armor * 3)/100.0)));
		int fullProtection = 0;
		if(le.getEquipment() != null) {
		for(ItemStack armorItem : le.getEquipment().getArmorContents()) {
			if(armorItem != null) {
				if(armorItem.containsEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL)) {
					int power = (armorItem.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL));
					fullProtection += power;
				}
			}
		}
		}
		reducedArmorDamage = (reducedArmorDamage - (reducedArmorDamage * ((fullProtection * 3)/100.0)));
		damage = reducedArmorDamage;
		
		if(le.hasPotionEffect(PotionEffectType.DAMAGE_RESISTANCE)) {
			int amp = (le.getPotionEffect(PotionEffectType.DAMAGE_RESISTANCE).getAmplifier() + 1);
			damage = (damage - (damage * ((amp * 20)/100.0)));
		}
		
		// check for armor and potion buffs
		if(damage <= 0) {
			damage = .1;
		}
		return damage;
	}
	
	public static double getPercentageOff(int clicks) {
		switch(clicks) {
		case 0:
			return 1;
		case 1:
			return 1;
		case 2:
			return .85;
		case 3:
			return .75;
		case 4:
			return .60;
		case 5:
			return .40;
		case 6:
			return .30;
		case 7:
			return .20;
		case 8:
			return .15;
		case 9:
			return .10;
		case 10:
			return .5;
		case 11:
			return .4;
		case 12:
			return .3;
		case 13:
			return .2;
		case 14:
			return .1;
		default: 
			return .1;
		}
	}
	
	public static void addPotionEffectBetter(LivingEntity e, PotionEffectType pt, int duration, int amp, boolean ambient, boolean hp, boolean additive) {
		if(e.hasPotionEffect(pt)) {
			int level = amp;
			if(additive == true) {
				level = e.getPotionEffect(pt).getAmplifier() + (amp+1);
			}
			if(level < 200) {
			e.removePotionEffect(pt);
			e.addPotionEffect(new PotionEffect(pt, duration, level, ambient, hp));
			}
			else {
				e.removePotionEffect(pt);
				e.addPotionEffect(new PotionEffect(pt, duration, 200, ambient, hp));
			}
		}
		else {
			e.addPotionEffect(new PotionEffect(pt, duration, amp, ambient, hp));
		}
	}

	public static boolean decreaseHoldingDurability(ItemStack item, Player le) {
		if(item != null) {
			int durability = -1;
			if(item.containsEnchantment(Enchantment.DURABILITY)) {
				durability = item.getEnchantmentLevel(Enchantment.DURABILITY);
				if(durability < 0) {
					durability = 1;
				}
			}
			if(item.getType().getMaxDurability()>1) {
			if(item.getItemMeta() instanceof Damageable) {
				Damageable metaDamage = (Damageable) item.getItemMeta();
				double damage = metaDamage.getDamage() + 1;
				if(damage >= item.getType().getMaxDurability()) {
					le.getWorld().playSound(le.getEyeLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
					le.getWorld().spawnParticle(Particle.ITEM_CRACK, le.getEyeLocation(), 3, .1, 0.1, 0.1, 0.0001, item);
					le.getInventory().remove(item);
					return true;
				}
				else {
					boolean damageNow = true;
					if(durability != -1) {
						if(random.nextInt(durability + 1) != 0) {
							damageNow = false;
						}
					}
					if(damageNow) {
						metaDamage.setDamage((int) damage);
						item.setItemMeta((ItemMeta) metaDamage);
						return false;
					}
				}
			}
			}
		}
		return false;
	}
	
}
