package net.praqma.clearcase.test.unit;

import java.io.File;
import net.praqma.clearcase.Cool;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.exceptions.UnableToInitializeEntityException;
import net.praqma.clearcase.ucm.entities.*;

import net.praqma.util.execute.*;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

public class VersionTest {

	@Test
	public void getVersion() throws ClearCaseException {
		String version = "c:\\code\\lib\\common.h@@\\main\\int\\1";
		
		Version v = Version.getVersion( version );
		
		assertThat( v.getRevision(), is( 1 ) );
		assertThat( v.getVersion(), is( "\\main\\int\\1" ) );
		assertThat( v.getBranch(), is( "\\main\\int" ) );
		if( Cool.getOS().equals( CommandLineInterface.OperatingSystem.WINDOWS ) ) {
			assertThat( v.getFile(), is( new File( "c:\\code\\lib\\common.h" ) ) );
		}
		
		String version2 = "/code/lib/common.h@@/main/int/1";
		
		Version v2 = Version.getVersion( version2 );
		
		assertThat( v2.getRevision(), is( 1 ) );
		assertThat( v2.getVersion(), is( "/main/int/1" ) );
		assertThat( v2.getBranch(), is( "/main/int" ) );
		if( Cool.getOS().equals( CommandLineInterface.OperatingSystem.UNIX ) ) {
			assertThat( v2.getFile(), is( new File( "/code/lib/common.h" ) ) );
		}
	}
	
	@Test
	public void getVersionNew() throws ClearCaseException {
		String version = "c:\\code\\lib\\common.h@@\\main\\int\\1";
		
		Version v = Version.getVersion( version );
		
		assertThat( v.getRevision(), is( 1 ) );
		assertThat( v.getVersion(), is( "\\main\\int\\1" ) );
		assertThat( v.getBranch(), is( "\\main\\int" ) );
		if( Cool.getOS().equals( CommandLineInterface.OperatingSystem.WINDOWS ) ) {
			assertThat( v.getFile(), is( new File( "c:\\code\\lib\\common.h" ) ) );
		}
		
		String version2 = "/code/lib/common.h@@/main/int/1";
		
		Version v2 = Version.getVersion( version2 );
		
		assertThat( v2.getRevision(), is( 1 ) );
		assertThat( v2.getVersion(), is( "/main/int/1" ) );
		assertThat( v2.getBranch(), is( "/main/int" ) );
		if( Cool.getOS().equals( CommandLineInterface.OperatingSystem.UNIX ) ) {
			assertThat( v2.getFile(), is( new File( "/code/lib/common.h" ) ) );
		}
	}

    //@Test
    public void testBranches() throws UnableToInitializeEntityException {
        String s;
        if( Cool.getOS().equals( CommandLineInterface.OperatingSystem.WINDOWS ) ) {
            s = "C:\\views\\chw-server\\night-vobadmin_one_int_3\\crot\\Model@@\\main\\wolles_dev\\1\\wolles.txt";
        } else {
            s = "/views/chw-server/night-vobadmin_one_int_3/crot/Model@@/main/wolles_dev/1/wolles.txt";
        }
        Version v = Version.get( s );

        assertThat( v.getBranches().size(), is( 2 ) );
        assertThat( v.getBranches().get( 0 ).getName(), is( "main" ) );
        assertThat( v.getBranches().get( 1 ).getName(), is( "wolles_dev" ) );
    }

    //@Test
    public void testOffBranchedVersions() throws UnableToInitializeEntityException {
        String s = "C:\\views\\chw-server\\night-vobadmin_one_int_3\\crot\\Model@@\\main\\wolles_dev\\1\\wolles.txt";
        Version v = Version.get( s );

        assertThat( v.getFile().getAbsolutePath(), is( "C:\\views\\chw-server\\night-vobadmin_one_int_3\\crot\\Model\\wolles.txt" ) );
    }

    //@Test
    public void testOffBranchedVersionsEmpty() throws UnableToInitializeEntityException {
        String s;
        if( Cool.getOS().equals( CommandLineInterface.OperatingSystem.WINDOWS ) ) {
            s = "C:\\views\\chw-server\\night-vobadmin_one_int_3\\crot\\Model@@\\main\\wolles_dev\\1";
        } else {
            s = "/views/chw-server/night-vobadmin_one_int_3/crot/Model@@/main/wolles_dev/1";
        }
        Version v = Version.get( s );

        assertThat( v.getFile().getAbsolutePath(), is( "C:\\views\\chw-server\\night-vobadmin_one_int_3\\crot\\Model" ) );
    }

