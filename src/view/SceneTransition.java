package view;

public class SceneTransition {
	private String name;
	private Runnable setupRunnable;
	private Runnable startRunnable;
	private Runnable wrapUpRunnable; //TODO: implement wrapUpRunnable?
	private int fadeOutDuration;
	private int fadedDuration;
	private int fadeInDuration;
	private String fadedText;
	
	/**
	 * Creates a simple scene transition with a 1 second fade out followed by 1 second fade in
	 */
	public SceneTransition(String name) {
		this.name = name;
		fadeOutDuration = 1000;
		fadedDuration = 0;
		fadedText = "";
		fadeInDuration = 1000;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getFadeOutDuration() {
		return fadeOutDuration;
	}
	public void setFadeOutDuration(int fadeOutDuration) {
		this.fadeOutDuration = fadeOutDuration;
	}
	public int getFadedDuration() {
		return fadedDuration;
	}
	public void setFadedDuration(int fadedDuration) {
		this.fadedDuration = fadedDuration;
	}
	public String getFadedText() {
		return fadedText;
	}
	public void setFadedText(String fadedText) {
		this.fadedText = fadedText;
	}
	public Runnable getSetupRunnable() {
		return setupRunnable;
	}
	public void setSetupRunnable(Runnable setupRunnable) {
		this.setupRunnable = setupRunnable;
	}
	public int getFadeInDuration() {
		return fadeInDuration;
	}
	public void setFadeInDuration(int fadeInDuration) {
		this.fadeInDuration = fadeInDuration;
	}
	public Runnable getStartRunnable() {
		return startRunnable;
	}
	public void setStartRunnable(Runnable startRunnable) {
		this.startRunnable = startRunnable;
	}
}
