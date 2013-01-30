package com.matejdro.bukkit.portalstick;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;
import org.surgedev.util.BlockStorage;
import org.surgedev.util.SurgeLocation;

import com.matejdro.bukkit.portalstick.util.Config.Sound;
import com.matejdro.bukkit.portalstick.util.RegionSetting;

public class GelManager {
	private final PortalStick plugin;
	final HashMap<String, Float> onRedGel = new HashMap<String, Float>();
	private final HashSet<Entity> ignore = new HashSet<Entity>();
	final HashMap<String, Integer> redTasks = new HashMap<String, Integer>();
	public final HashMap<SurgeLocation, Integer> tubePids = new HashMap<SurgeLocation, Integer>();
	public final HashSet<SurgeLocation> activeGelTubes = new HashSet<SurgeLocation>();
	public final HashMap<UUID, SurgeLocation> flyingGels = new HashMap<UUID, SurgeLocation>();
	public final HashMap<SurgeLocation, ArrayList<BlockStorage>> gels = new HashMap<SurgeLocation, ArrayList<BlockStorage>>();
	public final HashMap<BlockStorage, BlockStorage> gelMap = new HashMap<BlockStorage, BlockStorage>();
	
	GelManager(PortalStick plugin)
	{
		this.plugin = plugin;
	}
	
	public void useGel(Entity entity, SurgeLocation locTo, Vector vector, Block block, Block under, HashMap<BlockFace, Block> faceMap)
	{
		Region region = plugin.regionManager.getRegion(locTo);
		
		if(region.getBoolean(RegionSetting.ENABLE_RED_GEL_BLOCKS))
		  redGel(entity, under, region);

		if (region.getBoolean(RegionSetting.ENABLE_BLUE_GEL_BLOCKS))
		{
			if(ignore.contains(entity) || (entity instanceof Player && ((Player)entity).isSneaking()))
			  return;
			String bg = region.getString(RegionSetting.BLUE_GEL_BLOCK);
			Block block2;
			for(BlockFace face: new BlockFace[] {null, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST})
			{
			  if(face == null)
				block2 = under;
			  else if(faceMap.containsKey(face))
				block2 = faceMap.get(face);
			  else
			  {
				block2 = block.getRelative(face);
				faceMap.put(face, block);
			  }
			  if(plugin.blockUtil.compareBlockToString(block2, bg))
			  {
				if(isPortal(new SurgeLocation(block2)))
				  continue;
				byte dir;
				if(face == null)
				  dir = 0;
				else
				{
				  switch(face)
				  {
				  	case EAST:
				  	case WEST:
				  	  dir = 1;
				  	  break;
				  	default:
				  	  dir = 2;
				  }
				}
				blueGel(entity, region, dir, vector, region.getDouble(RegionSetting.BLUE_GEL_MIN_VELOCITY));
				break;
			  }
			}
		}
	}
	
	private boolean isPortal(SurgeLocation vl)
	{
	  for(SurgeLocation loc: plugin.portalManager.borderBlocks.keySet())
		if(loc.equals(vl))
		  return true;
	  for(SurgeLocation loc: plugin.portalManager.insideBlocks.keySet())
		if(loc.equals(vl))
		  return true;
	  return false;
	}
	
