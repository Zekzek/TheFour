package test.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import model.Ability;
import model.ReadiedAction;
import model.GameObject.TEAM;
import model.Unit;
import model.Unit.ID;
import model.World;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import controller.ActionQueue;

public class BattleQueueTest {
	private static Unit defender;
	private static Unit berserker;
	private static Ability ability1;
	private static Ability ability2;
	private static ActionQueue battleQueue;
	
	@BeforeClass
	public static void setupUnits() {
		World world = new World();
		battleQueue = new ActionQueue(world);
		defender = Unit.get(ID.DEFENDER, TEAM.PLAYER, "Defender");
		berserker = Unit.get(ID.BERSERKER, TEAM.ENEMY1, "Berserker");
		ability1 = Ability.get(Ability.ID.DELAY);
		ability2 = Ability.get(Ability.ID.WATCH);
	}
	
	@Test
	public void testAddCombatants() {
		battleQueue.addGameObject(defender, 1, 1);
		battleQueue.addGameObject(berserker, 1, 2);
		assertEquals(battleQueue.getLastScheduledTime(defender), 0);
		assertEquals(battleQueue.getCompletionTime(defender), 0);
		assertEquals(battleQueue.getLastScheduledTime(berserker), 0);
		assertEquals(battleQueue.getCompletionTime(berserker), 0);
		assertFalse(battleQueue.getActionQueueIterator().hasNext());		
	}

	@Test
	public void testAddCombatant() {
		battleQueue.addGameObject(defender, 1, 1);
		assertEquals(battleQueue.getLastScheduledTime(defender), 0);
		assertEquals(battleQueue.getCompletionTime(defender), 0);
		assertEquals(battleQueue.getLastScheduledTime(berserker), -1);
		assertEquals(battleQueue.getCompletionTime(berserker), -1);
		assertFalse(battleQueue.getActionQueueIterator().hasNext());
	}

	@Test
	public void testRemoveCombatant() {
		battleQueue.addGameObject(defender, 1, 1);
		battleQueue.addGameObject(berserker, 1, 2);
		assertEquals(battleQueue.getLastScheduledTime(defender), 0);
		assertEquals(battleQueue.getCompletionTime(defender), 0);
		assertEquals(battleQueue.getLastScheduledTime(berserker), 0);
		assertEquals(battleQueue.getCompletionTime(berserker), 0);
		assertFalse(battleQueue.getActionQueueIterator().hasNext());
		
		battleQueue.queueAction(ability1, defender, defender);
		assertEquals(battleQueue.getLastScheduledTime(defender), 0);
		assertEquals(battleQueue.getCompletionTime(defender), ability1.getDelay());
		assertEquals(battleQueue.getLastScheduledTime(berserker), 0);
		assertEquals(battleQueue.getCompletionTime(berserker), 0);
		assertTrue(battleQueue.getActionQueueIterator().hasNext());
		
		battleQueue.defeatGameObject(defender);
		assertEquals(battleQueue.getLastScheduledTime(defender), -1);
		assertEquals(battleQueue.getCompletionTime(defender), -1);
		assertEquals(battleQueue.getLastScheduledTime(berserker), 0);
		assertEquals(battleQueue.getCompletionTime(berserker), 0);
		assertFalse(battleQueue.getActionQueueIterator().hasNext());
	}

	@Test
	public void testAddRandomCombatDelays() {
		battleQueue.addGameObject(defender, 1, 1);
		battleQueue.addGameObject(berserker, 1, 2);
		assertEquals(battleQueue.getLastScheduledTime(defender), 0);
		assertEquals(battleQueue.getCompletionTime(defender), 0);
		assertEquals(battleQueue.getLastScheduledTime(berserker), 0);
		assertEquals(battleQueue.getCompletionTime(berserker), 0);
		assertFalse(battleQueue.getActionQueueIterator().hasNext());

		battleQueue.queueAction(ability1, defender, defender);
		battleQueue.addRandomCombatDelays();
		battleQueue.queueAction(ability1, berserker, berserker);
		assertTrue(battleQueue.getLastScheduledTime(defender) > 0);
		assertTrue(battleQueue.getCompletionTime(defender) > 0);
		assertTrue(battleQueue.getLastScheduledTime(berserker) > 0);
		assertTrue(battleQueue.getCompletionTime(berserker) > 0);
		Iterator<ReadiedAction> actions = battleQueue.getActionQueueIterator();
		while (actions.hasNext()) {
			ReadiedAction action = actions.next();
			assertEquals(battleQueue.getLastScheduledTime(action.getSource()), action.getStartTime());
		}
	}

	@Test
	public void testEndCombat() {
		battleQueue.addGameObject(defender, 1, 1);
		battleQueue.addGameObject(berserker, 1, 2);
		battleQueue.queueAction(ability1, defender, defender);
		battleQueue.queueAction(ability1, berserker, berserker);
		battleQueue.addRandomCombatDelays();
		assertTrue(battleQueue.getLastScheduledTime(defender) > 0);
		assertTrue(battleQueue.getCompletionTime(defender) > 0);
		assertTrue(battleQueue.getLastScheduledTime(berserker) > 0);
		assertTrue(battleQueue.getCompletionTime(berserker) > 0);
		assertTrue(battleQueue.getActionQueueIterator().hasNext());
	}

