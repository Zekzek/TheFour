package controller;

import model.Unit;
import model.Unit.TEAM;

public interface BattleListenerInterface {

	public void onTeamDefeated(TEAM team);
	public void onUnitDefeated(Unit unit);
}
