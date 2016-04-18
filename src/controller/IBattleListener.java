package controller;

import model.TallObject.TEAM;
import model.Unit;

public interface IBattleListener {
	public void onUnitAdded(Unit unit);
	public void onUnitRemoved(Unit unit);
	public void onUnitDefeated(Unit unit);
	public void onTeamDefeated(TEAM team);
}