	@Test
	public void testQueueAction() {
		battleQueue.addGameObject(defender, 1, 1);
		battleQueue.queueAction(ability1, defender, defender);
		assertEquals(battleQueue.getLastScheduledTime(defender), 0);
		assertTrue(battleQueue.getCompletionTime(defender) > 0);
		assertTrue(battleQueue.getActionQueueIterator().hasNext());
		Iterator<ReadiedAction> actions = battleQueue.getActionQueueIterator();
		while (actions.hasNext()) {
			ReadiedAction action = actions.next();
			assertEquals(ability1, action.getAbility());
			assertEquals(battleQueue.getLastScheduledTime(action.getSource()), action.getStartTime());
		}
	}

	@Test
	public void testInsertFirstActionAbilityUnitITargetable() {
		battleQueue.addGameObject(defender, 1, 1);
		battleQueue.queueAction(ability1, defender, defender);
		assertEquals(battleQueue.getLastScheduledTime(defender), 0);
		assertTrue(battleQueue.getCompletionTime(defender) > 0);
		
		battleQueue.insertFirstAction(ability2, defender, defender);
		assertTrue(battleQueue.getLastScheduledTime(defender) > 0);
		assertEquals(battleQueue.getCompletionTime(defender), ability1.getDelay() + ability2.getDelay());
		
		Iterator<ReadiedAction> actions = battleQueue.getActionQueueIterator();
		ReadiedAction firstAction = actions.next();
		assertEquals(ability2, firstAction.getAbility());
		assertEquals(0, firstAction.getStartTime());
		ReadiedAction secondAction = actions.next();
		assertEquals(ability1, secondAction.getAbility());
		assertEquals(ability2.getDelay(), secondAction.getStartTime());
	}

	@Test
	public void testInsertFirstActionAbilityUnitITargetableRunnableRunnable() {
		battleQueue.addGameObject(defender, 1, 1);
		battleQueue.queueAction(ability1, defender, defender);
		assertEquals(battleQueue.getLastScheduledTime(defender), 0);
		assertTrue(battleQueue.getCompletionTime(defender) > 0);
		
		battleQueue.insertFirstAction(ability2, defender, defender, new Runnable(){
			@Override
			public void run() {
				// do nothing for this test
			}}, new Runnable(){
			@Override
			public void run() {
				// do nothing for this test
			}});
		assertTrue(battleQueue.getLastScheduledTime(defender) > 0);
		assertEquals(battleQueue.getCompletionTime(defender), ability1.getDelay() + ability2.getDelay());
		
		Iterator<ReadiedAction> actions = battleQueue.getActionQueueIterator();
		ReadiedAction firstAction = actions.next();
		assertEquals(ability2, firstAction.getAbility());
		assertEquals(0, firstAction.getStartTime());
		ReadiedAction secondAction = actions.next();
		assertEquals(ability1, secondAction.getAbility());
		assertEquals(ability2.getDelay(), secondAction.getStartTime());
	}

	@Test
	public void testDequeueAction() {
		battleQueue.addGameObject(defender, 1, 1);
		battleQueue.queueAction(ability1, defender, defender);
		battleQueue.queueAction(ability2, defender, defender);
		
		battleQueue.dequeueAction(battleQueue.getActionQueueIterator().next());
		assertEquals(battleQueue.getLastScheduledTime(defender), 0);
		assertEquals(battleQueue.getCompletionTime(defender), ability2.getDelay());
		
		Iterator<ReadiedAction> actions = battleQueue.getActionQueueIterator();
		ReadiedAction firstAction = actions.next();
		assertEquals(ability2, firstAction.getAbility());
		assertEquals(0, firstAction.getStartTime());
		assertFalse(actions.hasNext());
	}

	@Test
	public void testGetMostReadyCombatant() {
		battleQueue.addGameObject(defender, 1, 1);
		battleQueue.addGameObject(berserker, 1, 2);
		battleQueue.queueAction(ability1, defender, defender);
		assertEquals(berserker, battleQueue.getMostReadyPlayer());
	}

	@Test
	public void testDelay() {
		battleQueue.addGameObject(defender, 1, 1);
		battleQueue.addGameObject(berserker, 1, 2);
		battleQueue.queueAction(ability1, defender, defender);
		battleQueue.queueAction(ability1, berserker, berserker);
		assertEquals(battleQueue.getLastScheduledTime(defender), 0);
		assertTrue(battleQueue.getCompletionTime(defender) > 0);
		
		int delayAmount = 5100;
		battleQueue.delay(defender, delayAmount);
		assertEquals(battleQueue.getLastScheduledTime(defender), delayAmount);
		assertEquals(battleQueue.getCompletionTime(defender), ability1.getDelay() + delayAmount);
		assertEquals(battleQueue.getLastScheduledTime(berserker), 0);
		assertEquals(battleQueue.getCompletionTime(berserker), ability1.getDelay());
		
		Iterator<ReadiedAction> actions = battleQueue.getActionQueueIterator();
		ReadiedAction firstAction = actions.next();
		assertEquals(berserker, firstAction.getSource());
		assertEquals(0, firstAction.getStartTime());
		ReadiedAction secondAction = actions.next();
		assertEquals(defender, secondAction.getSource());
		assertEquals(delayAmount, secondAction.getStartTime());
	}
}
