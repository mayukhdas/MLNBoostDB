package edu.soic.Learning.Boosting;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import edu.soic.FOL.Atom;
import edu.soic.FOL.HornClause;
import edu.soic.FOL.Predicate;
import edu.soic.Util.Config;

/** 
 * Implementation of the MLN-Boost for MLN's
 * using in-memory RDB
 *
 * @author Marcin Malec
 * 
 * Last Modified: November 7, 2015
 */
public class MLNBoost {
	LearningProblem lp;

	// Mapping of related predicates through common types
	HashMap<Predicate, Set<Predicate>> relatedPreds 
	= new HashMap<Predicate, Set<Predicate>>();
	
	boolean allowRecursion;

	/**
	 * @param lp - the representation of the learning problem
	 * @return
	 */
	public MLN ClauseBoostForMLN(LearningProblem lp, boolean allowRecursion){
		this.allowRecursion = allowRecursion;
		
		this.lp = lp;

		MLN mln = new MLN(lp);

		if(Config.verbose){
			System.out.println("======================= SETTINGS =======================");
			System.out.println("-Query preds: " + lp.getQPreds());
			System.out.println("-Gradient steps: " + lp.getGradientSteps());
			System.out.println("-Clauses per gradient steps: " + lp.getClausesPerGS());
			System.out.println("-Max clause length: " + lp.getMaxClauseLen());
			System.out.println("-Max beam length: " + lp.getBeamLength());
			System.out.println("========================================================");
		}

		// Set up the related preds mapping
		for(Predicate p1 : lp.getPreds()){
			for(Predicate p2 : lp.getPreds()){
				if(p1.equals(p2) && !allowRecursion){
					continue;
				}
				if(!relatedPreds.containsKey(p1)){
					relatedPreds.put(p1, new HashSet<>());
				}
				relatedPreds.get(p1).add(p2);
			}
		}

		long totalTime = 0;

		//Iterate through m gradient steps
		for(int i = 0; i < lp.getGradientSteps(); ++i){
			long startTime = System.currentTimeMillis();

			//Iterate through target predicates
			for(Predicate target : lp.getQPreds()){

				for(int j = 0; j < lp.getClausesPerGS(); ++j){
					HornClause c = FitRelRegressionClause(target);
					mln.addClause(target, c);
				}
			}
			long elapsedTime = System.currentTimeMillis() - startTime;
			totalTime += elapsedTime;
			System.out.printf("Gradient %d, Elapsed time %d.\n", i, elapsedTime);
		}
		System.out.printf("Total Elapsed time %d.\n", totalTime);



		System.out.println("==================== MLN Learned: ======================");
		System.out.print(mln.getNiceString());
		System.out.println("========================================================");

		System.out.println("Generating WILL model");
		mln.generateWillModel();

		lp.getDB().close();
		return mln;
	}

	/**
	 * 
	 * @param s - training examples.
	 * @param p - the predicate.
	 * @param n - max length of a clause.
	 * @param b - max number of candidate clauses in the beam.
	 */
	private HornClause FitRelRegressionClause(Predicate p){

		int pArity = p.getArity();
		Integer[] argVars = new Integer[pArity];

		for(int i = 0; i < pArity; ++i){
			argVars[i] = i + 1;
		}

		HornClause initClause = new HornClause(new Atom(p, argVars), lp);
		HornClause bestClause = initClause;
		int numExpansions = 0;

		Beam beam = new Beam(lp.getBeamLength());
		beam.add(initClause);
		HashSet<Integer> evalClauses = new HashSet<Integer>();

		while(!beam.isEmpty()){
			HornClause current = beam.popFront();

			if(current.getSize() >= lp.getMaxClauseLen()){
				continue; //the clause cannot be expanded
			}

			HashSet<Atom> candLits = new HashSet<Atom>();

			for(Atom a : getCandidateLits(current)){
				int new_hash = current.hashCode(a);
				if(!evalClauses.contains(new_hash)){
					evalClauses.add(new_hash);
					candLits.add(a);
				}
			}

			if(Config.verbose){
				System.out.println("THE CURRENT CLAUSE: " + current);
				System.out.println("CANDIDATE LITERALS: " + candLits);
			}

			ArrayList<HornClause> newClauses = null;

			//			// If the clause was not yet stored - then do the
			//			// join partitioning scheme
			//			int hash = current.hashCode();
			//			if(!lp.getWasExpanded(current.hashCode())){
			//				lp.setWasExpanded(hash);
			//				//				TODO add indexes on K
			//				current.addIndexesON_K();
			//				newClauses = current.processPartitions(
			//						partitionCandLits(candLits, current), candLits.size());
			//			}
			//			else{
			newClauses = current.processCandLits(candLits);
			//			}

			for(HornClause nc : newClauses){
				//TODO
//				System.out.println(nc);
				
				if(nc.getScore() >= current.getScore() && nc.getPrecision() > 0){
					beam.add(nc);
				}

				if(nc.getScore() >= bestClause.getScore()){
					bestClause = nc;
				}
			}

			if(++numExpansions >= lp.getMaxExp()){
				return bestClause;
			}
		}
		return bestClause;
	}

