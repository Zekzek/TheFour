package controller;

import model.GameObject.TEAM;
import model.Unit;

public interface IBattleListener {
	public void onUnitAdded(Unit unit);
	public void onUnitRemoved(Unit unit);
	public void onUnitDefeated(Unit unit);
	public void onTeamDefeated(TEAM team);
	public void onUnitChangedTeam(Unit unit);
}
