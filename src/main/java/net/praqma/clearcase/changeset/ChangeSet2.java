package net.praqma.clearcase.changeset;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import net.praqma.clearcase.Cool;
import net.praqma.clearcase.interfaces.Diffable;
import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.entities.Version;
import net.praqma.clearcase.ucm.entities.Version.Status;

public class ChangeSet2 extends Cool {

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
		if( elements.containsKey( file ) ) {
			ChangeSetElement2 element = elements.get( file );
			switch( status ) {
			case DELETED:
					elements.remove( file );
					break;
			case ADDED:
				/* If the version status was changed, let it be added */
				if( element.getStatus().equals( Status.CHANGED ) ) {
					elements.put( file, new ChangeSetElement2( file, status, origin ) );		
				}
			}
		} else {
			elements.put( file, new ChangeSetElement2( file, status, origin ) );
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
				} catch (UCMException e) {
					sb.append( "  (unknown)" );
				}
				
				sb.append( " " + v.getStatus() + "\n" );
			}
			
		}
		
		sb.append( "\nListing elements:\n" );
		
		Set<File> elements = this.elements.keySet();
		for( File file : elements ) {
			sb.append( file.getAbsolutePath().toLowerCase().substring( length ) + ": " + this.elements.get( file ).getStatus() );
			if(  this.elements.get( file ).getOldFile() != null ) {
				sb.append( "(<==" + this.elements.get( file ).getOldFile() + ")" );
			}
			sb.append( linesep );
		}
		
		return sb.toString();
	}
	
	public Map<File, List<Version>> getChangeset() {
		return changesetVersions;
	}
	
	public Map<File, ChangeSetElement2> getElements()  {
		return elements;
	}
	
	public List<ChangeSetElement2> getElementsAsList()  {
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
								String oldver = context.getPreviousVersion( elements.get( file1 ).getOrigin().getFullyQualifiedName(), null );
								String oldoid = context.getObjectId( elements.get( file1 ).getOrigin().getFile().getAbsolutePath() + "@@" + oldver + filesep + file1.getName() + "@@", null );
								
								String newoid = context.getObjectId( elements.get( file2 ).getOrigin().getFullyQualifiedName() + filesep + file2.getName() + "@@", null );
								
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
						} catch (UCMException e) {
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
	
	public static ChangeSet2 getChangeSet( Diffable d1, Diffable d2, File viewContext ) throws UCMException {
		ChangeSet2 changeset = context.getChangeset( d1, d2, true, viewContext );
		
		/* Sorting the version */
		Set<File> files = changeset.getChangeset().keySet();
		for( File file : files ) {
			Collections.sort( changeset.getChangeset().get( file ) );
			
			for( Version v : changeset.getChangeset().get( file ) ) {
				if( v.isDirectory() ) {
					context.getDirectoryStatus( v, changeset );
				}
			}
		}
		
		changeset.checkOverlap();
		
		return changeset;
	}
}
