package controller.plot;

import controller.BattleQueue;
import view.GraphicsPanel;
import view.SceneTransition;
import model.Unit;
import model.Unit.TEAM;
import model.World;

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
			}
		});
		
		addSceneTransition(morningChat);
	}

	@Override
	protected String getStartingScene() {
		return "Morning Chat";
	}
}
