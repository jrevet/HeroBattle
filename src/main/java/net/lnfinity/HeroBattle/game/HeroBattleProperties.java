package net.lnfinity.HeroBattle.game;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.lnfinity.HeroBattle.utils.Utils;
import net.samagames.api.SamaGamesAPI;
import net.samagames.api.games.IGameProperties;
import org.bukkit.Location;

import java.util.List;


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
public class HeroBattleProperties
{
	// Rewards
	private final Integer coinsPerKill;
	private final Integer coinsPerAssist;
	private final Integer coinsPerVictory;
	private final Integer starsPerVictory;
	private final Integer coinsIfFirstRanked;
	private final Integer coinsIfSecondRanked;
	private final Integer coinsIfThirdRanked;

	// Locations
	private final Location hub;
	private final List<Location> playerSpawns;
	private final List<Location> powerupSpawns;
	private final List<Location> tutorialPOV;
	private final List<Location> teleportationPortalsDestinations;

	private final Double bottomHub;
	private final Double bottomGame;

	// Times of the day
	private final Long hubDayTime;
	private final Long gameDayTime;

	// Toxicities
	private final Boolean toxicWater;
	private final Boolean toxicLava;

	// Cosmetics
	private final Boolean marioPortalsTeleportationsSound;
	private final Boolean permanentNightVision;

	// Game properties
	private final Integer gameDuration;
	private final Boolean gameRanked;


	/**
	 * @throws InvalidConfigurationException
	 */
	public HeroBattleProperties() throws InvalidConfigurationException
	{
		IGameProperties properties = SamaGamesAPI.get().getGameManager().getGameProperties();


		/* **  Rewards  ** */

		JsonObject coinsRewards = null;
		JsonObject starsRewards = null;
		JsonObject rewards = properties.getOptions().getAsJsonObject("rewards");
		if(rewards != null)
		{
			coinsRewards = rewards.getAsJsonObject("coins");
			starsRewards = rewards.getAsJsonObject("stars");
		}

		coinsPerKill        = getInt(coinsRewards, "per-kill", 5);
		coinsPerAssist      = getInt(coinsRewards, "per-assist", 3);
		coinsPerVictory     = getInt(coinsRewards, "per-victory", 20);
		starsPerVictory     = getInt(starsRewards, "per-victory", 1);

		coinsIfFirstRanked  = getInt(coinsRewards, "if-first-ranked", 10);
		coinsIfSecondRanked = getInt(coinsRewards, "if-second-ranked", 6);
		coinsIfThirdRanked  = getInt(coinsRewards, "if-third-ranked", 4);


		/* **  Locations  ** */

		JsonObject locations = properties.getConfigs().getAsJsonObject("locations");

		hub = getLocation(locations, "hub", null);

		playerSpawns = getLocations(locations, "player-spawns");
		powerupSpawns = getLocations(locations, "powerup-spawns");
		tutorialPOV = getLocations(locations, "tutorial-points-of-views");
		teleportationPortalsDestinations = getLocations(locations, "teleportation-portals-destinations");


		JsonObject bottomAltitudes = locations.getAsJsonObject("bottom-altitude");

		bottomHub  = getDouble(bottomAltitudes, "waiting-lobby", 0d);
		bottomGame = getDouble(bottomAltitudes, "in-game", 0d);


		/* **  Times of the day  ** */

		JsonObject timesOfDay = properties.getConfigs().getAsJsonObject("times-of-day");
		hubDayTime  = getLong(timesOfDay, "waiting-lobby", 6000l);
		gameDayTime = getLong(timesOfDay, "in-game", 6000l);


		/* **  Toxicities  ** */

		JsonObject toxicities = properties.getConfigs().getAsJsonObject("toxicities");
		toxicWater = getBoolean(toxicities, "water", false);
		toxicLava  = getBoolean(toxicities, "lava", false);


		/* **  Cosmetics  ** */

		marioPortalsTeleportationsSound = getBoolean(properties.getConfigs(), "mario-portal-teleportation-sound", true);
		permanentNightVision = getBoolean(properties.getConfigs(), "permanent-night-vision", false);


		/* **  Game properties  ** */

		gameDuration = getInt(properties.getConfigs(), "game-duration", 15);
		gameRanked = getBoolean(properties.getOptions(), "ranked", true);


		/* **  - Errors -  ** */

		if(hub == null)
			throw new InvalidConfigurationException("Hub location undefined!", "arena.json", "locations.hub");

		if(playerSpawns.isEmpty())
			throw new InvalidConfigurationException("No spawn points defined!", "arena.json", "locations.player-spawns");

		if(teleportationPortalsDestinations.isEmpty())
			throw new InvalidConfigurationException("No teleportation portal locations set in arena.json. Feature disabled.", "arena.json", "locations.teleportation-portals-destinations", false);

		if(!tutorialPOV.isEmpty() && tutorialPOV.size() < 4)
		{
			tutorialPOV.clear();
			throw new InvalidConfigurationException("If set, at least 4 tutorial points of view are required (and the points after the 4th will be ignored).", "arena.json", "locations.tutorial-points-of-views", false);
		}
	}


