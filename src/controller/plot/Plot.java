package controller.plot;

import java.util.HashMap;
import java.util.Map;

import view.GraphicsPanel;
import view.SceneTransition;
import model.Unit;
import model.Unit.TEAM;
import controller.BattleListenerInterface;
import controller.BattleQueue;

public abstract class Plot implements BattleListenerInterface{

	private final Map<String, SceneTransition> sceneTransitions = new HashMap<String, SceneTransition>();
	private String sceneName;
	
	public Plot() {
		BattleQueue.addBattleListener(this);
	}
	
	public abstract void start();
	public abstract void initSceneTransitions();
	
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
