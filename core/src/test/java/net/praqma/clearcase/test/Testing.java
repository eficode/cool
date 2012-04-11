package net.praqma.clearcase.test;

import net.praqma.clearcase.test.junit.CoolTestCase;

import org.junit.Test;

// http://publib.boulder.ibm.com/infocenter/cchelp/v7r0m0/index.jsp?topic=/com.ibm.rational.clearcase.cc_ref.doc/topics/ct_rmproject.htm

public class Testing extends CoolTestCase {


	@Test
	public void testBasic() throws Exception {
		bootStrap( defaultSetup );
		
		/*
		File vp = new File( bootstrap.viewpath, "test01" );
		SnapshotView view = SnapshotView.create( bootstrap.integrationStream, vp, "test01" );
		view.Update( false, false, false, false, new LoadRules( view, Components.ALL ) );
		*/

		assertTrue( true );
	}

}
