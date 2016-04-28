package controller.plot;

import model.Ability;
import model.Dialog;
import model.GameObject.TEAM;
import model.GroundTarget;
import model.StatusEffect;
import model.Structure;
import model.Unit;
import model.World;
import view.SceneTransition;
import view.SpriteSheet.FACING;
import controller.ActionQueue;
import controller.BattleTrigger;
import controller.ProximityTrigger;
import controller.Trigger;

public class Plot_Beginnings extends Plot {

	public Plot_Beginnings(ActionQueue battleQueue, World world) {
		super(battleQueue, world);
		// TODO Auto-generated constructor stub
	}

	private Unit defender, savior, guardCaptain, damsel, damselsBrother, sorceress;
	private Unit[] guards, bandits;
	private Structure targetDummy;
	
	@Override
	protected void initUnits() {
		defender = Unit.get(Unit.ID.DEFENDER, TEAM.PLAYER);
		battleQueue.addGameObject(defender, 73, 28);
		
		savior = Unit.get(Unit.ID.BOY, TEAM.NONCOMBATANT, "Uncle Xalvador");
		savior.setFacing(FACING.N);
		battleQueue.addGameObject(savior, 69, 23);
		
		guardCaptain = Unit.get(Unit.ID.GUARD, TEAM.NONCOMBATANT, "Guard Captain");
		guardCaptain.setFacing(FACING.N);
		battleQueue.addGameObject(guardCaptain, 61, 17);
		
		guards = new Unit[6];
		for (int i = 0; i < guards.length; i++) {
			guards[i] = Unit.get(Unit.ID.GUARD, TEAM.NONCOMBATANT, "Guard #" + (i + 1));
			battleQueue.addGameObject(guards[i], 58 + i, 14);
		}
		guards[0].setName("Guard Gerrart");
		guards[1].setName("Guard Amelyn");
		guards[2].setName("Guard Meriel");
		guards[3].setName("Guard Nob");
		
		targetDummy = Structure.get(Structure.ID.TARGET_DUMMY, null);
		targetDummy.setName("Old Stuffy");
		targetDummy.setTeam(TEAM.ENEMY1);
		battleQueue.addGameObject(targetDummy, 59, 17);
		
		damsel = Unit.get(Unit.ID.GIRL, TEAM.NONCOMBATANT, "Helpless Damsel");
		damsel.setFacing(FACING.E);
		battleQueue.addGameObject(damsel, 115, 19);
		
		damselsBrother = Unit.get(Unit.ID.BOY, TEAM.NONCOMBATANT, "Defensive Brother");
		damselsBrother.setFacing(FACING.W);
		battleQueue.addGameObject(damselsBrother, 116, 19);
		
		sorceress = Unit.get(Unit.ID.SORCERESS, TEAM.NONCOMBATANT);
		sorceress.setFacing(FACING.W);
		battleQueue.addGameObject(sorceress, 151, 26);
		
		bandits = new Unit[3];
		for (int i = 0; i < bandits.length; i++) {
			bandits[i] = Unit.get(Unit.ID.FEMALE_BANDIT, TEAM.ENEMY1, "Bandit #" + (i + 1));
		}
	}

