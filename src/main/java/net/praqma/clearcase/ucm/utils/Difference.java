package net.praqma.clearcase.ucm.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.exceptions.UnableToInitializeEntityException;
import net.praqma.clearcase.exceptions.UnableToLoadEntityException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Version;
import net.praqma.clearcase.ucm.entities.Version.Status;
import net.praqma.clearcase.ucm.view.SnapshotView;

public class Difference {
	
	private static final Pattern rx_baselineDiff = Pattern.compile( "^(\\S+)\\s*(.*?)\\s*(.*)\\s*$" );
	
	private Baseline bl1;
	private Baseline bl2;
	
	public Difference( Baseline bl1, Baseline bl2 ) {
		this.bl1 = bl1;
		this.bl2 = bl2;
	}

	public List<Version> get( boolean merge, SnapshotView view ) throws CleartoolException, UnableToInitializeEntityException, UnableToLoadEntityException {
		String cmd = "diffbl -version " + ( !merge ? "-nmerge " : "" ) + ( bl1 != null ? bl1.getFullyQualifiedName() : "-pre " ) + " " + bl2.getFullyQualifiedName();

		List<String> lines = null;

		try {
			lines = Cleartool.run( cmd, view.getViewRoot() ).stdoutList;
		} catch( Exception e ) {
			throw new CleartoolException( "Could not retreive the differences of " + bl1 + " and " + bl2 );
		}

		int length = view.getViewRoot().getAbsoluteFile().toString().length();
		List<Version> versions = new ArrayList<Version>();

		for( int i = 4; i < lines.size(); i++ ) {
			Matcher m = rx_baselineDiff.matcher( lines.get( i ) );
			if( m.find() ) {

				String f = m.group( 3 ).trim();
				Version v = (Version) Version.get( f );
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
