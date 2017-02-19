package controller;

import java.util.Iterator;
import java.util.Set;

import model.Ability;
import model.Dialog;
import model.GameObject;
import model.GameObject.TEAM;
import model.ReadiedAction;
import model.Unit;

public class BattleTrigger extends Trigger implements IBattleListener, IPlayerListener{

	private Unit triggerUnit;
	private TEAM triggerTeam;
	private Ability.ID triggerAbilityId;
	private ReadiedAction triggerAction;
	
	public BattleTrigger(ID id, Unit unit, Dialog[] dialog, Runnable effect, ActionPlayer battleQueue) {
		super(id, dialog, effect);
		triggerUnit = unit;
		Watcher.registerBattleListener(this);
		Watcher.registerPlayerListener(this);
	}
	
	public BattleTrigger(ID id, TEAM team, Dialog[] dialog, Runnable effect, ActionPlayer battleQueue) {
		super(id, dialog, effect);
		triggerTeam = team;
		Watcher.registerBattleListener(this);
		Watcher.registerPlayerListener(this);
	}
	
	public BattleTrigger(ID id, Ability.ID abilityId, Dialog[] dialog, Runnable effect, ActionPlayer battleQueue) {
		super(id, dialog, effect);
		triggerAbilityId = abilityId;
		Watcher.registerBattleListener(this);
		Watcher.registerPlayerListener(this);
	}
	
	public BattleTrigger(ID id, ReadiedAction action, TEAM team, Dialog[] dialog, Runnable effect, ActionPlayer battleQueue) {
		super(id, dialog, effect);
		triggerAction = action;
		Watcher.registerBattleListener(this);
		Watcher.registerPlayerListener(this);
	}

	@Override
	public void onUnitAdded(Unit unit) {}

	@Override
	public void onUnitRemoved(Unit unit) {}

	@Override
	public void onUnitDefeated(Unit unit) {
		if (triggerUnit != null && triggerUnit.equals(unit)) {
			checkTrigger();
		}
	}

	@Override
	public void onTeamDefeated(TEAM team) {
		if (triggerTeam != null && triggerTeam.equals(team)) {
			checkTrigger();
		}
	}

	@Override
	public void onUnitChangedTeam(Unit unit) {}
	
	@Override
	public void onChangedActivePlayer(Unit unit) {}

	@Override
	public void onChangedMostReadyPlayer(Unit unit) {}

	@Override
	public void onActivePlayerAbilityQueueChanged(Iterator<ReadiedAction> actions) {}

	@Override
	public void onPlayerUsedAbility(ReadiedAction action) {
		if (triggerAbilityId != null && triggerAbilityId == action.getAbility().getId()) {
			checkTrigger();
		}
		else if (triggerAction != null && triggerAction.sourceAbilityTargetEquals(action)) {
			checkTrigger();
		}
	}

	@Override
	public void onChangedPlayerTeam(Set<GameObject> playerObjects) {}
}
