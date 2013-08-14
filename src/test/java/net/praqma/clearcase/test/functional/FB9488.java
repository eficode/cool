package net.praqma.clearcase.test.functional;

import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.test.junit.ClearCaseRule;
import net.praqma.clearcase.ucm.entities.Activity;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Version;
import net.praqma.clearcase.ucm.utils.VersionList;
import net.praqma.util.debug.Logger;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class FB9488 {
	
	private static Logger logger = Logger.getLogger();

	@ClassRule
	public static ClearCaseRule ccenv = new ClearCaseRule( "FB6497", "setup-9488.xml" );
	
	@Test
	public void fb9488() throws ClearCaseException {
        Activity a1 = ccenv.context.activities.get( "first-activity" );
        Activity a2 = ccenv.context.activities.get( "second-activity" );
        Activity a3 = ccenv.context.activities.get( "third-activity" );

        VersionList vl = new VersionList().addActivity( a1 ).addActivity( a2 ).addActivity( a3 );
        System.out.println( vl.getLatestForActivities() );
	}
}
