package net.citizensnpcs.questers.quests.types;

import net.citizensnpcs.questers.quests.ObjectiveProgress;
import net.citizensnpcs.questers.quests.QuestObjective;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Type;
import org.bukkit.event.entity.EntityDeathEvent;

public class CombatQuest implements QuestObjective {
	private static final Type[] EVENTS = new Type[] { Type.ENTITY_DEATH };

	@Override
	public boolean update(Event event, ObjectiveProgress progress) {
		if (event instanceof EntityDeathEvent) {
			EntityDeathEvent ev = (EntityDeathEvent) event;
			if (ev.getEntity() instanceof Player) {
				progress.incrementCompleted(1);
			}
		}
		return isCompleted(progress);
	}

	@Override
	public Type[] getEventTypes() {
		return EVENTS;
	}

	@Override
	public boolean isCompleted(ObjectiveProgress progress) {
		return progress.getAmount() >= progress.getObjective().getAmount();
	}
}