package net.praqma.clearcase.ucm.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import net.praqma.clearcase.ucm.entities.Version;

public class VersionList extends ArrayList<Version> {

	private static Logger logger = Logger.getLogger( VersionList.class.getName() );
	
	public VersionList() {
		
	}
	
	public VersionList( List<Version> versions ) {
		this.addAll( versions );
	}
	
	public VersionList getLatest() {
		VersionList list = new VersionList();
		
		Map<File, Map<String, Version>> fmap = new HashMap<File, Map<String, Version>>();
		
		for( Version v : this ) {
			
			if( fmap.containsKey( v.getFile() ) ) {
				
				Map<String, Version> bmap = fmap.get( v.getFile() );
				if( bmap.containsKey( v.getBranch() ) ) {
					logger.fine( "Existing branch: " + v.getFile() + ", " + v.getBranch() + ", " + v.getRevision() );
					Version iv = bmap.get( v.getBranch() );
					if( iv.getRevision().intValue() < v.getRevision().intValue() ) {
						logger.fine( "Updating branch: " + v.getFile() + ", " + v.getBranch() + ", " + v.getRevision() );
						bmap.put( v.getBranch(), v );
						//fmap.put( v.getFile(), nbmap );
					}
				} else {
					logger.fine( "New branch: " + v.getFile() + ", " + v.getBranch() + ", " + v.getRevision() );
					bmap.put( v.getBranch(), v );
					//fmap.put( v.getFile(), nbmap );
				}

			} else {
				logger.fine( "New file: " + v.getFile() + ", " + v.getBranch() + ", " + v.getRevision() );
				Map<String, Version> nmap = new HashMap<String, Version>();
				nmap.put( v.getBranch(), v );
				fmap.put( v.getFile(), nmap );
			}
		}
		
		Set<File> keys = fmap.keySet();
		for( File file : keys ) {
			Set<String> bkeys = fmap.get( file ).keySet();
			for( String branch : bkeys ) {
				list.add( fmap.get( file ).get( branch ) );
			}
		}
		
		return list;
	}
}
