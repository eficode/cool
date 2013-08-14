package net.praqma.clearcase.ucm.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.exceptions.UnableToInitializeEntityException;
import net.praqma.clearcase.ucm.entities.Activity;
import net.praqma.clearcase.ucm.entities.Version;

public class VersionList extends ArrayList<Version> {

	private static Logger logger = Logger.getLogger( VersionList.class.getName() );

    private String branchName;
    private File path;

    public VersionList() {

    }

    public VersionList( List<Version> versions ) {
        this.addAll( versions );
    }

    public VersionList addActivities( List<Activity> activities ) {
        System.out.println( "Adding " + activities.size() + " activities" );
        for( Activity activity : activities ) {
            System.out.println( "Adding " + activity.changeset.versions.size() + " versions" );
            this.addAll( activity.changeset.versions );
        }

        return this;
    }

    public VersionList addActivity( Activity activity ) throws ClearCaseException {
        this.addAll( activity.getVersions( path ) );
        return this;
    }

    public VersionList setBranchName( String branchName ) {
        this.branchName = branchName;
        return this;
    }

    public VersionList setPath( File path ) {
        this.path = path;
        return this;
    }

    public Map<Activity, List<Version>> getLatestForActivities() {

        /* Compile the latest versions */
        Map<File, Version> map = new HashMap<File, Version>();
        getLatestChanges( this, branchName, map );

        /* Group the versions into activities */
        Map<Activity, List<Version>> changeSet = new HashMap<Activity, List<Version>>();

        for( Version v : map.values() ) {
            /* Put in a new activity if doesn't exist */
            if( !changeSet.containsKey( v.getActivity() ) ) {
                changeSet.put( v.getActivity(), new ArrayList<Version>(  ) );
            }

            changeSet.get( v.getActivity() ).add( v );
        }

        return changeSet;
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

    public static List<Version> getLatestChanges( List<Activity> activities, String branchName ) {
        Map<File, Version> map = new HashMap<File, Version>();

        for( Activity a : activities ) {
            getLatestChanges( a.changeset.versions, branchName, map );
        }

        return new ArrayList<Version>( map.values() );
    }

    private static void getLatestChanges( List<Version> versions, String branchName, Map<File, Version> map ) {
        for( Version v : versions ) {
            if( branchName == null || ( branchName != null && branchName.equals( v.getBranch() ) ) ) {
                if( map.containsKey( v.getFile() ) ) {
                    if( v.getRevision() > map.get( v.getFile() ).getRevision() ) {
                        map.put( v.getFile(), v );
                    }
                } else {
                    map.put( v.getFile(), v );
                }
            }
        }
    }
}
