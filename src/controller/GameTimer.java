package controller;

import java.util.Map;

/**
 * GameTimer should allow events to be scheduled, just like ActionQueue
 * Subevents are then scheduled to occur and can be paused/played
 * block until all subevents complete
 * if out-of-combat, block only same-unit events, in-combat, block all events
 * @author Al
 */
public class GameTimer {

	private static int CHECK_DELAY = 50;
	
	private long lastCheck;
	
	private boolean pause;
	private Map<Runnable,Long> scheduledThreads;
	
	public GameTimer() {
		pause = false;
	}
	
	public void scheduleEvent(Runnable event, long delay) {
		scheduledThreads.put(event, getCurrentTime() + delay);
	}
	
	public void pause() {	
		pause = true;
	}
	
	public void play() {
		pause = false;
	}
	
	private long getCurrentTime() {
		return pause ? lastCheck : System.currentTimeMillis(); 
	}
}
