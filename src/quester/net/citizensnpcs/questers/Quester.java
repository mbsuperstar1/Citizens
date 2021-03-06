package net.citizensnpcs.questers;

import java.util.List;
import java.util.Map;

import net.citizensnpcs.commands.CommandHandler;
import net.citizensnpcs.npctypes.CitizensNPC;
import net.citizensnpcs.npctypes.CitizensNPCManager;
import net.citizensnpcs.properties.Properties;
import net.citizensnpcs.questers.listeners.QuesterBlockListen;
import net.citizensnpcs.questers.listeners.QuesterPlayerListen;
import net.citizensnpcs.questers.quests.CompletedQuest;
import net.citizensnpcs.questers.quests.Quest;
import net.citizensnpcs.resources.npclib.HumanNPC;
import net.citizensnpcs.utils.Messaging;
import net.citizensnpcs.utils.PageUtils;
import net.citizensnpcs.utils.PageUtils.PageInstance;
import net.citizensnpcs.utils.StringUtils;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class Quester extends CitizensNPC {
	private final Map<Player, PageInstance> displays = Maps.newHashMap();
	private final Map<Player, Integer> queue = Maps.newHashMap();
	private final List<String> quests = Lists.newArrayList();

	// TODO - make this queue per-player.

	/**
	 * Add a quest
	 * 
	 * @param quest
	 */
	public void addQuest(String quest) {
		quests.add(quest);
	}

	public void removeQuest(String quest) {
		quests.remove(quests.indexOf(quest));
	}

	public List<String> getQuests() {
		return quests;
	}

	@Override
	public String getType() {
		return "quester";
	}

	@Override
	public void onLeftClick(Player player, HumanNPC npc) {
		cycle(player);
	}

	@Override
	public void onRightClick(Player player, HumanNPC npc) {
		if (QuestManager.hasQuest(player)) {
			checkCompletion(player, npc);
		} else {
			if (displays.get(player) == null) {
				cycle(player);
			}
			PageInstance display = displays.get(player);
			if (display.currentPage() != display.maxPages()) {
				display.displayNext();
				if (display.currentPage() == display.maxPages()) {
					player.sendMessage(ChatColor.GREEN
							+ "Right click again to accept.");
				}
			} else {
				attemptAssign(player, npc);
			}
		}
	}

	private void checkCompletion(Player player, HumanNPC npc) {
		PlayerProfile profile = QuestManager.getProfile(player.getName());
		if (profile.getProgress().getQuesterUID() == npc.getUID()) {
			if (profile.getProgress().fullyCompleted()) {
				Quest quest = QuestManager.getQuest(profile.getProgress()
						.getQuestName());
				Messaging.send(player, quest.getCompletedText());
				for (Reward reward : quest.getRewards()) {
					reward.grant(player, npc);
				}
				long elapsed = System.currentTimeMillis()
						- profile.getProgress().getStartTime();
				profile.setProgress(null);
				profile.addCompletedQuest(new CompletedQuest(quest, npc
						.getStrippedName(), elapsed));
			} else {
				player.sendMessage(ChatColor.GRAY
						+ "The quest isn't completed yet.");
			}
		} else {
			player.sendMessage(ChatColor.GRAY
					+ "You already have a quest from another NPC.");
		}
	}

	private void attemptAssign(Player player, HumanNPC npc) {
		Quest quest = getQuest(fetchFromList(player));
		if (QuestManager.getProfile(player.getName()).hasCompleted(
				fetchFromList(player))
				&& !quest.isRepeatable()) {
			player.sendMessage(ChatColor.GRAY
					+ "You are not allowed to repeat this quest.");
			return;
		}
		for (Reward requirement : quest.getRequirements()) {
			if (!requirement.canTake(player)) {
				player.sendMessage(ChatColor.GRAY + "Missing requirement. "
						+ requirement.getRequiredText(player));
				return;
			}
		}

		QuestManager.assignQuest(npc, player, fetchFromList(player));
		Messaging.send(player, quest.getAcceptanceText());

		displays.remove(player);

	}

	private void cycle(Player player) {
		if (QuestManager.hasQuest(player)) {
			player.sendMessage(ChatColor.GRAY
					+ "Only one quest can be taken on at a time.");
			return;
		}
		if (quests == null) {
			player.sendMessage(ChatColor.GRAY + "No quests available.");
			return;
		}
		if (queue.get(player) == null) {
			queue.put(player, 0);
		} else {
			queue.put(player, queue.get(player) + 1);
		}
		updateDescription(player);
	}

	private void updateDescription(Player player) {
		Quest quest = getQuest(fetchFromList(player));
		PageInstance display = PageUtils.newInstance(player);
		display.setSmoothTransition(true);
		display.header(ChatColor.GREEN
				+ StringUtils.listify("Quest %x/%y - "
						+ StringUtils.wrap(quest.getName())));
		for (String push : quest.getDescription().split("<br>")) {
			display.push(push);
			if ((display.elements() % 8 == 0 && display.maxPages() == 1)
					|| display.elements() % 9 == 0) {
				display.push(ChatColor.GOLD
						+ "Right click to continue description.");
			}
		}
		if (display.maxPages() == 1)
			player.sendMessage(ChatColor.GOLD + "Right click again to accept.");
		display.process(1);
		displays.put(player, display);
	}

	private Quest getQuest(String name) {
		return QuestManager.getQuest(name);
	}

	private String fetchFromList(Player player) {
		return quests.get(queue.get(player));
	}

	public boolean hasQuests() {
		return this.quests == null ? false : this.quests.size() > 0;
	}

	@Override
	public Properties getProperties() {
		return QuesterProperties.INSTANCE;
	}

	@Override
	public CommandHandler getCommands() {
		return new QuesterCommands();
	}

	@Override
	public void onEnable() {
		QuestProperties.initialize();
		QuestManager.initialize();
	}

	@Override
	public void addListeners() {
		CitizensNPCManager.addListener(new QuesterBlockListen());
		CitizensNPCManager.addListener(new QuesterPlayerListen());
	}
}