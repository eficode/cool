package net.praqma.clearcase.ucm.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.exceptions.UCMException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.UCMEntity;
import net.praqma.clearcase.ucm.entities.Version;
import net.praqma.clearcase.ucm.entities.Version.Status;
import net.praqma.clearcase.ucm.view.SnapshotView;
import net.praqma.util.debug.Logger;

/**
 * 
 * 
	 * Compare two {@link Baseline}s. I.e. bl1 -> bl2.
	 * 
	 * @param bl1
	 *            {@link Baseline} dated before bl2
	 * @param bl2
	 *            {@link Baseline} dated after bl1
	 * @param merge
	 *            true => , false => -nmerge
	 * @param view
	 *            A {@link SnapshotView}
	 * @return A list of {@link Version}s
	 * @throws UCMException
	 *
 *	public static List<Version> baselineDifferences( Baseline bl1, Baseline bl2, boolean merge, SnapshotView view ) throws UCMException {
 *		return context.baselineDifferences( bl1, bl2, merge, view );
 *	}
 *
	*
	 * Compare this {@link Baseline} to another previous {@link Baseline}. I.e.
	 * baseline -> this.
	 * 
	 * @param before
	 *            {@link Baseline} dated before this. Can be null = -pre
	 * @param view
	 *            A {@link SnapshotView}
	 * @return A list of {@link Version}s
	 * @throws UCMException
	 *
 *	public List<Version> beforeBaselineDifferences( Baseline before, SnapshotView view ) throws UCMException {
 *		return context.baselineDifferences( before, this, true, view );
 *	}
 *
	 * Compare this {@link Baseline} to another subsequent {@link Baseline}.
	 * I.e. this -> baseline.
	 * 
	 * @param after
	 *            {@link Baseline} dated after this
	 * @param view
	 *            A {@link SnapshotView}
	 * @return A list of {@link Version}s
	 * @throws UCMException
 	 *
 *	public List<Version> afterBaselineDifferences( Baseline after, SnapshotView view ) throws UCMException {
 *		return context.baselineDifferences( this, after, true, view );
 *	}
 * 
 * @author wolfgang
 *
 */
public class Difference {
	
	private static final Pattern rx_baselineDiff = Pattern.compile( "^(\\S+)\\s*(.*?)\\s*(.*)\\s*$" );
	
	private static Logger logger = Logger.getLogger();
	
	private Baseline bl1;
	private Baseline bl2;
	
	public Difference( Baseline bl1, Baseline bl2 ) {
		this.bl1 = bl1;
		this.bl2 = bl2;
	}

	public void get( boolean merge, SnapshotView view ) {
		String cmd = "diffbl -version " + ( !merge ? "-nmerge " : "" ) + ( bl1 != null ? bl1.getFullyQualifiedName() : "-pre " ) + " " + bl2.getFullyQualifiedName();

		List<String> lines = null;

		try {
			lines = Cleartool.run( cmd, view.getViewRoot() ).stdoutList;
		} catch( Exception e ) {
			throw new UCMException( "Could not retreive the differences of " + bl1 + " and " + bl2 );
		}

		int length = view.getViewRoot().getAbsoluteFile().toString().length();
		List<Version> versions = new ArrayList<Version>();

		for( int i = 4; i < lines.size(); i++ ) {
			Matcher m = rx_baselineDiff.matcher( lines.get( i ) );
			if( m.find() ) {

				String f = m.group( 3 ).trim();
				logger.debug( "F: " + f );
				Version v = (Version) UCMEntity.getEntity( f );
				v.setSFile( v.getFile().getAbsolutePath().substring( length ) );

				if( m.group( 1 ).equals( ">>" ) ) {
					v.setStatus( Status.ADDED );
				} else if( m.group( 1 ).equals( "<<" ) ) {
					v.setStatus( Status.DELETED );
				} else {
					v.setStatus( Status.CHANGED );
				}

				v.load();
				versions.add( v );
			}
		}

		return versions;
	}
}
