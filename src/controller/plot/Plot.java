package controller.plot;

import java.util.HashMap;
import java.util.Map;

import view.DialogPanel;
import view.GraphicsPanel;
import view.SceneTransition;
import model.Dialog;
import model.Unit;
import model.Unit.TEAM;
import controller.BattleListenerInterface;
import controller.BattleQueue;

public abstract class Plot implements BattleListenerInterface{
	protected static final int SECOND = 1000;
	private final Map<String, SceneTransition> sceneTransitions = new HashMap<String, SceneTransition>();
	private String sceneName;
	protected Runnable theEnd = new Runnable() {
		@Override
		public void run() {
			end();
		}
	};
	
	public Plot() {
		BattleQueue.addBattleListener(this);
	}
	
	public void start() {
		initUnits();
		initSceneTransitions();
		changeScene(getStartingScene());
		BattleQueue.setPause(false);
		BattleQueue.startPlayingActions();
	}
	
	protected void end() {
		BattleQueue.endCombat();
		BattleQueue.stopPlayingActions();
		Dialog[] theEnd = new Dialog[] {
			new Dialog(getNarrator(), "The End")
		};
		DialogPanel.setGoToTitleOnConclusion(true);
		DialogPanel.showDialog(theEnd, null);
	}
	
	protected abstract void initUnits();
	protected abstract void initSceneTransitions();
	protected abstract String getStartingScene();
	
	protected Unit getNarrator() {
		return Unit.get(Unit.ID.ANNOUNCER, Unit.TEAM.NONCOMBATANT, "Announcer");
	}
	
	
	public void addSceneTransition(SceneTransition transition) {
		sceneTransitions.put(transition.getName(), transition);
	}
	
	public void changeScene(String sceneName) {
		SceneTransition transition = sceneTransitions.get(sceneName);
		if (transition == null) {
			System.err.println("Scene '" + sceneName + "' not found!");
		}
		GraphicsPanel.changeScene(transition);
		this.sceneName = transition.getName();
	}
	
	public String getSceneName() {
		return sceneName;
	}
	
	@Override
	public void onUnitDefeated(Unit unit) {
	}
	
	@Override
	public void onTeamDefeated(TEAM team) {
	}
}
