package net.praqma.clearcase.ucm.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import edu.umd.cs.findbugs.annotations.*;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.ucm.entities.Activity;
import net.praqma.clearcase.ucm.entities.Version;

@SuppressFBWarnings("")
public class VersionList extends ArrayList<Version> {

    private static final Logger logger = Logger.getLogger( VersionList.class.getName() );
    private List<VersionFilter> filters = new ArrayList<VersionFilter>();
    private String branchName;
    private File path;
    private List<Activity> activities = new ArrayList<Activity>();

    public VersionList() { }

    public VersionList( List<Version> versions ) {
        this.addAll( versions );
    }
    
    public VersionList( List<Version> versions, List<Activity> acts ) {
        activities.addAll(acts);
        this.addAll( versions );
    }
    
    public VersionList addActivities( List<Activity> acts ) {
        if(this.activities.addAll(acts)) {
            logger.fine("Succesfully added all activities!");
        }

        for( Activity a : acts ) {
            this.addAll( a.changeset.versions );
        }

        return this;
    }

    public VersionList addActivity( Activity activity ) throws ClearCaseException {
        this.activities.add(activity);
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
        for(Activity a : getActivities()) {
            changeSet.put(a, new ArrayList<Version>());            
        }

        for( Version v : map.values() ) {
            if(changeSet.containsKey(v.getActivity())) {
                changeSet.get( v.getActivity() ).add( v );
            }
        }
        
        /**
         * Remove activities with no changes
         */
        Iterator<Activity> acts = changeSet.keySet().iterator();
        while(acts.hasNext()) {
            if(changeSet.get(acts.next()).isEmpty()) {
                acts.remove();
            }
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
					Version iv = bmap.get( v.getBranch() );
					if( iv.getRevision()< v.getRevision()) {
						bmap.put( v.getBranch(), v );
					}
				} else {
					bmap.put( v.getBranch(), v );
				}

			} else {
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
    
    public VersionList apply() {
        
        for(VersionFilter f : filters) {
            f.filter(this);
        }
        
        return this;
    }

    private static void getLatestChanges( List<Version> versions, String branchName, Map<File, Version> map ) {
        for( Version v : versions ) {
            if( branchName == null || v.getBranch().matches( branchName ) ) {
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
    
    public VersionList addFilter(VersionFilter filter) {
        filters.add(filter);
        return this;
    }

    /**
     * @return the activities
     */
    public List<Activity> getActivities() {
        return activities;
    }

    /**
     * @param activities the activities to set
     */
    public void setActivities(List<Activity> activities) {
        this.activities = activities;
    }
}
