package net.praqma.clearcase.test;

import net.praqma.logging.PraqmaticLogFormatter;
import net.praqma.util.test.junit.LoggingRule;
import net.praqma.util.test.junit.TestAnnouncer;
import org.junit.ClassRule;
import org.junit.Rule;

/**
 * @author cwolfgang
 */
public class BaseClearCaseTest {

    @ClassRule
    public static LoggingRule lrule = new LoggingRule( "net.praqma" ).setFormat( PraqmaticLogFormatter.TINY_FORMAT );

    @Rule
    public TestAnnouncer ta = new TestAnnouncer();
}
