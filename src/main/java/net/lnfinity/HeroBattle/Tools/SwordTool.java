package net.lnfinity.HeroBattle.Tools;

import net.lnfinity.HeroBattle.Game.GamePlayer;
import net.lnfinity.HeroBattle.HeroBattle;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class SwordTool extends PlayerTool {

	public SwordTool(HeroBattle plugin) {
		super(plugin);
	}

	@Override
	public String getToolID() {
		return "tool.sword";
	}

	@Override
	public String getName() {
		return ChatColor.RED + "Épée repoussante";
	}

	@Override
	public List<String> getDescription() {
		return Arrays.asList(ChatColor.GRAY + "Frappez les joueurs pour les repousser", ChatColor.GRAY
				+ "Clic droit pour faire un double saut");
	}

	@Override
	public ItemStack getItem() {
		ItemStack item = new ItemStack(Material.IRON_SWORD, 1);

		ItemMeta meta = item.getItemMeta();
		meta.spigot().setUnbreakable(true);
		item.setItemMeta(meta);

		return item;
	}

	@Override
	public void onRightClick(Player player, ItemStack tool, PlayerInteractEvent event) {
		GamePlayer hbPlayer = p.getGamePlayer(player);

		if (player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() != Material.AIR) {
			hbPlayer.setDoubleJump(2);
		}

		if (hbPlayer.getDoubleJump() > 0) {
			hbPlayer.setDoubleJump(hbPlayer.getDoubleJump() - 1);
			player.setVelocity(player.getVelocity().setY(1.5));
		}
	}

	@Override
	public void onLeftClick(Player player, ItemStack tool, PlayerInteractEvent event) {

	}
}