    //@Test
    public void complexVersionSyntax() throws UnableToInitializeEntityException {
        String s = "M:\\vobadm_view\\kerne2\\.@@\\main\\ker2_work\\3\\01_Domænetest\\main\\ker2_work\\1\\03_Leverancetestrapporter\\main\\ker2_work\\1\\Release 7\\main\\ker2_work\\1\\Kerne2_R7_LeveranceTestRapport 01-03-11.doc@@\\main\\ker2_work\\1";

        Version v = Version.get( s );

        assertThat( v.getFile(), is( new File( "M:\\vobadm_view\\kerne2\\.\\01_Domænetest\\03_Leverancetestrapporter\\Release 7\\Kerne2_R7_LeveranceTestRapport 01-03-11.doc" ) ) );
    }

    @Test
    public void testExtendedNaming() throws UnableToInitializeEntityException {
        String s = getOSFileString( "\\vobadm_view\\kerne2\\.@@\\main\\ker2_work\\3\\01_Domænetest\\main\\ker2_work\\1\\03_Leverancetestrapporter\\main\\ker2_work\\1\\Release 7\\main\\ker2_work\\1\\Kerne2_R7_LeveranceTestRapport 01-03-11.doc@@\\main\\ker2_work\\1" );

        Version v = Version.get( s );

        assertThat( v.getRevision(), is( 1 ) );
        assertThat( v.getBranches().size(), is( 2 ) );
        assertThat( v.getFile().toString(), is( getOSFileString( "\\vobadm_view\\kerne2\\01_Domænetest\\03_Leverancetestrapporter\\Release 7\\Kerne2_R7_LeveranceTestRapport 01-03-11.doc" ) ) );
    }

    @Test
    public void testQualifiedFilename() throws UnableToInitializeEntityException {
        String s = "/vobadm_view/kerne2/.@@/main/ker2_work/3/01_Domænetest/main/ker2_work/1/03_Leverancetestrapporter/main/ker2_work/1/Release 7/main/ker2_work/1/Kerne2_R7_LeveranceTestRapport 01-03-11.doc@@/main/ker2_work/1";
        String osstring = getOSFileString( s );

        Version v = Version.get( osstring );

        assertThat( v.getQualifiedFilename(), is( getOSFileString( "\\vobadm_view\\kerne2\\.@@\\main\\ker2_work\\3\\01_Domænetest\\main\\ker2_work\\1\\03_Leverancetestrapporter\\main\\ker2_work\\1\\Release 7\\main\\ker2_work\\1\\Kerne2_R7_LeveranceTestRapport 01-03-11.doc@@" ) ) );
    }

    @Test
    public void testQualifiedFilename2() throws UnableToInitializeEntityException {
        String s = getOSFileString( "\\vobadm_view\\kerne2\\Kerne2_R7_LeveranceTestRapport 01-03-11.doc@@\\main\\ker2_work\\1" );

        Version v = Version.get( s );

        assertThat( v.getQualifiedFilename(), is( getOSFileString( "\\vobadm_view\\kerne2\\Kerne2_R7_LeveranceTestRapport 01-03-11.doc@@" ) ) );
    }

    @Test
    public void testQualifiedFilename3() throws UnableToInitializeEntityException {
        String s = getOSFileString( "\\vobadm_view\\kerne2\\Kerne2_R7_LeveranceTestRapport 01-03-11.doc" );

        Version v = Version.get( s );

        assertThat( v.getQualifiedFilename(), is( getOSFileString( "\\vobadm_view\\kerne2\\Kerne2_R7_LeveranceTestRapport 01-03-11.doc@@" ) ) );
    }

    public String getOSFileString( String fileString ) {
        if( Cool.getOS().equals( CommandLineInterface.OperatingSystem.WINDOWS ) ) {
            return "M:" + fileString.replaceAll( Cool.qfsor, Cool.qfs );
        } else {
            return fileString.replaceAll( Cool.qfsor, Cool.qfs );
        }
    }
}
