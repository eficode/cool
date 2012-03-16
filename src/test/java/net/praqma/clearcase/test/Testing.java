package net.praqma.clearcase.test;

import net.praqma.clearcase.annotations.TestConfiguration;
import net.praqma.clearcase.exceptions.UCMEntityNotFoundException;
import net.praqma.clearcase.exceptions.UnableToCreateEntityException;
import net.praqma.clearcase.exceptions.UnableToGetEntityException;

import org.junit.Test;

// http://publib.boulder.ibm.com/infocenter/cchelp/v7r0m0/index.jsp?topic=/com.ibm.rational.clearcase.cc_ref.doc/topics/ct_rmproject.htm

@TestConfiguration( pvob="TESTING_PVOB" )
public class Testing extends CoolTestCase {
	
	@Test
	public void testbla() throws UnableToCreateEntityException, UCMEntityNotFoundException, UnableToGetEntityException {
		bootStrap();
		assertTrue( true );
	}

}
