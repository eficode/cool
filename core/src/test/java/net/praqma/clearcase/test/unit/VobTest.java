package net.praqma.clearcase.test.unit;

import net.praqma.clearcase.PVob;
import net.praqma.clearcase.Vob;

import org.junit.Test;

import junit.framework.TestCase;

public class VobTest extends TestCase {

	@Test
	public void testVobEqualsDifferent() {
		Vob v1 = new Vob( "/vob1" );
		Vob v2 = new Vob( "/vob2" );
		
		assertNotSame( v1, v2 );
		assertFalse( v1.hashCode() == v2.hashCode() );
	}
	
	@Test
	public void testVobEquals() {
		Vob v1 = new Vob( "/vob1" );
		Vob v2 = new Vob( "/vob1" );
		
		assertEquals( v1, v2 );
		assertTrue( v1.hashCode() == v2.hashCode() );
	}
	
	@Test
	public void testPVobEqualsDifferent() {
		PVob v1 = new PVob( "/vob_pvob1" );
		PVob v2 = new PVob( "/vob_pvob2" );
		
		assertNotSame( v1, v2 );
		assertFalse( v1.hashCode() == v2.hashCode() );
	}
	
	@Test
	public void testPVobEquals() {
		PVob v1 = new PVob( "/vob_pvob1" );
		PVob v2 = new PVob( "/vob_pvob1" );
		
		assertEquals( v1, v2 );
		assertTrue( v1.hashCode() == v2.hashCode() );
	}
}
