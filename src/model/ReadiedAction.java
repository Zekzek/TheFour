package model;

public class ReadiedAction {
	private Ability ability;
	private Unit source;
	private ITargetable target;
	private int startTime;
	
	public ReadiedAction(Ability ability, Unit source, ITargetable target, int startTime) {
		super();
		this.ability = ability;
		this.source = source;
		this.target = target;
		this.startTime = startTime;
	}
	
	public void delay(int delay) {
		startTime += delay;
	}

	public Ability getAbility() {
		return ability;
	}

	public Unit getSource() {
		return source;
	}

	public ITargetable getTarget() {
		return target;
	}
	
	public int getStartTime() {
		return startTime;
	}
}
