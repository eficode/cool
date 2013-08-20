package net.praqma.clearcase.test.functional;

import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.test.junit.ClearCaseRule;
import net.praqma.clearcase.ucm.entities.Activity;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Version;
import net.praqma.clearcase.ucm.utils.VersionList;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class FB9488 {
	
	private static Logger logger = Logger.getLogger( FB9488.class.getName() );

	@ClassRule
	public static ClearCaseRule ccenv = new ClearCaseRule( "FB9488", "setup-9488.xml" );
	
	@Test
	public void fb9488() throws ClearCaseException {

        Baseline bl1 = ccenv.context.baselines.get( "model-1" );
        Baseline bl2 = ccenv.context.baselines.get( "model-dev-2" );

        String viewTag = ccenv.getUniqueName() + "_one_dev";
        logger.finer( "Creating content for " + viewTag );

        File path = new File( ccenv.context.mvfs + "/" + viewTag + "/" + ccenv.getVobName() );
        logger.finer( "Using path " + path );

        List<Activity> activities = Version.getBaselineDiff( bl2, bl1, true, path );

        VersionList vl = new VersionList().addActivities( activities );
        Map<Activity, List<Version>> s = vl.getLatestForActivities();
        for( Activity a : s.keySet() ) {
            System.out.println( "Activity: " + a.getHeadline() );
            if( s.get( a ) != null ) {
                for( Version v : s.get( a ) ) {
                    System.out.println( " * " + v );
                }
            }
        }

        /*  The third activity */
        assertThat( s.size(), is(1) );
	}

    @Test
    public void fb9488_2() throws ClearCaseException {
        Activity a1 = ccenv.context.activities.get( "first-activity" );
        Activity a2 = ccenv.context.activities.get( "second-activity" );
        Activity a3 = ccenv.context.activities.get( "third-activity" );

        String viewTag = ccenv.getUniqueName() + "_one_dev";
        logger.finer( "Creating content for " + viewTag );

        File path = new File( ccenv.context.mvfs + "/" + viewTag + "/" + ccenv.getVobName() );
        logger.finer( "Using path " + path );

        VersionList vl = new VersionList().setPath( path ).addActivity( a1 ).addActivity( a2 ).addActivity( a3 );

        Map<Activity, List<Version>> s = vl.getLatestForActivities();
        for( Activity a : s.keySet() ) {
            System.out.println( "Activity: " + a.getHeadline() );
            if( s.get( a ) != null ) {
                for( Version v : s.get( a ) ) {
                    System.out.println( " * " + v );
                }
            }
        }

        /*  The third activity AND the first, containing the Model folder */
        assertThat( s.size(), is(2) );
    }
}
