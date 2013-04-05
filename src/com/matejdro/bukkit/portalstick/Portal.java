package com.matejdro.bukkit.portalstick;

import java.util.HashSet;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.libigot.LibigotLocation;
import org.libigot.block.BlockStorage;

import com.matejdro.bukkit.portalstick.util.RegionSetting;

public class Portal {
	private final PortalStick plugin;
	final HashSet<LibigotLocation> border;
	public final LibigotLocation[] inside;
	public final LibigotLocation[] teleport;
	private final LibigotLocation[] behind;
	public final boolean horizontal;
	public final User owner;
	public final boolean orange;
	public boolean open = false;
	boolean disabled = false;
	public boolean transmitter = false;
	public final BlockFace teleportFace;
	private final HashSet<LibigotLocation> awayBlocks;
	final LibigotLocation[] awayBlocksY = new LibigotLocation[2];
	private boolean placetorch = false;
	
	public Portal(PortalStick plugin, LibigotLocation[] teleport, HashSet<LibigotLocation> Border, LibigotLocation[] inside, LibigotLocation[] behind, User Owner, boolean Orange, boolean horizontal, BlockFace Teleportface)
	{
		this.plugin = plugin;
		this.teleport = teleport;
		border = Border;
		this.inside = inside;
		orange = Orange;
		owner = Owner;
		this.horizontal = horizontal;
		teleportFace = Teleportface;
		this.behind = behind;
		if(horizontal)
		  awayBlocks = new HashSet<LibigotLocation>();
		else
		  awayBlocks = null;
	}
	
	public void delete()
	{
		BlockStorage bh;
		for (LibigotLocation loc: border)
		{
			if (plugin.portalManager.oldBlocks.containsKey(loc))
			{
				bh = plugin.portalManager.oldBlocks.get(loc);
				bh.set();
				if(plugin.gelManager.gelMap.containsKey(bh))
					plugin.gelManager.removeGel(bh);
				plugin.portalManager.oldBlocks.remove(loc);
			}
			plugin.portalManager.borderBlocks.remove(loc);
		}
		for (LibigotLocation loc: inside)
		{
		  if(loc == null)
			continue;
		  if (plugin.portalManager.oldBlocks.containsKey(loc))
			{
				bh = plugin.portalManager.oldBlocks.get(loc);
				bh.set();
				if(plugin.gelManager.gelMap.containsKey(bh))
					plugin.gelManager.removeGel(bh);
				plugin.portalManager.oldBlocks.remove(loc);
			}
		  plugin.portalManager.insideBlocks.remove(loc);
		}
		if (plugin.config.FillPortalBack > -1)
		{
			for (LibigotLocation loc: behind)
			{
				if (plugin.portalManager.oldBlocks.containsKey(loc))
				{
					bh = plugin.portalManager.oldBlocks.get(loc);
					bh.set();
					if(plugin.gelManager.gelMap.containsKey(bh))
						plugin.gelManager.removeGel(bh);
					plugin.portalManager.oldBlocks.remove(loc);
				}
				plugin.portalManager.behindBlocks.remove(loc);
			}
		}
		if(horizontal)
		{
		  for(LibigotLocation l: awayBlocks)
			plugin.portalManager.awayBlocks.remove(l);
		  plugin.portalManager.awayBlocksY.remove(awayBlocksY[0]);
		  plugin.portalManager.awayBlocksY.remove(awayBlocksY[1]);
		}
		
		if (orange)
			owner.orangePortal = null;
		else
			owner.bluePortal = null;
			
		plugin.portalManager.portals.remove(this);
		plugin.regionManager.getRegion(inside[0]).portalDeleted(this);
		
		Portal oldDestination = getDestination();
		if(oldDestination != null)
		  if (oldDestination.getDestination() == null) oldDestination.close();

   	}
	
