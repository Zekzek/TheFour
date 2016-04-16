package controller;

import java.util.Iterator;

import model.Dialog;
import model.ReadiedAction;
import model.Unit;
import model.Unit.TEAM;

public class BattleTrigger extends Trigger implements IBattleListener{

	private Unit triggerUnit;
	private TEAM triggerTeam;
	
	public BattleTrigger(ID id, Unit unit, Dialog[] dialog, Runnable effect) {
		super(id, dialog, effect);
		triggerUnit = unit;
	}
	
	public BattleTrigger(ID id, TEAM team, Dialog[] dialog, Runnable effect) {
		super(id, dialog, effect);
		triggerTeam = team;
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
	public void onChangedActivePlayer(Unit unit) {}

	@Override
	public void onActivePlayerAbilityQueueChanged(Iterator<ReadiedAction> actions) {}
}
