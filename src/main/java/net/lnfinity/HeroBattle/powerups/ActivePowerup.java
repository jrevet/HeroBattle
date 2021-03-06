package net.lnfinity.HeroBattle.powerups;


import net.lnfinity.HeroBattle.HeroBattle;
import net.lnfinity.HeroBattle.utils.ParticleEffect;
import net.lnfinity.HeroBattle.utils.Utils;
import net.samagames.tools.Titles;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

/*
 * This file is part of HeroBattle.
 *
 * HeroBattle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * HeroBattle is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with HeroBattle.  If not, see <http://www.gnu.org/licenses/>.
 */
public class ActivePowerup
{
	HeroBattle p;

	// UUID of this specific active powerup
	private final UUID activePowerupID = UUID.randomUUID();

	// Powerup
	private final Powerup powerup;

	// Location
	private final Location location;

	// Entities of the powerup
	private Item entityItem;
	private ArmorStand entityBase;
	private ArmorStand entityTitle;

	// Async task used to display the particles
	private BukkitTask particlesTask;

	// Alive?
	private boolean alive = false;


	public ActivePowerup(final HeroBattle plugin, final Location location, final Powerup powerup)
	{
		this.p = plugin;
		this.location = location;
		this.powerup = powerup;
	}

	public void spawn()
	{
		/*** ***  ITEM AND HOLOGRAM  *** ***/

		final World world = location.getWorld();

		final ItemStack powerupItem = powerup.getItem().clone();
		final ItemMeta powerupItemMeta = powerupItem.getItemMeta();

		powerupItemMeta.setDisplayName(activePowerupID.toString());
		powerupItem.setItemMeta(powerupItemMeta);


		entityBase = world.spawn(location.clone().add(0, -0.5, 0), ArmorStand.class);
		entityBase.setVisible(false);
		entityBase.setSmall(true);
		//entityBase.setMarker(true);  // TODO Cannot simply be set as marker, we need to place the pieces manually.
		entityBase.setGravity(false);

		entityItem = world.dropItem(location, powerupItem);
		entityItem.setPickupDelay(0);

		entityTitle = world.spawn(location, ArmorStand.class);
		entityTitle.setGravity(false);
		entityTitle.setVisible(false);
		entityTitle.setSmall(true);
		//entityTitle.setMarker(true);  // TODO
		entityTitle.setCustomName(powerup.getName());
		entityTitle.setCustomNameVisible(true);
		entityTitle.setCanPickupItems(false);


		entityBase.setPassenger(entityItem);
		entityItem.setPassenger(entityTitle);


		/*** ***  EFFECTS AND BROADCAST  *** ***/

		p.getServer().broadcastMessage(HeroBattle.GAME_TAG + ChatColor.GREEN + "Un bonus vient de faire son apparition !");

		for (final Player player : p.getServer().getOnlinePlayers())
		{
			player.playSound(player.getLocation(), Sound.SUCCESSFUL_HIT, 1, 1);
			Titles.sendTitle(player, 5, 30, 5, ChatColor.DARK_GREEN + "\u272F", "");
		}

		final Location itemLocation = Utils.blockLocation(location).add(0, 1, 0);

		final Color fwColor;
		if (powerup instanceof PositivePowerup) fwColor = Color.GREEN.mixColors(Color.YELLOW);
		else fwColor = Color.RED.mixColors(Color.YELLOW);

		final Firework fw = location.getWorld().spawn(itemLocation, Firework.class);
		final FireworkMeta fwm = fw.getFireworkMeta();
		final FireworkEffect effect = FireworkEffect.builder()
				.withColor(fwColor).with(FireworkEffect.Type.BALL)
				.withFade(Color.YELLOW).build();
		fwm.addEffects(effect);
		fwm.setPower(0);
		fw.setFireworkMeta(fwm);

		Bukkit.getScheduler().runTaskLater(p, fw::detonate, 1l);


		particlesTask = Bukkit.getScheduler().runTaskTimerAsynchronously(p, () -> ParticleEffect.SPELL_INSTANT.display(0.5F, 0.5F, 0.5F, 0.1F, 2, itemLocation, 100.0), 1l, 5l);


		alive = true;
	}

	/**
	 * Removes a powerup.
	 *
	 * @param got If true the powerup is removed because someone picked-up it.
	 */
	public void remove(final boolean got)
	{
		/*** ***  ITEM AND HOLOGRAM  *** ***/

		entityTitle.remove();
		entityItem.remove();
		entityBase.remove();


		/*** ***  EFFECTS AND BROADCAST  *** ***/

		final Color fwColor = got ? Color.BLUE : Color.RED;

		final Firework fw = location.getWorld().spawn(Utils.blockLocation(location).add(0, 1, 0), Firework.class);
		final FireworkMeta fwm = fw.getFireworkMeta();
		final FireworkEffect effect = FireworkEffect.builder()
				.withColor(fwColor).with(FireworkEffect.Type.BALL).build();
		fwm.addEffects(effect);
		fwm.setPower(0);
		fw.setFireworkMeta(fwm);

		Bukkit.getScheduler().runTaskLater(p, fw::detonate, 1l);

		particlesTask.cancel();


		alive = false;
	}


	public boolean isAlive()
	{
		return alive;
	}

	public Powerup getPowerup()
	{
		return powerup;
	}

	public Location getLocation()
	{
		return location;
	}

	public UUID getActivePowerupUniqueID()
	{
		return activePowerupID;
	}
}
