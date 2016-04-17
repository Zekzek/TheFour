package test.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import model.Ability;
import model.ReadiedAction;
import model.Unit;
import model.Unit.ID;
import model.Unit.TEAM;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import controller.BattleQueue;

public class BattleQueueTest {
	private static Unit defender;
	private static Unit berserker;
	private static Ability ability1;
	private static Ability ability2;
	
	@BeforeClass
	public static void setupUnits() {
		defender = Unit.get(ID.DEFENDER, TEAM.PLAYER, "Defender");
		berserker = Unit.get(ID.BERSERKER, TEAM.ENEMY1, "Berserker");
		ability1 = Ability.get(Ability.ID.DELAY);
		ability2 = Ability.get(Ability.ID.WATCH);
	}
	
	@After
	public void reset() {
		BattleQueue.endCombat();
	}
	
	@Test
	public void testAddCombatants() {
		Set<Unit> units = new HashSet<Unit>();
		units.add(defender);
		units.add(berserker);
		BattleQueue.addCombatants(units.iterator());
		assertEquals(BattleQueue.getLastScheduledTime(defender), 0);
		assertEquals(BattleQueue.getCompletionTime(defender), 0);
		assertEquals(BattleQueue.getLastScheduledTime(berserker), 0);
		assertEquals(BattleQueue.getCompletionTime(berserker), 0);
		assertFalse(BattleQueue.getActionQueueIterator().hasNext());		
	}

	@Test
	public void testAddCombatant() {
		BattleQueue.addCombatant(defender);
		assertEquals(BattleQueue.getLastScheduledTime(defender), 0);
		assertEquals(BattleQueue.getCompletionTime(defender), 0);
		assertEquals(BattleQueue.getLastScheduledTime(berserker), -1);
		assertEquals(BattleQueue.getCompletionTime(berserker), -1);
		assertFalse(BattleQueue.getActionQueueIterator().hasNext());
	}

	@Test
	public void testRemoveCombatant() {
		BattleQueue.addCombatant(defender);
		BattleQueue.addCombatant(berserker);
		assertEquals(BattleQueue.getLastScheduledTime(defender), 0);
		assertEquals(BattleQueue.getCompletionTime(defender), 0);
		assertEquals(BattleQueue.getLastScheduledTime(berserker), 0);
		assertEquals(BattleQueue.getCompletionTime(berserker), 0);
		assertFalse(BattleQueue.getActionQueueIterator().hasNext());
		
		BattleQueue.queueAction(ability1, defender, defender);
		assertEquals(BattleQueue.getLastScheduledTime(defender), 0);
		assertEquals(BattleQueue.getCompletionTime(defender), ability1.getDelay());
		assertEquals(BattleQueue.getLastScheduledTime(berserker), 0);
		assertEquals(BattleQueue.getCompletionTime(berserker), 0);
		assertTrue(BattleQueue.getActionQueueIterator().hasNext());
		
		BattleQueue.removeCombatant(defender, null, null);
		assertEquals(BattleQueue.getLastScheduledTime(defender), -1);
		assertEquals(BattleQueue.getCompletionTime(defender), -1);
		assertEquals(BattleQueue.getLastScheduledTime(berserker), 0);
		assertEquals(BattleQueue.getCompletionTime(berserker), 0);
		assertFalse(BattleQueue.getActionQueueIterator().hasNext());
	}

