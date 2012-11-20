package com.matejdro.bukkit.portalstick;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

public class UserManager {
	private final PortalStick plugin;
	private final HashMap<String, User> playerUsers = new HashMap<String, User>();
	private final HashMap<UUID, User> entityUsers = new HashMap<UUID, User>();
	
	UserManager(PortalStick plugin)
	{
		this.plugin = plugin;
	}
	
	public void createUser(Entity entity)
	{
	  if(entity instanceof Player)
		playerUsers.put(((Player)entity).getName(), new User(entity));
	  else
	  {
		UUID uuid = entity.getUniqueId();
		entityUsers.put(uuid, new User(entity));
		plugin.entityManager.oldLocations.put(uuid, entity.getLocation());
	  }
	}
	
	public User getUser(Entity entity)
	{
	  if(entity instanceof Player)
		return getUser(((Player)entity).getName());
	  else
		return getUser(entity.getUniqueId());
	}
	
	public User getUser(UUID uuid)
	{
	  return entityUsers.get(uuid);
	}
	
	public User getUser(String player)
	{
		return playerUsers.get(player);
	}

	public void deleteUser(Entity entity)
	{
	  deleteUser(getUser(entity));
	  plugin.entityManager.oldLocations.remove(entity.getUniqueId());
	}
	
	public void deleteUser(User user) {
		plugin.portalManager.deletePortals(user);
		deleteDroppedItems(user);
		playerUsers.values().remove(user);
		entityUsers.values().remove(user);
	}

	public void deleteDroppedItems(Player player) {
		deleteDroppedItems(getUser(player));
	}
	
	public void deleteDroppedItems(User user) {
		if (user != null && user.droppedItems != null) {
			for (Item item : user.droppedItems)
				if (item != null)
					item.remove();
			user.droppedItems.clear();
		}
	}

	public List<User> getUsers()
	{
	  List<User> ret = new ArrayList<User>(playerUsers.values());
	  for(User user: entityUsers.values())
		ret.add(user);
	  return ret;
	}
}
