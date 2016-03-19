package test.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import model.StatusEffect;
import model.StatusEffect.ID;
import model.Unit;
import model.Unit.TEAM;

import org.junit.Test;

public class StatusEffectTest {

	@Test
	public void testStatusEffectIDInt() {
		ID[] ids = StatusEffect.ID.values();
		int duration = 0;
		for (ID id : ids) {
			StatusEffect statusEffect = new StatusEffect(id, duration);
			assertEquals(id, statusEffect.getId());
			assertEquals(duration, statusEffect.getDuration());
			assertNull(statusEffect.getNoPenaltyTarget());
			assertNotNull(statusEffect.getName());
			assertNotNull(statusEffect.getIcon());
		}
	}

	@Test
	public void testStatusEffectIDIntUnit() {
		Unit unit = Unit.get(Unit.ID.ANNOUNCER, TEAM.NONCOMBATANT);
		ID[] ids = StatusEffect.ID.values();
		int duration = 0;
		for (ID id : ids) {
			StatusEffect statusEffect = new StatusEffect(id, duration, unit);
			assertEquals(id, statusEffect.getId());
			assertEquals(duration, statusEffect.getDuration());
			assertEquals(unit, statusEffect.getNoPenaltyTarget());
			assertNotNull(statusEffect.getName());
			assertNotNull(statusEffect.getIcon());
			duration += 100;
		}
	}

	@Test
	public void testStatusEffectStatusEffect() {
		Unit unit = Unit.get(Unit.ID.ANNOUNCER, TEAM.NONCOMBATANT);
		ID[] ids = StatusEffect.ID.values();
		int duration = 0;
		for (ID id : ids) {
			StatusEffect statusEffect = new StatusEffect(id, duration);
			StatusEffect statusEffectCopy = new StatusEffect(statusEffect);
			assertEquals(statusEffect.getId(), statusEffectCopy.getId());
			assertEquals(statusEffect.getDuration(), statusEffectCopy.getDuration());
			assertEquals(statusEffect.getNoPenaltyTarget(), statusEffectCopy.getNoPenaltyTarget());
			assertEquals(statusEffect.getName(), statusEffectCopy.getName());
			statusEffect = new StatusEffect(id, duration, unit);
			statusEffectCopy = new StatusEffect(statusEffect);
			assertEquals(statusEffect.getId(), statusEffectCopy.getId());
			assertEquals(statusEffect.getDuration(), statusEffectCopy.getDuration());
			assertEquals(statusEffect.getNoPenaltyTarget(), statusEffectCopy.getNoPenaltyTarget());
			assertEquals(statusEffect.getName(), statusEffectCopy.getName());
			duration += 100;
		}
	}

	@Test
	public void testTick() {
		StatusEffect statusEffect = new StatusEffect(ID.BIND, 2000);
		assertEquals(2000, statusEffect.getDuration());
		statusEffect.tick(500);
		assertEquals(1500, statusEffect.getDuration());
		statusEffect.tick(397);
		assertEquals(1103, statusEffect.getDuration());
	}

	@Test
	public void testIsOver() {
		StatusEffect statusEffect = new StatusEffect(ID.BIND, 2000);
		assertEquals(2000, statusEffect.getDuration());
		statusEffect.tick(1500);
		assertFalse(statusEffect.isOver());
		statusEffect.tick(597);
		assertTrue(statusEffect.isOver());
	}
}
