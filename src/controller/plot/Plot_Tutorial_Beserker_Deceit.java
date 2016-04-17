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
import view.SpriteSheet.FACING;
import controller.BattleQueue;

public class Plot_Tutorial_Beserker_Deceit extends Plot{

	private Unit defender;
	private Unit berserker;
	private Unit[] goblinMooks;
	private Unit goblinChief;
	private Unit[] fans;
	private Structure treasure;
	private int soloFightCount = 0;
	
	@Override
	protected String getStartingScene() {
		return "Berserkers Request";
	}
	
	@Override
	protected void initUnits() {
		defender = Unit.get(ID.DEFENDER, TEAM.PLAYER);
		berserker = Unit.get(ID.BERSERKER, TEAM.NONCOMBATANT);
		goblinMooks = new Unit[] {
				Unit.get(Unit.ID.FEMALE_GOBLIN, TEAM.ENEMY1, "Goblin Mook #1"),
				Unit.get(Unit.ID.MALE_GOBLIN, TEAM.ENEMY1, "Goblin Mook #2"),
				Unit.get(Unit.ID.FEMALE_GOBLIN, TEAM.ENEMY1, "Goblin Mook #3"),
				Unit.get(Unit.ID.MALE_GOBLIN, TEAM.ENEMY1, "Goblin Mook #4")
		};
		goblinChief = Unit.get(Unit.ID.GOBLIN_CHIEF, TEAM.ENEMY1);
		fans = new Unit[] {
				Unit.get(Unit.ID.GIRL, TEAM.NONCOMBATANT, "Berserker Fan #1"),
				Unit.get(Unit.ID.GIRL, TEAM.NONCOMBATANT, "Berserker Fan #2"),
				Unit.get(Unit.ID.BOY, TEAM.NONCOMBATANT, "Berserker Fan #3"),
				Unit.get(Unit.ID.GIRL, TEAM.NONCOMBATANT, "Berserker Fan #4"),
				Unit.get(Unit.ID.GIRL, TEAM.NONCOMBATANT, "Berserker Fan #5"),
				Unit.get(Unit.ID.BOY, TEAM.NONCOMBATANT, "Berserker Fan #6"),
		};
		treasure = Structure.get(Structure.ID.TREASURE, null);
	}

	@Override
	public void initSceneTransitions() {
		//Berserker asks for help, claiming noble intentions
		//Berserker joins the party
		SceneTransition berserkerRequest = new SceneTransition("Berserkers Request");
		berserkerRequest.setFadeInDuration(0);
		berserkerRequest.setFadedText("The start of another average day");
		berserkerRequest.setFadedDuration(5 * SECOND);
		berserkerRequest.setSetupRunnable(new Runnable() {
			@Override
			public void run() {
				addBerserkerRequest();
			}
		});
		berserkerRequest.setStartRunnable(new Runnable() {
			@Override
			public void run() {
				startBerserkerRequest();
			}
		});
		addSceneTransition(berserkerRequest);
		
		//Infiltrate goblin camp
		//Beserkers goes to 'take care of something'
		SceneTransition infiltrate = new SceneTransition("Infiltrate");
		infiltrate.setSetupRunnable(new Runnable() {
			@Override
			public void run() {
				addInfiltrate();
			}
		});
		infiltrate.setStartRunnable(new Runnable() {
			@Override
			public void run() {
				startInfiltrate();
			}
		});
		addSceneTransition(infiltrate);
		
		//Becomes noncombatant and goes to back of screen to steal some stuff
		//Fight some dudes all alone
		SceneTransition solo = new SceneTransition("Solo");
		solo.setSetupRunnable(new Runnable() {
			@Override
			public void run() {
				addSolo();
			}
		});
		solo.setStartRunnable(new Runnable() {
			@Override
			public void run() {
				startSolo();
			}
		});
		addSceneTransition(solo);
		
		//Boss appears
		//Berserker joins the fight again
		//Defeat the boss together
		SceneTransition boss = new SceneTransition("Boss");
		boss.setSetupRunnable(new Runnable() {
			@Override
			public void run() {
				addBoss();
			}
		});
		boss.setStartRunnable(new Runnable() {
			@Override
			public void run() {
				startBoss();
			}
		});
		addSceneTransition(boss);
		
		//Berserker accepts praise and the admiration of girls for 'saving' defender
		SceneTransition celebration = new SceneTransition("Celebration");
		celebration.setSetupRunnable(new Runnable() {
			@Override
			public void run() {
				addCelebration();
			}
		});
		celebration.setStartRunnable(new Runnable() {
			@Override
			public void run() {
				startCelebration();
			}
		});
		addSceneTransition(celebration);
		
	}
	
	private void addBerserkerRequest() {
		GraphicsPanel.moveScreenTo(100, 100);
		World.addTallObject(defender, 109, 107);
		defender.setFacing(FACING.W);
		World.addTallObject(berserker, 107, 107);
		berserker.setFacing(FACING.E);
	}
	private void startBerserkerRequest() {
		Dialog[] speech = new Dialog[] {
			new Dialog(berserker, "Come quick. The goblins have gone too far this time!")
		};
		DialogPanel.showDialog(speech, new Runnable() {
			@Override
			public void run() {
				changeScene("Infiltrate");
			}});
	}
	
