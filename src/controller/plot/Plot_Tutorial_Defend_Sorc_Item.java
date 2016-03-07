package controller.plot;

import model.Ability;
import model.Dialog;
import model.Structure;
import model.Unit;
import model.Unit.TEAM;
import model.World;
import view.DialogPanel;
import view.GameFrame;
import view.GraphicsPanel;
import view.SpriteSheet;
import view.SpriteSheet.FACING;
import controller.BattleQueue;

public class Plot_Tutorial_Defend_Sorc_Item extends Plot{
	
	private Unit defender;
	private Unit sorceress;
	private Unit guard1, guard2;
	private Unit bandit1, bandit2, bandit3;
	private Structure trees[] = new Structure[18];
	private int banditDeaths = 0;
	
	@Override
	public void start() {
		GraphicsPanel.changeScene(0, "", 0, new Runnable() {
			@Override
			public void run() {
				createUnits();
				addSorcMeeting();
			}
		},  1000, new Runnable() {
			@Override
			public void run() {
				startSorcMeeting();
			}
		});
	}
	
	private void addSorcMeeting() {
		GraphicsPanel.moveScreenTo(140, 120);
		defender.setFacing(FACING.S);
		World.addTallObject(defender, 151, 131);
		sorceress.setFacing(FACING.N);
		World.addTallObject(sorceress, 151, 132);
		
		World.addTallObject(trees[0], 141, 121);
		World.addTallObject(trees[1], 142, 124);
		World.addTallObject(trees[2], 141, 129);
		World.addTallObject(trees[3], 145, 131);
		World.addTallObject(trees[4], 145, 133);
		World.addTallObject(trees[5], 151, 135);
		World.addTallObject(trees[6], 154, 135);
		World.addTallObject(trees[7], 156, 136);
		World.addTallObject(trees[10], 143, 122);
		World.addTallObject(trees[11], 143, 126);
		World.addTallObject(trees[12], 143, 127);
		World.addTallObject(trees[13], 144, 133);
		World.addTallObject(trees[14], 144, 137);
		World.addTallObject(trees[15], 148, 130);
		World.addTallObject(trees[16], 148, 132);
		World.addTallObject(trees[17], 155, 122);
	}
	
	private void startSorcMeeting() {
		Runnable run = new Runnable(){
			@Override
			public void run() {
				GraphicsPanel.changeScene(1000, "Hours later, finally nearing civilization", 500, new Runnable() {
					@Override
					public void run() {
						World.remove(sorceress);
						addBanditMeeting();
					}
				},  1000, new Runnable() {
					@Override
					public void run() {
						startBanditMeeting();
					}
				});
			}};

		Dialog[] sorcRequest = new Dialog[] {
			new Dialog(sorceress, "There you are, DEFENDER! Available to save a pretty, young damsel in distress? Some bandits are "
					+ "after this case. Can you hold onto it until I see you again? Say, tonight at the banquet?"),
			new Dialog(defender, "Of course, SORC. I've been there for you since we were kids, haven't I? It won't leave my side."),
			new Dialog(sorceress, "Aw, thanks! I gotta run now, but find me tonight and I'll figure out some way to repay you.")
		};
		DialogPanel.showDialog(sorcRequest, run);
	}
	
	private void addBanditMeeting() {
		GraphicsPanel.moveScreenTo(43, 18);
		World.moveObject(defender, 51, 31);
		bandit1.setFacing(FACING.E);
		World.addTallObject(bandit1, 43, 31);
		bandit2.setFacing(FACING.S);
		World.addTallObject(bandit2, 51, 20);
		bandit3.setFacing(FACING.W);
		World.addTallObject(bandit3, 59, 31);
		World.addTallObject(trees[8], 48, 29);
		World.addTallObject(trees[9], 53, 23);	
	}
	
	private void startBanditMeeting() {
		Runnable attack = new Runnable(){
			@Override
			public void run() {
				defender.setFacing(FACING.N);
				BattleQueue.addCombatant(defender);
				BattleQueue.addCombatant(bandit1);
				BattleQueue.addCombatant(bandit2);
				BattleQueue.addCombatant(bandit3);
				BattleQueue.addRandomCombatDelays();
				BattleQueue.startPlayingActions();
				BattleQueue.resumeBattle();
			    GameFrame.updateMenu();
			}};
		
		Dialog[] joinDialog = new Dialog[] {
			new Dialog(bandit1, "Haha, we've got you surrounded now! Just give up that case and we might not gut you."),
			new Dialog(defender, "I can't do that. There may be three of you, but a promise is a promise."),
			new Dialog(bandit1, "Pretty cocky, huh? Let's see how you feel with a few holes in you!"),
		};
		DialogPanel.showDialog(joinDialog, attack);
	}
	
	private void addGuardsJoinFight() {
		guard1.setFacing(FACING.S);
		World.addTallObject(guard1, 43, 30);
		guard2.setFacing(FACING.S);
		World.addTallObject(guard2, 59, 30);
	}
	
	private void startGuardsJoinFight() {
		BattleQueue.addCombatant(guard1);
		BattleQueue.addCombatant(guard2);
		Dialog[] joinDialog = new Dialog[] {
			new Dialog(guard1, "Three on one! That's not very fair. Let's see how you fair when the numbers are even.")
		};
		DialogPanel.showDialog(joinDialog, null);
	}
	
	private void banditRetreatSequence() {
		Runnable retreat = new Runnable() {
			@Override
			public void run() {
				GraphicsPanel.changeScene(1000, "", 0, new Runnable() {
					@Override
					public void run() {
						addRetreatBandits();
					}
				},  1000, new Runnable() {
					@Override
					public void run() {
						guardsCompromise();
					}
				});
			}
		};
		
		Dialog[] retreatDialog = new Dialog[] {
			new Dialog(bandit2, "The situation's gotten a bit too hot guys. Let's get out of here!")
		};
		DialogPanel.showDialog(retreatDialog, retreat);
	}
	
