package controller.plot;

import java.util.ArrayList;

import model.Dialog;
import model.Position;
import model.Structure;
import model.TallObject;
import model.Unit;
import model.Unit.TEAM;
import model.World;
import view.DialogPanel;
import view.GraphicsPanel;
import controller.BattleQueue;

public class Plot_SpeedTest extends Plot{
	
	@Override
	public void start() {
		GraphicsPanel.moveScreenTo(24, 23);
		addUnitsToWorld();
		
		BattleQueue.addCombatants(World.getSortedContentsWithin(GraphicsPanel.getScreenPos(), Unit.class).iterator());
	    BattleQueue.addRandomCombatDelays();
	}
	
	private void addUnitsToWorld() {
		Position searchScreen = new Position(490, 490, 16, 16);
		
		Long startTime = System.currentTimeMillis();
		for (int i = 0; i < 500; i++) {
			Unit guard = Unit.get("Guard", TEAM.NONCOMBATANT);
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
		
		Unit announcer = Unit.get("Announcer", TEAM.NONCOMBATANT);
		World.addTallObject(announcer, -1, -1);
	
		Dialog[] report = new Dialog[] {
			new Dialog(announcer, addObjectTest + "\n" + displayObjectTest),
		};
		DialogPanel.setGoToTitleOnConclusion(true);
		DialogPanel.showDialog(report, null);
	}
}

