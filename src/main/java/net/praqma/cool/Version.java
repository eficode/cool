/**
 * 
 */
package net.praqma.cool;

import edu.umd.cs.findbugs.annotations.*;

/**
 * @author jssu
 * 
 */
@SuppressFBWarnings("")
public class Version  {
	private static final String major    = "0"; // buildnumber.major
	private static final String minor    = "2"; // buildnumber.minor
	private static final String patch    = "1"; // buildnumber.patch
	private static final String sequence = "277"; // buildnumber.sequence
	
	public static final  String version  = major + '.' + minor + '.' + patch + '.' + sequence;
}
