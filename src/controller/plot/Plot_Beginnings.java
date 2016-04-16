package controller.plot;

import model.Dialog;
import model.GridPosition;
import model.GroundTarget;
import model.Unit;
import model.Unit.TEAM;
import model.World;
import view.GraphicsPanel;
import view.SceneTransition;
import controller.BattleQueue;
import controller.ProximityTrigger;
import controller.Trigger;

public class Plot_Beginnings extends Plot {

	private Unit defender, savior;
	
	@Override
	protected void initUnits() {
		defender = Unit.get(Unit.ID.DEFENDER, TEAM.PLAYER);
		savior = Unit.get(Unit.ID.BOY, TEAM.NONCOMBATANT, "Uncle Xalvador");
	}

	@Override
	protected void initSceneTransitions() {
		SceneTransition morningChat = new SceneTransition("Morning Chat");
		morningChat.setFadeInDuration(0);
		morningChat.setFadedText("Your first day as a town guard, unbeknownst to your Uncle Xalvador");
		morningChat.setFadedDuration(7000);
		morningChat.setSetupRunnable(new Runnable() {
			@Override
			public void run() {
				World.addTallObject(defender, 73, 28);
				GraphicsPanel.moveScreenTo(defender, 0);
				BattleQueue.addCombatant(defender);
				
				World.addTallObject(savior, 69, 23);
				ProximityTrigger lovedOneTrigger = new ProximityTrigger(Trigger.ID.BEGINNINGS_TALK_LOVED_ONE_1, savior, 2, 
						new Dialog[]{new Dialog(savior, "Hi")}, null);
				World.addQueust(lovedOneTrigger);
				ProximityTrigger testQuest = new ProximityTrigger(Trigger.ID.BEGINNINGS_TALK_LOVED_ONE_1, 
						new GroundTarget(new GridPosition(73, 28)), 2, 
						new Dialog[]{new Dialog(defender, "TEST: You already talked to your uncle.")}, null);
				testQuest.setMinAllowed(Trigger.ID.BEGINNINGS_TALK_LOVED_ONE_1, 1);
				World.addQueust(testQuest);
			}
		});
		
		addSceneTransition(morningChat);
	}

	@Override
	protected String getStartingScene() {
		return "Morning Chat";
	}
}
