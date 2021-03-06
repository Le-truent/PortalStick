package de.V10lator.PortalStick;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.libigot.LibigotLocation;

import de.V10lator.PortalStick.util.RegionSetting;

public class FunnelBridgeManager {
	private final PortalStick plugin;
	
	FunnelBridgeManager(PortalStick plugin)
	{
	  this.plugin = plugin;
	}
	
	public HashSet<Bridge> bridges = new HashSet<Bridge>();
	public HashMap<Portal, Bridge> involvedPortals = new HashMap<Portal, Bridge>();
	public HashMap<LibigotLocation, Bridge> bridgeBlocks = new HashMap<LibigotLocation, Bridge>();
	public HashMap<LibigotLocation, Bridge> bridgeMachineBlocks = new HashMap<LibigotLocation, Bridge>();
//	private HashSet<Entity> inFunnel = new HashSet<Entity>();
	HashMap<Entity, List<LibigotLocation>> glassBlocks = new HashMap<Entity, List<LibigotLocation>>();
//	private HashMap<LibigotLocation, Entity> glassBlockOwners = new HashMap<LibigotLocation, Entity>();

	public boolean placeGlassBridge(Player player, LibigotLocation first)
	{
		if (player != null && !plugin.hasPermission(player, plugin.PERM_CREATE_BRIDGE))
		  return false;
		
		Region region = plugin.regionManager.getRegion(first);
		if (!region.getBoolean(RegionSetting.ENABLE_HARD_GLASS_BRIDGES))
		  return false;
		
		HashSet<LibigotLocation> machineBlocks = new HashSet<LibigotLocation>();

		//Check if two blocks are iron
		if (!plugin.blockUtil.compareBlockToString(first, region.getString(RegionSetting.HARD_GLASS_BRIDGE_BASE_MATERIAL)) && !plugin.blockUtil.compareBlockToString(first, region.getString(RegionSetting.FUNNEL_BASE_MATERIAL))) return false;
		BlockFace face = null;
		Block firstIron = first.getHandle().getBlock();
		for (BlockFace check : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST})
		{
			if (plugin.blockUtil.compareBlockToString(firstIron.getRelative(check).getRelative(check), region.getString(RegionSetting.HARD_GLASS_BRIDGE_BASE_MATERIAL)) || plugin.blockUtil.compareBlockToString(firstIron.getRelative(check).getRelative(check), region.getString(RegionSetting.FUNNEL_BASE_MATERIAL)))
			{
				face = check;
				break;
			}
		}
		
		if (face == null) return false;
		
		Block startingBlock = firstIron.getRelative(face);
		Block secondIron = startingBlock.getRelative(face);
		
		machineBlocks.add(new LibigotLocation(firstIron));
		machineBlocks.add(new LibigotLocation(secondIron));
		
