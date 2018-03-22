import java.sql.SQLException;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import edu.soic.Learning.Boosting.LearningProblem;
import edu.soic.Learning.Boosting.MLNBoost;
import edu.soic.Parse.CmdOpts;
import edu.soic.Parse.DataLoader;

public class Driver {

	/**
	 * Driver class
	 * 
	 * @author Marcin Malec
	 * 
	 * Last Modified: July 6, 2015
	 * @throws SQLException 
	 */

	public static void main(String[] args){
		CmdOpts opts = new CmdOpts();
		CmdLineParser parser = new CmdLineParser(opts); 

		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {
			System.out.println("Errors encountered while processing commandline args:");
			e.printStackTrace();
			System.exit(1);
		}

		//Test loading the predicate definitions
		LearningProblem lp = new DataLoader().loadLearningProblem(opts);
		new MLNBoost().ClauseBoostForMLN(lp, opts.recursion.equals("true"));
	}
}
