package com.matejdro.bukkit.portalstick.util;

import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.getspout.spoutapi.SpoutManager;

import com.matejdro.bukkit.portalstick.PortalStick;
import com.matejdro.bukkit.portalstick.Region;
import com.matejdro.bukkit.portalstick.util.Config.Sound;

public class Util {
	private static PortalStick plugin;
	
	public static void setPlugin(PortalStick plugin) // Workaround for static...
	{
		Util.plugin = plugin;
	}
	
	private static final Logger log = Logger.getLogger("Minecraft");
	private static int maxLength = 105;
	
	public static void info(String msg) {
		log.info("[PortalStick] " + msg);
	}
	public static void severe(String msg) {
		log.severe("[PortalStick] " + msg);
	}
	
	public static void sendMessage(CommandSender player, String msg) {
		int i;
		String part;
		CustomColor lastColor = CustomColor.WHITE;
		for (String line : msg.split("`n")) {
			i = 0;
			while (i < line.length()) {
				part = getMaxString(line.substring(i));
				if (i+part.length() < line.length() && part.contains(" "))
					part = part.substring(0, part.lastIndexOf(" "));
				part = lastColor.getCustom() + part;
				player.sendMessage(replaceColors(part));
				lastColor = getLastColor(part);
				i = i + part.length() -1;
			}
		}
	}
	
	public static Location getSimpleLocation(Location location) {
		location.setX((double)Math.round(location.getX() * 10) / 10);
		location.setY((double)Math.round(location.getY() * 10) / 10);
		location.setZ((double)Math.round(location.getZ() * 10) / 10);
		return location;
	}
	
	public static String stripColors(String str) {
		str = str.replaceAll("(?i)\u00A7[0-F]", "");
		str = str.replaceAll("(?i)&[0-F]", "");
		return str;
	}
	
	public static CustomColor getLastColor(String str) {
		int i = 0;
		CustomColor lastColor = CustomColor.WHITE;
		while (i < str.length()-2) {
			for (CustomColor color: CustomColor.values()) {
				if (str.substring(i, i+2).equalsIgnoreCase(color.getCustom()))
					lastColor = color;
			}
			i = i+2;
		}
		return lastColor;
	}
	
    public static String replaceColors(String str) {
    	for (CustomColor color : CustomColor.values())
    		str = str.replace(color.getCustom(), color.getString());
        return str;
    }
    
    private static String getMaxString(String str) {
    	for (int i = 0; i < str.length(); i++) {
    		if (stripColors(str.substring(0, i)).length() == maxLength) {
    			if (stripColors(str.substring(i, i+1)) == "")
    				return str.substring(0, i-1);
    			else
    				return str.substring(0, i);
    		}
    	}
    	return str;
    }
    
    public static void PlayNote(final Player player, final byte instrument, final byte note)
    {
    	final Block block = player.getLocation().getBlock().getRelative(0,-5,0);
        final Byte data = block.getData();
        final Material material = block.getType();
        
        Region region = plugin.regionManager.getRegion(player.getLocation());
        if (!region.getBoolean(RegionSetting.ENABLE_SOUNDS)) return;

        player.sendBlockChange(block.getLocation(), Material.NOTE_BLOCK, (byte)instrument);
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(
        		plugin,
                new Runnable(){
                    public void run(){
                        player.playNote(block.getLocation(), instrument, note);
                        player.sendBlockChange(block.getLocation(), material, data);
                    }
                },
                1
        );
    }
    
    public static void PlaySound(Sound sound, Player player, Location loc)
    {
    	if (!plugin.regionManager.getRegion(loc).getBoolean(RegionSetting.ENABLE_SOUNDS)) return;
        Plugin spoutPlugin = plugin.getServer().getPluginManager().getPlugin("Spout");
        if (spoutPlugin == null || !Config.useBukkitContribSounds)
        {
        	if (player != null && !Config.soundNotes[sound.ordinal()].trim().equals(""))
        	{
        		Byte instrument = Byte.parseByte(Config.soundNotes[sound.ordinal()].split("-")[0]);
        		Byte note = Byte.parseByte(Config.soundNotes[sound.ordinal()].split("-")[1]);
        		PlayNote(player, instrument, note);
        	}
        }
        if (spoutPlugin != null && Config.useBukkitContribSounds)
        {
        	if (!Config.soundUrls[sound.ordinal()].trim().equals(""))
        	{
                SpoutManager.getSoundManager().playGlobalCustomSoundEffect(plugin, Config.soundUrls[sound.ordinal()], false, loc, Config.soundRange);
        	}
        }
        
    }
       
    
    public static int getLeftPortalColor(int preset)
    {
    	String p = Config.ColorPresets.get(preset);
    	return Integer.parseInt(p.split("-")[0]);
    }
    
    public static int getRightPortalColor(int preset)
    {
    	String p = Config.ColorPresets.get(preset);
    	return Integer.parseInt(p.split("-")[1]);
    }
    
    public static ItemStack getItemData(String itemString)
    {
    	int num;
    	int id;
    	short data;
    	
    	String[] split = itemString.split(",");
    	if (split.length < 2)
    		num = 1;
    	else
    		num = Integer.parseInt(split[1]);
    	String[] split2 = split[0].split(":");
    	if (split2.length < 2)
    		data = 0;
    	else
    		data = Short.parseShort(split2[1]);

    	id = Integer.parseInt(split2[0]);
    	return new ItemStack(id, num, data);
    }

           
    private enum CustomColor {
    	
    	RED("c", 0xC),
    	DARK_RED("4", 0x4),
    	YELLOW("e", 0xE),
    	GOLD("6", 0x6),
    	GREEN("a", 0xA),
    	DARK_GREEN("2", 0x2),
    	AQUA("b", 0xB),
    	DARK_AQUA("8", 0x8),
    	BLUE("9", 0x9),
    	DARK_BLUE("1", 0x1),
    	LIGHT_PURPLE("d", 0xD),
    	DARK_PURPLE("5", 0x5),
    	BLACK("0", 0x0),
    	DARK_GRAY("8", 0x8),
    	GRAY("7", 0x7),
    	WHITE("f", 0xf);
    	
    	private String custom;
    	private int code;
    	
    	private CustomColor(String custom, int code) {
    		this.custom = custom;
    		this.code = code;
    	}
    	public String getCustom() {
    		return "&" + custom;
    	}
    	public String getString() {
    		return String.format("\u00A7%x", code);
    	}
    	
    }

}
