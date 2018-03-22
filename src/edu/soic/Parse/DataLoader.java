package edu.soic.Parse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;

import edu.soic.Database.Database;
import edu.soic.Learning.Boosting.LearningProblem;
import edu.soic.Parse.BK.BK_Parser;
import edu.soic.Parse.BK.ParseException;
import edu.soic.Parse.Facts.Facts_Parser;

/**
 * Reads in the bk and facts files that are in input
 * command line arguments
 * 
 * Uses two javacc parsers:
 * (1) BK_Parser - for parsing background knowledge (BK) file
 * (2) Facts_Parser - for parsing facts file
 * 
 * @author Marcin Malec
 * 
 * Last Modified: November 4, 2015
 */
public class DataLoader {

	/** The learning problem formulation */
	private LearningProblem lp;

	public LearningProblem loadLearningProblem(CmdOpts opts){
		try {
			Database.DB_type = opts.dt;
			Reader bkFile = new InputStreamReader(new FileInputStream(new File(opts.fBK)));
			BK_Parser bkp = new BK_Parser(bkFile);
			bkp.BKFile();
			lp = bkp.getLP();

			Reader factsFile = new InputStreamReader(new FileInputStream(new File(opts.ffacts))); 
			Facts_Parser fp = new Facts_Parser(factsFile);
			fp.factFile(lp);
			if(opts.caching != null)
				lp.setCacheType(Integer.parseInt(opts.caching));
			lp.generateNegatives(opts.posNegRatio);
			lp.computeGroundCount();
			lp.setOutputDir(opts.fOut);

		} catch (FileNotFoundException | ParseException | edu.soic.Parse.Facts.ParseException e) {
			e.printStackTrace();
		}
		return lp;
	}
}