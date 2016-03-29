package controller.plot;

import model.Unit;
import model.Unit.ID;
import model.Unit.TEAM;

public class Plot_Tutorial_Beserker_Deceit extends Plot{

	private Unit defender;
	private Unit berserker;
	private Unit[] goblinMooks;
	private Unit goblinChief;
	private Unit[] fans;
	
	@Override
	protected String getStartingScene() {
		return "Berserkers Request";
	}
	
	@Override
	protected void initUnits() {
		defender = Unit.get(ID.DEFENDER, TEAM.PLAYER, "Defender");
		berserker = Unit.get(ID.BERSERKER, TEAM.NONCOMBATANT, "Berserker");
		goblinMooks = new Unit[] {
				Unit.get(Unit.ID.FEMALE_GOBLIN, TEAM.NONCOMBATANT, "Goblin Mook #1"),
				Unit.get(Unit.ID.MALE_GOBLIN, TEAM.NONCOMBATANT, "Goblin Mook #2"),
				Unit.get(Unit.ID.FEMALE_GOBLIN, TEAM.NONCOMBATANT, "Goblin Mook #3"),
				Unit.get(Unit.ID.MALE_GOBLIN, TEAM.NONCOMBATANT, "Goblin Mook #4")
		};
		goblinChief = Unit.get(Unit.ID.GOBLIN_CHIEF, TEAM.NONCOMBATANT, "Goblin Chieftain");
		fans = new Unit[] {
				Unit.get(Unit.ID.GIRL, TEAM.NONCOMBATANT, "Berserker Fan #1"),
				Unit.get(Unit.ID.GIRL, TEAM.NONCOMBATANT, "Berserker Fan #2"),
				Unit.get(Unit.ID.BOY, TEAM.NONCOMBATANT, "Berserker Fan #3"),
				Unit.get(Unit.ID.GIRL, TEAM.NONCOMBATANT, "Berserker Fan #4"),
				Unit.get(Unit.ID.GIRL, TEAM.NONCOMBATANT, "Berserker Fan #5"),
				Unit.get(Unit.ID.BOY, TEAM.NONCOMBATANT, "Berserker Fan #6"),
		};
	}

	@Override
	public void initSceneTransitions() {
		// TODO Auto-generated method stub
		//Berserker asks for help, claiming noble intentions
		//Berserker joins the party
		//Infiltrate goblin camp
		//Beserkers goes to 'take care of something'
		//Becomes noncombatant and goes to back of screen to steal some stuff
		//Fight some dudes all alone
		//Boss appears
		//Berserker joins the fight again
		//Defeat the boss together
		//Berserker accepts praise and the admiration of girls for 'saving' defender
	}
}
