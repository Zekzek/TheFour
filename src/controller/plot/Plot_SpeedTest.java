package controller.plot;

import java.util.ArrayList;

import model.Dialog;
import model.GridRectangle;
import model.Structure;
import model.GameObject;
import model.GameObject.TEAM;
import model.Unit;
import model.Unit.ID;
import model.World;
import view.DialogPanel;
import view.GraphicsPanel;
import view.SceneTransition;
import view.SpriteSheet.CLIMATE;
import controller.BattleQueue;

public class Plot_SpeedTest extends Plot{
	
	@Override
	protected String getStartingScene() {
		return "Speed Test";
	}
	
	@Override
	protected void initUnits() {
		//Do nothing, will be created and added as part of the speed test
	}
	
	@Override
	protected void initSceneTransitions() {
		SceneTransition sorcRequest = new SceneTransition("Speed Test");
		sorcRequest.setFadeInDuration(0);
		sorcRequest.setFadedText("Ensure common operations can be done in a timely manner");
		sorcRequest.setFadedDuration(5000);
		sorcRequest.setStartRunnable(new Runnable() {
			@Override
			public void run() {
				GraphicsPanel.moveScreenTo(24, 23);
				addUnitsToWorld();
				
				BattleQueue.addCombatants(World.getSortedContentsWithin(GraphicsPanel.getScreenRectangle(), Unit.class).iterator());
			    BattleQueue.addRandomCombatDelays();
				addUnitsToWorld();
			}
		});
		addSceneTransition(sorcRequest);
	}
	
	private void addUnitsToWorld() {
		GridRectangle searchScreen = new GridRectangle(490, 490, 16, 16);
		
		Long startTime = System.currentTimeMillis();
		for (int i = 0; i < 5000; i++) {
			Unit guard = Unit.get(ID.GUARD, TEAM.NONCOMBATANT);
			World.addTallObject(guard, i, i);
		}
		for (int i = 0; i < 5000; i++) {
			Structure tree = Structure.get(Structure.ID.TREE, CLIMATE.CASTLE);
			World.addTallObject(tree, i, i+1);
		}
		Long endTime = System.currentTimeMillis();
		String addObjectTest = "Added 10,000 objects (5,000 units and 5,000 structures) in " + (endTime - startTime) + "ms";
		
		ArrayList<GameObject> objects = null;
		startTime = System.currentTimeMillis();
		for (int i = 0; i < 10000; i++) {
			objects = World.getSortedContentsWithin(searchScreen, GameObject.class);
		}
		endTime = System.currentTimeMillis();
		String displayObjectTest = "Searched for a screen of objects 10,000 times in " + (endTime - startTime) 
				+ "ms (found " + objects.size() + ")";
		
		Unit announcer = Unit.get(ID.ANNOUNCER, TEAM.NONCOMBATANT, "Announcer");
		World.addTallObject(announcer, -1, -1);
	
		Dialog[] report = new Dialog[] {
			new Dialog(announcer, addObjectTest + "\n" + displayObjectTest),
		};
		DialogPanel.showDialog(report, theEnd);
	}
}

