package test.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import model.Ability;
import model.Ability.ID;

import org.junit.Test;

public class AbilityTest {

	@Test
	public void testGet() {
		ID[] ids = ID.values();
		for (ID id : ids) {
			Ability ability = Ability.get(id);
			assertEquals(id, ability.getId());
			assertNotNull(ability.getName());
			assertNotNull(ability.getSelectionTargetType());
			assertNotNull(ability.getCategory());
			assertNotNull(ability.getDelay());
			assertNotNull(ability.getDamage());
			assertNotNull(ability.getRange());
			assertNotNull(ability.getAreaOfEffectDistance());
			assertNotNull(ability.getSpecial());
			assertNotNull(ability.getStance());
			assertNotNull(ability.getMoveDistance());
			assertNotNull(ability.getDelayOpponent());
			assertNotNull(ability.getStatusToTargetEffectIterator());
		}
	}
}
