package net.praqma.clearcase.test;

import org.junit.Test;

import net.praqma.clearcase.test.junit.CoolTestCase;
import net.praqma.clearcase.ucm.entities.Stream;

public class TestStream extends CoolTestCase {

	@Test
	public void testFoundationBaselines() throws Exception {
		
		String uniqueTestVobName = "cool" + uniqueTimeStamp;
		variables.put( "vobname", uniqueTestVobName );
		variables.put( "pvobname", uniqueTestVobName + "_PVOB" );
		
		bootStrap( defaultSetup );
		
		Stream stream = Stream.get( uniqueTestVobName + "_one_int", getPVob() ).load();
		
		System.out.println( "Foundation baselines:" + stream.getFoundationBaselines() );

		assertTrue( true );
	}

}
