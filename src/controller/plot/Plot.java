package controller.plot;

import java.awt.Color;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import model.Dialog;
import model.ReadiedAction;
import model.Structure;
import model.Unit;
import model.World;
import model.Unit.TEAM;
import view.DialogPanel;
import view.GraphicsPanel;
import view.SceneTransition;
import controller.IBattleListener;
import controller.BattleQueue;
import controller.MapBuilder;
import controller.TemplateReader;

public abstract class Plot implements IBattleListener{
	private static boolean initialized = false;
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
		if (!initialized) {
			initialized = true;
			initialize();
		}
	}
	
	private void initialize() {
		TemplateReader objectTemplate = new TemplateReader("/resource/img/templates/objects.png");
		for (int x = 0; x < objectTemplate.getWidth(); x++) {
			for (int y = 0; y < objectTemplate.getHeight(); y++) {
				Color color = objectTemplate.getColorAt(x, y);
				boolean reddish = color.getRed() > 125;
				boolean greenish = color.getGreen() > 125;
				boolean blueish = color.getBlue() > 125;
				boolean visible = color.getAlpha() > 125;
				
				if (visible) {
					if (greenish) {
						if (!reddish && !blueish) {//green
							World.addTallObject(Structure.get(Structure.ID.TREE, MapBuilder.getClimateType(x, y)), x, y);
						}
					} else if (!reddish && !blueish) {//black
						World.addTallObject(Structure.get(Structure.ID.WALL, MapBuilder.getClimateType(x, y)), x, y);
					}
				}
			}
		}
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
	public void onUnitDefeated(Unit unit) {}
	
	@Override
	public void onTeamDefeated(TEAM team) {}

	@Override
	public void onUnitAdded(Unit unit) {}

	@Override
	public void onUnitRemoved(Unit unit) {}

	@Override
	public void onChangedActivePlayer(Unit unit) {}
	
	@Override
	public void onActivePlayerAbilityQueueChanged(Iterator<ReadiedAction> actions) {}
}