	public void open()
	{
		Region region = plugin.regionManager.getRegion(inside[0]);
		
		Block b;
//		BlockStorage bh;
		for (LibigotLocation loc: inside)
    	{
		  if(loc == null)
			continue;
			b = loc.getHandle().getBlock();
//			bh = new BlockStorage(b);
//			if(plugin.gelManager.gelMap.containsKey(bh))
//			  plugin.gelManager.removeGel(bh);
			b.setType(Material.AIR); 
			
			if (region.getBoolean(RegionSetting.ENABLE_REDSTONE_TRANSFER))
			 {			 				 
				 for (int i = 0; i < 4; i++)
				 {
					 BlockFace face = BlockFace.values()[i];
					 if (b.getRelative(face).getBlockPower() > 0) 
						 {						 
						 	Portal destination = getDestination();
						 	if (destination == null || destination.transmitter) continue;
						 
						 		transmitter = true;
						 		if (destination.open)
						 		{
							 		for (LibigotLocation b2: destination.inside)
							 		  if(b2 != null)
							 			b2.getHandle().getBlock().setType(Material.REDSTONE_TORCH_ON);
						 		}
						 		else
						 			destination.placetorch = true;
						 }
				 }
			 }

    	}
		
		if (placetorch)
		{
			inside[0].getHandle().getBlock().setType(Material.REDSTONE_TORCH_ON);
			placetorch = false;
		}
		
		open = true;
		plugin.funnelBridgeManager.reorientBridge(this);
	}
	
	public void close()
	{
		byte color;
		if (orange)
			color = (byte) plugin.util.getRightPortalColor(owner.colorPreset);
		else
			color = (byte) plugin.util.getLeftPortalColor(owner.colorPreset);
		int w = Material.WOOL.getId();
		for (LibigotLocation b: inside)
    	{
		  if(b != null)
		  {
    		b.getHandle().getBlock().setTypeIdAndData(w, color, true);
    		open = false;
		  }
    	}
		
		plugin.funnelBridgeManager.reorientBridge(this);
	}
	
	public void recreate()
	{
		byte color;
		if (orange)
			color = (byte) plugin.util.getRightPortalColor(owner.colorPreset);
		else
			color = (byte) plugin.util.getLeftPortalColor(owner.colorPreset);			
		
		for (LibigotLocation b: border)
    		b.getHandle().getBlock().setData(color);

		if (!open)
			for (LibigotLocation b: inside)
			  if(b != null)
	    		b.getHandle().getBlock().setData(color);
		
		if (plugin.config.CompactPortal)
			for (LibigotLocation b: behind)
	    		b.getHandle().getBlock().setData(color);
	}
	
