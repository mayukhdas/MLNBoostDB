package edu.soic.Learning.Boosting;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;

import edu.soic.FOL.Atom;
import edu.soic.FOL.HornClause;
import edu.soic.FOL.Predicate;
import edu.soic.Util.Config;

public class MLN {
	ArrayList<HornClause> clauses = new ArrayList<>();
	HashMap<Predicate, HashSet<HornClause>> pred2clauses = new HashMap<>();
	LearningProblem lp;

	/** Initializes initial model - with no expert information */
	public MLN(LearningProblem lp){
		this.lp = lp;
	}

	//TODO: right now assuming single target
	public void addClause(Predicate target, HornClause c){
//		DBTablePrinter.printResultSet(lp.getDB().executSelectQuery("Select * from " + "atom_smokes"));
//
//		DBTablePrinter.printResultSet(lp.getDB().executSelectQuery("Select * from " + c.getClauseTableName() + "_1"));
//		DBTablePrinter.printResultSet(lp.getDB().executSelectQuery("Select * from " + c.getClauseTableName() + "_2"));
		
		clauses.add(c);

		Atom head = c.getHead();

		// --- UPDATE THE Probability SUM
		StringBuilder query = new StringBuilder();

		query.append("UPDATE atom_" + target.getName() + " SET "
				+ "PSUM = ISNULL((Select  (PSUM + SAT * " + c.getWeight() + ") from "
				+ c.getClauseTableName() + "_1" + " where ");

		Iterator<Integer> arg = head.getArgs().iterator();
		for(int i = 0; i < target.getArity(); ++i){
			int headarg = arg.next();

			if(i != 0){
				query.append(" AND ");
			}
			query.append("arg" + i + "=" + (headarg > 0 ? "var":"const") + Math.abs(headarg));
		}
		query.append("), PSUM)");
		lp.getDB().execute(query.toString());

		// --- UPDATE THE GRADIENT VALUE
		lp.getDB().execute("Update atom_" + target.getName() + " SET G = truth - (1.0 / (1.0 + exp(-PSUM)))");
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(HornClause c : clauses){
			sb.append(c + "\n");
		}
		return sb.toString();
	}

	public String getNiceString(){
		HashMap<Integer, Double> clauses2weight = new HashMap<>();

		for(HornClause c : clauses){
			if(clauses2weight.containsKey(c.hashCode())){
				clauses2weight.put(c.hashCode(),
						clauses2weight.get(c.hashCode()) + c.getWeight());
			}
			else{
				clauses2weight.put(c.hashCode(), c.getWeight());
			}
		}
		HashSet<Integer> clausesPrinted = new HashSet<>();
		StringBuilder sb = new StringBuilder();
		for(HornClause hc : clauses){
			if(!clausesPrinted.contains(hc.hashCode())){
				clausesPrinted.add(hc.hashCode());
				HornClause t = new HornClause(hc);
				t.setWeight(clauses2weight.get(hc.hashCode()));
				sb.append(t.toString() + "\n");
			}
		}

		return sb.toString();
	}

	//TODO assumes single target, and one clause per gradient step
	public void generateWillModel(){
		String root = lp.output_dir + "/bRDNs";

		try {
			File f = new File(root);
			if(f.exists()){
				FileUtils.cleanDirectory(new File(root));
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 

		new File(root).mkdirs();

		Predicate target = lp.getQPreds().iterator().next();

		String model_file = root + "/" + target.getName() + ".model";

		try {
			PrintWriter writer = new PrintWriter(model_file);
			writer.println(lp.getGradientSteps());
			writer.println(target.getName());
			ArrayList<Double> weights = new ArrayList<Double>();
			for(int i = 0; i < lp.getGradientSteps(); ++i) {weights.add(1.0);}
			writer.println(weights);
			writer.println(Config.PSUM_INIT);
			writer.println(target.getName());
			writer.close();

			root += "/Trees";
			new File(root).mkdirs();

			//do the trees 1 by 1
			for(int i = 0; i < clauses.size(); ++i){
				String treeFile = root + "/" + target.getName() + "Tree" + i + ".tree";
				writer = new PrintWriter(treeFile);
				writer.println("setParam: stringsAreCaseSensitive = true.");
				writer.println();
				writer.println("usePrologVariables: true.");
				writer.println();
				writer.println();
				writer.println(clauses.get(i).getPrologString());
				writer.close();
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}