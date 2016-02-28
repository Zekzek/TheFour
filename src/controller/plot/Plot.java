package controller.plot;

import model.Unit;
import model.Unit.TEAM;
import controller.BattleListenerInterface;
import controller.BattleQueue;

public abstract class Plot implements BattleListenerInterface{

	private Plot nextPlot;
	
	public Plot() {
		BattleQueue.addBattleListener(this);
	}
	
	public abstract void start();
	
	@Override
	public void onTeamDefeated(TEAM team) {
	}
	
	@Override
	public void onUnitDefeated(Unit unit) {
	}
	
	public void setNextPlot(Plot plot) {
		this.nextPlot = plot;
	}
	
	public void startNextPlot() {
		nextPlot.start();
	}
}
