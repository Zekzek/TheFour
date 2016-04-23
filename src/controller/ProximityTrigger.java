package controller;

import model.Dialog;
import model.GridPosition;
import model.ITargetable;

public class ProximityTrigger extends Trigger {

	private final ITargetable target;
	private final int range;
	private final boolean triggerOnLeaving;
	
	public ProximityTrigger(ID id, ITargetable trigger, int range, Dialog[] dialog, Runnable effect) {
		super(id, dialog, effect);
		this.target = trigger;
		this.range = range;
		this.triggerOnLeaving = false;
	}
	
	public ProximityTrigger(ID id, ITargetable trigger, int range, boolean triggerOnLeaving, Dialog[] dialog, Runnable effect) {
		super(id, dialog, effect);
		this.target = trigger;
		this.range = range;
		this.triggerOnLeaving = triggerOnLeaving;
	}
	
	public void checkTrigger(GridPosition pos) {
		if (triggerOnLeaving) {
			if (pos.getDistanceTo(target.getPos()) > range) {
				super.checkTrigger();
			}
		}
		else {
			if (pos.getDistanceTo(target.getPos()) <= range) {
				super.checkTrigger();
			}
		}
	}
	
	
}
