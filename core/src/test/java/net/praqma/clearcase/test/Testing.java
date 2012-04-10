package net.praqma.clearcase.test;

import java.io.File;
import java.io.IOException;

import net.praqma.clearcase.test.junit.CoolTestCase;

import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.exceptions.UCMEntityNotFoundException;
import net.praqma.clearcase.exceptions.UnableToCreateEntityException;
import net.praqma.clearcase.exceptions.UnableToGetEntityException;
import net.praqma.clearcase.exceptions.UnableToLoadEntityException;
import net.praqma.clearcase.exceptions.ViewException;
import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.view.SnapshotView;
import net.praqma.clearcase.ucm.view.SnapshotView.Components;
import net.praqma.clearcase.ucm.view.SnapshotView.LoadRules;

import org.junit.Test;

// http://publib.boulder.ibm.com/infocenter/cchelp/v7r0m0/index.jsp?topic=/com.ibm.rational.clearcase.cc_ref.doc/topics/ct_rmproject.htm

public class Testing extends CoolTestCase {

	public Testing() throws CleartoolException {
		super();
	}

	@Test
	public void testBasic() throws Exception {
		bootStrap( new File( "" ) );
		
		File vp = new File( bootstrap.viewpath, "test01" );
		SnapshotView view = SnapshotView.create( bootstrap.integrationStream, vp, "test01" );
		view.Update( false, false, false, false, new LoadRules( view, Components.ALL ) );
		

		assertTrue( true );
	}

}
