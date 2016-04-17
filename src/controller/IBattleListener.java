package controller;

import model.Unit;
import model.Unit.TEAM;

public interface IBattleListener {
	public void onUnitAdded(Unit unit);
	public void onUnitRemoved(Unit unit);
	public void onUnitDefeated(Unit unit);
	public void onTeamDefeated(TEAM team);
}