	public Integer getCoinsPerKill()
	{
		return coinsPerKill;
	}

	public Integer getCoinsPerAssist()
	{
		return coinsPerAssist;
	}

	public Integer getCoinsPerVictory()
	{
		return coinsPerVictory;
	}

	public Integer getStarsPerVictory()
	{
		return starsPerVictory;
	}

	public Integer getCoinsIfFirstRanked()
	{
		return coinsIfFirstRanked;
	}

	public Integer getCoinsIfSecondRanked()
	{
		return coinsIfSecondRanked;
	}

	public Integer getCoinsIfThirdRanked()
	{
		return coinsIfThirdRanked;
	}

	public Location getHub()
	{
		return hub;
	}

	public List<Location> getPlayerSpawns()
	{
		return playerSpawns;
	}

	public List<Location> getPowerupSpawns()
	{
		return powerupSpawns;
	}

	public List<Location> getTutorialPOV()
	{
		return tutorialPOV;
	}

	public Double getBottomHub()
	{
		return bottomHub;
	}

	public Double getBottomGame()
	{
		return bottomGame;
	}

	public Long getHubDayTime()
	{
		return hubDayTime;
	}

	public Long getGameDayTime()
	{
		return gameDayTime;
	}

	public List<Location> getTeleportationPortalsDestinations()
	{
		return teleportationPortalsDestinations;
	}

	public Boolean getMarioPortalsTeleportationsSound()
	{
		return marioPortalsTeleportationsSound;
	}

	public Boolean getPermanentNightVision()
	{
		return permanentNightVision;
	}

	public Boolean isWaterToxic()
	{
		return toxicWater;
	}

	public Boolean isLavaToxic()
	{
		return toxicLava;
	}

	public Integer getGameDuration()
	{
		return gameDuration;
	}

	public Boolean isGameRanked()
	{
		return gameRanked;
	}


	/* **  JSON data accessors  ** */

	public Integer getInt(JsonObject root, String key, Integer defaultValue)
	{
		if(root != null && root.has(key))
			return root.get(key).getAsInt();
		else
			return defaultValue;
	}

	public Double getDouble(JsonObject root, String key, Double defaultValue)
	{
		if(root != null && root.has(key))
			return root.get(key).getAsDouble();
		else
			return defaultValue;
	}

	public Long getLong(JsonObject root, String key, Long defaultValue)
	{
		if(root != null && root.has(key))
			return root.get(key).getAsLong();
		else
			return defaultValue;
	}

	public Boolean getBoolean(JsonObject root, String key, Boolean defaultValue)
	{
		if(root != null && root.has(key))
			return root.get(key).getAsBoolean();
		else
			return defaultValue;
	}

	public Location getLocation(JsonObject root, String key, Location defaultValue)
	{
		if(root != null && root.has(key))
		{
			String rawLocation = root.get(key).getAsString();
			if(rawLocation.isEmpty())
				return defaultValue;

			try
			{
				return Utils.stringToLocation(rawLocation);
			}
			catch (IllegalArgumentException e)
			{
				return defaultValue;
			}
		}
		else
		{
			return defaultValue;
		}
	}

	public List<Location> getLocations(JsonObject root, String key)
	{
		if(root == null || !root.has(key))
			return ImmutableList.of();

		List<Location> locations;

		JsonArray rawPlayerSpawns = root.getAsJsonArray(key);
		if(rawPlayerSpawns.size() == 0)
		{
			locations = ImmutableList.of();
		}
		else
		{
			ImmutableList.Builder<Location> builder = ImmutableList.builder();

			for(JsonElement rawPlayerSpawn : rawPlayerSpawns)
			{
				Location spawn = null;
				try
				{
					spawn = Utils.stringToLocation(rawPlayerSpawn.getAsString());
				}
				catch (Exception ignored) {}

				if(spawn != null)
					builder.add(spawn);
			}

			locations = builder.build();
		}

		return locations;
	}


	public class InvalidConfigurationException extends Exception
	{
		private String file;
		private String key;
		private boolean fatal;

		/**
		 * Constructs a fatal error.
		 *
		 * @see InvalidConfigurationException#InvalidConfigurationException(String, String, String, boolean)
		 */
		public InvalidConfigurationException(String message, String file, String key)
		{
			this(message, file, key, true);
		}

		/**
		 * @param message Exception message.
		 * @param file The file where the problem is located.
		 * @param key The key where the problem is located.
		 * @param fatal {@code true} if the game cannot run with this error.
		 */
		public InvalidConfigurationException(String message, String file, String key, boolean fatal)
		{
			super(message);

			this.file  = file;
			this.key   = key;
			this.fatal = fatal;
		}

		public String getFile()
		{
			return file;
		}

		public String getKey()
		{
			return key;
		}

		public boolean isFatal()
		{
			return fatal;
		}
	}
}
