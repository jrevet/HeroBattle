package net.lnfinity.HeroBattle.gui;

import net.lnfinity.HeroBattle.HeroBattle;
import net.lnfinity.HeroBattle.classes.EasterEggClass;
import net.lnfinity.HeroBattle.classes.PlayerClass;
import net.lnfinity.HeroBattle.game.HeroBattlePlayer;
import net.lnfinity.HeroBattle.gui.core.ActionGui;
import net.lnfinity.HeroBattle.gui.core.Gui;
import net.lnfinity.HeroBattle.gui.core.GuiUtils;
import net.lnfinity.HeroBattle.tools.PlayerTool;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
public class ClassDetailsGui extends ActionGui
{

	private final PlayerClass displayedClass;

	/**
	 * True if the class can be selected from this GUI. False for easter-egg classes and locked
	 * ones.
	 */
	private Boolean canBeUsed;

	/**
	 * True only if opened from the selection GUI.
	 */
	private Boolean fromSelectionGUI;

	/**
	 * True if, when opened, the game accepts a class change.
	 */
	private Boolean acceptClassChange;

	private HeroBattlePlayer gPlayer = null;


	public ClassDetailsGui(PlayerClass displayedClass, Boolean fromSelectionGUI)
	{
		Validate.notNull(displayedClass, "Cannot display the `null` class!");

		this.displayedClass = displayedClass;
		this.fromSelectionGUI = fromSelectionGUI;

		acceptClassChange = !HeroBattle.get().getGame().isGameStarted();
	}

	public ClassDetailsGui(PlayerClass displayedClass)
	{
		this(displayedClass, false);
	}


	/* **  Display part  ** */

	@Override
	protected void onUpdate()
	{
		gPlayer = HeroBattle.get().getGamePlayer(getPlayer().getUniqueId());
		if (gPlayer == null)
			throw new IllegalStateException("Cannot open the selector GUI of a non-player! - UUID: " + getPlayer().getUniqueId());


		if (displayedClass instanceof EasterEggClass)
			canBeUsed = false;
		else
			canBeUsed = HeroBattle.get().getClassManager().playerHasClass(gPlayer, displayedClass.getType());


		// Displayed in a line with n items (n != 4);
		// with the first tool (weapon) centered on another line with 4 tools.
		List<PlayerTool> tools = new ArrayList<>(displayedClass.getTools());
		Integer slotShift = (tools.size() == 4 ? 9 : 0);


		setTitle(ChatColor.BLACK + "Classe " + displayedClass.getName());
		setSize(45 + slotShift);


		if (tools.size() == 4)
			action("", 13, tools.remove(0).generateCompleteItem());

		int slot = 9 + slotShift + GuiUtils.calculateShiftNeededToCenter(tools.size());
		for (PlayerTool tool : tools)
		{
			action("", slot, tool.generateCompleteItem());
			slot++;

			// Special case to center the tools if they are an even number.
			if (tools.size() % 2 == 0 && slot % 9 == 4)
				slot++;
		}


		Integer slotReview = !acceptClassChange && !fromSelectionGUI ? 31 : 29;
		Integer slotUse = fromSelectionGUI ? 31 : 33;
		Integer slotBack = 33;

		slotReview += slotShift;
		slotUse += slotShift;
		slotBack += slotShift;


		action("", slotReview, getClassReviewItem());

		if (acceptClassChange)
			action("use", slotUse, getSelectClassButton());

		if (fromSelectionGUI) // implies acceptClassChange
			action("back", slotBack, getBackButton());
	}

	private ItemStack getClassReviewItem()
	{
		ItemStack item = displayedClass.getHat().clone();
		ItemMeta meta = item.getItemMeta();

		meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Classe " + displayedClass.getName());
		meta.setLore(displayedClass.getClassDetailsLore());

		item.setItemMeta(meta);
		return item;
	}

	private ItemStack getSelectClassButton()
	{
		Material button;

		if (displayedClass instanceof EasterEggClass)
		{
			button = Material.RECORD_11;
		}
		else if (HeroBattle.get().getClassManager().playerHasClass(gPlayer, displayedClass.getType()))
		{
			button = Material.RECORD_5;
		}
		else
		{
			button = Material.RECORD_4;
		}

		ItemStack item = new ItemStack(button);
		ItemMeta meta = item.getItemMeta();

		if (canBeUsed)
		{
			meta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Utiliser cette classe");

			meta.setLore(
					Arrays.asList(
							ChatColor.GRAY + "Clic droit pour sélectionner",
							ChatColor.GRAY + "la classe " + displayedClass.getName()
					)
			);
		}
		else
		{
			meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Indisponible");

			List<String> lore = new ArrayList<>();

			lore.add(ChatColor.GRAY + "Vous ne pouvez pas sélectionner");
			lore.add(ChatColor.GRAY + "la classe " + displayedClass.getName() + ".");

			if (displayedClass instanceof EasterEggClass)
			{
				lore.add("");
				lore.add(ChatColor.WHITE + "Envie d'easter-eggs ?");
				lore.add(ChatColor.GRAY + "C'est à vous de trouver comment");
				lore.add(ChatColor.GRAY + "débloquer cette classe !");
			}

			meta.setLore(lore);
		}

		GuiUtils.hideItemAttributes(meta);

		item.setItemMeta(meta);

		return item;
	}

	/**
	 * Returns the item (door) which, when clicked, displays back the list of the classes.
	 *
	 * @return The item.
	 */
	private ItemStack getBackButton()
	{
		ItemStack item = new ItemStack(Material.WOOD_DOOR);

		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.BLUE + "" + ChatColor.BOLD + "Revenir au choix des classes");
		meta.setLore(Collections.singletonList(ChatColor.GRAY + "Clic droit pour revenir"));
		item.setItemMeta(meta);

		return item;
	}


	/* **  Actions  ** */

	protected void action_use()
	{
		if (canBeUsed)
		{
			HeroBattle.get().getClassManager().setPlayerClass(((Player) getPlayer()), displayedClass, true);
			close();
		}
	}

	protected void action_back()
	{
		Gui.open(getPlayer(), new ClassSelectorGui());
	}
}
