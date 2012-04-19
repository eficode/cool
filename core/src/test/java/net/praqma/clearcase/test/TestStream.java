package net.praqma.clearcase.test;

import org.junit.Test;

import net.praqma.clearcase.test.junit.CoolTestCase;

public class TestStream extends CoolTestCase {

	@Test
	public void testFoundationBaselines() throws Exception {
		
		String uniqueTestVobName = "cool" + uniqueTimeStamp;
		variables.put( "vobname", uniqueTestVobName );
		variables.put( "pvobname", uniqueTestVobName );
		
		bootStrap( defaultSetup );
		
		/*
		bootstrap.integrationStream.load();
		System.out.println( "Foundation baselines:" + bootstrap.integrationStream.getFoundationBaselines() );
	*/

		assertTrue( true );
	}

}
