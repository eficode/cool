package net.praqma.clearcase.cleartool;

import java.io.File;
import java.util.ArrayList;

import net.praqma.util.debug.Logger;
import net.praqma.util.execute.AbnormalProcessTerminationException;
import net.praqma.util.execute.CmdResult;
import net.praqma.util.execute.CommandLineException;
import net.praqma.util.execute.CommandLineInterface;

public class CommandLineMock implements CommandLineInterface {
	private Logger logger = Logger.getLogger();

	private CommandLineMock() {

	}

	private static CommandLineMock instance = new CommandLineMock();

	public static CommandLineMock getInstance() {
		return instance;
	}

	public void setLogger( Logger logger ) {
		this.logger = logger;
	}

	@Override
	public CmdResult run( String cmd ) throws CommandLineException, AbnormalProcessTerminationException {
		return run( cmd, null, false, false );
	}

	@Override
	public CmdResult run( String cmd, File dir ) throws CommandLineException, AbnormalProcessTerminationException {
		return run( cmd, dir, false, false );
	}

	@Override
	public CmdResult run( String cmd, File dir, boolean merge ) throws CommandLineException, AbnormalProcessTerminationException {
		return run( cmd, dir, merge, false );
	}

	/**/
	private static File versionDotH = null;

	public static void setVersionDotH( File f ) {
		versionDotH = f;
	}

