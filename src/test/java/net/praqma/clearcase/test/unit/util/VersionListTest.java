package net.praqma.clearcase.test.unit.util;

import net.praqma.clearcase.Cool;
import net.praqma.clearcase.exceptions.UnableToInitializeEntityException;
import net.praqma.clearcase.ucm.entities.Activity;
import net.praqma.clearcase.ucm.entities.Version;
import net.praqma.clearcase.ucm.utils.VersionList;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @author cwolfgang
 */
public class VersionListTest {

    @Test
    public void testGetChanges() throws UnableToInitializeEntityException {
        Version v1 = Version.get( "foo.bar" );
        v1.setFile( new File( "foo.bar" ) );

        Version v1s = Mockito.spy( v1 );
        Mockito.doReturn( 1 ).when( v1s ).getRevision();

        Activity a1 = Activity.get( "act1@\\snade" );
        a1.changeset.versions.add( v1s );

        List<Activity> activities = new ArrayList<Activity>(  );
        activities.add( a1 );

        List<Version> versions = VersionList.getLatestChanges( activities, null );

        assertThat( versions.size(), is( 1 ) );
        assertThat( versions.get( 0 ).getRevision(), is( 1 ) );
    }

    @Test
    public void testGetChangesMultiple() throws UnableToInitializeEntityException {
        Version v1 = Version.get( "foo.bar" );
        v1.setFile( new File( "foo.bar" ) );

        Version v1s = Mockito.spy( v1 );
        Mockito.doReturn( 1 ).when( v1s ).getRevision();

        Version v2s = Mockito.spy( v1 );
        Mockito.doReturn( 2 ).when( v1s ).getRevision();

        Version v3s = Mockito.spy( v1 );
        Mockito.doReturn( 3 ).when( v1s ).getRevision();

        Activity a1 = Activity.get( "act1@\\snade" );
        a1.changeset.versions.add( v1s );
        a1.changeset.versions.add( v2s );
        a1.changeset.versions.add( v3s );

        List<Activity> activities = new ArrayList<Activity>(  );
        activities.add( a1 );

        List<Version> versions = VersionList.getLatestChanges( activities, null );

        assertThat( versions.size(), is( 1 ) );
        assertThat( versions.get( 0 ).getRevision(), is( 3 ) );
    }

    @Test
    public void testGetChangesMultipleFiles() throws UnableToInitializeEntityException {
        Version v1 = Version.get( "model.h" );

        Version v11s = Mockito.spy( v1 );
        Mockito.doReturn( 1 ).when( v11s ).getRevision();

        Version v12s = Mockito.spy( v1 );
        Mockito.doReturn( 2 ).when( v12s ).getRevision();

        Version v13s = Mockito.spy( v1 );
        Mockito.doReturn( 3 ).when( v13s ).getRevision();

        Version v2 = Version.get( "common.h" );

        Version v21s = Mockito.spy( v2 );
        Mockito.doReturn( 1 ).when( v21s ).getRevision();

        Version v22s = Mockito.spy( v2 );
        Mockito.doReturn( 2 ).when( v22s ).getRevision();

        Version v23s = Mockito.spy( v2 );
        Mockito.doReturn( 3 ).when( v23s ).getRevision();

        Activity a1 = Activity.get( "act1@\\snade" );
        a1.changeset.versions.add( v11s );
        a1.changeset.versions.add( v12s );
        a1.changeset.versions.add( v13s );

        a1.changeset.versions.add( v21s );
        a1.changeset.versions.add( v22s );
        a1.changeset.versions.add( v23s );

        List<Activity> activities = new ArrayList<Activity>(  );
        activities.add( a1 );

        List<Version> versions = VersionList.getLatestChanges( activities, null );

        assertThat( versions.size(), is( 2 ) );
        assertThat( versions.get( 0 ).getRevision(), is( 3 ) );
        assertThat( versions.get( 1 ).getRevision(), is( 3 ) );
    }

