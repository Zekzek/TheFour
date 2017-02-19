package controller.plot;

import java.util.HashMap;
import java.util.Map;

import controller.ActionPlayer;
import controller.IBattleListener;
import controller.Watcher;
import model.Dialog;
import model.GameObject.TEAM;
import model.Unit;
import model.World;
import view.DialogPanel;
import view.GameFrame;
import view.SceneTransition;

public abstract class Plot implements IBattleListener{
	protected static final int SECOND = 10;//1000;
	private final Map<String, SceneTransition> sceneTransitions = new HashMap<String, SceneTransition>();
	protected ActionPlayer battleQueue;
	protected World world;
	private String sceneName;
	protected Runnable theEnd = new Runnable() {
		@Override
		public void run() {
			end();
		}
	};
	protected static GameFrame gameFrame;
	
	public Plot(ActionPlayer battleQueue, World world) {
		this.battleQueue = battleQueue;
		this.world = world;
		Watcher.registerBattleListener(this);
	}
	
	public void start() {
		initUnits();
		initSceneTransitions();
		changeScene(getStartingScene());
		//battleQueue.setPause(false);
		battleQueue.startPlayingActions();
	}
	
	protected void end() {
		battleQueue.stopPlayingActions();
		Dialog[] theEnd = new Dialog[] {
			new Dialog(getNarrator(), "The End")
		};
		DialogPanel.setGoToTitleOnConclusion(true);
		gameFrame.showDialog(theEnd, null);
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
		else {
			gameFrame.changeScene(transition);
			this.sceneName = transition.getName();
		}
	}
	
	public String getSceneName() {
		return sceneName;
	}
	
	@Override
	public void onUnitDefeated(Unit unit) {}
	
	@Override
	public void onTeamDefeated(TEAM team) {}

	@Override
	public void onUnitAdded(Unit unit) {}

	@Override
	public void onUnitRemoved(Unit unit) {}
	
	@Override
	public void onUnitChangedTeam(Unit unit) {}
	
	public static void setGameFrame(GameFrame aGameFrame) {
		gameFrame = aGameFrame;
	}
}
