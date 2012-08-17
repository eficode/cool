package net.praqma.clearcase.cleartool;

import java.io.File;

import net.praqma.clearcase.*;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.exceptions.CleartoolNotInstalledException;
import net.praqma.clearcase.exceptions.NoLicenseServerException;
import net.praqma.clearcase.exceptions.NoLicensesException;
import net.praqma.logging.Config;
import net.praqma.util.debug.Logger;
import net.praqma.util.execute.AbnormalProcessTerminationException;
import net.praqma.util.execute.CmdResult;
import net.praqma.util.execute.CommandLine;
import net.praqma.util.execute.CommandLineException;
import net.praqma.util.execute.CommandLineInterface;

/**
 * The Cleartool proxy class All calls to cleartool, should be done through
 * these static functions. run( String ) : returns the return value as String.
 * run_a( String ): returns the return value as an array of Strings, separated
 * by new lines.
 * 
 * @author wolfgang
 * 
 */
public abstract class Cleartool extends Cool {

	private static CommandLineInterface cli = null;
	private static Logger logger = Logger.getLogger();

	private static java.util.logging.Logger tracer = java.util.logging.Logger.getLogger(Config.GLOBAL_LOGGER_NAME);
	
	static {
		cli = CommandLine.getInstance();
	}

	public static CmdResult run( String cmd ) throws CommandLineException, AbnormalProcessTerminationException {
		tracer.entering(Cleartool.class.getSimpleName(), "run");
		
		return _run( cmd, null, true, false );
	}

	public static CmdResult run( String cmd, File dir ) throws CommandLineException, AbnormalProcessTerminationException {
		tracer.entering(Cleartool.class.getSimpleName(), "run");
		
		return _run( cmd, dir, true, false );
	}

	public static CmdResult run( String cmd, File dir, boolean merge ) throws CommandLineException, AbnormalProcessTerminationException {
		tracer.entering(Cleartool.class.getSimpleName(), "run");
		
		return _run( cmd, dir, merge, false );
	}
	
	public static CmdResult run( String cmd, File dir, boolean merge, boolean ignore ) throws CommandLineException, AbnormalProcessTerminationException {
		tracer.entering(Cleartool.class.getSimpleName(), "run");
		
		return _run( cmd, dir, merge, ignore );
	}
	
	private static CmdResult _run( String cmd, File dir, boolean merge, boolean ignore ) throws CommandLineException, AbnormalProcessTerminationException {
		tracer.entering(Cleartool.class.getSimpleName(), "run");
		
		try {
			return cli.run( "cleartool " + cmd, dir, merge, ignore );
		} catch( AbnormalProcessTerminationException e ) {
			
			/* Validate exit errors */
			if( e.getMessage().contains( "cleartool: command not found" ) ) {
				CleartoolNotInstalledException exception = new CleartoolNotInstalledException( "Cleartool not installed", e );
				tracer.severe(String.format("Exception thrown type: %s; message: %s", exception.getClass(), exception.getMessage()));
				throw exception;
			} else if( e.getMessage().contains( "FLEXnet Licensing error:-15,570" )) {
				NoLicenseServerException exception =  new NoLicenseServerException( "No license server available", e );
				tracer.severe(String.format("Exception thrown type: %s; message: %s", exception.getClass(), exception.getMessage()));
				throw exception;
			} else if( e.getMessage().contains( "FLEXnet Licensing error:-18,147" )) {
				NoLicensesException exception =  new NoLicensesException( "No licenses available", e );
				tracer.severe(String.format("Exception thrown type: %s; message: %s", exception.getClass(), exception.getMessage()));
				throw exception;
			} else if( e.getMessage().contains( "There are no valid licenses in the NT registry for ClearCase" )) {
				NoLicensesException exception =  new NoLicensesException( "No licenses available", e );
				tracer.severe(String.format("Exception thrown type: %s; message: %s", exception.getClass(), exception.getMessage()));
				throw exception;
			} else {
				throw e;
			}
		}
	}
}

/*
 * License server is not running 
 * 
cleartool: Error: License checkout error from Rational Common Licensing:
Cannot connect to license server system.
 The license server manager (lmgrd) has not been started yet,
 the wrong port@host or license file is being used, or the
 port or hostname in the license file has been changed.
Feature:       RLPwCC
Server name:   NIGHTCRAWLER
License path:  C:\Program Files (x86)\IBM\RationalRLKS\common\rational_perm.dat;C:\Program Files (x86)\IBM\RationalRLKS\common\rational_temp.dat;@NIGHTCRAWLER;
FLEXnet Licensing error:-15,570
For further information, refer to the FLEXnet Licensing documentation,
available at "www.flexerasoftware.com".
cleartool: Error: You do not have a license to run ClearCase.
 *
 * 
 * 
 * No licenses available
 * 
 * 
cleartool: Error: License checkout error from Rational Common Licensing:
The FEATURE name RLPwCC with version 1.0 cannot be found
License server system does not support this feature.
Feature:       RLPwCC
License path:  C:\Program Files (x86)\IBM\RationalRLKS\common\rational_perm.dat;C:\Program Files (x86)\IBM\RationalRLKS\common\rational_temp.dat;@NIGHTCRAWLER;
FLEXnet Licensing error:-18,147
For further information, refer to the FLEXnet Licensing documentation,
available at "www.flexerasoftware.com".
cleartool: Error: You do not have a license to run ClearCase.
 *
 *
 * No licenses
 *
 *
cleartool: Error: There are no valid licenses in the NT registry for ClearCase.
cleartool: Error: You do not have a license to run ClearCase.
*/ 



