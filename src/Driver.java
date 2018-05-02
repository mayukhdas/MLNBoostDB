import java.sql.SQLException;
import java.util.Arrays;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import edu.soic.Learning.Boosting.LearningProblem;
import edu.soic.Learning.Boosting.MLNBoost;
import edu.soic.Parse.CmdOpts;
import edu.soic.Parse.DataLoader;
import edu.soic.ParseWrapper.CmdOptsWrapper;

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
		CmdOptsWrapper cmd = new CmdOptsWrapper(args);
		String newArgs = null;
		if(cmd.trainIndicator)
		{
			newArgs = "-bk "+cmd.bkPathForDB+" -f "+cmd.evidencePathForDB+" -dt "+cmd.DB+" -allowRecursion yes "+" -o "+cmd.modelPath;
		}
		//System.out.println(Arrays.toString(newArgs.split(" ")));
		CmdOpts opts = new CmdOpts();
		CmdLineParser parser = new CmdLineParser(opts); 
		//args = newArgs.split(" ");
		args = new String[10];
		args[0] = "-i";
		args[1] = "./Datasets/Toy-Cancer/train/DB_bk.txt";
		args[2] = "-f";
		args[3] = "./Datasets/Toy-Cancer/train/train_factsDB.txt";
		args[4] = "-allowRecursion";
		args[5] = "true";
		args[6] = "-dt";
		args[7] = "hsqldb";
		args[8] = "-o";
		args[9] = "./Datasets/IMDB/model";
		try {
			//if(newArgs!=null)
			{
				parser.parseArgument(args);
				//Test loading the predicate definitions
				LearningProblem lp = new DataLoader().loadLearningProblem(opts);
				new MLNBoost().ClauseBoostForMLN(lp, opts.recursion.equals("true"));
			}
		} catch (CmdLineException e) {
			System.out.println("Errors encountered while processing commandline args:");
			e.printStackTrace();
			System.exit(1);
		}

		//Test loading the predicate definitions
		//LearningProblem lp = new DataLoader().loadLearningProblem(opts);
		//new MLNBoost().ClauseBoostForMLN(lp, opts.recursion.equals("true"));
	}
}
