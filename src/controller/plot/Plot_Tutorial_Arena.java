package controller.plot;

import model.Dialog;
import model.Structure;
import model.Unit;
import model.Unit.ID;
import model.Unit.TEAM;
import model.World;
import view.DialogPanel;
import view.GraphicsPanel;
import view.SceneTransition;
import view.SpriteSheet;
import controller.BattleQueue;

public class Plot_Tutorial_Arena extends Plot{
	
	private Unit announcer, defender, berserker;
		
	@Override
	public void start() {
		initSceneTransitions();
		changeScene("Arena Fight");
	}
	
	@Override
	public void initSceneTransitions() {
		SceneTransition sorcRequest = new SceneTransition("Arena Fight");
		sorcRequest.setFadeInDuration(0);
		sorcRequest.setFadedText("One afternoon, in the city arena");
		sorcRequest.setFadedDuration(5000);
		sorcRequest.setSetupRunnable(new Runnable() {
			@Override
			public void run() {
				addArena();
			}
		});
		sorcRequest.setStartRunnable(new Runnable() {
			@Override
			public void run() {
				startArena();
			}
		});
		addSceneTransition(sorcRequest);
	}
	
	private void addArena() {
		World.addTallObject(Unit.get(ID.GUARD, TEAM.NONCOMBATANT), 28, 28);
		World.addTallObject(Unit.get(ID.GUARD, TEAM.NONCOMBATANT), 36, 28);
		World.addTallObject(Unit.get(ID.GUARD, TEAM.NONCOMBATANT), 28, 36);
		World.addTallObject(Unit.get(ID.GUARD, TEAM.NONCOMBATANT), 36, 36);
		
		World.addTallObject(new Structure("Tree 1", Plot.class.getResource("/resource/img/trees/tree.png"), 200), 28, 27);
		World.addTallObject(new Structure("Tree 2", Plot.class.getResource("/resource/img/trees/tree.png"), 200), 36, 27);
		World.addTallObject(new Structure("Tree 3", Plot.class.getResource("/resource/img/trees/tree.png"), 200), 28, 35);
		World.addTallObject(new Structure("Tree 4", Plot.class.getResource("/resource/img/trees/tree.png"), 200), 36, 35);

		defender = Unit.get(ID.DEFENDER, TEAM.PLAYER);
		defender.setFacing(SpriteSheet.FACING.S);
		
		berserker = Unit.get(ID.BERSERKER, TEAM.ENEMY1);
		berserker.setFacing(SpriteSheet.FACING.S);
		
		announcer = Unit.get(ID.ANNOUNCER, TEAM.NONCOMBATANT);
		
		GraphicsPanel.moveScreenTo(24, 23);
		World.addTallObject(defender, 26, 31);
		World.addTallObject(berserker, 37, 31);
		World.addTallObject(announcer, -1, -1);
	}
	
	private void startArena() {
		Dialog[] announcerIntro = new Dialog[] {
			new Dialog(announcer, "Welcome, one and all. Before we get to the main events today, the guards have asked to " +
					"give a little demonstration. It seems the flow of new recruits has been a bit lacking of late, and " +
					"they'd like to demonstrate what their defensive style can do."),
			new Dialog(announcer, "They've sent a representative, and claim he can withstand constant blows for hours on end. " +
					"But we don't have that kind of time, do we folks? So, I thought we'd see how long this championon of " + 
					"theirs can stand against our own champion"),
			new Dialog(announcer, "That's right, folks! The champion of the arena himself! A " + 
					"man who's ferocious fighting style has toppled every man and beast we've managed to throw at him. " +
					"The undefeated, undisputed champion of the arena, BERSERKER!!!")
		};
		DialogPanel.showDialog(announcerIntro, null);
		
		BattleQueue.addCombatants(World.getSortedContentsWithin(GraphicsPanel.getScreenRectangle(), Unit.class).iterator());
	    BattleQueue.addRandomCombatDelays();
	    BattleQueue.startPlayingActions();
	}
	
	@Override
	public void onTeamDefeated(TEAM team) {
		BattleQueue.endCombat();
		DialogPanel.setGoToTitleOnConclusion(true);
		if (team == Unit.TEAM.ENEMY1) { 
			Dialog[] announcerSummary = new Dialog[] {
					new Dialog(announcer, "What's this! Ladies and gentlemen, I can scarcely believe my eyes!!! After dozens " 
							+ "of challengers, man and beast alike, the king of the arena has been brought down by this mere guard. " 
							+ "Truly, he is a force to be reckoned with!!!"),
					new Dialog(announcer, "Now, technically, this wasn't an official bout, and was simply intended to promote " 
							+ "the palace guards' program. While it has certainly accomplished that, I'd like to remind everyone "
							+ "that this does not effect the official standings. That said, I welcome DEENDER to return " 
							+ "to our little arena as he could clearly be a contender! Will the champion fall a second time?")
				};
			DialogPanel.showDialog(announcerSummary, null);
		}
		else if (team == Unit.TEAM.PLAYER) { 
			Dialog[] announcerSummary = new Dialog[] {
					new Dialog(announcer, "I must say, I'm impressed. BERSERKER actually looks a bit winded " 
							+ "after this battle with a mere guard. There may be more to this defensive style than hot air. " 
							+ " With a bit more time in the arena, DEFENDER may even " 
							+ "be a contender, and I welcome him back in the coming weeks to prove himself.")
				};
			DialogPanel.showDialog(announcerSummary, null);
		} else {
			Dialog[] announcerSummary = new Dialog[] {
					new Dialog(announcer, "Wait... how did you get to this dialog.? " + team + " was defeated?")
				};
			DialogPanel.showDialog(announcerSummary, null);
		}
	}

	
}

