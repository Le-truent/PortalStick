package com.matejdro.bukkit.portalstick;

import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.matejdro.bukkit.portalstick.util.BlockUtil;
import com.matejdro.bukkit.portalstick.util.RegionSetting;
import com.matejdro.bukkit.portalstick.util.Util;

public class Portal {
	private Location teleport;
	private HashSet<Block> border;
	private HashSet<Block> inside;
	private boolean vertical;
	private User owner;
	private Boolean orange = false;
	private Boolean open = false;
	private boolean disabled = false;
	private boolean transmitter = false;
	private boolean placetorch = false;
	BlockFace teleportFace;
	private HashSet<Location> awayBlocks = new HashSet<Location>();
	private HashMap<Location, String> oldBlocks = new HashMap<Location, String>();
	
	public Portal()
	{
		border = new HashSet<Block>();
		inside = new HashSet<Block>();
	}
	
	public Portal(Location Teleport, HashSet<Block> Border, HashSet<Block> Inside, User Owner, Boolean Orange, Boolean Vertical, BlockFace Teleportface)
	{
		teleport = Teleport;
		border = Border;
		inside = Inside;
		orange = Orange;
		owner = Owner;
		vertical = Vertical;
		teleportFace = Teleportface;
	}
	
	public void delete()
	{
		
		if (orange != null && owner != null) {
			for (Block b: border)
			{
				if (oldBlocks.containsKey(b.getLocation()))
					BlockUtil.setBlockData(b, oldBlocks.get(b.getLocation()));
				PortalManager.borderBlocks.remove(b.getLocation());
			}
			for (Block b: inside)
			{
				if (oldBlocks.containsKey(b.getLocation()))
					BlockUtil.setBlockData(b, oldBlocks.get(b.getLocation()));
				PortalManager.insideBlocks.remove(b.getLocation());
			}
			for (Location l : awayBlocks)
			{
				PortalManager.awayBlocksGeneral.remove(l);
				PortalManager.awayBlocksX.remove(l);
				PortalManager.awayBlocksY.remove(l);
				PortalManager.awayBlocksZ.remove(l);
			}
			
			if (orange)
			{
				owner.setOrangePortal(null);
			}
			else
			{
				owner.setBluePortal(null);
			}
			
			if (orange)
	    	{
	    		if (owner.getBluePortal() != null)
	    			owner.getBluePortal().close();
	    	}
	    	else
	    	{
	    		if (owner.getOrangePortal() != null)
	    			owner.getOrangePortal().close();
	
	    	}
		}
				
		PortalManager.portals.remove(this);
		open = false;
	}
	
	public void open()
	{
		Region region = RegionManager.getRegion(((Block)inside.toArray()[0]).getLocation());
	
		for (Block b: inside)
    	{
			b.setType(Material.AIR); 
			
			if (region.getBoolean(RegionSetting.ENABLE_REDSTONE_TRANSFER))
			 {			 				 
				 for (int i = 0; i < 4; i++)
				 {
					 BlockFace face = BlockFace.values()[i];
					 if (b.getRelative(face).getBlockPower() > 0) 
						 {						 
						 	Portal destination = getDestination();
						 	if (destination == null || destination.isTransmitter()) continue;
						 
						 		setTransmitter(true);
						 		if (destination.isOpen())
						 			((Block)destination.getInside().toArray()[0]).setType(Material.REDSTONE_TORCH_ON);
						 		else
						 			destination.setPlaceTorch(true);
						 }
				 }
			 }

    	}
		
		if (placetorch)
		{
			((Block)inside.toArray()[0]).setType(Material.REDSTONE_TORCH_ON);
			placetorch = false;
		}
		
		open = true;
		
	}
	
	public void close()
	{
		byte color;
		if (orange)
			color = (byte) Util.getRightPortalColor(getOwner().getColorPreset());
		else
			color = (byte) Util.getLeftPortalColor(getOwner().getColorPreset());			
		for (Block b: inside)
    	{
    		b.setType(Material.WOOL);
    		b.setData(color);
    		open = false;
    	}
	}
	
	public void recreate()
	{
		byte color;
		if (orange)
			color = (byte) Util.getRightPortalColor(getOwner().getColorPreset());
		else
			color = (byte) Util.getLeftPortalColor(getOwner().getColorPreset());			
		
		for (Block b: border)
    	{
    		b.setData(color);
    	}

		if (!isOpen())
		{
			for (Block b: inside)
	    	{
	    		b.setData(color);
	    	}
		}
	}
	
