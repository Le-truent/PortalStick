package com.matejdro.bukkit.portalstick;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import com.matejdro.bukkit.portalstick.commands.BaseCommand;
import com.matejdro.bukkit.portalstick.commands.DeleteAllCommand;
import com.matejdro.bukkit.portalstick.commands.DeleteCommand;
import com.matejdro.bukkit.portalstick.commands.DeleteRegionCommand;
import com.matejdro.bukkit.portalstick.commands.FlagCommand;
import com.matejdro.bukkit.portalstick.commands.HelpCommand;
import com.matejdro.bukkit.portalstick.commands.RegionInfoCommand;
import com.matejdro.bukkit.portalstick.commands.RegionListCommand;
import com.matejdro.bukkit.portalstick.commands.RegionToolCommand;
import com.matejdro.bukkit.portalstick.commands.ReloadCommand;
import com.matejdro.bukkit.portalstick.commands.SetRegionCommand;
import com.matejdro.bukkit.portalstick.listeners.PortalStickBlockListener;
import com.matejdro.bukkit.portalstick.listeners.PortalStickEntityListener;
import com.matejdro.bukkit.portalstick.listeners.PortalStickPlayerListener;
import com.matejdro.bukkit.portalstick.listeners.PortalStickVehicleListener;
import com.matejdro.bukkit.portalstick.listeners.PortalStickWorldListener;
import com.matejdro.bukkit.portalstick.util.Config;
import com.matejdro.bukkit.portalstick.util.Util;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class PortalStick extends JavaPlugin {
	public static Logger log = Logger.getLogger("Minecraft");

	private PortalStickPlayerListener playerListener;
	private PortalStickBlockListener blockListener;
	private PortalStickVehicleListener vehicleListener;
	private PortalStickEntityListener entityListener;
	private PortalStickWorldListener worldListener;
	private GrillManager grillManager;
	
	public static List<BaseCommand> commands = new ArrayList<BaseCommand>();
	public static Config config;
	
	public static PortalStick instance;

	public static WorldGuardPlugin worldGuard = null;

	public void onDisable() {
		Config.saveAll();
		Config.unLoad();
		Util.info(this + " unloaded");
	}

	public void onEnable() {
		instance = this;
		
		playerListener = new PortalStickPlayerListener();
		blockListener = new PortalStickBlockListener(this);
		vehicleListener = new PortalStickVehicleListener();
		entityListener = new PortalStickEntityListener();
		worldListener = new PortalStickWorldListener();
		
		//Register events		
		getServer().getPluginManager().registerEvents(playerListener, this);
		getServer().getPluginManager().registerEvents(blockListener, this);
		getServer().getPluginManager().registerEvents(vehicleListener, this);
		getServer().getPluginManager().registerEvents(entityListener, this);
		getServer().getPluginManager().registerEvents(worldListener, this);
		
		grillManager = new GrillManager(this);
		config = new Config(this);
		
		worldGuard = (WorldGuardPlugin) this.getServer().getPluginManager().getPlugin("WorldGuard");

		//Start grill checking timer
		getServer().getScheduler().scheduleSyncRepeatingTask(this, grillManager, 400, 400);
		
		//Teleport all entities.
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new EntityManager(this), 2, 2);
		
		//Register commands
		commands.add(new RegionToolCommand());
		commands.add(new SetRegionCommand());
		commands.add(new ReloadCommand());
		commands.add(new DeleteAllCommand());
		commands.add(new DeleteCommand());
		commands.add(new HelpCommand());
		commands.add(new RegionListCommand());
		commands.add(new DeleteRegionCommand());
		commands.add(new FlagCommand());
		commands.add(new RegionInfoCommand());
		
		Util.info(this + " enabled");

	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String args[])
	{
		if (cmd.getName().equalsIgnoreCase("portalstick")) {
			if (args.length == 0)
				args = new String[]{"help"};
			for (BaseCommand command : commands.toArray(new BaseCommand[0])) {
				if (command.name.equalsIgnoreCase(args[0]))
					return command.run(sender, args, commandLabel);
			}
		}
		return false;
	}
    
}
		    
