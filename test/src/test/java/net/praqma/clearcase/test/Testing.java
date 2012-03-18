package net.praqma.clearcase.test;

import java.io.File;
import java.io.IOException;

import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.exceptions.UCMEntityNotFoundException;
import net.praqma.clearcase.exceptions.UnableToCreateEntityException;
import net.praqma.clearcase.exceptions.UnableToGetEntityException;
import net.praqma.clearcase.exceptions.UnableToLoadEntityException;
import net.praqma.clearcase.exceptions.ViewException;
import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.view.SnapshotView;

import org.junit.Test;

// http://publib.boulder.ibm.com/infocenter/cchelp/v7r0m0/index.jsp?topic=/com.ibm.rational.clearcase.cc_ref.doc/topics/ct_rmproject.htm

public class Testing extends CoolTestCase {

	@Test
	public void testBasic() throws UnableToCreateEntityException, UCMEntityNotFoundException, UnableToGetEntityException, CleartoolException, ViewException, UnableToLoadEntityException, IOException {
		bootStrap( "testProject", "test_int" );
		
		SnapshotView.create( integrationStream, new File( viewpath, "test01" ), "test01" );

		assertTrue( true );
	}

}
