package controller.plot;

import model.Ability;
import model.Dialog;
import model.GroundTarget;
import model.Unit;
import model.Unit.TEAM;
import model.World;
import view.GraphicsPanel;
import view.SceneTransition;
import view.SpriteSheet.FACING;
import controller.BattleQueue;
import controller.BattleTrigger;
import controller.ProximityTrigger;
import controller.Trigger;

public class Plot_Beginnings extends Plot {

	private Unit defender, savior, guardCaptain;
	private Unit[] guards;
	
	@Override
	protected void initUnits() {
		defender = Unit.get(Unit.ID.DEFENDER, TEAM.PLAYER);
		World.addTallObject(defender, 73, 28);
		
		savior = Unit.get(Unit.ID.BOY, TEAM.NONCOMBATANT, "Uncle Xalvador");
		savior.setFacing(FACING.N);
		World.addTallObject(savior, 69, 23);
		
		guardCaptain = Unit.get(Unit.ID.GUARD, TEAM.NONCOMBATANT, "Guard Captain");
		guardCaptain.setFacing(FACING.N);
		World.addTallObject(guardCaptain, 61, 17);
		
		guards = new Unit[6];
		for (int i = 0; i < guards.length; i++) {
			guards[i] = Unit.get(Unit.ID.GUARD, TEAM.NONCOMBATANT, "Guard #" + (i + 1));
			World.addTallObject(guards[i], 58 + i, 14);
		}
		guards[0].setName("Guard Gerrart");
		guards[1].setName("Guard Amelyn");
		guards[2].setName("Guard Meriel");
		guards[3].setName("Guard Nob");
		
		//TODO: add target dummy
		
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
				GraphicsPanel.moveScreenTo(defender, 0);
				BattleQueue.addCombatant(defender);
				
				ProximityTrigger lovedOneTrigger = new ProximityTrigger(Trigger.ID.BEGINNINGS_TALK_LOVED_ONE_1, savior, 2, 
						new Dialog[]{
							new Dialog(savior, "Morning, little one. Something you want to tell me?"),
							new Dialog(defender, "Yes. I joined the guard last night. I'm going in for my first shift now."),
							new Dialog(savior, "I had hoped I was wrong. You know how I feel about you risking yourself like "
									+ "that. Why would you join the guard?"),
							new Dialog(defender, savior.getName() + ", I want to help people, just like you used to. I know "
									+ "you worry about me, but I can help, and I can make it so other people don't have to go "
									+ "through a loss like ours."),
							new Dialog(savior, "..."),
							new Dialog(savior, "I know, but that doesn't mean I have to like it."),
							new Dialog(savior, "You know, in a lot of ways, you're just like Aunt Isabella. She wanted so much "
									+ "to stop the suffering of our world. Go. Do what you feel is right. Just try to keep "
									+ "yourself out of too much danger. I love you."),
							new Dialog(defender, "I love you too, " + savior.getName())
						}, new Runnable(){
							@Override
							public void run() {
								BattleQueue.nonCombatantQueueAction(Ability.get(Ability.ID.MOVE), savior, new GroundTarget(67, 28));
							}
						});
				World.addTrigger(lovedOneTrigger);
				
				ProximityTrigger guardCaptainTrigger = new ProximityTrigger(Trigger.ID.BEGINNINGS_GUARDS_1, guardCaptain, 3,
						new Dialog[]{
							new Dialog(guardCaptain, "So, the savior's ward has finally arrived."),
							new Dialog(defender, "My appologies. It won't happen again."),
							new Dialog(guardCaptain, "See that it doesn't."),
							new Dialog(guards[0], "Pssh, what's with that ridiculous shield?"),
							new Dialog(guards[1], "Haha, ya, he must be a bit of a coward."),
							new Dialog(guardCaptain, "Enough! For now, let's review the ability I showed you yesterday. "
									+ "Show me what your Guard Attack can do to old stuffy, here.")
						}, null);
				World.addTrigger(guardCaptainTrigger);
				
				BattleTrigger guardTrainingTrigger = new BattleTrigger(Trigger.ID.BEGINNINGS_GUARDS_2, Ability.ID.GUARD_ATTACK,
						new Dialog[]{ 
							new Dialog(guardCaptain, "Not bad. You successfully made it less effective for stuffy to attack "
									+ "for a few moments... Don't look at me like that. It's much more effective against living "
									+ "opponents. Now, if you're going to carry that ridiculous shield around, let's see what "
									+ "you can do with it. Show me your Shield Bash.")
					}, null);
				guardTrainingTrigger.setMinAllowed(Trigger.ID.BEGINNINGS_GUARDS_1, 1);
				World.addTrigger(guardTrainingTrigger);
				
				BattleTrigger guardTrainingTrigger2 = new BattleTrigger(Trigger.ID.BEGINNINGS_GUARDS_3, Ability.ID.SHIELD_BASH,
						new Dialog[]{ 
							new Dialog(guardCaptain, "Well done. That looked quite dissorienting. You can probably pair it with "
									+ "Guard Attack to good effect."),
							new Dialog(defender, "Thank you. I'll have to try that out."),
							new Dialog(guardCaptain, "Regardless, we have a new assignment today. It seems they've uncovered "
									+ "something at the dig site and have requested an escort to bring it back to town. " 
									+ defender.getName() + ", since you were late, I'm assigning you this task. " + guards[0].getName()
									+ ", you go with him."),
							new Dialog(guards[0], "Aw, Captain! Why do I get saddled with the new guy?"),
							new Dialog(guardCaptain, "We've all gotta pitch in. Besides, I heard what you said about my wife's stew."),
							new Dialog(guards[0], "Aw, man... C'mon " + defender.getName() + ", let's get going.")
					}, new Runnable(){
						@Override
						public void run() {
							guards[0].setTeam(TEAM.PLAYER);
							BattleQueue.addCombatant(guards[0]);
						}
				});
				guardTrainingTrigger2.setMinAllowed(Trigger.ID.BEGINNINGS_GUARDS_2, 1);
				World.addTrigger(guardTrainingTrigger2);
			}
		});
		
		addSceneTransition(morningChat);
	}

	@Override
	protected String getStartingScene() {
		return "Morning Chat";
	}
}
