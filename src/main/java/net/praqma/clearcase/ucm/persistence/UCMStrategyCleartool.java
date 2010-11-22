package net.praqma.clearcase.ucm.persistence;

import java.util.ArrayList;
import java.util.List;

import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.entities.UCMEntity;
import net.praqma.utils.Debug;


public class UCMStrategyCleartool implements UCMStrategyInterface
{
	private static Debug logger = Debug.GetLogger();
	
	static
	{
		//logger.ExcludeClass( UCMStrategyXML.class.getName() );
	}
	
	
	private static final String filesep = System.getProperty( "file.separator" );
	private static final String linesep = System.getProperty( "line.separator" );
	
	public UCMStrategyCleartool()
	{
		logger.log( "Using CLEARTOOL strategy" );
	}
	
	@Override
	public String LoadBaseline( String baseline )
	{
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String GetBaselineDiff( String baseline, String other, boolean nmerge )
	{
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String SetPromotionLevel( String baseline, String plevel )
	{
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String GetBaselineActivities( String baseline )
	{
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public List<String> GetBaselines( String component, String stream, String plevel )
	{
		// my @retval = cleartool_qx(' lsbl -s -component '.$self->get_fqname().' -stream '.$stream->get_fqname().' -level '.$params{'plevel'});
		String cmd = "lsbl -s -component " + component + " -stream " + stream + " -level " + plevel;
		return Cleartool.run( cmd );
	}
	@Override
	public void RecommendBaseline( String stream, String baseline ) throws UCMException
	{
		// TODO Auto-generated method stub
		
	}
	@Override
	public String GetRecommendedBaselines( String stream )
	{
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String GetVersion( String version, String separator )
	{
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String GetTags( String fqname )
	{
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String GetTag( String fqname )
	{
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String NewTag( UCMEntity entity, String cgi )
	{
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void DeleteTag( String fqname )
	{
		// TODO Auto-generated method stub
		
	}
	@Override
	public void DeleteTagsWithID( String tagType, String tagID, String entity )
	{
		// TODO Auto-generated method stub
		
	}
	@Override
	public String PutTag( String fqname, String keyval, UCMEntity entity )
	{
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void MakeSnapshotView( String stream, String viewtag, String viewroot )
	{
		// TODO Auto-generated method stub
		
	}
	@Override
	public String GetXML()
	{
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void SaveState()
	{
		// TODO Auto-generated method stub
		
	}
}