	private void blueGel(final Entity entity, Region region, byte dir, Vector vector, double min)
	{
//		Vector vector = player.getVelocity(); //We need a self-calculated vector from the player move event as this has 0.0 everywhere.
		Location loc = entity.getLocation();
		double y = vector.getY();
		if(dir == 0)
		{
		  y = -y;
		  if(entity instanceof Player && onRedGel.containsKey(((Player)entity).getName()) && y < min)
			y = min;
		  else if(y < 0.1D)
			return;
		  if(y < min)
			y = min;
		  vector.setY(y);
		}
		else
		{
		  if(y < min/3.0D)
			vector.setY(min / 3.0D);
		  boolean m;
		  if(dir == 1)
			y = vector.getX();
		  else
			y = vector.getZ();
		  if(y == 0)
			return;
		  if(y < 0)
		  {
			m = true;
			y = -y;
		  }
		  else
			m = false;
		  if(y < min)
			y = min;
		  if(!m)
			y = -y;
		  if(dir == 1)
			vector.setX(y);
		  else
			vector.setZ(y);
		  loc.setY(loc.getY()+0.01D);
		  entity.teleport(loc);
		}
		entity.setVelocity(vector);
		
		plugin.util.playSound(Sound.GEL_BLUE_BOUNCE, new SurgeLocation(loc));
		
		ignore.add(entity);
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() { public void run() { ignore.remove(entity); }}, 5L);
	}
	
	private boolean redGel(Entity entity, Block under, Region region)
	{
	  if(!(entity instanceof Player)) // TODO
		return false;
	  
	  final Player player = (Player)entity;
	  if(isPortal(new SurgeLocation(under)))
	  {
		resetPlayer(player);
		return false;
	  }
	  
	  final String pn = player.getName();
	  String rg = region.getString(RegionSetting.RED_GEL_BLOCK);
	  
	  if(!plugin.blockUtil.compareBlockToString(under, rg))
		return false;
	  
	  BukkitScheduler s = plugin.getServer().getScheduler();
	  if(redTasks.containsKey(pn))
		s.cancelTask(redTasks.get(pn));
	  redTasks.put(pn, s.scheduleSyncDelayedTask(plugin, new Runnable(){public void run(){resetPlayer(player);}} , 10L));
	  
	  float os = player.getWalkSpeed();
	  float ns = os * (float)region.getDouble(RegionSetting.RED_GEL_VELOCITY_MULTIPLIER);
	  if(ns > (float)region.getDouble(RegionSetting.RED_GEL_MAX_VELOCITY))
		return true;
	  player.setWalkSpeed(ns);
	  if(!onRedGel.containsKey(pn))
		onRedGel.put(pn, os);
	  return true;
	}
	
	public void resetPlayer(Player player)
	{
	  String pn = player.getName();
	  if(!onRedGel.containsKey(pn))
		return;
	  player.setWalkSpeed(onRedGel.get(pn));
	  onRedGel.remove(pn);
	  redTasks.remove(pn);
	}
	
	public void stopGelTube(SurgeLocation loc)
	{
	  if(!tubePids.containsKey(loc))
		return;
	  plugin.getServer().getScheduler().cancelTask(tubePids.get(loc));
	  tubePids.remove(loc);
	  activeGelTubes.remove(loc);
	  ArrayList<BlockStorage> tc = new ArrayList<BlockStorage>();
	  Portal portal;
	  if(gels.containsKey(loc))
	  {
		for(BlockStorage bh: gels.get(loc))
		{
		  if(plugin.portalManager.insideBlocks.containsKey(loc))
		  {
			portal = plugin.portalManager.insideBlocks.get(loc);
			if(portal.open)
			  loc.getHandle().getBlock().setType(Material.AIR);
			else
			  portal.close();
		  }
		  else
			plugin.resetBlock(bh);
		  gelMap.remove(bh);
		  tc.add(bh);
		}
		gels.remove(loc);
		for(ArrayList<BlockStorage> blocks: gels.values())
		  for(BlockStorage bh: tc)
			blocks.remove(bh);
	  }
	  World world = loc.getWorld();
	  UUID uuid;
	  for(Chunk c: world.getLoadedChunks())
		for(Entity e: c.getEntities())
		{
		  uuid = e.getUniqueId();
		  if(flyingGels.containsKey(uuid))
		  {
			e.remove();
			flyingGels.remove(uuid);
		  }
		}
	}
	
	public void removeGel(BlockStorage bh)
	{
	  gelMap.remove(bh);
	  for(ArrayList<BlockStorage> blocks: gels.values())
		blocks.remove(bh);
	}
}