	private void addRetreatBandits() {
		BattleQueue.removeCombatant(bandit1);
		BattleQueue.removeCombatant(bandit2);
		BattleQueue.removeCombatant(bandit3);
		World.remove(bandit1);
		World.remove(bandit2);
		World.remove(bandit3);
	}
	
	private void guardsCompromise() {
		System.out.println("guardsCompromise");
		Dialog[] guardsCompromise = new Dialog[] {
			new Dialog(guard1, "Well, they've been getting more brazen.\n\n Thank you for your assistance citizen. Now, let's "
					+ "have that case, and we'll see it safely to the academy."),
			new Dialog(defender, "My apologies, but I can't give it to you. I gave my word that it wouldn't leave my side "
					+ "until I returned it to SORCERESS presonally."),
			new Dialog(guard1, "What? I don't care. That's city property and I'll be taking it now. If you resist, we'll "
					+ "take it by force and you'll be looking at a night in the stockades."),
			new Dialog(defender, "I understand your position, but I can't let you take it. Do as you must."),
			new Dialog(guard1, "..."),
			new Dialog(guard2, "Now, hold on a minute. We can't just let you walk away with that case, but I think we can "
					+ "settle this without a fight. You said SORCERESS. She's Professor TEACHER's aide, right? That will do "
					+ "just fine, but we will need to provide an escort back to town. Is that acceptible to everyone?"),
			new Dialog(defender, "Thank you for the offer. I welcome your company. There are still bandits out there after all"),
			new Dialog(guard1, "Then let's be off. We haven't got all day to chat in the forest"),
		};
		DialogPanel.setGoToTitleOnConclusion(true);
		DialogPanel.showDialog(guardsCompromise, null);
	}
	
	private void createUnits() {
		Ability weakAttack = new Ability("Weak Attack", Ability.TARGET_TYPE.HARMFUL, 1000, 11, 1, "", SpriteSheet.ANIMATION.MELEE);
		Ability quickAttack = new Ability("Quick Attack", Ability.TARGET_TYPE.HARMFUL, 1000, 40, 1, "", SpriteSheet.ANIMATION.MELEE);
		Ability guardAttack = new Ability("Guard Attack", Ability.TARGET_TYPE.HARMFUL, 1000, 20, 1, "", SpriteSheet.ANIMATION.MELEE);
		Ability shieldBash = new Ability("Shield Bash", Ability.TARGET_TYPE.HARMFUL, 1100, 35, 1, 
				"Briefly disorients the target", SpriteSheet.ANIMATION.MELEE);
		shieldBash.setDelayOpponent(300);
		
		defender = new Unit("Defender", Unit.TEAM.PLAYER, Plot.class.getResource("/resource/img/spriteSheet/defender.png"), 200);
		defender.learnAction(quickAttack);
		defender.learnAction(shieldBash);
		
		sorceress = new Unit("Sorceress", Unit.TEAM.NONCOMBATANT, Plot.class.getResource("/resource/img/spriteSheet/sorceress.png"), 160);
		
		guard1 = new Unit("Guard 1", Unit.TEAM.ALLY, Plot.class.getResource("/resource/img/spriteSheet/guard.png"), 100);
		guard1.learnAction(guardAttack);
		guard2 = new Unit("Guard 2", Unit.TEAM.ALLY, Plot.class.getResource("/resource/img/spriteSheet/guard.png"), 100);
		guard2.learnAction(guardAttack);
		
		bandit1 = new Unit("Bandit 1", Unit.TEAM.ENEMY1, Plot.class.getResource("/resource/img/spriteSheet/banditFemale.png"), 100);
		bandit1.learnAction(weakAttack);
		bandit2 = new Unit("Bandit 2", Unit.TEAM.ENEMY1, Plot.class.getResource("/resource/img/spriteSheet/banditMale.png"), 100);
		bandit2.learnAction(weakAttack);
		bandit3 = new Unit("Bandit 3", Unit.TEAM.ENEMY1, Plot.class.getResource("/resource/img/spriteSheet/banditFemale.png"), 100);
		bandit3.learnAction(weakAttack);
		
		for (int i = 0; i < trees.length; i+=3) {
			trees[i] = new Structure("Tree", Plot.class.getResource("/resource/img/trees/tree13.png"), 200);
			trees[i+1] = new Structure("Tree", Plot.class.getResource("/resource/img/trees/tree15.png"), 200);
			trees[i+2] = new Structure("Tree", Plot.class.getResource("/resource/img/trees/tree18.png"), 200);
		}
	}

	@Override
	public void onUnitDefeated(Unit unit) {
		if (unit.getTeam() == TEAM.ENEMY1) {
			banditDeaths++;
			if (banditDeaths == 1) {
				GraphicsPanel.changeScene(500, "", 0, new Runnable() {
						@Override
						public void run() {
							addGuardsJoinFight();
						}
					},  500, new Runnable() {
						@Override
						public void run() {
							startGuardsJoinFight();
						}
					});
			} else if (banditDeaths == 2) {
				banditRetreatSequence();
			}
		} else if (unit.equals(defender)) {
			Dialog[] deathSpeech = new Dialog[] {
				new Dialog(defender, "Nooooooooo!!! In death, my duty will go unfulfilled!")
			};
			DialogPanel.setGoToTitleOnConclusion(true);
			DialogPanel.showDialog(deathSpeech, null);
		}
	}
}

