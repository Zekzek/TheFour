package controller.plot;

import java.util.ArrayList;

import model.Dialog;
import model.GridRectangle;
import model.Structure;
import model.TallObject;
import model.Unit;
import model.Unit.TEAM;
import model.Unit.ID;
import model.World;
import view.DialogPanel;
import view.GraphicsPanel;
import view.SceneTransition;
import controller.BattleQueue;

public class Plot_SpeedTest extends Plot{
	
	@Override
	public void start() {
		GraphicsPanel.moveScreenTo(24, 23);
		addUnitsToWorld();
		
		BattleQueue.addCombatants(World.getSortedContentsWithin(GraphicsPanel.getScreenRectangle(), Unit.class).iterator());
	    BattleQueue.addRandomCombatDelays();
	}
	
	@Override
	public void initSceneTransitions() {
		
		SceneTransition sorcRequest = new SceneTransition("Speed Test");
		sorcRequest.setFadeInDuration(0);
		sorcRequest.setFadedText("Ensure common operations can be done in a timely manner");
		sorcRequest.setFadedDuration(5000);
		sorcRequest.setStartRunnable(new Runnable() {
			@Override
			public void run() {
				addUnitsToWorld();
			}
		});
		addSceneTransition(sorcRequest);
	}
	
	private void addUnitsToWorld() {
		GridRectangle searchScreen = new GridRectangle(490, 490, 16, 16);
		
		Long startTime = System.currentTimeMillis();
		for (int i = 0; i < 500; i++) {
			Unit guard = Unit.get(ID.GUARD, TEAM.NONCOMBATANT, "Guard");
			World.addTallObject(guard, i, i);
		}
		for (int i = 0; i < 500; i++) {
			Structure tree = new Structure("Tree 1", Plot.class.getResource("/resource/img/trees/tree.png"), 100);
			World.addTallObject(tree, i, i+1);
		}
		Long endTime = System.currentTimeMillis();
		String addObjectTest = "Added 1,000 objects (500 units and 500 structures) in " + (endTime - startTime) + "ms";
		
		ArrayList<TallObject> objects = null;
		startTime = System.currentTimeMillis();
		for (int i = 0; i < 10000; i++) {
			objects = World.getSortedContentsWithin(searchScreen, TallObject.class);
		}
		endTime = System.currentTimeMillis();
		String displayObjectTest = "Searched for a screen of objects 10,000 times in " + (endTime - startTime) 
				+ "ms (found " + objects.size() + ")";
		
		Unit announcer = Unit.get(ID.ANNOUNCER, TEAM.NONCOMBATANT, "Announcer");
		World.addTallObject(announcer, -1, -1);
	
		Dialog[] report = new Dialog[] {
			new Dialog(announcer, addObjectTest + "\n" + displayObjectTest),
		};
		DialogPanel.setGoToTitleOnConclusion(true);
		DialogPanel.showDialog(report, null);
	}
}

