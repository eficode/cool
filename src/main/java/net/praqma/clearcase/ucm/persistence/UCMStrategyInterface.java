package net.praqma.clearcase.ucm.persistence;

import java.io.File;
import java.util.List;
import java.util.Map;

import net.praqma.clearcase.Region;
import net.praqma.clearcase.PVob;
import net.praqma.clearcase.Vob;
import net.praqma.clearcase.ucm.view.SnapshotView;
import net.praqma.clearcase.ucm.view.UCMView;
import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.entities.Activity;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.clearcase.ucm.entities.Project.Plevel;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.entities.UCMEntity;
import net.praqma.clearcase.ucm.entities.Version;
import net.praqma.util.structure.Tuple;

interface UCMStrategyInterface
{
	public static final String delim = "::";
	
	/* General command line stuff */
	public void checkViewContext( File dir ) throws UCMException;
	
	/* Common entity */
	public String getMastership( String fqname ) throws UCMException;
	
	/* Project */
	String getProjectFromStream( String stream );
	List<String> getModifiableComponents( String project );
	public String loadProject( String project ) throws UCMException;
	public List<Project> getProjects( PVob vob ) throws UCMException;
	public void createProject( String name, String root, PVob pvob, int policy, String comment, Component ... mcomps ) throws UCMException;
	
	/* Activity */
	public String loadActivity( String activity ) throws UCMException;
	public void createActivity( String name, PVob pvob, boolean force, String comment, File view ) throws UCMException;
	
	/* Baseline */
	public String loadBaseline( String baseline ) throws UCMException;
	public List<String> getBaselineDiff( File dir, String baseline, String other, boolean nmerge, String pvob ) throws UCMException;
	public void setPromotionLevel( String baseline, String plevel ) throws UCMException;
	public void createBaseline( String baseline, Component component, File view, boolean incremental, boolean identical, Activity[] activities, Component[] depends ) throws UCMException;
	public List<Version> baselineDifferences( Baseline bl1, Baseline bl2, boolean merge, SnapshotView view ) throws UCMException;
	
	public String getBaselineActivities( String baseline );
	
	public String deliver( String baseline, String stream, String target, File viewroot, String viewtag, boolean force, boolean complete, boolean abort ) throws UCMException;
	public void cancelDeliver( File viewcontext ) throws UCMException;
	public String deliverStatus( String stream ) throws UCMException;
	
	/* Component */
	public List<String> getBaselines( String component, String stream, Plevel plevel );
	public String getRootDir( String component );
	public String loadComponent( String component ) throws UCMException;
	public void createComponent( String name, PVob pvob, String root, String comment, File view ) throws UCMException;
	
	/* Hyper Links */
	public String loadHyperLink( String fqname, File dir ) throws UCMException;
	public List<Tuple<String, String>> getHlinks( String fqname, String hlinkType, File dir ) throws UCMException;
	
	/* Stream */

	public void recommendBaseline( String stream, String baseline ) throws UCMException;
	public String getRecommendedBaselines( String stream ) throws UCMException;
	public String getStreamFromView( String viewtag ) throws UCMException;
	public void createStream( String pstream, String nstream, boolean readonly, String baseline ) throws UCMException;
	public void createIntegrationStream( String name, Project project, Baseline baseline ) throws UCMException;
	public boolean streamExists( String fqname );
	public boolean rebaseStream( String viewtag, String stream, String baseline, boolean complete );
	public boolean isRebasing( String stream );
	public void cancelRebase( String stream );
	public List<String> getLatestBaselines( String stream );
	public void generate( String stream );
	public String loadStream( String stream ) throws UCMException;
	public List<Stream> getChildStreams(String fqstream) throws UCMException;
	
	/* Version */
	public void loadVersion( Version version ) throws UCMException;
	public String getVersion( String version, String separator );
	public String getVersionExtension( File file, File viewroot ) throws UCMException;
	public void addToSourceControl( File file, boolean mkdir, File view ) throws UCMException;
	public void checkOut( File file, File viewContext ) throws UCMException;
	public void checkIn( File file, boolean identical, File viewContext ) throws UCMException;
	public void removeVersion( File file, File viewContext ) throws UCMException;
	public List<File> getUnchecedInFiles( File viewContext ) throws UCMException;
	public void uncheckout( File file, boolean keep, File viewContext ) throws UCMException;
	public void removeName( File file, File viewContext ) throws UCMException;
	public void moveFile( File file, File destination, File viewContext ) throws UCMException;
	public boolean isUnderSourceControl( File element, File viewContext ) throws UCMException;
	public boolean isCheckedout( File element, File viewContext ) throws UCMException;
	
	/* Tag */
	public List<String[]> getTags( String fqname ) throws UCMException;
	public String getTag( String fqname );
	public String newTag( UCMEntity entity, String cgi ) throws UCMException;
	public void deleteTag( String fqname );
	public void deleteTagsWithID( String tagType, String tagID, String entity ) throws UCMException;
	
	public String putTag( String fqname, String keyval, UCMEntity entity );
	
	/* Snapshot Views */
	public void makeSnapshotView( String stream, File viewroot, String viewtag ) throws UCMException;
	public File getCurrentViewRoot( File viewroot ) throws UCMException;
	public String viewrootIsValid( File viewroot ) throws UCMException;
	public boolean viewExists( String viewtag );
	public void regenerateViewDotDat( File dir, String viewtag ) throws UCMException;
	public Map<String, Integer> swipeView( File viewroot, boolean excludeRoot );
	public String viewUpdate( File viewroot, boolean overwrite, String loadrules );
	public void startView( UCMView view ) throws UCMException;
	
	/* Views */
	public void createView( String tag, String path, boolean snapshotView, Stream stream ) throws UCMException;
	public void removeView( UCMView view ) throws UCMException;
	public Map<String, String> loadView( UCMView view ) throws UCMException;
	
	/* Vobs */
	public void createVob( String vobname, boolean UCMProject, String path, String comment ) throws UCMException;
	public void loadVob( Vob vob ) throws UCMException;
	public void mountVob( Vob vob ) throws UCMException;
	public List<Vob> getVobs( Region region );
	public void removeVob( Vob vob ) throws UCMException;
	public void unmountVob( Vob vob ) throws UCMException;
	public List<Vob> getVobs( boolean pvobs ) throws UCMException;

	/* Attributes */
	public String getAttribute( String fqname, String attribute ) throws UCMException;
	public Map<String, String> getAttributes( String fqname ) throws UCMException;
	public Map<String, String> getAttributes( String fqname, File dir ) throws UCMException;
	public void setAttribute( String fqname, String attribute, String value ) throws UCMException;
	
	public List<UCMView> getViews( Region region );
	
	
	
	/* 
	 * OTHER STUFF
	 * TODO Should be phased out
	 */
	public String getXML();
	public void saveState();
}