package model;

public class AppliedStatusEffect {
	private int duration;
	private StatusEffect statusEffect;
	
	public AppliedStatusEffect(StatusEffect statusEffect) {
		this.statusEffect = statusEffect;
		this.duration = statusEffect.getDuration();
	}

	public void tick(int time) {
		duration -= time;
	}
	
	public boolean isOver() {
		return duration <= 0;
	}
	
	public int getDuration() {
		return duration;
	}

	public StatusEffect getStatusEffect() {
		return statusEffect;
	}
}
