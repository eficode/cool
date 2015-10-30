package net.praqma.clearcase.ucm.utils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.praqma.clearcase.Cool;
import net.praqma.clearcase.exceptions.BuildNumberException;
import net.praqma.clearcase.exceptions.BuildNumberException.Type;
import net.praqma.clearcase.exceptions.HyperlinkException;
import net.praqma.clearcase.exceptions.NoSingleTopComponentException;
import net.praqma.clearcase.exceptions.NothingNewException;
import net.praqma.clearcase.exceptions.UCMEntityNotFoundException;
import net.praqma.clearcase.exceptions.UnableToCreateEntityException;
import net.praqma.clearcase.exceptions.UnableToGetEntityException;
import net.praqma.clearcase.exceptions.UnableToInitializeEntityException;
import net.praqma.clearcase.exceptions.UnableToListAttributesException;
import net.praqma.clearcase.exceptions.UnableToLoadEntityException;
import net.praqma.clearcase.exceptions.UnableToSetAttributeException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.entities.HyperLink;
import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.clearcase.ucm.entities.Baseline.LabelBehaviour;
import net.praqma.util.io.BuildNumberStamper;
import net.praqma.util.structure.Tuple;

public class BuildNumber extends Cool {
	private static final transient Logger logger = Logger.getLogger( BuildNumber.class.getName()  );
	
	private final static String rx_buildnumber = "\\S+__(\\d+)_(\\d+)_(\\d+)_(\\d+)";
	private final static Pattern pattern_buildnumber = Pattern.compile( "^" + rx_buildnumber + "$" );

	public static final String __BUILD_NUMBER_MAJOR = "buildnumber.major";
	public static final String __BUILD_NUMBER_MINOR = "buildnumber.minor";
	public static final String __BUILD_NUMBER_PATCH = "buildnumber.patch";
	public static final String __BUILD_NUMBER_SEQUENCE = "buildnumber.sequence";
	public static final String __BUILD_NUMBER_FILE = "buildnumber.file";

	public static String[] isBuildNumber( Baseline baseline ) throws BuildNumberException {
		Matcher match = pattern_buildnumber.matcher( baseline.getShortname() );

		if( !match.find() ) {
			throw new BuildNumberException( "The given Baseline was not a valid build number" );
		}

		String[] result = new String[4];

		result[0] = match.group( 1 );
		result[1] = match.group( 2 );
		result[2] = match.group( 3 );
		result[3] = match.group( 4 );

		return result;
	}

	public static Tuple<Baseline, String[]> createBuildNumber( String baseline, Component component, File view ) throws BuildNumberException, UnableToInitializeEntityException, UnableToCreateEntityException, NothingNewException  {
		Matcher match = pattern_buildnumber.matcher( baseline );

		if( !match.find() ) {
			throw new BuildNumberException( "The given Baseline name, " + baseline + ", was not recognized as a build number." );
		}

		Tuple<Baseline, String[]> result = new Tuple<Baseline, String[]>();

		Baseline bl = Baseline.create( "baseline:" + baseline, component, view, LabelBehaviour.INCREMENTAL, true );

		result.t1 = bl;
		result.t2 = new String[4];

		result.t2[0] = match.group( 1 );
		result.t2[1] = match.group( 2 );
		result.t2[2] = match.group( 3 );
		result.t2[3] = match.group( 4 );

		return result;
	}

	public static int stampFromComponent( Component component, File dir, String major, String minor, String patch, String sequence, boolean ignoreErrors ) throws HyperlinkException, UnableToInitializeEntityException, BuildNumberException, IOException {
		List<HyperLink> result = component.getHyperlinks( __BUILD_NUMBER_FILE, dir );
        
		if( result.isEmpty() ) {
			throw new BuildNumberException( "No build number file references found.", Type.ZERO_MATCHES );
		}

		int number = 0;

		for( HyperLink h : result ) {
			String f = h.getValue().replaceFirst( "@@\\s*$", "" );
			File stampee = new File( f );
            number += BuildNumber.stampIntoCode( stampee, major, minor, patch, sequence );
		}

		return number;
	}

	public static int stampIntoCode( Baseline baseline ) throws BuildNumberException, UnableToLoadEntityException, UnableToCreateEntityException, UCMEntityNotFoundException, HyperlinkException, IOException, UnableToGetEntityException, UnableToInitializeEntityException {
		return stampIntoCode( baseline, null, false );
	}

	public static int stampIntoCode( Baseline baseline, File dir ) throws BuildNumberException, HyperlinkException, UnableToInitializeEntityException, IOException {
		String[] numbers = isBuildNumber( baseline );
		Component component = baseline.getComponent();
		return stampFromComponent( component, dir, numbers[0], numbers[1], numbers[2], numbers[3], false );
	}

	public static int stampIntoCode( Baseline baseline, File dir, boolean ignoreErrors ) throws BuildNumberException, HyperlinkException, UnableToInitializeEntityException, IOException  {
		String[] numbers = isBuildNumber( baseline );
		Component component = baseline.getComponent();
		return stampFromComponent( component, dir, numbers[0], numbers[1], numbers[2], numbers[3], ignoreErrors );
	}

