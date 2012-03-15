package net.praqma.clearcase.changeset;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.praqma.clearcase.Cool;
import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.exceptions.UnableToLoadEntityException;
import net.praqma.clearcase.interfaces.Diffable;
import net.praqma.clearcase.ucm.entities.Version;
import net.praqma.clearcase.ucm.entities.Version.Status;
import net.praqma.util.debug.Logger;
import net.praqma.util.execute.AbnormalProcessTerminationException;

public class ChangeSet2 extends Cool {
	
	private static final Pattern rx_diffAction = Pattern.compile( "^-{5}\\[\\s*(.+)\\s*\\]-{5}$" );
	private static final Pattern rx_diffFileName = Pattern.compile( "^..(.*)\\s+--\\d+.*$" );
	
	List<Version> changeset2 = new ArrayList<Version>();
	Map<File, List<Version>> changesetVersions = new HashMap<File, List<Version>>();
	Map<File, ChangeSetElement2> elements = new HashMap<File, ChangeSetElement2>();

	private static Logger logger = Logger.getLogger();

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
	
	public static void getDirectoryStatus( Version version, ChangeSet2 changeset ) throws ClearCaseException {

		String cmd = "diff -diff -pre \"" + version.getFullyQualifiedName() + "\"";

		// System.out.println( "$ " + cmd );

		try {
			List<String> lines = Cleartool.run( cmd, null, true, true ).stdoutList;

			for( int i = 0; i < lines.size(); ++i ) {
				// System.out.println( "[" + i + "] " + lines.get( i ) );
				Matcher m = rx_diffAction.matcher( lines.get( i ) );

				/* A diff action */
				if( m.find() ) {
					String action = m.group( 1 ).trim();

					/* ADDED action */
					if( action.equals( "added" ) ) {
						/* This is an add, the next line is the file added */
						Matcher mname = rx_diffFileName.matcher( lines.get( i + 1 ) );
						if( mname.find() ) {
							changeset.addElement( new File( version.getFile(), mname.group( 1 ).trim() ), Version.Status.ADDED, version );
						} else {
							logger.warning( "Unknown filename line: " + lines.get( i + 1 ) );
						}

						/* Fast forward one line */
						i++;
						/* ADDED action */
					} else if( action.equals( "deleted" ) ) {
						/* This is an add, the next line is the file added */
						Matcher mname = rx_diffFileName.matcher( lines.get( i + 1 ) );
						if( mname.find() ) {
							changeset.addElement( new File( version.getFile(), mname.group( 1 ).trim() ), Version.Status.DELETED, version );
						} else {
							logger.warning( "Unknown filename line: " + lines.get( i + 1 ) );
						}

						/* Fast forward one line */
						i++;

					} else if( action.equals( "renamed to" ) ) {
						/* This is a rename, the next line is the file added */
						Matcher oldname = rx_diffFileName.matcher( lines.get( i + 1 ) );
						Matcher newname = rx_diffFileName.matcher( lines.get( i + 3 ) );

						File newFile = null;
						File oldFile = null;

						if( newname.find() ) {
							newFile = new File( version.getFile(), newname.group( 1 ).trim() );
						} else {
							logger.warning( "Unknown filename line: " + lines.get( i + 1 ) );
						}

						if( oldname.find() ) {
							oldFile = new File( version.getFile(), oldname.group( 1 ).trim() );
						} else {
							logger.warning( "Unknown filename line: " + lines.get( i + 1 ) );
						}

						// changeset.addElement( newFile,
						// Version.Status.CHANGED, version );

						logger.debug( "[" + oldFile + "]" );
						logger.debug( "[" + newFile + "]" );
						ChangeSetElement2 element = new ChangeSetElement2( newFile, Version.Status.CHANGED, version );
						element.setOldFile( oldFile );
						changeset.addElement( element );

						/* Fast forward four line */
						i += 4;

					} else {
						/* I don't know this action, let's move on */
						logger.warning( "Unhandled diff action: " + action );
					}
				}
			}
		} catch( AbnormalProcessTerminationException e ) {
			throw new CleartoolException( "Could not execute the command: " + e.getCommand(), e );
		} catch( IndexOutOfBoundsException e1 ) {
			throw new ClearCaseException( "Out of bounds: " + e1.getMessage(), e1 );
		} catch( Exception e2 ) {
			throw new ClearCaseException( "Something new, something unhandled: " + e2.getMessage(), e2 );
		}
	}

	public static ChangeSet2 getChangeSet( Diffable d1, Diffable d2, File viewContext ) throws ClearCaseException {
		ChangeSet2 changeset = Version.getChangeset( d1, d2, true, viewContext );

		/* Sorting the version */
		Set<File> files = changeset.getChangeset().keySet();
		for( File file : files ) {
			Collections.sort( changeset.getChangeset().get( file ) );

			for( Version v : changeset.getChangeset().get( file ) ) {
				if( v.isDirectory() ) {
					getDirectoryStatus( v, changeset );
				}
			}
		}

		changeset.checkOverlap();

		return changeset;
	}
}
