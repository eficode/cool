package net.praqma.clearcase.test.functional;

import java.io.File;
import java.util.List;

import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.test.junit.ClearCaseRule;
import net.praqma.clearcase.ucm.entities.Activity;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Version;
import net.praqma.clearcase.ucm.utils.VersionList;
import net.praqma.util.debug.Logger;

import org.junit.ClassRule;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class FB6497 {
	
	private static Logger logger = Logger.getLogger();

	@ClassRule
	public static ClearCaseRule ccenv = new ClearCaseRule( "FB6497", "setup-multiple-versions-in-baseline.xml" );
	
	@Test
	public void fb6497() throws ClearCaseException {
		Baseline first = ccenv.context.baselines.get( "model-1" );
		Baseline last = ccenv.context.baselines.get( "model-2" );
		
		String viewtag = ccenv.getUniqueName() + "_one_int";
		File path = new File( ccenv.context.mvfs + "/" + viewtag + "/" + ccenv.getVobName() );
		
		List<Activity> acts = Version.getBaselineDiff( first, last, false, path, true );
		
		for( Activity act : acts ) {
			logger.debug( act );
			List<Version> versions = act.changeset.versions;
			for( Version v : versions ) {
				logger.debug( "FILE: " + v.getSFile() + " (" + v.getVersion() + ") user: " + v.blame() );
			}
			
			assertThat( versions.size(), is( 3 ) );
		}
		
		assertThat( acts.size(), is( 1 ) );

		
		for( Activity act : acts ) {
			logger.debug( act );
			VersionList versions = new VersionList( act.changeset.versions ).getLatest();
			for( Version v : versions ) {
				logger.debug( "FILE: " + v.getSFile() + " (" + v.getVersion() + ") user: " + v.blame() );
			}
			
			assertThat( versions.size(), is( 1 ) );
		}
		
	}
}
