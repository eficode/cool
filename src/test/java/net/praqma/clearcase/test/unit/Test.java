package net.praqma.clearcase.test.unit;

import net.praqma.cli.Report;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author cwolfgang
 */
public class Test {

    @org.junit.Test
    public void test() {
        double d = Report.getPercentage( 1, 10, 1 );
        System.out.println( d );
        assertThat( d, is( 10.0 ) );
    }

    @org.junit.Test
    public void test1() {
        double d = Report.getPercentage( 1, 3, 10 );
        System.out.println( d );
        assertThat( d, is( 33.3 ) );
    }

    @org.junit.Test
    public void test3() {
        double d = Report.getPercentage( 1, 3, 1000 );
        System.out.println( d );
        assertThat( d, is( 33.333 ) );
    }
}
