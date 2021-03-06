package de.V10lator.PortalStick.commands;

import org.bukkit.entity.Player;

import de.V10lator.PortalStick.PortalStick;
import de.V10lator.PortalStick.Region;

public class RegionListCommand extends BaseCommand {

	public RegionListCommand(PortalStick plugin) {
		super(plugin, "regionlist", 0, "<- list all portal regions", false);
	}
	
	public boolean execute() {
		plugin.util.sendMessage(sender, "&c---------- &7Portal Regions &c----------");
		for (Region region : plugin.regionManager.regions.values())
			plugin.util.sendMessage(sender, "&7- &c" + region.name + " &7- &c" + region.min.toString() + " &7-&c " + region.max.toString());
		return true;
	}
	
	public boolean permission(Player player) {
		return plugin.hasPermission(player, plugin.PERM_ADMIN_REGIONS);
	}

}