	public ArrayList<Partition> partitionCandLits(HashSet<Atom> candLits, HornClause c){

		HashMap<String, Partition> partitions = new HashMap<>();

		//Group by the join column of the inner relation
		//Group by the outer relation
		//Group by the join column of the outer relation
		for(Atom a : candLits){
			Iterator<Integer> ai = a.getArgs().iterator();

			for(int i = 0; i < a.getArgs().size(); ++i){
				int arg = ai.next();
				if(c.getVars().contains(arg)){
					int var = arg;
					int pos = i + 1;
					String key = a.getPredicate().getName() + " "
							+ var + " "  + pos;

					if(!partitions.containsKey(key)){
						partitions.put(key, new Partition(var, pos, a.getPredicate().getName()));
					}
					partitions.get(key).add(a);
					break;
				}
			}

		}
		return new ArrayList<>(partitions.values());
	}

	public HashSet<Atom> getCandidateLits(HornClause c){

		//TODO modified to allow recursion
		HashSet<Predicate> preds = new HashSet<Predicate>();
		for(Predicate p : c.getPreds()){
			preds.addAll(relatedPreds.get(p));
		}
//		System.out.println("BEFORE: " + preds);
		if(allowRecursion){
			preds.remove(c.getHead().getPredicate());
		}
		else{
			preds.removeAll(c.getPreds());
		}
//		System.out.println("AFTER: " + preds);

		HashSet<Atom> candLits = new HashSet<>();

		HashMap<String,HashSet<Integer>> typeString2vars = new HashMap<>();
//		int highestVar = 0;

		for(Atom a : c.getAllAtoms()){
			Iterator<Integer> ai = a.getArgs().iterator();
			for(int i = 0; i < a.getPredicate().getArity(); ++i){
				String t = a.getPredicate().getType(i);

				if(!typeString2vars.containsKey(t)){
					typeString2vars.put(t, new HashSet<>());
				}

				Integer arg = ai.next();

//				if(arg > highestVar){
//					highestVar = arg;
//				}
				if(arg != null && arg > 0 ){
					typeString2vars.get(t).add(arg);
				}
			}
		}

		//Hacked up version - Improve in the future
		//TODO support for > binary predicates
		for(Predicate p : preds){
			//do case by case
			//In this case the predicate must match one of the existing variables
			if(p.getArity() == 1){
				for(int i : typeString2vars.get(p.getType(0))){
					candLits.add(new Atom(p, new Integer[]{i}));
				}
			}
			else if(p.getArity() == 2){				
				for(Integer[] mode : p.getModes()){
					if(mode[0] != Predicate.NEW && mode[1] != Predicate.NEW
							&& mode[0] != Predicate.CONST && mode[1] != Predicate.CONST){
						//both variables match
						if(typeString2vars.containsKey(p.getType(0)) &&
								typeString2vars.containsKey(p.getType(1)))
							for(int i : typeString2vars.get(p.getType(0))){
								for(int j : typeString2vars.get(p.getType(1))){
									candLits.add(new Atom(p, new Integer[]{i,j}));
								}
							}
					}
					if(mode[0] == Predicate.PLUS && mode[1] != Predicate.CONST){
						//1st variable matches
						if(typeString2vars.containsKey(p.getType(0)))
							for(int i : typeString2vars.get(p.getType(0))){
								candLits.add(new Atom(p, new Integer[]{i,null}));
							}
					}
					if(mode[0] != Predicate.CONST  && mode[1] == Predicate.PLUS){
						//2nd variable matches
						if(typeString2vars.containsKey(p.getType(1)))
							for(int i : typeString2vars.get(p.getType(1))){
								candLits.add(new Atom(p, new Integer[]{null, i}));
							}
					}

					//first constant
					if(mode[0] == Predicate.CONST){
						if(typeString2vars.containsKey(p.getType(1))){

							Set<Integer> s1 = lp.type2const2id.get(p.getType(0)).inverse().keySet();
							for(int i : s1){
								for(int j : typeString2vars.get(p.getType(1))){
									candLits.add(new Atom(p, new Integer[]{-i, j}));
								}
							}
						}
					}					
					//second constant
					else if(mode[1] == Predicate.CONST){
						if(typeString2vars.containsKey(p.getType(1))){

							Set<Integer> s2 = lp.type2const2id.get(p.getType(1)).inverse().keySet();
							for(int i : typeString2vars.get(p.getType(1))){
								for(int j : s2){
									candLits.add(new Atom(p, new Integer[]{i, -j}));
								}
							}
						}
					}
				}
			}
			else{
				System.out.println("TERNARY PREDICATES NO SUPPORTED YET");
			}
		}
		//TODO
		for(Atom a : c.getAllAtoms()){
			candLits.remove(a);
		}
//		System.out.println("CANDIDATES: " + candLits);
		return candLits;
	}
}