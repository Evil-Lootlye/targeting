package vanillaamplified.main;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.craftbukkit.Main;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;

import net.md_5.bungee.api.ChatColor;
import vanillaamplified.events.MonsterEvent;
import vanillaamplified.generation.CaveGenerator;
import vanillaamplified.generation.DungeonTools;
import vanillaamplified.generation.EndGenerator;
import vanillaamplified.generation.NetherGenerator;
import vanillaamplified.generation.NetherGenerator2;
import vanillaamplified.protection.WorldGuardMethods;
import vanillaamplified.combat.CombatMain;
import vanillaamplified.events.DetectionEvent;
import vanillaamplified.events.ItemColorEvent;

public class VanillaMain extends JavaPlugin implements Listener, CommandExecutor {
	
	///////////////////////////////////////////////////////////////////////////////
	
	/** This section contains useful variables. */

	public static VanillaMain instance;
	public BukkitScheduler scheduler = null;
	public ConsoleCommandSender console = getServer().getConsoleSender();
	Random random = new Random();
	public ConfigManager config = null;
	
	public static WorldGuardMethods wgm = null;
	
	public static boolean nethstruct = true;
	public static int structchance = 0;
	public static int webchance = 0;
	public static int mushchance = 0;
	public static int nspikechance = 0;
	public static int coralchance = 0;
	public static int rosechance = 0;
	public static int mushroomchance = 0;
	public static int replacerackchance = 0;
	public static int nheight = 120;

	///////////////////////////////////////////////////////////////////////////////

	/** This contains the enable and disable methods.
	 *  It also contains useful methods for NMS and packet manipulation. */
	
