package controller;

import model.Dialog;
import model.GridPosition;
import model.ITargetable;

public class ProximityTrigger extends Trigger {

	private final ITargetable target;
	private final int range;
	
	public ProximityTrigger(ID id, ITargetable trigger, int range, Dialog[] dialog, Runnable effect) {
		super(id, dialog, effect);
		this.target = trigger;
		this.range = range;
	}
	
	public void checkTrigger(GridPosition pos) {
		if (pos.getDistanceTo(target.getPos()) <= range) {
			super.checkTrigger();
		}
	}
}
