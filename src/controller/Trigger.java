package controller;

import java.util.HashMap;
import java.util.Map;

import model.Dialog;
import view.GameFrame;

public abstract class Trigger {
	public static enum ID {
		BEGINNINGS_TALK_TO_LOVED_ONE, BEGINNINGS_GUARD_CAPTAIN_INTRO, BEGINNINGS_GUARD_CAPTAIN_INTRO_2, 
		BEGINNINGS_FIRST_GUARD_MISSION_INTRO, BEGINNINGS_DAMSEL_IN_DISTRESS, BEGINNINGS_SORCERESS_REQUEST, 
		BEGINNINGS_BANDIT_AMBUSH_SETUP, BEGINNINGS_BANDIT_AMBUSH_THREAT, BEGINNINGS_BANDIT_ATTACK_3, 
		BEGINNINGS_BANDIT_AMBUSH_GUARDS_ARRIVE, BEGINNINGS_BANDIT_AMBUSH_RETREAT, BEGINNINGS_BANDIT_ESCAPES,
		BEGINNINGS_POST_AMBUSH_TALK, BEGINNINGS_BANDIT_AMBUSH_GUARD_SPEECH, THE_END
	}
	
	private static final Map<ID, Integer> TRIGGER_COUNT = new HashMap<ID, Integer>();
	private static GameFrame gameFrame;
	
	private final ID id;
	private final Dialog[] dialog;
	private final Runnable effect;
	private final Map<ID, Integer> minAllowedTriggers;
	private final Map<ID, Integer> maxAllowedTriggers;
	private boolean triggered = false;
	
	public Trigger(ID id, Dialog[] dialog, Runnable effect) {
		this.id = id;
		this.dialog = dialog;
		this.effect = effect;
		maxAllowedTriggers = new HashMap<ID, Integer>();
		minAllowedTriggers = new HashMap<ID, Integer>();
	}
	
	public void setMinAllowed(ID id, int num) {
		minAllowedTriggers.put(id, num);
	}
	
	public void setMaxAllowed(ID id, int num) {
		maxAllowedTriggers.put(id, num);
	}
	
	protected void checkTrigger() {
		//If already triggered, fail to trigger
		if (triggered)
			return;
		
		//If a minimum threshold is not met, fail to trigger
		for (ID reqId : minAllowedTriggers.keySet())
			if (getCount(reqId) < getMinAllowed(reqId))
				return;
		
		//If a maximum threshold has been exceeded, fail to trigger
		for (ID reqId : maxAllowedTriggers.keySet())
			if (getCount(reqId) > getMaxAllowed(reqId))
				return;
				
		//Otherwise, trigger
		trigger();
	}
	
	private void trigger() {
		TRIGGER_COUNT.put(id, getCount(id) + 1);
		if (dialog == null) {
			if (effect != null) {
				effect.run();
			}
		}
		else {
			gameFrame.showDialog(dialog, effect);
		}
		triggered = true;
	}

	private int getCount(ID id) {
		Integer num = TRIGGER_COUNT.get(id);
		return num == null ? 0 : num;
	}
	
	private int getMinAllowed(ID id) {
		Integer num = minAllowedTriggers.get(id);
		return num == null ? 0 : num;
	}
	
	private int getMaxAllowed(ID id) {
		Integer num = maxAllowedTriggers.get(id);
		return num == null ? Integer.MAX_VALUE : num;
	}

	public ID getId() {
		return id;
	}
	
	public static void setGameFrame(GameFrame aGameFrame) {
		gameFrame = aGameFrame;
	}
}
