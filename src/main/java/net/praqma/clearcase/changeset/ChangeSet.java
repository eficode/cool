package net.praqma.clearcase.changeset;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ChangeSet {
	Map<File, Map<String, Map<Integer, ChangeSetElement>>> changes = new HashMap<File, Map<String, Map<Integer, ChangeSetElement>>>();
	
	private File viewContext;
	
	public ChangeSet() {
		
	}
	
	public ChangeSet( File viewContext ) {
		this.viewContext = viewContext;
	}
	
	public void addElement( ChangeSetElement element ) {
		System.out.println( "Adding: " + element );
		if( !changes.containsKey( element.getFile() ) ) {
			changes.put( element.getFile(), new HashMap<String, Map<Integer, ChangeSetElement>>() );
		}
		
		Map<String, Map<Integer, ChangeSetElement>> vfile = changes.get( element.getFile() );
		
		if( !vfile.containsKey( element.getVersion() ) ) {
			vfile.put( element.getVersion(), new HashMap<Integer, ChangeSetElement>() );
		}
		
		Map<Integer, ChangeSetElement> el = vfile.get( element.getVersion() );
		
		el.put( element.getRevision(), element );
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		int length = this.viewContext.getAbsoluteFile().toString().length();
		
		Set<File> files = changes.keySet();
		for( File file : files ) {
			sb.append( "--- " + file.getAbsolutePath().toLowerCase().substring( length ) + " ---\n" );
			
			Set<String> versions = changes.get( file ).keySet();
			
			for( String version : versions ) {
				sb.append( version + ": " );
				
				Set<Integer> revisions = changes.get( file ).get( version ).keySet();
				
				for( Integer revision : revisions ) {
					sb.append( revision + " " );
				}
				
				sb.append( "\n" );
			}
		}
		
		return sb.toString();
	}
}
