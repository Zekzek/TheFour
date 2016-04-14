package controller;

import java.util.Iterator;

import model.ReadiedAction;
import model.Unit;
import model.Unit.TEAM;

public interface IBattleListener {
	public void onUnitAdded(Unit unit);
	public void onUnitRemoved(Unit unit);
	public void onUnitDefeated(Unit unit);
	public void onTeamDefeated(TEAM team);
	public void onChangedActivePlayer(Unit unit);
	public void onActivePlayerAbilityQueueChanged(Iterator<ReadiedAction> actions);
}
