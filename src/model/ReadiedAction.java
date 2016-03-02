package model;

public class ReadiedAction {
	private Ability ability;
	private Unit source;
	private ITargetable[] targets;
	private int startTime;
	private int endTime;
	
	public ReadiedAction(Ability ability, Unit source, ITargetable[] targets, int startTime) {
		super();
		this.ability = ability;
		this.source = source;
		this.targets = targets;
		this.startTime = startTime;
		this.endTime = startTime + ability.getDelay();
	}
	
	public void delay(int delay) {
		startTime += delay;
		endTime += delay;
	}

	public Ability getAbility() {
		return ability;
	}

	public Unit getSource() {
		return source;
	}

	public ITargetable[] getTargets() {
		return targets;
	}
	
	public String getTargetsDescription() {
		return targets[0].toString() + (targets.length>1 ? " among others" : "");
	}

	public int getStartTime() {
		return startTime;
	}
	
	public int getEndTime() {
		return endTime;
	}
}