	public void create()
	{
		byte color;
		if (orange)
			color = (byte) plugin.util.getRightPortalColor(owner.colorPreset);
		else
			color = (byte) plugin.util.getLeftPortalColor(owner.colorPreset);			

		Block rb;
		BlockStorage bh;
		int wool = Material.WOOL.getId();
    	for (LibigotLocation loc: border)
    	{
    		if (plugin.portalManager.insideBlocks.containsKey(loc))
    			plugin.portalManager.insideBlocks.get(loc).delete();
    		if (plugin.portalManager.behindBlocks.containsKey(loc))
    			plugin.portalManager.behindBlocks.get(loc).delete();
    		
    		rb = loc.getHandle().getBlock();
    		if(!plugin.portalManager.oldBlocks.containsKey(loc))
    		{
    		  bh = new BlockStorage(rb);
    		  if(plugin.gelManager.gelMap.containsKey(bh))
    		  {
    			bh = plugin.gelManager.gelMap.get(bh);
    			plugin.gelManager.removeGel(bh);
    		  }
    		  plugin.portalManager.oldBlocks.put(loc, bh);
    		}
    		rb.setTypeIdAndData(wool, color, false);
    		plugin.portalManager.borderBlocks.put(loc, this);
       	}
    	for (LibigotLocation loc: inside)
    	{
    	  if(loc != null && !plugin.portalManager.oldBlocks.containsKey(loc))
    	  {
    		rb = loc.getHandle().getBlock();
    		bh = new BlockStorage(rb);
    		if(plugin.gelManager.gelMap.containsKey(bh))
    		{
      		  bh = plugin.gelManager.gelMap.get(bh);
      		  plugin.gelManager.removeGel(bh);
    		}
    		plugin.portalManager.oldBlocks.put(loc, bh);
    	  }
    	}
    	byte data;
    	if (plugin.config.FillPortalBack > -1)
    	{
    		for (LibigotLocation loc: behind)
        	{
        		if (plugin.portalManager.borderBlocks.containsKey(loc))
        			plugin.portalManager.borderBlocks.get(loc).delete();
        		if (plugin.portalManager.insideBlocks.containsKey(loc))
        			plugin.portalManager.insideBlocks.get(loc).delete();

        		rb = loc.getHandle().getBlock();
        		if(!plugin.portalManager.oldBlocks.containsKey(loc))
        		{
        		  bh = new BlockStorage(rb);
        		  if(plugin.gelManager.gelMap.containsKey(bh))
        		  {
        			bh = plugin.gelManager.gelMap.get(bh);
          		  	plugin.gelManager.removeGel(bh);
        		  }
        		  plugin.portalManager.oldBlocks.put(loc, bh);
        		}
        		if (plugin.config.CompactPortal)
        		{
        			wool = Material.WOOL.getId();
            		data = color;
        		}
        		else
        		{
        			wool = plugin.config.FillPortalBack;
        			data = plugin.config.portalBackData;
        		}
        		rb.setTypeIdAndData(wool, data, false);
        		plugin.portalManager.behindBlocks.put(loc, this);
        	}
    	}
    	
    	Portal dest = getDestination();
    	if (dest == null)
    		close();
    	else
    	{
    		open();
    		if(!dest.open)
    		  dest.open(); //TODO: I think we double-open portals now. Need research where the other open is...
    	}
    	
    	
    	LibigotLocation oloc;
    	LibigotLocation loc;
    	int i;
    	oloc = inside[0].clone();
    	plugin.portalManager.insideBlocks.put(inside[0], this);
    	if(inside[1] != null)
    	  plugin.portalManager.insideBlocks.put(inside[1], this);
    	
    	if(horizontal)
    	{
    	  for (int y = -1;y<2;y++)
    	  {
    		if(y != 0)
    		{
    		  loc = new LibigotLocation(oloc.getWorldName(), oloc.getX(), oloc.getY() + y, oloc.getZ());
    		  plugin.portalManager.awayBlocksY.put(loc, this);
    		  if(y < 1)
    			i = 0;
    		  else
    			i = 1;
    		  awayBlocksY[i] = loc;
    		}
    		for (int x = -1;x<2;x++)
    		{
    		  for (int z = -1;z<2;z++)
    		  {
    			loc = new LibigotLocation(oloc.getWorldName(), oloc.getX() + x, oloc.getY() + y, oloc.getZ() + z);
    			plugin.portalManager.awayBlocks.put(loc, this);
    			awayBlocks.add(loc);
    		  }
    		}
    	  }
    	}
    	
    	plugin.regionManager.getRegion(inside[0]).portalCreated(this);
	}
	
	public Portal getDestination()
	{
		Region region = plugin.regionManager.getRegion(inside[0]);
		
		if (orange)
		{
			if (owner.bluePortal != null) 
				return owner.bluePortal;
			else if (!isRegionPortal())
				return region.bluePortal;
			else
				return region.orangeDestination;
		}
		else
		{
			if (owner.orangePortal != null) 
				return owner.orangePortal;
			else if (!isRegionPortal())
				return region.orangePortal;
			else
				return region.blueDestination;

		}
	}
	
	public boolean isRegionPortal()
	{
		return owner.name.startsWith("§region§_");
	}
}
