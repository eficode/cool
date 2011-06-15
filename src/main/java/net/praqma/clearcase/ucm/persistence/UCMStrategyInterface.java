package net.praqma.clearcase.ucm.persistence;

import java.io.File;
import java.util.List;
import java.util.Map;

import net.praqma.clearcase.Region;
import net.praqma.clearcase.View;
import net.praqma.clearcase.Vob;
import net.praqma.clearcase.ucm.entities.*;
import net.praqma.clearcase.ucm.view.UCMView;
import net.praqma.clearcase.ucm.UCMException;
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
	
	/* Activity */
	public String loadActivity( String activity ) throws UCMException;
	
	/* Baseline */
	public String loadBaseline( String baseline ) throws UCMException;
	public List<String> getBaselineDiff( File dir, String baseline, String other, boolean nmerge, String pvob ) throws UCMException;
	public void setPromotionLevel( String baseline, String plevel ) throws UCMException;
	public void createBaseline( String baseline, String component, File view, boolean incremental, boolean identical ) throws UCMException;
	
	public String getBaselineActivities( String baseline );
	
	public String deliver( String baseline, String stream, String target, File viewroot, String viewtag, boolean force, boolean complete, boolean abort ) throws UCMException;
	public void cancelDeliver( File viewcontext ) throws UCMException;
	public String deliverStatus( String stream ) throws UCMException;
	
	/* Component */
	public List<String> getBaselines( String component, String stream, String plevel );
	public String getRootDir( String component );
	public String loadComponent( String component ) throws UCMException;
	
	/* Hyper Links */
	public String loadHyperLink( String fqname, File dir ) throws UCMException;
	public List<Tuple<String, String>> getHlinks( String fqname, String hlinkType, File dir ) throws UCMException;
	
	/* Stream */
	public void recommendBaseline( String stream, String baseline ) throws UCMException;
	public String getRecommendedBaselines( String stream ) throws UCMException;
	public String getStreamFromView( String viewtag ) throws UCMException;
	public void createStream( String pstream, String nstream, boolean readonly, String baseline );
	public boolean streamExists( String fqname );
	public boolean rebaseStream( String viewtag, String stream, String baseline, boolean complete );
	public boolean isRebasing( String stream );
	public void cancelRebase( String stream );
	public List<String> getLatestBaselines( String stream );
	public void generate( String stream );
	public String loadStream( String stream ) throws UCMException;
	
	/* Version */
	public String getVersion( String version, String separator );
	public String getVersionExtension( String file, File viewroot ) throws UCMException;
	
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

	/* Attributes */
	public String getAttribute( String fqname, String attribute ) throws UCMException;
	public Map<String, String> getAttributes( String fqname ) throws UCMException;
	public Map<String, String> getAttributes( String fqname, File dir ) throws UCMException;
	public void setAttribute( String fqname, String attribute, String value ) throws UCMException;
	
	public List<UCMView> getViews( Region region );
	
	public List<Vob> getVobs( Region region );
	
	/* 
	 * OTHER STUFF
	 * TODO Should be phased out
	 */
	public String getXML();
	public void saveState();
}