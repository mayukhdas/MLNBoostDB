import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Arrays;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import edu.soic.Learning.Boosting.LearningProblem;
import edu.soic.Learning.Boosting.MLNBoost;
import edu.soic.Parse.CmdOpts;
import edu.soic.Parse.DataLoader;
import edu.soic.ParseWrapper.CmdOptsWrapper;
import edu.wisc.cs.will.Utils.Utils;

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
		String[] oldArgs = args;
		if(cmd.trainIndicator)
		{
			//newArgs = "-bk "+cmd.bkPathForDB+" -f "+cmd.evidencePathForDB+" -dt "+cmd.DB+" -allowRecursion true "+" -o "+cmd.modelPath;
		
			//System.out.println(Arrays.toString(newArgs.split(" ")));

			//args = newArgs.split(" ");
			//String[] newArgsArr = newArgs.split(" ");
			//args = new String[newArgsArr.length];
			args = new String[10];
			args[0] = "-i";
			args[1] = cmd.bkPathForDB;// "./Datasets/Toy-Cancer/train/DB_bk.txt";
			args[2] = "-f";
			args[3] = cmd.evidencePathForDB; //"./Datasets/Toy-Cancer/train/train_factsDB.txt";
			args[4] = "-allowRecursion";
			args[5] = "true";
			args[6] = "-dt";
			args[7] = cmd.DB; //"hsqldb";
			args[8] = "-o";
			args[9] = cmd.modelPath; //"./Datasets/Toy-Cancer/train/model";
			/*for(int i=0;i<newArgsArr.length;i++)
			{
				args[i] = newArgsArr[i].intern();
			}*/
			//System.out.println(Arrays.toString(args));
			CmdOpts opts = new CmdOpts();
			CmdLineParser parser = new CmdLineParser(opts); 
			try {
				//if(newArgs!=null)
				{
					parser.parseArgument(args);
					//Test loading the predicate definitions
					LearningProblem lp = new DataLoader().loadLearningProblem(opts);
					new MLNBoost().ClauseBoostForMLN(lp, opts.recursion.equals("true"));
				}
			} catch (Exception e) {
				System.out.println("Errors encountered while processing commandline args:");
				e.printStackTrace();
				System.exit(1);
			}
		}
		
		if(cmd.testIndicator)
		{
			String command = "java -jar ./v1-0.jar -i -test "+cmd.testPath+" -model "+cmd.modelPath+" -target "+cmd.target
					+" -aucJarPath "+ cmd.aucJarDir+" -trees "+cmd.trees;
			try {
				Utils.println("\n% Running command: " + command); // See http://mark.goadrich.com/programs/AUC/
				Process     p  = Runtime.getRuntime().exec(command);
				InputStream inputStream = p.getInputStream();
				InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
				BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

				String line;
				while ((line = bufferedReader.readLine()) != null)
				{
				    System.out.println(line);
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		//Test loading the predicate definitions
		//LearningProblem lp = new DataLoader().loadLearningProblem(opts);
		//new MLNBoost().ClauseBoostForMLN(lp, opts.recursion.equals("true"));
	}
}
