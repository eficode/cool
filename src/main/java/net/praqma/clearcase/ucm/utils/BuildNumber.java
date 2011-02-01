package net.praqma.clearcase.ucm.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Component;

public class BuildNumber
{
	private Baseline baseline = null;
	private final Pattern pattern_parse_baseline = Pattern.compile( "^\\S+__(\\d+)_(\\d+)_(\\d+)_(\\d+)$" );
	
	private Integer major    = null;
	private Integer minor    = null;
	private Integer patch    = null;
	private Integer sequence = null;
	
	public BuildNumber( Baseline baseline ) throws UCMException
	{
		this.baseline = baseline;
		Matcher match = pattern_parse_baseline.matcher( baseline.GetShortname() );
		
		if( !match.find() )
		{
			throw new UCMException( "The given Baseline, " + baseline.GetShortname() + ", was not recognized as a build number." );
		}
		
		major    = Integer.parseInt( match.group( 1 ) );
		minor    = Integer.parseInt( match.group( 2 ) );
		patch    = Integer.parseInt( match.group( 3 ) );
		sequence = Integer.parseInt( match.group( 4 ) );		
	}
	
	public void GetNextBuildNumber() throws UCMException
	{
		Component c = this.baseline.GetStream().GetSingleTopComponent();
	}
	
	
}
