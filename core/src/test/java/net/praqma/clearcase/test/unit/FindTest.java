package net.praqma.clearcase.test.unit;

import net.praqma.clearcase.Find;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author cwolfgang
 */
public class FindTest {

    @Test
    public void simple() {
        Find find = new Find();

        assertThat( find.getCommandLine(), is( "find" ) );
    }

    @Test
    public void findAll() {
        Find find = new Find().setFindAll();

        assertThat( find.getCommandLine(), is( "find -all -visible" ) );
    }

    @Test
    public void findAllNVisible() {
        Find find = new Find().setFindAll().setNotVisible();

        assertThat( find.getCommandLine(), is( "find -all -nvisible" ) );
    }

    @Test
    public void findAllPathName() {
        Find find = new Find().setFindAll().addPathName( "praqma" );

        assertThat( find.getCommandLine(), is( "find praqma -all -visible" ) );
    }

    @Test
    public void findAllPathNamePrint() {
        Find find = new Find().setFindAll().addPathName( "." ).print();

        assertThat( find.getCommandLine(), is( "find . -all -visible -print" ) );
    }
}
