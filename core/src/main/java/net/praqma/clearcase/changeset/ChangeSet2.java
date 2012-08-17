package net.praqma.clearcase.changeset;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
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
import net.praqma.logging.Config;
import net.praqma.util.debug.Logger;
import net.praqma.util.execute.AbnormalProcessTerminationException;

public class ChangeSet2 extends Cool {

	private static final Pattern rx_diffAction = Pattern.compile( "^-{5}\\[\\s*(.+)\\s*\\]-{5}$" );
	private static final Pattern rx_diffFileName = Pattern.compile( "^..(.*)\\s+--\\d+.*$" );

	List<Version> changeset2 = new ArrayList<Version>();
	Map<File, List<Version>> changesetVersions = new HashMap<File, List<Version>>();
	Map<File, ChangeSetElement2> elements = new HashMap<File, ChangeSetElement2>();

	private static Logger logger = Logger.getLogger();
	private static java.util.logging.Logger tracer = java.util.logging.Logger.getLogger(Config.GLOBAL_LOGGER_NAME);

	private File viewContext;

	public ChangeSet2() {

	}

	public ChangeSet2( File viewContext ) {
		tracer.finest("Constructor called for class ChangeSet2(File viewContext)");
		tracer.finest(String.format("Input parameter viewContext type: %s; value: %s", viewContext.getClass(), viewContext));

		this.viewContext = viewContext;

		tracer.finest("Ending execution of constructor - ChangeSet2(File viewContext)");
	}

	public void addVersion( Version version ) {
		tracer.finest("Starting execution of method - addVersion( Version version )");
		tracer.finest(String.format("Input parameter version type: %s; value: %s", version.getClass(), version));

		/* Add to versions */
		if( !changesetVersions.containsKey( version.getFile() ) ) {
			tracer.finest(String.format("Changeset versions doesn't contain version file: %s", version.getFile()));
			tracer.finest(String.format("Adding version file %s to the changeset versions", version.getFile()));

			changesetVersions.put( version.getFile(), new ArrayList<Version>() );
		}


		List<Version> versions = changesetVersions.get( version.getFile() );
		versions.add( version );

		tracer.finest("Adding version to elements.");
		/* Add to elements */
		addElement( version.getFile(), version.getStatus(), version );

		tracer.finest("Ending execution of method - addVersion( Version version )");
	}

	public void addElement( File file, Status status, Version origin ) {
		tracer.finest("Starting execution of method - addElement( File file, Status status, Version origin )");
		tracer.finest(String.format("Input parameter file type: %s; value: %s", file.getClass(), file));
		tracer.finest(String.format("Input parameter status type: %s; value: %s", status.getClass(), status));
		tracer.finest(String.format("Input parameter origin type: %s; value: %s", origin.getClass(), origin));

		addElement( new ChangeSetElement2( file, status, origin ) );

		tracer.finest("Ending execution of method - addElement( File file, Status status, Version origin )");
	}