    @Test
    public void testGetChangesMultipleActivities() throws UnableToInitializeEntityException {
        Version v1 = Version.get( "model.h" );

        Version v11s = Mockito.spy( v1 );
        Mockito.doReturn( 1 ).when( v11s ).getRevision();

        Version v12s = Mockito.spy( v1 );
        Mockito.doReturn( 2 ).when( v12s ).getRevision();

        Version v13s = Mockito.spy( v1 );
        Mockito.doReturn( 3 ).when( v13s ).getRevision();

        Version v2 = Version.get( "common.h" );

        Version v21s = Mockito.spy( v2 );
        Mockito.doReturn( 1 ).when( v21s ).getRevision();

        Version v22s = Mockito.spy( v2 );
        Mockito.doReturn( 2 ).when( v22s ).getRevision();

        Version v23s = Mockito.spy( v2 );
        Mockito.doReturn( 3 ).when( v23s ).getRevision();

        Activity a1 = Activity.get( "act1@\\snade" );
        Activity a2 = Activity.get( "act2@\\snade" );
        a1.changeset.versions.add( v11s );
        a1.changeset.versions.add( v12s );
        a1.changeset.versions.add( v13s );

        a2.changeset.versions.add( v21s );
        a2.changeset.versions.add( v22s );
        a2.changeset.versions.add( v23s );

        List<Activity> activities = new ArrayList<Activity>(  );
        activities.add( a1 );
        activities.add( a2 );

        List<Version> versions = VersionList.getLatestChanges( activities, null );

        assertThat( versions.size(), is( 2 ) );
        assertThat( versions.get( 0 ).getRevision(), is( 3 ) );
        assertThat( versions.get( 1 ).getRevision(), is( 3 ) );
    }

    @Test
    public void testGetChangesMultipleActivitiesSameBranch() throws UnableToInitializeEntityException {
        Version v1 = Version.get( "model.h" );

        Version v11s = Mockito.spy( v1 );
        Mockito.doReturn( 1 ).when( v11s ).getRevision();
        Mockito.doReturn( "\\main\\one_int" ).when( v11s ).getBranch();

        Version v12s = Mockito.spy( v1 );
        Mockito.doReturn( 2 ).when( v12s ).getRevision();
        Mockito.doReturn( "\\main\\one_int\\one_dev" ).when( v12s ).getBranch();

        Version v13s = Mockito.spy( v1 );
        Mockito.doReturn( 3 ).when( v13s ).getRevision();
        Mockito.doReturn( "\\main\\one_int\\one_dev" ).when( v13s ).getBranch();

        Version v2 = Version.get( "common.h" );

        Version v21s = Mockito.spy( v2 );
        Mockito.doReturn( 1 ).when( v21s ).getRevision();
        Mockito.doReturn( "\\main\\one_int" ).when( v21s ).getBranch();

        Version v22s = Mockito.spy( v2 );
        Mockito.doReturn( 2 ).when( v22s ).getRevision();
        Mockito.doReturn( "\\main\\one_int" ).when( v22s ).getBranch();

        Version v23s = Mockito.spy( v2 );
        Mockito.doReturn( 3 ).when( v23s ).getRevision();
        Mockito.doReturn( "\\main\\one_int\\one_dev" ).when( v23s ).getBranch();

        Activity a1 = Activity.get( "act1@\\snade" );
        Activity a2 = Activity.get( "act2@\\snade" );
        a1.changeset.versions.add( v11s );
        a1.changeset.versions.add( v12s );
        a1.changeset.versions.add( v13s );

        a2.changeset.versions.add( v21s );
        a2.changeset.versions.add( v22s );
        a2.changeset.versions.add( v23s );

        List<Activity> activities = new ArrayList<Activity>(  );
        activities.add( a1 );
        activities.add( a2 );

        List<Version> versions = VersionList.getLatestChanges( activities, "\\main\\one_int" );

        assertThat( versions.size(), is( 2 ) );
        assertThat( versions.get( 0 ).getRevision(), is( 1 ) );
        assertThat( versions.get( 1 ).getRevision(), is( 2 ) );

        List<Version> versions2 = VersionList.getLatestChanges( activities, "\\main\\one_int\\one_dev" );

        assertThat( versions2.size(), is( 2 ) );
        assertThat( versions2.get( 0 ).getRevision(), is( 3 ) );
        assertThat( versions2.get( 1 ).getRevision(), is( 3 ) );
    }

    @Test
    public void testMatchBranchName() {
        String name = "^.*" + Cool.qfs + "snade.*$";
        System.out.println("NAME: " + name);
        String version = "\\main\\one_int\\snade";
        assertTrue( version.matches( name ) );
    }

    private void printVersions( List<Version> versions ) {
        for( Version v : versions ) {
            System.out.println( v.getFile() + ", revision " + v.getRevision() );
        }
    }
}