	public void create()
	{
		byte color;
		if (orange)
			color = (byte) Util.getRightPortalColor(getOwner().getColorPreset());
		else
			color = (byte) Util.getLeftPortalColor(getOwner().getColorPreset());			

    	for (Block b: border)
    	{
    		oldBlocks.put(b.getLocation(), BlockUtil.getBlockData(b));
    		b.setType(Material.WOOL);
    		b.setData(color);
    		PortalManager.borderBlocks.put(b.getLocation(), this);
    	}
    	for (Block b: inside)
    	{
			oldBlocks.put(b.getLocation(), BlockUtil.getBlockData(b));
    	}
    	
    	if (orange)
    	{
    		if (owner.getBluePortal() == null)
    			close();
    		else
    		{
    			open();
    			owner.getBluePortal().open();
    		}
    			
    		
    	}
    	else
    	{
    		if (owner.getOrangePortal() == null)
    			close();
    		else
    		{
    			open();
    			owner.getOrangePortal().open();
    		}

    	}
    	
    	for (Block b : inside)
    	{
    		PortalManager.insideBlocks.put(b.getLocation(), this);
    		
    		for (int x = -2;x<3;x++)
    		{
    			for (int y = -2;y<3;y++)
        		{
    				for (int z = -2;z<3;z++)
    	    		{
    	    			PortalManager.awayBlocksGeneral.put(b.getRelative(x,y,z).getLocation(), this);
    	    			awayBlocks.add(b.getRelative(x,y,z).getLocation());
    	    		}
        		}
    		}
    		
    			for (int y = -2;y<3;y++)
        		{
    				for (int z = -2;z<3;z++)
    	    		{
    	    			PortalManager.awayBlocksX.put(b.getRelative(3,y,z).getLocation(), this);
    	    			PortalManager.awayBlocksX.put(b.getRelative(-3,y,z).getLocation(), this);
    	    			awayBlocks.add(b.getRelative(3,y,z).getLocation());
    	    			awayBlocks.add(b.getRelative(-3,y,z).getLocation());
    	    		}
        		}
    			
    			for (int x = -2;x<3;x++)
        		{
    				for (int z = -2;z<3;z++)
    	    		{
    	    			PortalManager.awayBlocksY.put(b.getRelative(x,3,z).getLocation(), this);
    	    			PortalManager.awayBlocksY.put(b.getRelative(x,-3,z).getLocation(), this);
    	    			awayBlocks.add(b.getRelative(x,3,z).getLocation());
    	    			awayBlocks.add(b.getRelative(x,-3,z).getLocation());
    	    		}
        		}
    			
    			for (int x = -2;x<3;x++)
        		{
    				for (int y = -2;y<3;y++)
    	    		{
    	    			PortalManager.awayBlocksZ.put(b.getRelative(x,y,3).getLocation(), this);
    	    			PortalManager.awayBlocksZ.put(b.getRelative(x,y,-3).getLocation(), this);
    	    			awayBlocks.add(b.getRelative(x,y,3).getLocation());
    	    			awayBlocks.add(b.getRelative(x,y,-3).getLocation());
    	    		}
        		}
    		    		
    	}
    		
    	
    
    		
    	
	}
	
	public Location getTeleportLocation()
	{
		return teleport;
	}
	
	public User getOwner()
	{
		return owner;
	}
	
	public HashSet<Block> getBorder()
	{
		return border;
	}
	
	public HashSet<Block> getInside()
	{
		return inside;
	}
	
	
	public Boolean isOpen()
	{
		return open;
	}
	
	public Boolean isVertical()
	{
		return vertical;
	}
	
	public BlockFace getTeleportFace()
	{
		return teleportFace;
	}
	
	public Boolean isOrange()
	{
		return orange;
	}
	
	public Boolean isDisabled()
	{
		return disabled;
	}
	
	public void setDisabled(Boolean input)
	{
		disabled = input;
	}
	
	public Boolean isTransmitter()
	{
		return transmitter;
	}
	
	public void setTransmitter(Boolean input)
	{
		transmitter = input;
	}
	
	public void setPlaceTorch(Boolean input)
	{
		placetorch = true;
	}
	public Portal getDestination()
	{
		if (isOrange())
			return(owner.getBluePortal());
		else
			return(owner.getOrangePortal());

	}

}