	public void addElement( ChangeSetElement2 e ) {
		tracer.finest("Starting execution of method - addElement(ChangeSetElement2 e)");
		tracer.finest(String.format("Input parameter e type: %s; value: %s", e.getClass(), e));

		if( elements.containsKey( e.getFile() ) ) {
			tracer.finest("Element is already presant.");

			ChangeSetElement2 element = elements.get( e.getFile() );

			switch ( e.getStatus() ) {
			case DELETED:
				tracer.finest("Version is already presant, it will be deleted.");

				elements.remove( e.getFile() );
				break;
			case ADDED:
				tracer.finest("Version status is changed, it will be added.");

				/* If the version status was changed, let it be added */
				if( element.getStatus().equals( Status.CHANGED ) ) {
					elements.put( e.getFile(), e );
				}
			}
		} else {
			tracer.finest("Element isn't presant, it will be added.");
			
			elements.put( e.getFile(), e );
		}

		tracer.finest("Ending execution of method - addElement(ChangeSetElement2 e) ");
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();

		int length = this.viewContext.getAbsoluteFile().toString().length();

		Set<File> files = changesetVersions.keySet();
		for( File file : files ) {
			sb.append( file.getAbsolutePath().toLowerCase().substring( length ) + ":" + System.getProperty("line.separator") );
			Collections.sort( changesetVersions.get( file ) );
			for( Version v : changesetVersions.get( file ) ) {
				try {
					sb.append( "  " + v.getVersion() );
				} catch( UnableToLoadEntityException e ) {
					sb.append( "  (unknown)" );
				}

				sb.append( " " + v.getStatus() + System.getProperty("line.separator") );
			}

		}

		sb.append( System.getProperty("line.separator") + "Listing elements:" + System.getProperty("line.separator") );

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
		tracer.finest("Starting execution of method - getChangeset()");
		tracer.finest("Returning Map<File, List<Version>> with values:");
		for (Entry<File, List<Version>> entry : changesetVersions.entrySet()) {
			tracer.finest(String.format("    StatusCategory: %s, Number: %s", entry.getKey(), entry.getValue()));
		}
		
		return changesetVersions;
	}

	public Map<File, ChangeSetElement2> getElements() {
		tracer.finest("Starting execution of method - getElements()");
		tracer.finest("Returning Map<File, ChangeSetElement2> with values:");
		for (Entry<File, ChangeSetElement2> entry : elements.entrySet()) {
			tracer.finest(String.format("    StatusCategory: %s, Number: %s", entry.getKey(), entry.getValue()));
		}
		
		return elements;
	}

	public List<ChangeSetElement2> getElementsAsList() {
		tracer.finest("Starting execution of method - getElementsAsList()");
		
		Set<File> files = elements.keySet();
		List<ChangeSetElement2> list = new ArrayList<ChangeSetElement2>();
		for( File file : files ) {
			list.add( elements.get( file ) );
		}
		
		tracer.finest("Returning List<ChangeSetElement2> with values:");
		for (ChangeSetElement2 element : list) {
			tracer.finest(String.format("    %s", element));
		}

		return list;
	}

	public void checkOverlap() throws CleartoolException {
		tracer.finest("Starting execution of method - checkOverlap()");
		
		Set<File> files = elements.keySet();
		List<File> deletes = new ArrayList<File>();

		tracer.finest("Checking for file name overlaps...");
		for( File file1 : files ) {
			for( File file2 : files ) {
				if( !file1.equals( file2 ) ) {
					tracer.finest("Checking for overlaping file names...");
					/* Naming overlap */
					if( file1.getName().equals( file2.getName() ) ) {
						tracer.finest("Overlaping file names:");
						tracer.finest(String.format("File #1: %s", file1.getName()));
						tracer.finest(String.format("File #2: %s", file2.getName()));
	
						tracer.finest("checking if one of the files are deleted.");
						/* Check if one of the is deleted */
						try {
							/* file1 is deleted */
							if( elements.get( file1 ).getStatus().equals( Status.DELETED ) ) {
								tracer.finest(String.format("File #1 %s is deleted: ", file1.getName()));
								
								String oldver = getPreviousVersion( elements.get( file1 ).getOrigin().getFullyQualifiedName(), null );
								String oldoid = getObjectId( elements.get( file1 ).getOrigin().getFile().getAbsolutePath() + "@@" + oldver + filesep + file1.getName() + "@@", null );

								String newoid = getObjectId( elements.get( file2 ).getOrigin().getFullyQualifiedName() + filesep + file2.getName() + "@@", null );

								//System.out.println( "file1: " + oldver + " + " + oldoid );
								//System.out.println( "file1: " + newoid );

								if( oldoid.equals( newoid ) ) {
									tracer.finest("Object id overlap.");

									if( elements.get( file2 ).getStatus().equals( Status.ADDED ) ) {
										//System.out.println( "Potential move of dir" );

										elements.get( file2 ).setStatus( Status.CHANGED );
										elements.get( file2 ).setOldFile( file1 );
										
										tracer.finest(String.format("File #1 %s is marked for deletion", file1.getName()));
										//elements.remove( file1 );
										deletes.add( file1 );
									}
								}
								/* None of them were deleted */
							} else {
								tracer.finest("No files deleted.");
								continue;
							}
						} catch( CleartoolException e ) {
							tracer.severe(String.format("Exception thrown type: %s; message: %s", e.getClass(), e.getMessage()));
							
							throw e;
						}
					}
				}
			}
		}

		for( File file : deletes ) {
			elements.remove( file );
		}
		
		tracer.finest("Ending execution of method - checkOverlap()");
	}