	@Test
	public void testAddRandomCombatDelays() {
		BattleQueue.addCombatant(defender);
		BattleQueue.addCombatant(berserker);
		assertEquals(BattleQueue.getLastScheduledTime(defender), 0);
		assertEquals(BattleQueue.getCompletionTime(defender), 0);
		assertEquals(BattleQueue.getLastScheduledTime(berserker), 0);
		assertEquals(BattleQueue.getCompletionTime(berserker), 0);
		assertFalse(BattleQueue.getActionQueueIterator().hasNext());

		BattleQueue.queueAction(ability1, defender, defender);
		BattleQueue.addRandomCombatDelays();
		BattleQueue.queueAction(ability1, berserker, berserker);
		assertTrue(BattleQueue.getLastScheduledTime(defender) > 0);
		assertTrue(BattleQueue.getCompletionTime(defender) > 0);
		assertTrue(BattleQueue.getLastScheduledTime(berserker) > 0);
		assertTrue(BattleQueue.getCompletionTime(berserker) > 0);
		Iterator<ReadiedAction> actions = BattleQueue.getActionQueueIterator();
		while (actions.hasNext()) {
			ReadiedAction action = actions.next();
			assertEquals(BattleQueue.getLastScheduledTime(action.getSource()), action.getStartTime());
		}
	}

	@Test
	public void testEndCombat() {
		BattleQueue.addCombatant(defender);
		BattleQueue.addCombatant(berserker);
		BattleQueue.queueAction(ability1, defender, defender);
		BattleQueue.queueAction(ability1, berserker, berserker);
		BattleQueue.addRandomCombatDelays();
		assertTrue(BattleQueue.getLastScheduledTime(defender) > 0);
		assertTrue(BattleQueue.getCompletionTime(defender) > 0);
		assertTrue(BattleQueue.getLastScheduledTime(berserker) > 0);
		assertTrue(BattleQueue.getCompletionTime(berserker) > 0);
		assertTrue(BattleQueue.getActionQueueIterator().hasNext());

		BattleQueue.endCombat();
		assertEquals(BattleQueue.getLastScheduledTime(defender), -1);
		assertEquals(BattleQueue.getCompletionTime(defender), -1);
		assertEquals(BattleQueue.getLastScheduledTime(berserker), -1);
		assertEquals(BattleQueue.getCompletionTime(berserker), -1);
		assertFalse(BattleQueue.getActionQueueIterator().hasNext());
	}

	@Test
	public void testQueueAction() {
		BattleQueue.addCombatant(defender);
		BattleQueue.queueAction(ability1, defender, defender);
		assertEquals(BattleQueue.getLastScheduledTime(defender), 0);
		assertTrue(BattleQueue.getCompletionTime(defender) > 0);
		assertTrue(BattleQueue.getActionQueueIterator().hasNext());
		Iterator<ReadiedAction> actions = BattleQueue.getActionQueueIterator();
		while (actions.hasNext()) {
			ReadiedAction action = actions.next();
			assertEquals(ability1, action.getAbility());
			assertEquals(BattleQueue.getLastScheduledTime(action.getSource()), action.getStartTime());
		}
	}

	@Test
	public void testInsertFirstActionAbilityUnitITargetable() {
		BattleQueue.addCombatant(defender);
		BattleQueue.queueAction(ability1, defender, defender);
		assertEquals(BattleQueue.getLastScheduledTime(defender), 0);
		assertTrue(BattleQueue.getCompletionTime(defender) > 0);
		
		BattleQueue.insertFirstAction(ability2, defender, defender);
		assertTrue(BattleQueue.getLastScheduledTime(defender) > 0);
		assertEquals(BattleQueue.getCompletionTime(defender), ability1.getDelay() + ability2.getDelay());
		
		Iterator<ReadiedAction> actions = BattleQueue.getActionQueueIterator();
		ReadiedAction firstAction = actions.next();
		assertEquals(ability2, firstAction.getAbility());
		assertEquals(0, firstAction.getStartTime());
		ReadiedAction secondAction = actions.next();
		assertEquals(ability1, secondAction.getAbility());
		assertEquals(ability2.getDelay(), secondAction.getStartTime());
	}