		//Check if two irons have redstone torches on them
		Boolean havetorch = false;
		for (BlockFace check : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP})
		{
			if (firstIron.getRelative(check).getType() == Material.REDSTONE_TORCH_ON)
			{
				havetorch = true;
				machineBlocks.add(new LibigotLocation(firstIron.getRelative(check)));
				break;
			}
		}
		if (!havetorch) return false;
		havetorch = false;
		for (BlockFace check : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP})
		{
			if (secondIron.getRelative(check).getType() == Material.REDSTONE_TORCH_ON)
			{
				havetorch = true;
				machineBlocks.add(new LibigotLocation(secondIron.getRelative(check)));
				break;
			}
		}
		if (!havetorch) return false;
		
		//Which way should we create bridge to
		face = null;
		for (BlockFace check : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.DOWN, BlockFace.UP})
		{
			if (startingBlock.getRelative(check).isEmpty() || startingBlock.getRelative(check).isLiquid())
			{
				face = check;
				break;
			}
		}
		if (face == null) return false;
		
		Bridge bridge;
		first = new LibigotLocation(firstIron);
		if (plugin.blockUtil.compareBlockToString(firstIron, region.getString(RegionSetting.HARD_GLASS_BRIDGE_BASE_MATERIAL)))
			bridge = new Bridge(plugin, first, new LibigotLocation(startingBlock), face, machineBlocks);
		else
			bridge = new Funnel(plugin, first, new LibigotLocation(startingBlock), face, machineBlocks);
		bridge.activate();
		
		for (LibigotLocation b: machineBlocks)
			bridgeMachineBlocks.put(b, bridge);
		bridges.add(bridge);
		plugin.config.saveAll();
		return true;
	}
	
	public void reorientBridge(Portal portal)
	{
		Bridge bridge = involvedPortals.get(portal);
		if (bridge != null)
			bridge.activate();
		
		for (Bridge cbridge : bridges)
		{
			for (LibigotLocation b: portal.coord.inside)
			{
			  if(b != null && cbridge.isBlockNextToBridge(b))
				cbridge.activate();
			}
			for (LibigotLocation b: portal.coord.border)
			{
				if (cbridge.isBlockNextToBridge(b))
					cbridge.activate();
			}
		}
	}
	
	public void updateBridge(final LibigotLocation block)
	{
		//delay to make sure all blocks have updated
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

		    public void run() {
		    	for (Bridge cbridge : bridges)
				{
					if (cbridge.isBlockNextToBridge(block))
						cbridge.activate();
				}
		    }
		}, 1L);
		
	}
	
	public void loadBridge(String blockloc) {
		String[] locarr = blockloc.split(",");
		if (!placeGlassBridge(null, new LibigotLocation(plugin.getServer().getWorld(locarr[0]).getBlockAt((int)Double.parseDouble(locarr[1]), (int)Double.parseDouble(locarr[2]), (int)Double.parseDouble(locarr[3])))))
			plugin.config.deleteBridge(blockloc);
	}
	
	public void deleteAll()
	{
		for (Bridge bridge: bridges.toArray(new Bridge[0]))
			bridge.deactivate();
	}
	
