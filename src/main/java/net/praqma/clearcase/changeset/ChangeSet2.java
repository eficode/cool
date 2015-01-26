package net.praqma.clearcase.changeset;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import net.praqma.clearcase.Cool;
import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.exceptions.UnableToLoadEntityException;
import net.praqma.clearcase.ucm.entities.Version;
import net.praqma.clearcase.ucm.entities.Version.Status;
import net.praqma.util.execute.AbnormalProcessTerminationException;

public class ChangeSet2 extends Cool {
	
	private static final Pattern rx_diffAction = Pattern.compile( "^-{5}\\[\\s*(.+)\\s*\\]-{5}$" );
	private static final Pattern rx_diffFileName = Pattern.compile( "^..(.*)\\s+--\\d+.*$" );
	
	List<Version> changeset2 = new ArrayList<Version>();
	Map<File, List<Version>> changesetVersions = new HashMap<File, List<Version>>();
	Map<File, ChangeSetElement2> elements = new HashMap<File, ChangeSetElement2>();

	private File viewContext;

	public ChangeSet2() {

	}

	public ChangeSet2( File viewContext ) {
		this.viewContext = viewContext;
	}

	public void addVersion( Version version ) {

		/* Add to versions */
		if( !changesetVersions.containsKey( version.getFile() ) ) {
			changesetVersions.put( version.getFile(), new ArrayList<Version>() );
		}

		List<Version> versions = changesetVersions.get( version.getFile() );
		versions.add( version );

		/* Add to elements */
		addElement( version.getFile(), version.getStatus(), version );
	}

	public void addElement( File file, Status status, Version origin ) {
		addElement( new ChangeSetElement2( file, status, origin ) );
	}

	public void addElement( ChangeSetElement2 e ) {
		if( elements.containsKey( e.getFile() ) ) {
			ChangeSetElement2 element = elements.get( e.getFile() );
			switch ( e.getStatus() ) {
			case DELETED:
				elements.remove( e.getFile() );
				break;
			case ADDED:
				/* If the version status was changed, let it be added */
				if( element.getStatus().equals( Status.CHANGED ) ) {
					elements.put( e.getFile(), e );
				}
			}
		} else {
			elements.put( e.getFile(), e );
		}
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();

		int length = this.viewContext.getAbsoluteFile().toString().length();

		Set<File> files = changesetVersions.keySet();
		for( File file : files ) {
			sb.append( file.getAbsolutePath().toLowerCase().substring( length ) + ":\n" );
			Collections.sort( changesetVersions.get( file ) );
			for( Version v : changesetVersions.get( file ) ) {
				try {
					sb.append( "  " + v.getVersion() );
				} catch( UnableToLoadEntityException e ) {
					sb.append( "  (unknown)" );
				}

				sb.append( " " + v.getStatus() + "\n" );
			}

		}

		sb.append( "\nListing elements:\n" );

		Set<File> elements = this.elements.keySet();
		for( File file : elements ) {
			sb.append( file.getAbsolutePath().toLowerCase().substring( length ) + ": " + this.elements.get( file ).getStatus() );
			if( this.elements.get( file ).getOldFile() != null ) {
				sb.append( "(<==" + this.elements.get( file ).getOldFile() + ")" );
			}
			sb.append( linesep );
		}

		return sb.toString();
	}

	public Map<File, List<Version>> getChangeset() {
		return changesetVersions;
	}

	public Map<File, ChangeSetElement2> getElements() {
		return elements;
	}

	public List<ChangeSetElement2> getElementsAsList() {
		Set<File> files = elements.keySet();
		List<ChangeSetElement2> list = new ArrayList<ChangeSetElement2>();
		for( File file : files ) {
			list.add( elements.get( file ) );
		}

		return list;
	}

	public void checkOverlap() {
		Set<File> files = elements.keySet();
		List<File> deletes = new ArrayList<File>();

		for( File file1 : files ) {
			for( File file2 : files ) {
				if( !file1.equals( file2 ) ) {
					/* Naming overlap */
					if( file1.getName().equals( file2.getName() ) ) {
						/* Check if one of the is deleted */
						try {
							/* file1 is deleted */
							if( elements.get( file1 ).getStatus().equals( Status.DELETED ) ) {
								String oldver = getPreviousVersion( elements.get( file1 ).getOrigin().getFullyQualifiedName(), null );
								String oldoid = getObjectId( elements.get( file1 ).getOrigin().getFile().getAbsolutePath() + "@@" + oldver + filesep + file1.getName() + "@@", null );

								String newoid = getObjectId( elements.get( file2 ).getOrigin().getFullyQualifiedName() + filesep + file2.getName() + "@@", null );

								//System.out.println( "file1: " + oldver + " + " + oldoid );
								//System.out.println( "file1: " + newoid );

								if( oldoid.equals( newoid ) ) {
									//System.out.println( "Object id overlap" );
									if( elements.get( file2 ).getStatus().equals( Status.ADDED ) ) {
										//System.out.println( "Potential move of dir" );

										elements.get( file2 ).setStatus( Status.CHANGED );
										elements.get( file2 ).setOldFile( file1 );
										//elements.remove( file1 );
										deletes.add( file1 );
									}
								}
								/* None of them were deleted */
							} else {
								continue;
							}
						} catch( CleartoolException e ) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}

		for( File file : deletes ) {
			elements.remove( file );
		}
	}

	public String getPreviousVersion( String version, File viewContext ) throws CleartoolException {
		String cmd = "describe -fmt %PVn " + version;

		try {
			return Cleartool.run( cmd, viewContext ).stdoutBuffer.toString();
		} catch( AbnormalProcessTerminationException e ) {
			throw new CleartoolException( "Could not get previous version: " + e.getMessage(), e );
		}
	}

	public String getObjectId( String fqname, File viewContext ) throws CleartoolException {
		String cmd = "describe -fmt %On " + fqname;

		try {
			return Cleartool.run( cmd, viewContext ).stdoutBuffer.toString();
		} catch( AbnormalProcessTerminationException e ) {
			throw new CleartoolException( "Could not get object id: " + e.getMessage(), e );
		}
	}
}
