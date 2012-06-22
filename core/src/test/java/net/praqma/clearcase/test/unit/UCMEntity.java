package net.praqma.clearcase.test.unit;

import net.praqma.clearcase.exceptions.UnableToInitializeEntityException;
import net.praqma.clearcase.ucm.entities.Component;

import org.junit.Test;

public class UCMEntity {

	@Test
	public void testInitialize() throws UnableToInitializeEntityException {
		String c = "component:_System@\\ccucm-9-22339111_PVOB";
		Component.get( c );
	}
}
