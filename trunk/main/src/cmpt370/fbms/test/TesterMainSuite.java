package cmpt370.fbms.test;

import java.util.Enumeration;

import junit.framework.TestFailure;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class TesterMainSuite
{

	public static void main(String[] args)
	{
		// Disable the internal logging of the MIME Magic library. We already log the exceptions
		// from it. We don't care about the internals.
		Logger.getLogger(net.sf.jmimemagic.Magic.class).setLevel(Level.OFF);
		Logger.getLogger(net.sf.jmimemagic.MagicParser.class).setLevel(Level.OFF);
		Logger.getLogger(net.sf.jmimemagic.MagicMatch.class).setLevel(Level.OFF);
		Logger.getLogger(net.sf.jmimemagic.MagicMatcher.class).setLevel(Level.OFF);
		Logger.getLogger(net.sf.jmimemagic.detectors.TextFileDetector.class).setLevel(Level.OFF);

		TestSuite ts = new TestSuite();
		TestResult result = new TestResult();
		ts.addTestSuite(TesterFileOp.class);
		ts.addTestSuite(TesterServices.class);
		ts.addTestSuite(TesterFileChangeHandler.class);
		ts.addTestSuite(TesterServices.class);
		ts.run(result);
		System.out.println("------\n" + "Test result\n" + "------");
		System.out.println("All tests succeed: " + result.wasSuccessful());
		Enumeration<TestFailure> failureList = result.failures();
		while(failureList.hasMoreElements())
		{
			TestFailure testFailure = (TestFailure) failureList.nextElement();
			System.out.println(testFailure);
		}

		System.out.println("The folder \"FileHandlerTest\" could not be automatically removed.\n"
				+ "Please remove it manually.\n" + "If not, it may affect next test.");
	}

}