	@Override
	public void onEnable() {
		instance = this;
		config = new ConfigManager(instance);
		this.getServer().getPluginManager().registerEvents(this, this);

		// Run any event pre-reqs
		ItemColorEvent.addMaterialColors();
		if(this.getServer().getPluginManager().getPlugin("WorldGuard") != null) {
			console.sendMessage(ChatColor.GREEN + "VanillaAmplified : Detected WorldGuard!");
			wgm = new WorldGuardMethods(this);
		}

		// Add world-generators
		
		ArrayList<String> endWorlds = new ArrayList<String>();

		if(endWorlds.size()==0) {
			for(World w : Bukkit.getWorlds()) {
				if(w.getName().toLowerCase().contains("end")) {
					endWorlds.add(w.getName());
					break;
				}
			}
		}
		if(endWorlds.size() != 0) {
		for(String namew : endWorlds) {
			World wor = Bukkit.getWorld(namew);
			if(wor!=null) {
				console.sendMessage(ChatColor.GRAY + "Adding end generator to world : " + namew);
				List<BlockPopulator> bp = new ArrayList<BlockPopulator>(wor.getPopulators());
				for(BlockPopulator bps : bp) {
					if(bps.toString().length()>=40) {
						if(bps.toString().substring(0, 40).equals("vanillaamplified.generation.EndGenerator")) {
							console.sendMessage(ChatColor.GRAY + "Removing Previous End Generator.");
							wor.getPopulators().remove(bps);
						}
					}
				}
				wor.getPopulators().add(new EndGenerator());
			}
		}
		}
		
		ArrayList<String> netherWorlds = new ArrayList<String>();
		
		if(netherWorlds.size()==0) {
			for(World w : Bukkit.getWorlds()) {
				if(w.getName().toLowerCase().contains("nether")) {
					netherWorlds.add(w.getName());
					break;
				}
			}
		}
		if(netherWorlds.size() != 0) {
		for(String worldName : netherWorlds) {
			World wor = Bukkit.getWorld(worldName);
			if(wor!=null) {
				console.sendMessage(ChatColor.GRAY + "Adding nether generator to world : " + worldName);
				List<BlockPopulator> bp = new ArrayList<BlockPopulator>(wor.getPopulators());
				for(BlockPopulator bps : bp) {
					if(bps.toString().length()>=43) {
						boolean number2 = false;
						if(bps.toString().length() >= 44) {
							if(bps.toString().substring(0, 44).equals("vanillaamplified.generation.NetherGenerator2")) {
								number2 = true;
							}
						}
						if(number2) {
							console.sendMessage(ChatColor.GRAY + "Removing Previous Nether Generator 2.");
							wor.getPopulators().remove(bps);
						}
						else if(bps.toString().substring(0, 43).equals("vanillaamplified.generation.NetherGenerator")) {
							console.sendMessage(ChatColor.GRAY + "Removing Previous Nether Generator.");
							wor.getPopulators().remove(bps);
						}
					}	
				}
				wor.getPopulators().add(new vanillaamplified.generation.NetherGenerator());
				wor.getPopulators().add(new vanillaamplified.generation.NetherGenerator2());
			}
		}
		}
		
		// Event Registration
		this.getServer().getPluginManager().registerEvents(new DetectionEvent(), this);
		this.getServer().getPluginManager().registerEvents(new MonsterEvent(), this);
		this.getServer().getPluginManager().registerEvents(new ItemColorEvent(), this);
		
		this.getServer().getPluginManager().registerEvents(new CaveGenerator(), this);
		this.getServer().getPluginManager().registerEvents(new EndGenerator(), this);
		this.getServer().getPluginManager().registerEvents(new NetherGenerator(), this);
		this.getServer().getPluginManager().registerEvents(new DungeonTools(), this);
		this.getServer().getPluginManager().registerEvents(new CombatMain(), this);
		//

		// Runnables
		scheduler = getServer().getScheduler();
		
		scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
			@Override
			public void run() {
				MonsterEvent.doCreeperEffects();
				CombatMain.calculateClicks();
			}
		}, 0L, ((long) 20));
		scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
			@Override
			public void run() {
				CombatMain.runQuickly();
			}
		}, 0L, ((long) 0));
		
	}

	@Override
	public void onDisable() {
		config.saveYamls();
		instance = null;
	}

	void sendPacket(Player p, Object packet) throws NoSuchMethodException, IllegalAccessException,
			InvocationTargetException, NoSuchFieldException, ClassNotFoundException {
		Object nmsPlayer = p.getClass().getMethod("getHandle").invoke(p);
		Object plrConnection = nmsPlayer.getClass().getField("playerConnection").get(nmsPlayer);
		plrConnection.getClass().getMethod("sendPacket", getNmsClass("Packet")).invoke(plrConnection, packet);
	}

	Class<?> getNmsClass(String nmsClassName) throws ClassNotFoundException {
		return Class.forName("net.minecraft.server."
				+ Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + "."
				+ nmsClassName);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("vanamp")) {
			if(args.length > 0) {
				String arg1 = args[0];
				// Help Command
				if(arg1.equals("help")) {
					if(args.length > 1) {
						String page = args[1];
						if(page.equals("1") || page.equals("0")) {
							sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "x-----[ Page : 1 ] -----x");
							sender.sendMessage(ChatColor.GRAY + "  /vanamp");
							sender.sendMessage(ChatColor.GRAY + "  /vanamp resetconfig");
							sender.sendMessage(ChatColor.GRAY + "  /vanamp readconfigs");
							sender.sendMessage(ChatColor.GRAY + "  /vanamp generateworld");
						}
						else {
							sender.sendMessage(ChatColor.RED + "That page does not exist!");
						}
					}
					else {
						sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "x-----[ Page : 1 ] -----x");
						sender.sendMessage(ChatColor.GRAY + "  /vanamp");
						sender.sendMessage(ChatColor.GRAY + "  /vanamp resetconfig");
						sender.sendMessage(ChatColor.GRAY + "  /vanamp readconfigs");
						sender.sendMessage(ChatColor.GRAY + "  /vanamp generateworld");
					}
				}
				// Config Reset Command
				else if(arg1.equals("resetconfig") && (sender.isOp() || sender.hasPermission("vanillaamplified.vanamp.resetconfig"))) {
					if(args.length > 1) {
						String configType = args[1];
						if(configType.equals("detection")) {
							config.resetDetectionConfig();
							sender.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Preparing to reset detection config...");
						}
						else if(configType.equals("mob")) {
							config.resetMobConfig();
							sender.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Preparing to mob detection config...");
						}
						else {
							sender.sendMessage(ChatColor.RED + "That config type does not exist!");
						}
					}
					else {
						sender.sendMessage(ChatColor.RED + "Resets a config file to it's default state.");
						sender.sendMessage(ChatColor.GRAY + "Do  /vanamp resetconfig <type>  .");
						sender.sendMessage(ChatColor.GRAY + "Acceptable Config Types:");
						sender.sendMessage(ChatColor.GRAY + "  * detection");
						sender.sendMessage(ChatColor.GRAY + "  * mob");
					}
				}
				else if(arg1.equals("resetconfig") && (!sender.hasPermission("vanillaamplified.vanamp.resetconfig"))) {
					sender.sendMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + "Sorry! You do not have permission to reset the configs.");
				}
				else if(arg1.equals("generationstop") && (sender.isOp() || sender.hasPermission("vanillaamplified.vanamp.generationstop"))) {
					if(CaveGenerator.generationDone == false) {
						sender.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Cancelling generation...");
						CaveGenerator.generationStop = true;
						CaveGenerator.generationDone = true;
					}
					else {
						sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Not generating any chunks.");
					}
				}
				else if(arg1.equals("generationstop") && (!sender.hasPermission("vanillaamplified.vanamp.generationstop"))) {
					sender.sendMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + "Sorry! You do not have permission to stop generation.");
				}
				else if(arg1.equals("readconfigs") && (sender.isOp() || sender.hasPermission("vanillaamplified.vanamp.readconfigs"))) {
					config.readConfigValues();
					sender.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Reading in current config values...");
				}
				else if(arg1.equals("readconfigs") && (!sender.hasPermission("vanillaamplified.vanamp.readconfigs"))) {
					sender.sendMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + "Sorry! You do not have permission to read the configs.");
				}
				else if(arg1.equals("generateworld") && (sender.isOp() || sender.hasPermission("vanillaamplified.vanamp.generateworld"))) {
					if(args.length > 1) {
						try {
							int chunkRadius = Integer.parseInt(args[1]);
							sender.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Beginning chunk generation in 20 seconds!");
							sender.sendMessage(ChatColor.GRAY + "All players will be kicked while the chunks are generating!");
							sender.sendMessage(ChatColor.GRAY + "If you want to stop the generation, type 'vanamp generationstop' in the console.");
							Bukkit.getScheduler().runTaskLater(this, () -> sender.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Beginning chunk generation in 5 seconds!"), 20*15);
							if(sender instanceof Player) {
								Bukkit.getScheduler().runTaskLater(this, () -> CaveGenerator.beginGeneration(chunkRadius, ((Player) sender).getWorld()), 20*20);
							}
							else {
								if(sender.getServer().getWorld("world") != null) {
									Bukkit.getScheduler().runTaskLater(this, () -> CaveGenerator.beginGeneration(chunkRadius, sender.getServer().getWorld("world")), 20*20);
								}
								else {
									Bukkit.getScheduler().runTaskLater(this, () -> CaveGenerator.beginGeneration(chunkRadius, sender.getServer().getWorlds().get(0)), 20*20);
								}
							}
						}
						catch (Exception e) {
							sender.sendMessage(ChatColor.RED + "Radius given was not a number! Example: - /vanamp generateworld 500");
						}
					}
					else {
						sender.sendMessage(ChatColor.RED + "You must indicate how big of a chunk radius you want.");
						sender.sendMessage(ChatColor.RED + "Example: - /vanamp generateworld 500 - generates 500 x 500 chunk square.");
					}
				}
				else if(arg1.equals("generateworld") && (!sender.hasPermission("vanillaamplified.vanamp.generateworld"))) {
					sender.sendMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + "Sorry! You do not have permission to generate chunks.");
				}
				else {
					sendToCommandMethods(sender, command, label, args);
				}
			}
			else {
				sender.sendMessage(ChatColor.RED + "Default Vanilla Amplified Command.");
				sender.sendMessage(ChatColor.GRAY + "Do  /vanamp help 1  for a list of commands.");
			}
			return true;
		}
		return true;
	}
	
	public void sendToCommandMethods(CommandSender sender, Command command, String label, String[] args) {
		DungeonTools.understandCommand(sender, command, label, args);
		CombatMain.understandCommand(sender, command, label, args);
	}

	///////////////////////////////////////////////////////////////////////////////
	
	/** This section contains useful methods for mob and location manipulation. */

	public boolean exists(Entity e) {
		if (e == null) {
			return false;
		}
		if (e.isDead()) {
			return false;
		}
		return true;
	}

	public void addPotionEffectBetter(LivingEntity e, PotionEffectType pt, int duration, int amp, boolean ambient,
			boolean hp, boolean additive) {
		if (e.hasPotionEffect(pt)) {
			int level = amp;
			if (additive == true) {
				level = e.getPotionEffect(pt).getAmplifier() + (amp + 1);
			}
			if (level < 200) {
				e.removePotionEffect(pt);
				e.addPotionEffect(new PotionEffect(pt, duration, level, ambient, hp));
			} else {
				e.removePotionEffect(pt);
				e.addPotionEffect(new PotionEffect(pt, duration, 200, ambient, hp));
			}
		} else {
			e.addPotionEffect(new PotionEffect(pt, duration, amp, ambient, hp));
		}
	}

	public boolean isAir(Material m) {
		if (m == Material.AIR || m == Material.CAVE_AIR || m == Material.VOID_AIR) {
			return true;
		}
		return false;
	}

	public Location getRandLoc(Location l, int radi) {
		if (l != null) {
			double radius = radi;
			double x0 = l.getX();
			double y0 = l.getY();
			double z0 = l.getZ();
			double u = Math.random();
			double v = Math.random();
			double theta = 2 * Math.PI * u;
			double phi = Math.acos(2 * v - 1);
			double x = x0 + (radius * Math.sin(phi) * Math.cos(theta));
			double y = y0 + (radius * Math.sin(phi) * Math.sin(theta));
			double z = z0 + (radius * Math.cos(phi));
			return new Location(l.getWorld(), x, y, z, random.nextInt(360), random.nextInt(360));
		}
		return null;
	}

	///////////////////////////////////////////////////////////////////////////////

}
