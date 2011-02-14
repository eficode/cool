package net.praqma.clearcase.ucm.persistence;

import java.io.File;
import java.util.List;
import java.util.Map;

import net.praqma.clearcase.ucm.entities.*;
import net.praqma.clearcase.ucm.UCMException;
import net.praqma.util.structure.Tuple;

interface UCMStrategyInterface
{
	public static final String delim = "::";
	
	/* General command line stuff */
	public void CheckViewContext( File dir ) throws UCMException;
	
	/* Common entity */
	public String getMastership( String fqname ) throws UCMException;
	
	/* Project */
	String GetProjectFromStream( String stream );
	List<String> GetModifiableComponents( String project );
	public String LoadProject( String project ) throws UCMException;
	
	/* Activity */
	public String LoadActivity( String activity ) throws UCMException;
	
	/* Baseline */
	public String LoadBaseline( String baseline ) throws UCMException;
	public List<String> GetBaselineDiff( File dir, String baseline, String other, boolean nmerge, String pvob ) throws UCMException;
	public void SetPromotionLevel( String baseline, String plevel );
	public void createBaseline( String baseline, String component, File view, boolean incremental, boolean identical ) throws UCMException;
	
	public String GetBaselineActivities( String baseline );
	
	/* Component */
	public List<String> GetBaselines( String component, String stream, String plevel );
	public String GetRootDir( String component );
	public String LoadComponent( String component ) throws UCMException;
	
	/* Hyper Links */
	public String LoadHyperLink( String fqname, File dir ) throws UCMException;
	public List<Tuple<String, String>> getHlinks( String fqname, String hlinkType, File dir ) throws UCMException;
	
	/* Stream */
	public void RecommendBaseline( String stream, String baseline ) throws UCMException;
	public String GetRecommendedBaselines( String stream ) throws UCMException;
	public String GetStreamFromView( String viewtag ) throws UCMException;
	public void CreateStream( String pstream, String nstream, boolean readonly, String baseline );
	public boolean StreamExists( String fqname );
	public boolean RebaseStream( String viewtag, String stream, String baseline, boolean complete );
	public boolean IsRebaseInProgress( String stream );
	public void CancelRebase( String stream );
	public List<String> GetLatestBaselines( String stream );
	public void Generate( String stream );
	public String LoadStream( String stream ) throws UCMException;
	
	/* Version */
	public String GetVersion( String version, String separator );
	public String getVersionExtension( String file, File viewroot ) throws UCMException;
	
	/* Tag */
	public List<String[]> GetTags( String fqname ) throws UCMException;
	public String GetTag( String fqname );
	public String NewTag( UCMEntity entity, String cgi ) throws UCMException;
	public void DeleteTag( String fqname );
	public void DeleteTagsWithID( String tagType, String tagID, String entity ) throws UCMException;
	
	public String PutTag( String fqname, String keyval, UCMEntity entity );
	
	/* Snapshot Views */
	public void MakeSnapshotView( String stream, File viewroot, String viewtag ) throws UCMException;
	public File GetCurrentViewRoot( File viewroot ) throws UCMException;
	public String ViewrootIsValid( File viewroot ) throws UCMException;
	public boolean ViewExists( String viewtag );
	public void RegenerateViewDotDat( File dir, String viewtag ) throws UCMException;
	public Map SwipeView( File viewroot, boolean excludeRoot );
	public String ViewUpdate( File viewroot, boolean overwrite, String loadrules );

	/* Attributes */
	public String getAttribute( String fqname, String attribute ) throws UCMException;
	public Map<String, String> getAttributes( String fqname ) throws UCMException;
	public Map<String, String> getAttributes( String fqname, File dir ) throws UCMException;
	public void setAttribute( String fqname, String attribute, String value ) throws UCMException;
		
	
	/* 
	 * OTHER STUFF
	 * TODO Should be phased out
	 */
	public String GetXML();
	public void SaveState();
}