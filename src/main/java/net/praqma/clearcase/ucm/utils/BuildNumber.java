package net.praqma.clearcase.ucm.utils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.entities.HyperLink;
import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.util.debug.Logger;
import net.praqma.util.io.BuildNumberStamper;
import net.praqma.util.structure.Tuple;

public class BuildNumber
{
	private final static String rx_buildnumber = "\\S+__(\\d+)_(\\d+)_(\\d+)_(\\d+)";
	private final static Pattern pattern_buildnumber = Pattern.compile( "^" + rx_buildnumber + "$" );
	
	private static Logger logger = Logger.getLogger();
	
	public static final String __BUILD_NUMBER_MAJOR    = "buildnumber.major";
	public static final String __BUILD_NUMBER_MINOR    = "buildnumber.minor";
	public static final String __BUILD_NUMBER_PATCH    = "buildnumber.patch";
	public static final String __BUILD_NUMBER_SEQUENCE = "buildnumber.sequence";
	public static final String __BUILD_NUMBER_FILE     = "buildnumber.file";
	
	public static String[] isBuildNumber( Baseline baseline ) throws UCMException
	{
		Matcher match = pattern_buildnumber.matcher( baseline.GetShortname() );
		
		if( !match.find() )
		{
			throw new UCMException( "The given Baseline was not a valid build number" );
		}
		
		String[] result = new String[4];
		
		result[0] = match.group( 1 );
		result[1] = match.group( 2 );
		result[2] = match.group( 3 );
		result[3] = match.group( 4 );
		
		return result;
	}
	
	public static Tuple<Baseline, String[]> createBuildNumber( String baseline, Component component, File view ) throws UCMException
	{
		Matcher match = pattern_buildnumber.matcher( baseline );
		
		if( !match.find() )
		{
			throw new UCMException( "The given Baseline name, " + baseline + ", was not recognized as a build number." );
		}
		
		Tuple<Baseline, String[]> result = new Tuple<Baseline, String[]>();
		
		Baseline bl = Baseline.create( "baseline:" + baseline, component, view, true, true );
		
		result.t1 = bl;
		result.t2 = new String[4];
		
		result.t2[0] = match.group( 1 );
		result.t2[1] = match.group( 2 );
		result.t2[2] = match.group( 3 );
		result.t2[3] = match.group( 4 );
		
		return result;
	}
	
	public static void stampFromComponent( Component component, File dir, String major, String minor, String patch, String sequence ) throws UCMException
	{
		List<HyperLink> result = component.getHlinks( __BUILD_NUMBER_FILE, dir );
		
		for( HyperLink h : result )
		{
			String f = h.getValue().replaceFirst( "@@\\s*$", "" );
			File stampee = new File( f );
			BuildNumber.stampIntoCode( stampee, major, minor, patch, sequence );
		}
	}
	
	public static void stampIntoCode( Baseline baseline ) throws UCMException
	{
		stampIntoCode( baseline, null );
	}
	
	public static void stampIntoCode( Baseline baseline, File dir ) throws UCMException
	{
		String[] numbers = isBuildNumber( baseline );
		Component component = baseline.GetComponent();
		
		stampFromComponent( component, dir, numbers[0], numbers[1], numbers[2], numbers[3] );
	}
	
	public static void stampIntoCode( File file, String major, String minor, String patch, String sequence ) throws UCMException
	{
		if( !file.exists() )
		{
			throw new UCMException( "The file " + file + " does not exist." );
		}
		
		BuildNumberStamper stamp = null;
		try
		{
			stamp = new BuildNumberStamper( file );
		}
		catch ( IOException e )
		{
			throw new UCMException( "Could not create temporary file" );
		}
		
		try
		{
			stamp.stampIntoCode( major, minor, patch, sequence );
			System.out.println( "Stamping file " + file );
			logger.log( "Stamping file " + file );
		}
		catch ( IOException e )
		{
			logger.log( "Cannot access file, trying to hijack it" );
			
			file.setWritable( true );
			
			try
			{
				stamp.stampIntoCode( major, minor, patch, sequence );
				System.out.println( "Stamping file " + file );
				logger.log( "Stamping file " + file );
			}
			catch ( IOException e2 )
			{
				throw new UCMException( "Failed hijacking. Could not access file" );
			}
		}
		

	}
	
	/**
	 * Given a Project, its top level Components sequence number is retrieved, incremented, stored and returned.
	 * @param project A UCM Project
	 * @return
	 * @throws UCMException
	 */
	public static Integer getNextBuildSequence( Project project ) throws UCMException
	{
		Component c = project.GetStream().GetSingleTopComponent();
		
		/* Get the build number sequence */
		Map<String, String> catts = c.getAttributes();
		if( !catts.containsKey( __BUILD_NUMBER_SEQUENCE ) )
		{
			throw new UCMException( "The Component did not have the " + __BUILD_NUMBER_SEQUENCE + " attribute." );
		}
		
		Integer sequence = Integer.parseInt( catts.get( __BUILD_NUMBER_SEQUENCE ) );
		sequence++;

		c.setAttribute( __BUILD_NUMBER_SEQUENCE, sequence.toString() );
		
		return sequence;
	}
	
	
	/**
	 * This method returns the new build number for a Baseline.
	 * @return
	 * @throws UCMException
	 */
	public static String getBuildNumber( Project project ) throws UCMException
	{
		String exceptionMsg = "";
		
		/* Get build number info */
		Map<String, String> patts = project.getAttributes();
		if( !patts.containsKey( __BUILD_NUMBER_MAJOR ) )
		{
			exceptionMsg += "The Project did not have the " + __BUILD_NUMBER_MAJOR + " attribute. ";
		}
		if( !patts.containsKey( __BUILD_NUMBER_MINOR ) )
		{
			exceptionMsg += "The Project did not have the " + __BUILD_NUMBER_MINOR + " attribute. ";
		}
		if( !patts.containsKey( __BUILD_NUMBER_PATCH ) )
		{
			exceptionMsg += "The Project did not have the " + __BUILD_NUMBER_PATCH + " attribute. ";
		}
		
		/* Throw if any errors */
		if( exceptionMsg.length() > 0 )
		{
			throw new UCMException( exceptionMsg );
		}
		
		String sequence = getNextBuildSequence( project ).toString();
		
		/*  If this is reached, all is good.... */
		String major    = patts.get( __BUILD_NUMBER_MAJOR );
		String minor    = patts.get( __BUILD_NUMBER_MINOR );
		String patch    = patts.get( __BUILD_NUMBER_PATCH );
				
		return "__" + major + "_" + minor + "_" + patch + "_" + sequence;
	}
	
	
	
}
