package controller;

import java.util.Iterator;

import model.ReadiedAction;
import model.Unit;

public interface IPlayerListener {
	public void onChangedActivePlayer(Unit unit);
	public void onActivePlayerAbilityQueueChanged(Iterator<ReadiedAction> actions);
	public void onPlayerUsedAbility(ReadiedAction action);
}
