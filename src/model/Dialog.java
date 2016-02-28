package model;

public class Dialog {
	
	private Unit speaker;
	private String speech;
	
	public Dialog(Unit speaker, String speech) {
		super();
		this.speaker = speaker;
		this.speech = speech;
	}

	public Unit getSpeaker() {
		return speaker;
	}
	public String getSpeech() {
		return speech;
	}
}
