package net.praqma.clearcase.test.unit;

import static org.junit.Assert.*;
import net.praqma.clearcase.Vob;

import org.junit.Test;

public class PVobTest {

	@Test
	public void naming() {
		String windowsName = "\\mypvob";
		String unixName1   = "/mypvob";
		String unixName2   = "/vobs/mypvob";
		
		assertTrue( Vob.isValidTag( windowsName ) );
		assertTrue( Vob.isValidTag( unixName1 ) );
		assertTrue( Vob.isValidTag( unixName2 ) );
	}
}
