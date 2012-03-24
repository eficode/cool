package net.praqma.clearcase.test;

import org.junit.Test;

import net.praqma.clearcase.test.junit.CoolTestCase;

public class TestStream extends CoolTestCase {

	@Test
	public void testFoundationBaselines() throws Exception {
		bootStrap( "testProject", "test_int" );
		
		bootstrap.integrationStream.load();
		System.out.println( "Foundation baselines:" + bootstrap.integrationStream.getFoundationBaselines() );

		assertTrue( true );
	}

}
