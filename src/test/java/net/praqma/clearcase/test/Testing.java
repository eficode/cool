package net.praqma.clearcase.test;

import net.praqma.clearcase.annotations.TestConfiguration;

import org.junit.Test;

// http://publib.boulder.ibm.com/infocenter/cchelp/v7r0m0/index.jsp?topic=/com.ibm.rational.clearcase.cc_ref.doc/topics/ct_rmproject.htm

@TestConfiguration( pvob="TESTING_PVOB" )
public class Testing extends CoolTestCase {
	
	@Test
	public void testbla() {
		assertTrue( true );
	}
	
	@Test
	public void testbla3() {
		assertTrue( true );
	}
}