	@Override
	protected void initSceneTransitions() {
		SceneTransition morningChat = new SceneTransition("Morning Chat");
		morningChat.setFadeInDuration(0);
		morningChat.setFadedText("Your first day as a town guard, unbeknownst to your Uncle Xalvador");
		morningChat.setFadedDuration(7 * SECOND);
		morningChat.setSetupRunnable(new Runnable() {
			@Override
			public void run() {
				world.setFocusTarget(defender);
				world.setQuestTarget(savior);
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
								battleQueue.queueAction(Ability.get(Ability.ID.MOVE), savior, new GroundTarget(67, 28));
								world.setQuestTarget(guardCaptain);
							}
						});
				world.addTrigger(lovedOneTrigger);
				
				ProximityTrigger guardCaptainTrigger = new ProximityTrigger(Trigger.ID.BEGINNINGS_GUARDS_1, guardCaptain, 3,
						new Dialog[]{
							new Dialog(guardCaptain, "So, the savior's ward has finally arrived."),
							new Dialog(defender, "My appologies. It won't happen again."),
							new Dialog(guardCaptain, "See that it doesn't."),
							new Dialog(guards[0], "Pssh, what's with that ridiculous shield?"),
							new Dialog(guards[1], "Haha, ya, he must be a bit of a coward."),
							new Dialog(guardCaptain, "Enough! For now, let's review the ability I showed you yesterday. "
									+ "Show me what your Guard Attack can do to " + targetDummy.getName() + ", here.")
						}, new Runnable() {
							@Override
							public void run() {
								world.setQuestTarget(targetDummy);
							}
						});
				world.addTrigger(guardCaptainTrigger);
				
				BattleTrigger guardTrainingTrigger = new BattleTrigger(Trigger.ID.BEGINNINGS_GUARDS_2, Ability.ID.GUARD_ATTACK,
						new Dialog[]{ 
							new Dialog(guardCaptain, "Not bad. You successfully made it less effective for stuffy to attack "
									+ "for a few moments... Don't look at me like that. It's much more effective against living "
									+ "opponents. Now, if you're going to carry that ridiculous shield around, let's see what "
									+ "you can do with it. Show me your Shield Bash.")
					}, null, battleQueue);
				guardTrainingTrigger.setMinAllowed(Trigger.ID.BEGINNINGS_GUARDS_1, 1);
				world.addTrigger(guardTrainingTrigger);
				
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
							world.setQuestTarget(damsel);
							damselsBrother.damage(9999);
						}
				}, battleQueue);
				guardTrainingTrigger2.setMinAllowed(Trigger.ID.BEGINNINGS_GUARDS_2, 1);
				world.addTrigger(guardTrainingTrigger2);
				
				Trigger damselInDistressTrigger = new ProximityTrigger(Trigger.ID.BEGINNINGS_DAMSEL_1, damsel, 3,
					new Dialog[]{ 
						new Dialog(guards[0], "Well, hello miss. What are you doing way out here?"),
						new Dialog(damsel, "Oh, thank goodness you found me. It was terrible. They came out of nowhere. My brother... "
								+ "He tried to stop them, but..."),
						new Dialog(guards[0], "Its okay, miss. I'm here now. They won't hurt you anymore."),
						new Dialog(guards[0], defender.getName() + ", I'll take care of things here. You go on ahead and complete "
								+ "the mission. It's just a bit further down the path here."),
						new Dialog(defender, "Understood.")
					}, new Runnable(){
						@Override
						public void run() {
							damsel.setFacing(FACING.W);
							guards[0].setTeam(TEAM.NONCOMBATANT);
							battleQueue.clearUnitActions(guards[0]);
							battleQueue.queueAction(Ability.get(Ability.ID.MOVE), guards[0], 
									new GroundTarget(damsel.getPos().getX() - 1, damsel.getPos().getY()));
							world.setQuestTarget(sorceress);
						}
				});
				damselInDistressTrigger.setMinAllowed(Trigger.ID.BEGINNINGS_GUARDS_3, 1);
				world.addTrigger(damselInDistressTrigger);
				
				Trigger sorceressRequestTrigger = new ProximityTrigger(Trigger.ID.BEGINNINGS_SORCERESS_1, sorceress, 3,
					new Dialog[] {
						new Dialog(sorceress, defender.getName() + "?! I can't believe you're all the way out here. But I suppose "
								+ "I shouldn't be all that surprised. You've always been there when I really need someone, ever "
								+ "since we were kids."),
						new Dialog(defender, "Of course. You're like a sister to me."),
						new Dialog(sorceress, "A sister?..."),
						new Dialog(sorceress, "Listen, there are bandits about and I need to get this artifact back to town? I'd "
								+ "do it myself, but my magic hasn't been responding well. Can you hold onto it until I see you "
								+ "again. Say, tonight at the banquet?"),
						new Dialog(defender, "It won't leave my side."),
						new Dialog(sorceress, "Aw, thanks! I gotta run now, but find me tonight, and I'll figure out some way to "
								+ "repay you. Anything you want!")
					}, new Runnable(){
						@Override
						public void run() {
							battleQueue.queueAction(Ability.get(Ability.ID.MOVE), sorceress, new GroundTarget(153, 20));
							world.setQuestTarget(guardCaptain);
						}
				});
				sorceressRequestTrigger.setMinAllowed(Trigger.ID.BEGINNINGS_DAMSEL_1, 1);
				world.addTrigger(sorceressRequestTrigger);
				
				Trigger banditAttackTrigger = new ProximityTrigger(Trigger.ID.BEGINNINGS_BANDIT_ATTACK_1, new GroundTarget(81, 21), 20,
					null, 
					new Runnable(){
						@Override
						public void run() {
							battleQueue.addObjectToWest(bandits[0], 0);
							battleQueue.addObjectToEast(bandits[1], 0);
							battleQueue.addObjectToNorth(bandits[2], 0);
						}
				});
				banditAttackTrigger.setMinAllowed(Trigger.ID.BEGINNINGS_SORCERESS_1, 1);
				world.addTrigger(banditAttackTrigger);
				
				for (int i = 0; i < 3; i++) {
					final int banditNum = i;
					Trigger banditAttackTriggerSpeech = new ProximityTrigger(Trigger.ID.BEGINNINGS_BANDIT_ATTACK_2, bandits[i], 10,
							new Dialog[] {
							new Dialog(bandits[0], "Haha, we've got you surrounded now! Just give up that case and we might not gut you."),
							new Dialog(defender, "I can't do that. There may be three of you, but a promise is a promise."),
							new Dialog(bandits[0], "Pretty cocky, huh? Let's see how you feel with a few holes in you!"),
						}, null
					);
					banditAttackTriggerSpeech.setMinAllowed(Trigger.ID.BEGINNINGS_BANDIT_ATTACK_1, 1);
					banditAttackTriggerSpeech.setMaxAllowed(Trigger.ID.BEGINNINGS_BANDIT_ATTACK_2, 0);
					world.addTrigger(banditAttackTriggerSpeech);
					
					Trigger banditDefeatTrigger = new BattleTrigger(Trigger.ID.BEGINNINGS_GUARDS_ASSIST_1, bandits[i],
						null, 
						new Runnable(){
							@Override
							public void run() {
								battleQueue.addObjectToEast(guardCaptain, 2);
								battleQueue.addObjectToWest(guards[1], 1);
							}
						}, battleQueue
					);
					banditDefeatTrigger.setMinAllowed(Trigger.ID.BEGINNINGS_BANDIT_ATTACK_2, 1);
					banditDefeatTrigger.setMaxAllowed(Trigger.ID.BEGINNINGS_GUARDS_ASSIST_1, 0);
					world.addTrigger(banditDefeatTrigger);
					
					Trigger banditDefeatTrigger2 = new BattleTrigger(Trigger.ID.BEGINNINGS_GUARDS_ASSIST_1, bandits[i],
						new Dialog[] {
							new Dialog(bandits[i], "The situation's gotten a bit too hot guys. Let's get out of here!")
						}, 
						new Runnable(){
							@Override
							public void run() {
								moveOffscreenAndRemove(bandits[0]);
								moveOffscreenAndRemove(bandits[1]);
								moveOffscreenAndRemove(bandits[2]);									
							}
						}, battleQueue
					);
					banditDefeatTrigger2.setMinAllowed(Trigger.ID.BEGINNINGS_BANDIT_ATTACK_1, 1);
					banditDefeatTrigger2.setMaxAllowed(Trigger.ID.BEGINNINGS_GUARDS_ASSIST_1, 1);
					world.addTrigger(banditDefeatTrigger2);
					
					Trigger banditsEscapeTriggerCount = new ProximityTrigger(Trigger.ID.BEGINNINGS_BANDIT_ESCAPE_1, bandits[banditNum], 10, true,
							null, 
							new Runnable() {
								@Override
								public void run() {
									battleQueue.clearUnitActions(bandits[banditNum]);
								}}
						);
					banditsEscapeTriggerCount.setMinAllowed(Trigger.ID.BEGINNINGS_BANDIT_ATTACK_1, 2);
					world.addTrigger(banditsEscapeTriggerCount);
					
					Trigger banditsEscapeTrigger = new ProximityTrigger(Trigger.ID.BEGINNINGS_BANDIT_ESCAPE_2, bandits[banditNum], 14, true,
						new Dialog[] {
							//TODO: tweak dialog to make sense
							new Dialog(guards[1], "Well, they've been getting more brazen.\n\n Thank you for your assistance citizen. Now, let's "
									+ "have that case, and we'll see it safely to the academy."),
							new Dialog(defender, "My apologies, but I can't give it to you. I gave my word that it wouldn't leave my side "
									+ "until I returned it to SORCERESS presonally."),
							new Dialog(guards[1], "What? I don't care. That's city property and I'll be taking it now. If you resist, we'll "
									+ "take it by force and you'll be looking at a night in the stockades."),
							new Dialog(defender, "I understand your position, but I can't let you take it. Do as you must."),
							new Dialog(guards[1], "..."),
							new Dialog(guardCaptain, "Now, hold on a minute. We can't just let you walk away with that case, but I think we can "
									+ "settle this without a fight. You said SORCERESS. She's Professor TEACHER's aide, right? That will do "
									+ "just fine, but we will need to provide an escort back to town. Is that acceptible to everyone?"),
							new Dialog(defender, "Thank you for the offer. I welcome your company. There are still bandits out there after all"),
							new Dialog(guards[1], "Then let's be off. We haven't got all day to chat in the forest"),
						}, 
							new Runnable() {
								@Override
								public void run() {
									guardCaptain.setTeam(TEAM.PLAYER);
									guards[1].setTeam(TEAM.PLAYER);
								}}
						);
					banditsEscapeTrigger.setMinAllowed(Trigger.ID.BEGINNINGS_BANDIT_ESCAPE_1, 2);
					banditsEscapeTrigger.setMaxAllowed(Trigger.ID.BEGINNINGS_BANDIT_ESCAPE_1, 0);
					world.addTrigger(banditsEscapeTrigger);
				}
				
				Trigger banditDefeatTriggerSpeech = new ProximityTrigger(Trigger.ID.BEGINNINGS_GUARDS_ASSIST_2, guardCaptain, 10,
						new Dialog[] {
							new Dialog(guardCaptain, "Three on one! That's not very fair. Let's see how you fair when the numbers are even.")
						}, null
					);
				banditDefeatTriggerSpeech.setMinAllowed(Trigger.ID.BEGINNINGS_GUARDS_ASSIST_1, 1);
				world.addTrigger(banditDefeatTriggerSpeech);
			}
		});
		
		addSceneTransition(morningChat);
	}

	@Override
	protected String getStartingScene() {
		return "Morning Chat";
	}
	
	private void moveOffscreenAndRemove(Unit unit) {
		unit.addStatusEffect(new StatusEffect(StatusEffect.ID.FLEE, 60000));
		battleQueue.clearUnitActions(unit);
		battleQueue.queueAction(Ability.get(Ability.ID.FLEE), unit, unit);
	}
}
