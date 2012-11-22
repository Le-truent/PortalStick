package com.matejdro.bukkit.portalstick.commands;

import org.bukkit.entity.Player;

import com.matejdro.bukkit.portalstick.PortalStick;

public class ReloadCommand extends BaseCommand {

	public ReloadCommand(PortalStick plugin) {
		super(plugin);
		name = "reload";
		argLength = 0;
		usage = "<- reloads the PortalStick config";
	}
	
	public boolean execute() {
		plugin.config.reLoad();
		plugin.util.sendMessage(player, plugin.i18n.getString("ConfigurationReloaded", player.getName(), args.get(0)));
		return true;
	}
	
	public boolean permission(Player player) {
		return plugin.hasPermission(player, plugin.PERM_ADMIN_REGIONS);
	}

}