/*	public Funnel getFunnelInEntity(Entity entity)
	{
		Bridge bridge = bridgeBlocks.get(new LibigotLocation(entity.getLocation()));
		if (bridge == null && ((entity.getLocation().getZ() - (double) entity.getLocation().getBlockZ()) < 0.5)) bridge = bridgeBlocks.get(new LibigotLocation(entity.getLocation().getBlock().getRelative(0,0,-1)));
		if (bridge == null && ((entity.getLocation().getZ() - (double) entity.getLocation().getBlockZ()) > 0.5)) bridge = bridgeBlocks.get(new LibigotLocation(entity.getLocation().getBlock().getRelative(0,0,1)));
		if (bridge == null && ((entity.getLocation().getX() - (double) entity.getLocation().getBlockX()) < 0.5)) bridge = bridgeBlocks.get(new LibigotLocation(entity.getLocation().getBlock().getRelative(-1,0,0)));
		if (bridge == null && ((entity.getLocation().getX() - (double) entity.getLocation().getBlockX()) > 0.5)) bridge = bridgeBlocks.get(new LibigotLocation(entity.getLocation().getBlock().getRelative(1,0,0)));

		if (bridge == null)
		{
			Location loc = entity.getLocation();
			for (int i = 1; i < 6; i++)
			{
				loc.subtract(0, 1, 0);
				
				bridge = bridgeBlocks.get(loc.getBlock());
				if (bridge == null && ((loc.getZ() - (double) loc.getBlockZ()) < 0.5)) bridge = bridgeBlocks.get(new LibigotLocation(loc.getBlock().getRelative(0,0,-1)));
				if (bridge == null && ((loc.getZ() - (double) loc.getBlockZ()) > 0.5)) bridge = bridgeBlocks.get(new LibigotLocation(loc.getBlock().getRelative(0,0,1)));
				if (bridge == null && ((loc.getX() - (double) loc.getBlockX()) < 0.5)) bridge = bridgeBlocks.get(new LibigotLocation(loc.getBlock().getRelative(-1,0,0)));
				if (bridge == null && ((loc.getX() - (double) loc.getBlockX()) > 0.5)) bridge = bridgeBlocks.get(new LibigotLocation(loc.getBlock().getRelative(1,0,0)));

				if (bridge != null && bridge instanceof Funnel)
				{
					List<LibigotLocation> list = glassBlocks.get(entity);
					if (list == null)
					{
						list = new ArrayList<LibigotLocation>();
						glassBlocks.put(entity, list);
					}
						
					Block block = entity.getLocation().getBlock().getRelative(BlockFace.DOWN, i + 1);
					if (block.isEmpty())
					{
						block.setType(Material.GLASS);
						list.add(new LibigotLocation(block));
					}
					
					break;

				}
			}
		}
		
		if (bridge != null && bridge instanceof Funnel)
			return (Funnel) bridge;
		else
			return null;
	}
	
	public void EntityMoveCheck(Entity entity)
	{
		Funnel funnel = getFunnelInEntity(entity);
		if (funnel == null && inFunnel.contains(entity))
		{
			EntityExitsFunnel(entity);
		}
		else if (funnel != null)
		{
			if (!inFunnel.contains(entity)) EntityEntersFunnel(entity);
			EntityMoveInFunnel(entity, funnel);
		}
	}
	
	private void EntityEntersFunnel(Entity entity)
	{
		inFunnel.add(entity);
		List<LibigotLocation> list = glassBlocks.get(entity);
		if (list == null)
			glassBlocks.put(entity, new ArrayList<LibigotLocation>());
	}
	
	public void EntityExitsFunnel(Entity entity)
	{
		List<LibigotLocation> list = glassBlocks.get(entity);
		if (list != null) 
			for (LibigotLocation b : list)
				b.getHandle().getBlock().setType(Material.AIR);
		inFunnel.remove(entity);
	
	}

	private void EntityMoveInFunnel(Entity entity, Funnel funnel)
	{
		BlockFace face = funnel.getDirection(entity);
		if (face == null) return;
				
		if (face == BlockFace.UP)
			entity.setVelocity(entity.getVelocity().setY(0.2));
		else if (face == BlockFace.DOWN)
			entity.setVelocity(entity.getVelocity().setY(-0.2));
		else
		{
			if (face.getModX() != 0) entity.setVelocity(entity.getVelocity().setX(((double)face.getModX()) * 0.2));
			if (face.getModZ() != 0) entity.setVelocity(entity.getVelocity().setZ(((double)face.getModZ()) * 0.2));
			
			//Generate glass
			
			Block pblock = entity.getLocation().getBlock().getRelative(BlockFace.DOWN);
			
			if (face != BlockFace.UP && face != BlockFace.DOWN && funnel.bridgeBlocks.containsKey(new LibigotLocation(pblock.getRelative(BlockFace.UP))))
			{
				if (pblock.getRelative(face).getType() == Material.AIR) 
				{
						Block block = pblock.getRelative(face);
						block.setType(Material.GLASS);
						LibigotLocation loc = new LibigotLocation(block);
						glassBlocks.get(entity).add(loc);
						glassBlockOwners.put(loc, entity);
				}
				else if (pblock.getRelative(face).getType() == Material.GLASS)
				{
					glassBlockOwners.put(new LibigotLocation(pblock.getRelative(face)), entity);
				}
								
				if (pblock.getRelative(face, 2).getType() == Material.AIR) 
				{
						Block block = pblock.getRelative(face, 2);
						block.setType(Material.GLASS);
						LibigotLocation loc = new LibigotLocation(block);
						glassBlocks.get(entity).add(loc);
						glassBlockOwners.put(loc, entity);
				}
				else if (pblock.getRelative(face, 2).getType() == Material.GLASS)
				{
					glassBlockOwners.put(new LibigotLocation(pblock.getRelative(face, 2)), entity);
				}
				Block block;
				for (LibigotLocation loc : glassBlocks.get(entity).toArray(new LibigotLocation[0]))
				{
					if (loc.getHandle().distanceSquared(entity.getLocation()) > 4) 
					{
						block = loc.getHandle().getBlock();
						if (glassBlockOwners.get(block) == entity)
						{
							block.setType(Material.AIR);
							glassBlocks.get(entity).remove(block);
						}
						if (block.getType() == Material.AIR) glassBlocks.get(entity).remove(block);
						
					}
				}
			}
		}
	}*/
}
