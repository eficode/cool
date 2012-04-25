package net.praqma.clearcase.test;

import java.util.List;

import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.test.junit.CoolTestCase;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.entities.Project.PromotionLevel;
import net.praqma.clearcase.ucm.utils.Baselines;
import net.praqma.util.debug.Logger;

import org.junit.Test;

// http://publib.boulder.ibm.com/infocenter/cchelp/v7r0m0/index.jsp?topic=/com.ibm.rational.clearcase.cc_ref.doc/topics/ct_rmproject.htm

public class Testing extends CoolTestCase {

	private static Logger logger = Logger.getLogger();

	@Test
	public void testBasic() throws ClearCaseException {
		
		String uniqueTestVobName = "cool" + uniqueTimeStamp;
		variables.put( "vobname", uniqueTestVobName );
		variables.put( "pvobname", uniqueTestVobName + "_PVOB" );
		
		try {
			bootStrap( defaultSetup );
		} catch( Exception e ) {
			fail();
		}
		
		Stream stream = Stream.get( uniqueTestVobName + "_one_int", getPVob() ).load();
		Component comp = Component.get( "Model", getPVob() );
		
		List<Baseline> baselines = Baselines.get( stream, comp, PromotionLevel.INITIAL );
		System.out.println( "Baselines:" + baselines );
		
		assertTrue( true );
	}

}
