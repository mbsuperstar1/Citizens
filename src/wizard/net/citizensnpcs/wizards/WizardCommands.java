package net.citizensnpcs.wizards;

import net.citizensnpcs.PermissionManager;
import net.citizensnpcs.commands.CommandHandler;
import net.citizensnpcs.resources.npclib.HumanNPC;
import net.citizensnpcs.resources.sk89q.Command;
import net.citizensnpcs.resources.sk89q.CommandContext;
import net.citizensnpcs.resources.sk89q.CommandPermissions;
import net.citizensnpcs.resources.sk89q.CommandRequirements;
import net.citizensnpcs.utils.HelpUtils;
import net.citizensnpcs.utils.StringUtils;
import net.citizensnpcs.wizards.WizardManager.WizardMode;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandRequirements(
		requireSelected = true,
		requireOwnership = true,
		requiredType = "wizard")
public class WizardCommands implements CommandHandler {

	@CommandRequirements()
	@Command(
			aliases = "wizard",
			usage = "help",
			desc = "view the wizard help page",
			modifiers = "help",
			min = 1,
			max = 1)
	@CommandPermissions("wizard.use.help")
	public static void wizardHelp(CommandContext args, CommandSender sender,
			HumanNPC npc) {
		HelpUtils.sendWizardHelp(sender);
	}

	@Command(
			aliases = "wizard",
			usage = "mode [mode]",
			desc = "change the mode of a wizard",
			modifiers = "mode",
			min = 2,
			max = 2)
	@CommandPermissions("wizard.modify.mode")
	public static void mode(CommandContext args, Player player, HumanNPC npc) {
		WizardMode wizardMode;
		if (WizardMode.parse(args.getString(1)) != null) {
			wizardMode = WizardMode.parse(args.getString(1));
			Wizard wizard = npc.getType("wizard");
			if (wizardMode != wizard.getMode()) {
				wizard.setMode(wizardMode);
				player.sendMessage(StringUtils.wrap(npc.getStrippedName()
						+ "'s")
						+ " mode was set to "
						+ StringUtils
								.wrap(wizardMode.name().toLowerCase() + "")
						+ ".");
			} else {
				player.sendMessage(ChatColor.RED + npc.getStrippedName()
						+ " is already that mode.");
			}
		} else {
			player.sendMessage(ChatColor.RED
					+ "That is not a valid wizard mode.");
		}
	}

	@Command(
			aliases = "wizard",
			usage = "status",
			desc = "display the status of a wizard",
			modifiers = "status",
			min = 1,
			max = 1)
	@CommandPermissions("wizard.use.status")
	public static void status(CommandContext args, Player player, HumanNPC npc) {
		player.sendMessage(ChatColor.YELLOW
				+ StringUtils.listify(ChatColor.GREEN + npc.getStrippedName()
						+ "'s Wizard Status" + ChatColor.YELLOW));
		Wizard wizard = npc.getType("wizard");
		player.sendMessage(ChatColor.GREEN + "    Mode: "
				+ StringUtils.wrap(wizard.getMode()));
		String mana = "" + wizard.getMana();
		if (wizard.hasUnlimitedMana()) {
			mana = "unlimited";
		}
		player.sendMessage(ChatColor.GREEN + "    Mana: "
				+ StringUtils.wrap(mana));
	}

	@Command(
			aliases = "wizard",
			usage = "addloc [location]",
			desc = "add a location to a wizard",
			modifiers = "addloc",
			min = 2,
			max = 2)
	@CommandPermissions("wizard.modify.addloc")
	public static void addLocation(CommandContext args, Player player,
			HumanNPC npc) {
		player.sendMessage(ChatColor.GREEN + "Added current location to "
				+ StringUtils.wrap(npc.getStrippedName()) + ChatColor.GREEN
				+ " as " + StringUtils.wrap(args.getString(1)) + ".");
		((Wizard) npc.getType("wizard")).addLocation(player.getLocation(),
				args.getString(1));
	}