	public static int stampIntoCode( File file, String major, String minor, String patch, String sequence ) throws IOException {
		if( !file.exists() ) {
			throw new IOException( "The file " + file + " does not exist." );
		}

		BuildNumberStamper stamp = null;
		try {
			stamp = new BuildNumberStamper( file );
		} catch (IOException e) {
			throw new IOException( "Could not create temporary file", e );
		}

		int number = 0;

		/* This is where the stamping is called */
		try {
			number = stamp.stampIntoCode( major, minor, patch, sequence );
		} catch (IOException e) {
			logger.warning( "Cannot access file, trying to hijack it" );

			file.setWritable( true );

			try {
				number = stamp.stampIntoCode( major, minor, patch, sequence );
				logger.log( Level.INFO, "Stamping file " + file );
			} catch (IOException e2) {
				throw new IOException( "Failed hijacking. Could not access file", e );
			}
		}

		if( number == 0 ) {
			logger.fine( "Stamping file " + file + ": No occurrences found" );
			System.err.println( "Stamping file " + file + ": No occurrences found" );
		} else {
			logger.fine( "Stamping file " + file + ": Occurrences found" );
			System.out.println( "Stamping file " + file + ": Occurrences found" );
		}

		return number;
	}

	/**
	 * Given a Project, its top level Components sequence number is retrieved,
	 * incremented, stored and returned.
	 * 
	 * @param project
	 *            A UCM Project
	 * @return Integer
	 * @throws UnableToInitializeEntityException Thrown when ClearCase reports errors 
	 * @throws UnableToSetAttributeException Thrown when ClearCase reports errors 
	 * @throws BuildNumberException Thrown when ClearCase reports errors   
	 * @throws UnableToListAttributesException Thrown when ClearCase reports errors 
	 * @throws NoSingleTopComponentException Thrown when ClearCase reports errors 
	 */
	public static Integer getNextBuildSequence( Project project ) throws NoSingleTopComponentException, UnableToInitializeEntityException, UnableToListAttributesException, BuildNumberException, UnableToSetAttributeException {
		Component c = project.getIntegrationStream().getSingleTopComponent();

		/* Get the build number sequence */
		Map<String, String> catts = c.getAttributes();
		if( !catts.containsKey( __BUILD_NUMBER_SEQUENCE ) ) {
			throw new BuildNumberException( "The Component did not have the " + __BUILD_NUMBER_SEQUENCE + " attribute.", Type.MISSING_ATTRIBUTE );
		}

		Integer sequence = Integer.parseInt( catts.get( __BUILD_NUMBER_SEQUENCE ) );
		sequence++;

		c.setAttribute( __BUILD_NUMBER_SEQUENCE, sequence.toString(), true );

		return sequence;
	}

	/* Masks */
	public static final int ALL_ATTRIBUTES = 15;
	public static final int ATTRIBUTE_MAJOR = 1;
	public static final int ATTRIBUTE_MINOR = 2;
	public static final int ATTRIBUTE_PATCH = 4;
	public static final int ATTRIBUTE_SEQUENCE = 8;

	/**
	 * Verify that the project has valid UCM build number attributes
	 * 
	 * @param project
	 *            The UCM project to verify
	 * @return A flag determining the attributes present
     * @throws net.praqma.clearcase.exceptions.NoSingleTopComponentException Thrown error
     * @throws net.praqma.clearcase.exceptions.UnableToInitializeEntityException Thrown error
     * @throws net.praqma.clearcase.exceptions.UnableToListAttributesException Thrown error
	 */
	public static int isValidUCMBuildNumber( Project project ) throws NoSingleTopComponentException, UnableToInitializeEntityException, UnableToListAttributesException {
		int valid = 0;

		Component c = project.getIntegrationStream().getSingleTopComponent();

		/* Get the build number sequence */
		Map<String, String> catts = c.getAttributes();
		if( !catts.containsKey( __BUILD_NUMBER_SEQUENCE ) ) {
			valid += 1 << 0;
		}

		/* Get major, minor and patch */
		Map<String, String> patts = project.getAttributes();
		if( !patts.containsKey( __BUILD_NUMBER_MAJOR ) ) {
			valid += 1 << 1;
		}
		if( !patts.containsKey( __BUILD_NUMBER_MINOR ) ) {
			valid += 1 << 2;
		}
		if( !patts.containsKey( __BUILD_NUMBER_PATCH ) ) {
			valid += 1 << 3;
		}

		return valid;
	}

	/**
	 * This method returns the new build number for a Baseline.
	 * 
     * @param project The project to get the build number from
	 * @return String
	 * @throws UnableToListAttributesException Error
	 * @throws BuildNumberException Error 
	 * @throws UnableToSetAttributeException Error 
	 * @throws UnableToInitializeEntityException Error 
	 * @throws NoSingleTopComponentException Error 
	 */
	public static String getBuildNumber( Project project ) throws UnableToListAttributesException, BuildNumberException, NoSingleTopComponentException, UnableToInitializeEntityException, UnableToSetAttributeException {
		String exceptionMsg = "";

		/* Get build number info */
		Map<String, String> patts = project.getAttributes();
		if( !patts.containsKey( __BUILD_NUMBER_MAJOR ) ) {
			exceptionMsg += "The Project did not have the " + __BUILD_NUMBER_MAJOR + " attribute. ";
		}
		if( !patts.containsKey( __BUILD_NUMBER_MINOR ) ) {
			exceptionMsg += "The Project did not have the " + __BUILD_NUMBER_MINOR + " attribute. ";
		}
		if( !patts.containsKey( __BUILD_NUMBER_PATCH ) ) {
			exceptionMsg += "The Project did not have the " + __BUILD_NUMBER_PATCH + " attribute. ";
		}

		/* Throw if any errors */
		if( exceptionMsg.length() > 0 ) {
			throw new BuildNumberException( exceptionMsg );
		}

		String sequence = getNextBuildSequence( project ).toString();

		/* If this is reached, all is good.... */
		String major = patts.get( __BUILD_NUMBER_MAJOR );
		String minor = patts.get( __BUILD_NUMBER_MINOR );
		String patch = patts.get( __BUILD_NUMBER_PATCH );

		return "__" + major + "_" + minor + "_" + patch + "_" + sequence;
	}

}
