package com.matejdro.bukkit.portalstick.commands;

import com.matejdro.bukkit.portalstick.RegionManager;
import com.matejdro.bukkit.portalstick.User;
import com.matejdro.bukkit.portalstick.UserManager;
import com.matejdro.bukkit.portalstick.util.Permission;
import com.matejdro.bukkit.portalstick.util.Util;

public class SetRegion extends BaseCommand {
	
	public SetRegion() {
		name = "setregion";
		argLength = 1;
		usage = "<name> <- saves your selected area as a region";
	}
	
	public boolean execute() {
		if (!Permission.adminRegions(player))
			return false;
		User user = UserManager.getUser(player);
		if (user.getPointOne() == null || user.getPointTwo() == null)
			Util.sendMessage(sender, "&cPlease select two points");
		else if (RegionManager.getRegion(args.get(0)) != null)
			Util.sendMessage(sender, "&cRegion already exists with that name!");
		else {
			Util.sendMessage(sender, "&aRegion &7" + args.get(0) + " created!");
			RegionManager.createRegion(args.get(0), user.getPointOne(), user.getPointTwo());
		}
		return true;
	}
}