	public String getPreviousVersion( String version, File viewContext ) throws CleartoolException {
		tracer.finest("Starting execution of method - getPreviousVersion( String version, File viewContext )");
		tracer.finest(String.format("Input parameter version type: %s; value: %s", version.getClass(), version));
		tracer.finest(String.format("Input parameter viewContext type: %s; value: %s", viewContext.getClass(), viewContext));
		
		String cmd = "describe -fmt %PVn " + version;

		try {
			return Cleartool.run( cmd, viewContext ).stdoutBuffer.toString();
		} catch( AbnormalProcessTerminationException e ) {
			CleartoolException exception = new CleartoolException( "Could not get previous version: " + e.getMessage(), e );
			
			tracer.severe(String.format("Exception thrown type: %s; message: %s", exception.getClass(), exception.getMessage()));
			
			throw exception;
		}
	}

	public String getObjectId( String fqname, File viewContext ) throws CleartoolException {
		tracer.finest("Starting execution of method - getObjectId( String fqname, File viewContext )");
		tracer.finest(String.format("Input parameter fqname type: %s; value: %s", fqname.getClass(), fqname));
		tracer.finest(String.format("Input parameter viewContext type: %s; value: %s", viewContext.getClass(), viewContext));
		
		String cmd = "describe -fmt %On " + fqname;

		try {
			return Cleartool.run( cmd, viewContext ).stdoutBuffer.toString();
		} catch( AbnormalProcessTerminationException e ) {
			CleartoolException exception = new CleartoolException( "Could not get object id: " + e.getMessage(), e );
			
			tracer.severe(String.format("Exception thrown type: %s; message: %s", exception.getClass(), exception.getMessage()));
			
			throw exception;
		}
	}

	public static void getDirectoryStatus( Version version, ChangeSet2 changeset ) throws ClearCaseException {
		tracer.finest("Starting execution of method - getObjectId( String fqname, File viewContext )");
		tracer.finest(String.format("Input parameter version type: %s; value: %s", version.getClass(), version));
		tracer.finest(String.format("Input parameter changeset type: %s; value: %s", changeset.getClass(), changeset));
		
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
			CleartoolException exception = new CleartoolException( "Could not execute the command: " + e.getCommand(), e );
			
			tracer.severe(String.format("Exception thrown type: %s; message: %s", exception.getClass(), exception.getMessage()));
			
			throw exception;
		} catch( IndexOutOfBoundsException e1 ) {
			ClearCaseException exception = new ClearCaseException( "Out of bounds: " + e1.getMessage(), e1 );
			
			tracer.severe(String.format("Exception thrown type: %s; message: %s", exception.getClass(), exception.getMessage()));
			
			throw exception;
		} catch( Exception e2 ) {
			ClearCaseException exception = new ClearCaseException( "Something new, something unhandled: " + e2.getMessage(), e2 );
			
			tracer.severe(String.format("Exception thrown type: %s; message: %s", exception.getClass(), exception.getMessage()));
			
			throw exception;
		}
	}

	public static ChangeSet2 getChangeSet( Diffable d1, Diffable d2, File viewContext ) throws ClearCaseException {
		tracer.entering(ChangeSet2.class.getSimpleName(), "getChangeSet", new Object[]{d1, d2, viewContext});

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

		tracer.exiting(ChangeSet2.class.getSimpleName(), "getChangeSet", new Object[]{d1, d2, viewContext});
		
		return changeset;
	}
}
