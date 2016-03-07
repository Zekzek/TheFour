package controller.plot;

import java.util.ArrayList;

import model.Ability;
import model.Dialog;
import model.Position;
import model.Structure;
import model.TallObject;
import model.Unit;
import model.World;
import view.DialogPanel;
import view.GraphicsPanel;
import view.SpriteSheet;
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
		Ability watch = new Ability("Watch", Ability.TARGET_TYPE.UBIQUITOUS, 5000, 0, 16, "", SpriteSheet.ANIMATION.WALK);
		Position searchScreen = new Position(490, 490, 16, 16);
		
		Long startTime = System.currentTimeMillis();
		for (int i = 0; i < 500; i++) {
			Unit guard = new Unit("Guard 1", Unit.TEAM.ENEMY1, Plot.class.getResource("/resource/img/spriteSheet/guard.png"), 100);
			guard.learnAction(watch);
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
		
		Unit announcer = new Unit("Tester", Unit.TEAM.NONCOMBATANT, Plot.class.getResource("/resource/img/spriteSheet/guard.png"), 1);
		World.addTallObject(announcer, -1, -1);
	
		Dialog[] report = new Dialog[] {
			new Dialog(announcer, addObjectTest + "\n" + displayObjectTest),
		};
		DialogPanel.setGoToTitleOnConclusion(true);
		DialogPanel.showDialog(report, null);
	}
}