	private void addInfiltrate() {
		GraphicsPanel.moveScreenTo(190, 10);
		World.moveObject(berserker, 191, 16);
		berserker.setFacing(FACING.E);
		berserker.setTeam(TEAM.PLAYER);
		BattleQueue.addCombatant(berserker);
		World.moveObject(defender, 191, 18);
		defender.setFacing(FACING.E);
		BattleQueue.addCombatant(defender);
		World.addTallObject(goblinMooks[0], 205, 22);
		BattleQueue.addCombatant(goblinMooks[0]);
	}
	private void startInfiltrate() {
		Dialog[] easySpeech = new Dialog[] {
			new Dialog(berserker, "Here we are. Just one guard, huh? We'll make short work of her.")
		};
		DialogPanel.showDialog(easySpeech, null);
	}

	private void addSolo() {
		GraphicsPanel.moveScreenTo(200, 10);
		BattleQueue.removeCombatant(berserker, null, null);
		berserker.setTeam(TEAM.NONCOMBATANT);
		World.moveObject(berserker, 208, 12);
		berserker.setFacing(FACING.E);
		World.addTallObject(treasure, 209, 12);
		World.moveObject(defender, 204, 20);
		defender.setFacing(FACING.E);
		World.addTallObject(goblinMooks[1], 215, 21);
		BattleQueue.addCombatant(goblinMooks[1]);
		soloFightCount = 1;
	}
	private void startSolo() {
		Dialog[] caughtSpeech = new Dialog[] {
			new Dialog(goblinMooks[1], "What you doin' here?")
		};
		DialogPanel.showDialog(caughtSpeech, null);
	}

	private void addBoss() {
		GraphicsPanel.moveScreenTo(210, 10);
		World.moveObject(defender, 214, 20);
		defender.setFacing(FACING.E);
		World.moveObject(berserker, 218, 11);
		berserker.setFacing(FACING.S);
		berserker.setTeam(TEAM.PLAYER);
		BattleQueue.addCombatant(berserker);
		World.addTallObject(goblinChief, 224, 22);
		BattleQueue.addCombatant(goblinChief);
	}
	private void startBoss() {
		Dialog[] saviorSpeech = new Dialog[] {
			new Dialog(goblinChief, "You touch treasure?! You die now!"),
			new Dialog(berserker, "Looks like I arrived in the nick of time. Don't worry, I'll save you!")
		};
		DialogPanel.showDialog(saviorSpeech, null);
	}
	
	private void addCelebration() {
		GraphicsPanel.moveScreenTo(100, 100);
		World.moveObject(defender, 102, 108);
		defender.setFacing(FACING.E);
		World.moveObject(berserker, 110, 110);
		berserker.setFacing(FACING.S);
		World.addTallObject(fans[0], 110, 112);
		fans[0].setFacing(FACING.N);
		World.addTallObject(fans[1], 112, 110);
		fans[1].setFacing(FACING.W);
		World.addTallObject(fans[2], 108, 110);
		fans[2].setFacing(FACING.E);
		World.addTallObject(fans[3], 108, 109);
		fans[3].setFacing(FACING.E);
		World.addTallObject(fans[4], 112, 109);
		fans[4].setFacing(FACING.W);
		World.addTallObject(fans[5], 109, 112);
		fans[5].setFacing(FACING.N);
	}
	private void startCelebration() {
		Dialog[] deathSpeech = new Dialog[] {
			new Dialog(fans[0], "Oooo! I'm your biggest fan!"),
			new Dialog(fans[1], "Nu-uh. That would be me! I love you Berserker!"),
			new Dialog(berserker, "Yes, yes. This time I even managed to impress myself."),
		};
		DialogPanel.showDialog(deathSpeech, theEnd);
	}
	
	@Override
	public void onUnitDefeated(Unit unit) {
		if (unit.getTeam() == TEAM.ENEMY1) {
			if ("Infiltrate".equals(getSceneName())) {
				Dialog[] speech = new Dialog[] {
						new Dialog(berserker, "Hah, not even worth my time. You hold our position"
								+ " here while I take care of something real quick.")
					};
					DialogPanel.showDialog(speech, new Runnable() {
						@Override
						public void run() {
							changeScene("Solo");
						}});
			} else if ("Solo".equals(getSceneName())) {
				if (soloFightCount < 3) {
					soloFightCount++;
					World.addTallObject(goblinMooks[soloFightCount], 215, 22 - soloFightCount);
					BattleQueue.addCombatant(goblinMooks[soloFightCount]);
				} else {
					changeScene("Boss");
				}
			} else if ("Boss".equals(getSceneName())) {
				changeScene("Celebration");
			}
		} else if (unit.equals(defender)) {
			Dialog[] deathSpeech = new Dialog[] {
				new Dialog(defender, "Nooooooooo!!! In death, my duty will go unfulfilled!")
			};
			DialogPanel.showDialog(deathSpeech, theEnd);
		}
	}
}
