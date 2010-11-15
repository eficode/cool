package net.praqma.clearcase.ucm.persistence;

import java.util.ArrayList;

import net.praqma.clearcase.ucm.entities.*;
import net.praqma.clearcase.ucm.UCMException;

interface UCMStrategyInterface
{
	/* Baseline */
	public String LoadBaseline( String baseline );
	public String GetBaselineDiff( String baseline, String other, boolean nmerge );
	public String SetPromotionLevel( String baseline, String plevel );
	
	public String GetBaselineActivities( String baseline );
	
	/* Component */
	public ArrayList<String> GetBaselines( String component, String stream, String plevel );
	
	/* Stream */
	public void RecommendBaseline( String stream, String baseline ) throws UCMException;
	public String GetRecommendedBaselines( String stream );
	
	/* Version */
	public String GetVersion( String version, String separator );
	
	/* Tag */
	public String GetTags( String fqname );
	public String GetTag( String fqname );
	public String NewTag( UCMEntity entity, String cgi );
	public void DeleteTag( String fqname );
	public String PutTag( String fqname, String keyval, UCMEntity entity );
	
	/* Snapshot Views */
	public void MakeSnapshotView( String stream, String viewtag, String viewroot );
	
	
	/* TEST */
	public String GetXML();
	public void SaveState();
}