	@Override
	public CmdResult run( String cmd, File dir, boolean merge, boolean ignore ) throws CommandLineException, AbnormalProcessTerminationException {
		/* This is the final */

		logger.debug( "$ " + cmd + ", " + dir + ", " + merge + ", " + ignore );
		// System.out.println( "$ " + cmd + ", " + dir + ", " + merge + ", " +
		// ignore );

		CmdResult res = new CmdResult();
		res.stdoutBuffer = new StringBuffer();
		res.stdoutList = new ArrayList<String>();

		/*
		 * Load() a Baseline
		 * 
		 * cleartool desc -fmt %n::%[component]p::%[bl_stream]p::%[plevel]p::%u
		 * baseline:CHW_BASELINE_51@\Cool_PVOB
		 * CHW_BASELINE_51::_System::Server_int::TESTED::chw
		 */
		if( cmd.equals( "cleartool desc -fmt %n::%[component]p::%[bl_stream]p::%[plevel]p::%u::%Nd::%[label_status]p baseline:CHW_BASELINE_51@\\Cool_PVOB" ) ) {
			res.stdoutBuffer.append( "CHW_BASELINE_51::_System::Server_int::TESTED::chw::20110810.232400::full" );
		}

		if( cmd.equals( "cleartool desc -fmt %n::%[component]p::%[bl_stream]p::%[plevel]p::%u::%Nd::%[label_status]p baseline:CHW_BASELINE_51_no@\\Cool_PVOB" ) ) {
			throw new AbnormalProcessTerminationException( "cleartool: Error: Baseline not found: \"CHW_BASELINE_51_no\"." );
		}

		if( cmd.equals( "cleartool chbl -level RELEASED baseline baseline:CHW_BASELINE_51@\\Cool_PVOB" ) ) {

		}

		/****
		 * 
		 * Tag Tests
		 * 
		 **** */

		/*
		 * CHW_BASELINE_51 Hyperlinks: tag@949@\Cool_PVOB ->
		 * "tagtype=hudson&tagid=PraqmaticTest&buildstatus=SUCCESS&"
		 */

		if( cmd.equals( "cleartool describe -ahlink tag -l baseline:bn__1_2_3_1234@\\Cool_PVOB" ) ) {
			res.stdoutList.add( "Something" );
			res.stdoutList.add( "  Hyperlinks:" );
			res.stdoutList.add( "     tag@949@\\Cool_PVOB ->  \"tagtype=a&tagid=1&k1=v1&k2=v2\"" );
		}

		/* Created hyperlink "tag@1512@\Cool_PVOB". */
		String s1 = "cleartool mkhlink -ttext \"tagtype=tt&tagid=001&k1=v1&k2=v2\" tag baseline:tagtest@\\Cool_PVOB";
		if( cmd.equals( s1 ) && cmd.length() == s1.length() ) {
			System.out.println( "CMD=" + cmd );
			System.out.println( "s1=" + s1 );
			System.out.println( cmd.length() + "=" + s1.length() );
			res.stdoutBuffer.append( "Created hyperlink \"tag@1512@\\Cool_PVOB\"." );
		}

		/****
		 * 
		 * Component Tests
		 * 
		 **** */

		if( cmd.equals( "cleartool describe component:_System@\\Cool_PVOB" ) ) {
			res.stdoutBuffer.append( "" );
		}

		/****
		 * 
		 * BuildNumber Tests
		 * 
		 **** */

		/* Projects */

		if( cmd.equals( "cleartool lsproj -fmt %[istream]Xp project:bn_project@\\Cool_PVOB" ) ) {
			res.stdoutBuffer.append( "stream:bn_stream@\\Cool_PVOB" );
		}

		if( cmd.equals( "cleartool lsproj -fmt %[istream]Xp project:bn_project_no@\\Cool_PVOB" ) ) {
			res.stdoutBuffer.append( "stream:bn_stream_no@\\Cool_PVOB" );
		}

		/*
		 * cleartool desc -fmt %[rec_bls]p stream:bn_stream@\Cool_PVOB
		 * CHW_BASELINE_22.5743
		 */
		/* STREAMS */

		if( cmd.equals( "cleartool desc -fmt %[rec_bls]p stream:bn_stream@\\Cool_PVOB" ) ) {
			res.stdoutBuffer.append( "rec_baseline000001" );
		}

		if( cmd.equals( "cleartool desc -fmt %[rec_bls]p stream:bn_stream_no@\\Cool_PVOB" ) ) {
			res.stdoutBuffer.append( "rec_baseline000002" );
		}

		if( cmd.equals( "cleartool desc -fmt %[rec_bls]p stream:Server_int@\\Cool_PVOB" ) ) {
			res.stdoutBuffer.append( "rec_baseline000001" );
		}

		/* Baselines */

		/*
		 * cleartool desc -fmt %n::%[component]p::%[bl_stream]p::%[plevel]p::%u
		 * baseline:rec_baseline000001@\Cool_PVOB
		 */
		if( cmd.equals( "cleartool desc -fmt %n::%[component]p::%[bl_stream]p::%[plevel]p::%u::%Nd::%[label_status]p baseline:rec_baseline000001@\\Cool_PVOB" ) ) {
			res.stdoutBuffer.append( "rec_baseline000001::_System::Server_int::INITIAL::chw::20110810.232400::full" );
		}

		if( cmd.equals( "cleartool desc -fmt %n::%[component]p::%[bl_stream]p::%[plevel]p::%u::%Nd::%[label_status]p baseline:rec_baseline000002@\\Cool_PVOB" ) ) {
			res.stdoutBuffer.append( "rec_baseline000002::_System_2::Server_int::INITIAL::chw::20110810.232400::full" );
		}

		if( cmd.equals( "cleartool desc -fmt %n::%[component]p::%[bl_stream]p::%[plevel]p::%u::%Nd::%[label_status]p baseline:bn__1_2_3_1234@\\Cool_PVOB" ) ) {
			res.stdoutBuffer.append( "rec_baseline000002::_System::Server_int::INITIAL::chw::20110810.232400::full" );
		}

		/* ATTRIBUTES */

		if( cmd.equals( "cleartool describe -aattr -all component:_System@\\Cool_PVOB" ) ) {
			res.stdoutList.add( "bogus" );
			res.stdoutList.add( "More bogus" );
			res.stdoutList.add( " buildnumber.sequence = 1234 " );
		}

		if( cmd.equals( "cleartool describe -aattr -all component:_System_2@\\Cool_PVOB" ) ) {
			res.stdoutList.add( "bogus" );
			res.stdoutList.add( "More bogus" );
		}

		if( cmd.equals( "cleartool describe -aattr -all project:bn_project@\\Cool_PVOB" ) ) {
			res.stdoutList.add( "bogus" );
			res.stdoutList.add( "More bogus" );
			res.stdoutList.add( " buildnumber.major = 1 " );
			res.stdoutList.add( " buildnumber.minor = 2 " );
			res.stdoutList.add( " buildnumber.patch = 3 " );
		}

		// cleartool describe -ahlink buildnumber.file -l
		// component:System@\Cool_PVOB
		if( cmd.equals( "cleartool describe -ahlink buildnumber.file -l component:_System@\\Cool_PVOB" ) ) {
			res.stdoutList.add( "bogus" );
			res.stdoutList.add( " Hyperlinks:" ); //
			res.stdoutList.add( "   buildnumber.file@1234@\\Cool_PVOB ->  " + ( versionDotH != null ? versionDotH.getAbsolutePath() : "version.h" ) + " " );
		}

		if( cmd.equals( "cleartool describe -ahlink buildnumber.file -l component:_System_no@\\Cool_PVOB" ) ) {
			res.stdoutList.add( "bogus" );
			res.stdoutList.add( " Hyperlinks:" );
			res.stdoutList.add( "   nobuildnumber.file@1234@\\Cool_PVOB ->  " + ( versionDotH != null ? versionDotH.getAbsolutePath() : "version.h" ) + " " );
		}

		return res;
	}

	@Override
	public OperatingSystem getOS() {
		// TODO Auto-generated method stub
		return null;
	}

}