	@Test
	public void testInsertFirstActionAbilityUnitITargetableRunnableRunnable() {
		BattleQueue.addCombatant(defender);
		BattleQueue.queueAction(ability1, defender, defender);
		assertEquals(BattleQueue.getLastScheduledTime(defender), 0);
		assertTrue(BattleQueue.getCompletionTime(defender) > 0);
		
		BattleQueue.insertFirstAction(ability2, defender, defender, new Runnable(){
			@Override
			public void run() {
				// do nothing for this test
			}}, new Runnable(){
			@Override
			public void run() {
				// do nothing for this test
			}});
		assertTrue(BattleQueue.getLastScheduledTime(defender) > 0);
		assertEquals(BattleQueue.getCompletionTime(defender), ability1.getDelay() + ability2.getDelay());
		
		Iterator<ReadiedAction> actions = BattleQueue.getActionQueueIterator();
		ReadiedAction firstAction = actions.next();
		assertEquals(ability2, firstAction.getAbility());
		assertEquals(0, firstAction.getStartTime());
		ReadiedAction secondAction = actions.next();
		assertEquals(ability1, secondAction.getAbility());
		assertEquals(ability2.getDelay(), secondAction.getStartTime());
	}

	@Test
	public void testDequeueAction() {
		BattleQueue.addCombatant(defender);
		BattleQueue.queueAction(ability1, defender, defender);
		BattleQueue.queueAction(ability2, defender, defender);
		
		BattleQueue.dequeueAction(BattleQueue.getActionQueueIterator().next());
		assertEquals(BattleQueue.getLastScheduledTime(defender), 0);
		assertEquals(BattleQueue.getCompletionTime(defender), ability2.getDelay());
		
		Iterator<ReadiedAction> actions = BattleQueue.getActionQueueIterator();
		ReadiedAction firstAction = actions.next();
		assertEquals(ability2, firstAction.getAbility());
		assertEquals(0, firstAction.getStartTime());
		assertFalse(actions.hasNext());
	}

	@Test
	public void testGetMostReadyCombatant() {
		BattleQueue.addCombatant(defender);
		BattleQueue.addCombatant(berserker);
		BattleQueue.queueAction(ability1, defender, defender);
		assertEquals(berserker, BattleQueue.getMostReadyPlayer());
	}

	@Test
	public void testDelay() {
		BattleQueue.addCombatant(defender);
		BattleQueue.addCombatant(berserker);
		BattleQueue.queueAction(ability1, defender, defender);
		BattleQueue.queueAction(ability1, berserker, berserker);
		assertEquals(BattleQueue.getLastScheduledTime(defender), 0);
		assertTrue(BattleQueue.getCompletionTime(defender) > 0);
		
		int delayAmount = 5100;
		BattleQueue.delay(defender, delayAmount);
		assertEquals(BattleQueue.getLastScheduledTime(defender), delayAmount);
		assertEquals(BattleQueue.getCompletionTime(defender), ability1.getDelay() + delayAmount);
		assertEquals(BattleQueue.getLastScheduledTime(berserker), 0);
		assertEquals(BattleQueue.getCompletionTime(berserker), ability1.getDelay());
		
		Iterator<ReadiedAction> actions = BattleQueue.getActionQueueIterator();
		ReadiedAction firstAction = actions.next();
		assertEquals(berserker, firstAction.getSource());
		assertEquals(0, firstAction.getStartTime());
		ReadiedAction secondAction = actions.next();
		assertEquals(defender, secondAction.getSource());
		assertEquals(delayAmount, secondAction.getStartTime());
	}

//	@Test
//	public void testAddBattleListener() {
//		fail("Not yet implemented");
//	}
//	@Test
//	public void testClearBattleListeners() {
//		fail("Not yet implemented");
//	}
//	@Test
//	public void testStartPlayingActions() {
//		fail("Not yet implemented");
//	}
//	@Test
//	public void testPerformNextAction() {
//		fail("Not yet implemented");
//	}
//	@Test
//	public void testSetActionComplete() {
//		fail("Not yet implemented");
//	}
}