	@Command(
			aliases = "wizard",
			usage = "removeloc [location]",
			desc = "remove a location from a wizard",
			modifiers = "removeloc",
			min = 2,
			max = 2)
	@CommandPermissions("wizard.modify.removeloc")
	public static void removeLocation(CommandContext args, Player player,
			HumanNPC npc) {
		Wizard wizard = npc.getType("wizard");
		String locations[] = wizard.getLocations().split(":");
		String newLoc = "";
		String removedName = "";
		for (int i = 0; i < locations.length; i++) {
			if (i + 1 != Integer.parseInt(args.getString(1))) {
				newLoc = newLoc + locations[i];
			} else {
				removedName = locations[i].split(",")[0].replace("(", "");
			}
		}
		wizard.cycle(npc, WizardMode.TELEPORT);
		wizard.setLocations(newLoc);
		player.sendMessage(StringUtils.wrap(npc.getStrippedName())
				+ " has amnesia and forgot about "
				+ StringUtils.wrap(removedName) + ".");
	}

	@Command(
			aliases = "wizard",
			usage = "locations",
			desc = "view the locations of a wizard",
			modifiers = "locations",
			min = 1,
			max = 1)
	@CommandPermissions("wizard.use.locations")
	public static void locations(CommandContext args, Player player,
			HumanNPC npc) {
		Wizard wizard = npc.getType("wizard");
		player.sendMessage(ChatColor.GREEN
				+ StringUtils.listify(StringUtils.wrap(npc.getStrippedName()
						+ "'s Wizard Locations")));
		String locations[] = wizard.getLocations().split(":");
		for (int i = 0; i < locations.length; i++) {
			player.sendMessage(ChatColor.YELLOW + "" + (i + 1)
					+ ChatColor.GREEN + ": "
					+ locations[i].split(",")[0].replace("(", ""));
		}
	}

	@Command(
			aliases = "wizard",
			usage = "unlimited",
			desc = "toggle a wizard's mana as unlimited",
			modifiers = { "unlimited", "unlim", "unl" },
			min = 1,
			max = 1)
	@CommandPermissions("wizard.modify.unlimited")
	public static void unlimited(CommandContext args, Player player,
			HumanNPC npc) {
		Wizard wizard = npc.getType("wizard");
		wizard.setUnlimitedMana(!wizard.hasUnlimitedMana());
		if (wizard.hasUnlimitedMana()) {
			player.sendMessage(StringUtils.wrap(npc.getStrippedName())
					+ " now has unlimited mana.");
		} else {
			player.sendMessage(StringUtils.wrap(npc.getStrippedName())
					+ " no longer has unlimited mana.");
		}
	}

	@Command(
			aliases = "wizard",
			usage = "command [command] (args)",
			desc = "set a wizard's command",
			modifiers = { "command", "cmd" },
			min = 2)
	@CommandPermissions("wizard.modify.command")
	public static void command(CommandContext args, Player player, HumanNPC npc) {
		String command = args.getJoinedStrings(1);
		((Wizard) npc.getType("wizard")).setCommand(command);
		player.sendMessage(StringUtils.wrap(npc.getStrippedName() + "'s")
				+ " command was set to " + StringUtils.wrap(command) + ".");
	}

	@Override
	public void addPermissions() {
		PermissionManager.addPerm("wizard.use.help");
		PermissionManager.addPerm("wizard.modify.mode");
		PermissionManager.addPerm("wizard.use.status");
		PermissionManager.addPerm("wizard.modify.addloc");
		PermissionManager.addPerm("wizard.modify.removeloc");
		PermissionManager.addPerm("wizard.use.locations");
		PermissionManager.addPerm("wizard.modify.unlimited");
		PermissionManager.addPerm("wizard.modify.command");
		PermissionManager.addPerm("wizard.use.interact");
	}
}