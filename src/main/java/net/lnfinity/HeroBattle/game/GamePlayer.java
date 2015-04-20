package net.lnfinity.HeroBattle.game;

import net.lnfinity.HeroBattle.HeroBattle;
import net.lnfinity.HeroBattle.classes.PlayerClass;
import net.lnfinity.HeroBattle.tasks.Task;
import net.lnfinity.HeroBattle.utils.ActionBar;
import net.md_5.bungee.api.ChatColor;
import net.samagames.gameapi.json.Status;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.StringUtil;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class GamePlayer {

	private UUID playerID;
	private String playerName;
	
	private int originalElo = 0;
	private int Elo = 0;

	private PlayerClass classe = null;
	private int jumps = 2;
	private int maxJumps = 2;
	private int percentage = 0;
	private int lives = 3;
	private boolean playing = true;
	private boolean doubleDamages = false;
	private boolean isInvisible = false;
	private boolean isInvulnerable = false;
	private boolean isRespawning = false;
	private UUID lastDamager = null;
	private List<PlayerClass> avaible = new ArrayList<PlayerClass>();
	private List<Task> tasks = new ArrayList<Task>();

	/**
	 * Avoid the death to be handled multiple times.
	 */
	private boolean deathHandled = false;

	/**
	 * The jumps left count can only be reset every ten ticks
	 * to avoid (n+1)th jumps.
	 */
	private boolean jumpsCountLocked = false;

	/**
	 * When a jump is in progress, no concurrent jump can be done
	 * by this player.
	 */
	private boolean jumpLocked = false;


	private long percentageInflicted = 0l;
	private int playersKilled = 0;


	public GamePlayer(UUID id) {
		playerID = id;
		playerName = Bukkit.getServer().getPlayer(id).getName();
	}

	public int getJumps() {
		return jumps;
	}

	public void setJumps(int jumps) {
		// The jumps cannot be reset when locked; they are locked ten ticks after
		// a call to this setJumps method.
		// This to avoid the PlayerMoveEvent to reset this at the beginning
		// of the jump, when the player is close to the ground.
		if(jumpsCountLocked && jumps >= getJumps()) return;

		this.jumps = jumps;


		// Lock manager
		jumpsCountLocked = true;
		Bukkit.getScheduler().runTaskLater(HeroBattle.getInstance(), new Runnable() {
			@Override
			public void run() {
				jumpsCountLocked = false;
			}
		}, 10l);


		// If there isn't any jump left, the fly mode is removed, so the PlayerToggleFlyEvent
		// is not called anymore and the player falls more naturally
		if(getJumps() <= 0) {
			Bukkit.getPlayer(this.getPlayerUniqueID()).setAllowFlight(false);
		}
	}

	public int getMaxJumps() {
		return maxJumps;
	}

	public void setMaxJumps(int maxJumps) {
		this.maxJumps = maxJumps;

		updateNotificationAboveInventory();
	}

	public int getPercentage() {
		return percentage;
	}

	public void setPercentage(int percentage) {
		setPercentage(percentage, null);
	}

	public void setPercentage(int percentage, GamePlayer aggressor) {
		if(isInvulnerable() && percentage >= this.percentage) return;

		int oldPercentage = this.percentage;
		this.percentage = percentage;

		if(aggressor != null) aggressor.addPercentageInflicted(percentage - oldPercentage);


		Player player = Bukkit.getPlayer(playerID);

		if(getPercentage() >= getPlayerClass().getMaxResistance()) {
			if(aggressor != null) setLastDamager(aggressor.getPlayerUniqueID());

			HeroBattle.getInstance().getGame().onPlayerDeath(playerID, DeathType.KO);

			if(player != null) {
				player.getWorld().playEffect(player.getLocation(), Effect.EXPLOSION_LARGE, 10);

				player.setLevel(0);
			}
		}

		else {
			if (player != null) {

				player.setLevel(0);
				player.setTotalExperience(0);

				player.setLevel(getPercentage());
				player.setExp(((float) getPercentage()) / ((float) getPlayerClass().getMaxResistance()));

				HeroBattle.getInstance().getGame().updatePlayerArmor(player);
			}
		}

		HeroBattle.getInstance().getScoreboardManager().update(this);
	}

	public int getLives() {
		return lives;
	}

	public void setLives(int lives) {
		this.lives = lives;
	}

	public boolean isPlaying() {
		return playing;
	}

	public void setPlaying(boolean bool) {
		playing = bool;
	}

	public boolean hasDoubleDamages() {
		return doubleDamages;
	}

	public void setDoubleDamages(boolean doubleDamages) {
		this.doubleDamages = doubleDamages;

		updateNotificationAboveInventory();
	}

	public boolean isInvisible() {
		return isInvisible;
	}

	public void setInvisible(boolean isInvisible) {
		this.isInvisible = isInvisible;

		updateNotificationAboveInventory();
	}

	public boolean isInvulnerable() {
		return isInvulnerable;
	}

	public void setInvulnerable(boolean isInvulnerable) {
		this.isInvulnerable = isInvulnerable;

		updateNotificationAboveInventory();
	}

	public UUID getLastDamager() {
		return lastDamager;
	}

	public void setLastDamager(UUID lastDamager) {
		this.lastDamager = lastDamager;
	}

	public PlayerClass getPlayerClass() {
		return classe;
	}

	public void setPlayerClass(PlayerClass classe) {
		this.classe = classe;
		if (classe != null) {
			lives = classe.getLives();
		} else {
			lives = 3;
		}

		if(classe != null) {
			ActionBar.sendPermanentMessage(Bukkit.getPlayer(playerID), ChatColor.GREEN + "Classe sélectionnée : " + ChatColor.DARK_GREEN + classe.getName());
		}
		else {
			ActionBar.sendPermanentMessage(Bukkit.getPlayer(playerID), ChatColor.GREEN + "Classe sélectionnée : " + ChatColor.DARK_GREEN + "aléatoire");
		}
	}

	public List<Task> getTasks() {
		return tasks;
	}

	public UUID getPlayerUniqueID() {
		return playerID;
	}

	public String getPlayerName() {
		return playerName;
	}

	public void doubleJump() {

		if(jumpLocked) return; // nop


		Player player = Bukkit.getServer().getPlayer(playerID);

		if (player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() != Material.AIR) {
			setJumps(maxJumps);
		}

		if (getJumps() > 0) {

			// The jump is locked when the velocity is applied, to avoid the player to
			// do dozens of jumps by spam-right-clicking or double-jumping.
			// The jumps count left is not reset directly but ten ticks after, to avoid it being
			// overwritten by the PlayerMoveEvent checking if the player is on the ground.
			final int futureJumps = getJumps() - 1;
			jumpLocked = true;
			Bukkit.getScheduler().runTaskLater(HeroBattle.getInstance(), new Runnable() {
				@Override
				public void run() {
					if(getJumps() == futureJumps + 1) {
						setJumps(futureJumps);
						jumpLocked = false;
					}
				}
			}, 6l);

			// The velocity is applied
			Vector direction = player.getLocation().getDirection().multiply(0.5);
			Vector vector = new Vector(direction.getX(), 0.85, direction.getZ());
			player.setVelocity(vector);
		}
	}

	public boolean playTask(Task t) {
		boolean min = false;
		for (int i = 0; i < tasks.size(); i++) {
			if (tasks.get(i).getClass() == t.getClass()) {
				tasks.get(i).playTask();
				tasks.remove(i);
				min = true;
			}
		}
		return min;
	}

	public void addTask(Task t) {
		tasks.add(t);
	}

	public boolean hasTask(Task t) {
		for (int i = 0; i < tasks.size(); i++) {
			if (tasks.get(i).getClass() == t.getClass()) {
				return true;
			}
		}
		return false;
	}

	public List<PlayerClass> getAvaibleClasses() {
		return avaible;
	}

	public void setAvaibleClasses(List<PlayerClass> avaible) {
		this.avaible = avaible;
	}
	
	public void addAvaibleClass(PlayerClass theClass) {
		this.avaible.add(theClass);
	}
	
	public int getOriginalElo() {
		return originalElo;
	}

	public void setOriginalElo(int originalElo) {
		this.originalElo = originalElo;
	}

	public int getElo() {
		return Elo;
	}

	public void setElo(int elo) {
		Elo = elo;
	}

	public long getPercentageInflicted() {
		return percentageInflicted;
	}

	public void addPercentageInflicted(long percentageInflicted) {
		this.percentageInflicted += percentageInflicted;
	}

	public int getPlayersKilled() {
		return playersKilled;
	}

	public void addPlayersKilled() {
		this.playersKilled++;
	}

	public boolean isDeathHandled() {
		return deathHandled;
	}

	public void setDeathHandled(boolean deathHandled) {
		this.deathHandled = deathHandled;
	}

	public boolean isRespawning() {
		return isRespawning;
	}

	public void setRespawning(boolean isRespawning) {
		this.isRespawning = isRespawning;
	}

	public void setJumpLocked(boolean jumpLocked) {
		this.jumpLocked = jumpLocked;
	}

	private void updateNotificationAboveInventory() {

		// Displays the selected class
		if(HeroBattle.getInstance().getGame().getStatus() == Status.InGame) {

			Player player = Bukkit.getPlayer(playerID);
			if(player == null || !player.isOnline()) return;


			List<String> currentStatus = new ArrayList<>();

			if(getMaxJumps() != 2) {
				if(getMaxJumps() == 3) currentStatus.add(ChatColor.RED + "Triple sauts");
				else                   currentStatus.add(ChatColor.RED + "Sauts : " + getMaxJumps() + "×");
			}

			if(hasDoubleDamages()) {
				currentStatus.add(ChatColor.DARK_GREEN + "Double dommages");
			}

			if(isInvisible()) {
				currentStatus.add(ChatColor.GRAY + "Invisible");
			}

			if(isInvulnerable()) {
				currentStatus.add(ChatColor.LIGHT_PURPLE + "Invulnérable");
			}


			if(currentStatus.size() == 0) {
				ActionBar.removeMessage(player, true);
			}
			else {
				ActionBar.sendPermanentMessage(player, StringUtils.join(currentStatus, ChatColor.DARK_GRAY + " - " + ChatColor.RESET));
			}
		}
	}
}
