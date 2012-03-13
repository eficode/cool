package net.praqma.clearcase.ucm.utils;

import java.util.ArrayList;
import java.util.List;

import net.praqma.clearcase.PVob;
import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.exceptions.UnableToListBaselinesException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.entities.Project.PromotionLevel;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.util.debug.Logger;
import net.praqma.util.execute.AbnormalProcessTerminationException;

public class Baselines {
	private static Logger logger = Logger.getLogger();
	
	
	
	public static List<Baseline> get( Stream stream, Component component, PromotionLevel plevel, PVob pvob ) throws UnableToListBaselinesException {
		logger.debug( "Getting baselines from " + stream.getFullyQualifiedName() + " and " + component.getFullyQualifiedName() + " with level " + plevel + " in VOB=" + pvob );
		//List<String> bls_str = strategy.getBaselines( component.getFullyQualifiedName(), stream.getFullyQualifiedName(), plevel );
		List<String> bls_str = null;
		
		String cmd = "lsbl -s -component " + component + " -stream " + stream + ( plevel != null ? " -level " + plevel.toString() : "" );
		try {
			bls_str = Cleartool.run( cmd ).stdoutList;
		} catch( AbnormalProcessTerminationException e ) {
			//throw new UCMException( "Unable to get baselines: " + e.getMessage() );
			throw new UnableToListBaselinesException( stream, component, plevel, e );
		}

		logger.debug( "I got " + bls_str.size() + " baselines." );
		List<Baseline> bls = new ArrayList<Baseline>();

		for( String bl : bls_str ) {
			bls.add( Baseline.get( bl + "@" + pvob, true ) );
		}

		return bls;
	}
}
