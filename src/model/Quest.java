package model;

import java.util.Set;

import view.DialogPanel;

public class Quest {
	private Set<Quest> prereqs;
	private ITargetable trigger;
	private int range;
	private Dialog[] dialog;
	private Runnable effect;
	private boolean triggered = false;
	
	public Quest(ITargetable trigger, int range, Dialog[] dialog, Runnable effect) {
		this.trigger = trigger;
		this.range = range;
		this.dialog = dialog;
		this.effect = effect;
	}
	
	public void checkTrigger(GridPosition pos) {
		if (!triggered && pos.getDistanceTo(trigger.getPos()) <= range) {
			trigger();
			triggered = true;
		}
	}
	
	private void trigger() {
		if (dialog == null) {
			if (effect != null) {
				effect.run();
			}
		}
		else {
			DialogPanel.showDialog(dialog, effect);
		}
	}
}
