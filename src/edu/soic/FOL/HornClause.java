package edu.soic.FOL;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.TreeSet;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

import edu.soic.Learning.Boosting.LearningProblem;
import edu.soic.Util.Config;
import edu.soic.Util.DBTablePrinter;


/**
 * cancer(1)
 * Friends(1,2)
 * smokes(2)
 */

/**
 * Representation of a FOL clause
 * 
 * @author Marcin Malec
 * 
 * Last Modified: November 7, 2015
 */

public class HornClause implements Comparable<HornClause>{
	// Unique id that identifies the clause 
	Integer table_id = null;

	// The head of this horn clause
	Atom head;

	// The precision of this clause
	double precision;

	/* The body of the clause,
	 * The ordered list is chosen to prevent
	 * the classes with the same atoms but different order
	 * to be stored in the db.
	 */
	TreeSet<Atom> body = new TreeSet<>();

	// Variables in this clause
	LinkedHashSet<Integer> vars = new LinkedHashSet<Integer>();

	// Score of this clause
	double score;

	// weight (significance) of this clause
	double weight;	

	StringBuilder joinString = new StringBuilder();

	// Representation of the learning problem
	final LearningProblem lp;

	final static double max_precision = .975;

	/** Default constructor:
	 *  (1) Horn clause is assumed to be initiated with a head
	 *  (2) Non constants are stored, for easier unification later
	 *  (3) The hc is retrieved from DB if already processed before, and if not
	 *  	it is processed and then stored. */
	public HornClause(Atom head, LearningProblem lp){
		// set atom head
		this.head = head;

		for(int i : head.args){
			if(i > 0){
				vars.add(i);
			}
		}

		this.lp = lp;
		
		if(table_id == null){
			table_id = hashCode();
		}
		
		if(!lp.HCinDB(table_id)){
			//Set up a new table
			setupDBtable();
			lp.markHCinDB(table_id);
		}

		//This is a target --- > set score to -inf
		this.score = Double.NEGATIVE_INFINITY;
	}

	/** Copy constructor */
	public HornClause(HornClause hc){
		head = hc.head;
		body = new TreeSet<>(hc.body);
		vars = new LinkedHashSet<Integer>(hc.vars);
		lp = hc.lp;
		table_id = hc.table_id;
		joinString = new StringBuilder(hc.joinString);
	}

	/**
	 * The goal is to compute weight and score of the clause
	 * (1) Compute the precision of the clause, and if it is too high,
	 * 		then stop there, and set the score to very low value.
	 * (2) Compute the weight of the clause by doing inner join.
	 * (3) Compute the score of the clause by doing outer join.
	 */
	public void calcWeightAndScore(){

		try {
			Predicate p = head.getPredicate();

			// Compute and deal with precision
			ResultSet rs = 
					lp.getDB().executSelectQuery("Select count(*) FROM " + getClauseTableName() + "_1");
			rs.next();

			precision = ((double) rs.getInt(1)) / (p.getGroundCount() + .1);


			if(precision > max_precision || precision == 0){
				score = Double.NEGATIVE_INFINITY;
				return;
			}

			// Compute the weight:

			// get the join conditions:
			String delimiter1 = "";
			String delimiter2 = "";
			StringBuffer on = new StringBuffer();
			StringBuffer where = new StringBuffer();


			for(int i = 0; i < head.args.size(); ++i){
				on.append(delimiter1);
				where.append(delimiter2);

				int arg = head.args.get(i);
				// the argument is a variable
				if(head.args.get(i) > 0){
					on.append(String.format("var%d = arg%d", arg, i));
					delimiter1 = " AND ";
				}
				// the argument is a constant
				else{
					where.append(String.format("arg%d = %d", i, -arg));
					delimiter2 = " AND ";
				}
			}

			rs = lp.getDB().executSelectQuery(
					String.format("Select sum(G*SAT)/sum(SAT*SAT) FROM %s_1 inner join atom_%s on %s %s",
							getClauseTableName(), p.getName(), on.toString(), (where.length() > 0 ? "where " + where.toString() : "")));

			rs.next();
			weight = rs.getDouble(1);

			// Compute the score:
			rs= lp.getDB().executSelectQuery(
					String.format("Select sum(Power((IFNULL(SAT,0) * %f - G), 2)) FROM %s_1 full outer join atom_%s on %s %s",
							weight, getClauseTableName(), p.getName(), on.toString(), (where.length() > 0 ? "where " + where.toString() : "")));
			rs.next();
			score = -rs.getDouble(1);

		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public int compareTo(HornClause o) {
		return Double.compare(score, o.score);
	}

	/** adds @param l - literal to the body of this horn clause. */
	public void expandBody(Atom a){
		String old_table_name = getClauseTableName();
		//		HashSet<Integer> new_vars = new HashSet<Integer>();

		//add the literal to the body of the clause and deal with the variables
		body.add(a);
		//		for(Integer i : a.args){
		//			if(i == null){
		//				int newVar = vars.size() + 1;
		//				new_vars.add(newVar);
		//			}
		//			else if(i > 0)
		//				if(!vars.contains(i)){
		//					new_vars.add(i);
		//				}
		//		}

		table_id = hashCode();

		if(!lp.HCinDB(table_id)){
			lp.markHCinDB(table_id);

			/* ============================= Create the K view
				Create view id_0(insertVars_k) AS 
					select selectVars from previous K join new atom on insertOn where insertWhere
			   ===============================================
			 */
			//			StringBuilder insertVars_k = new StringBuilder();
			StringBuilder selectVars_k = new StringBuilder();
			StringBuilder insertOn = new StringBuilder();
			StringBuilder insertWhere = new StringBuilder();

			String sep = "";
			for(int var : vars){
				//				insertVars_k.append(sep);
				selectVars_k.append(sep);
				//				insertVars_k.append("var" + var);
				selectVars_k.append("var" + var);
				sep = ",";
			}

			for(int i = 0 ; i < a.getArgs().size(); ++i){
				Integer arg = a.getArgs().get(i);
				if(arg == null){
					arg = vars.size() + 1;
					vars.add(arg);

					//					insertVars_k.append(sep);
					selectVars_k.append(sep);

					//					insertVars_k.append("var" + arg);
					selectVars_k.append("arg" + i + " AS var" + arg);
					sep = ",";
				}

				else if(arg > 0){
					if(insertOn.length() > 0){
						insertOn.append(" AND ");
					}
					insertOn.append("arg" + i + "=var" + arg);
				}
				else{
					if(insertWhere.length() > 0){
						insertWhere.append(" AND ");
					}
					insertWhere.append("arg" + i + "=" + -arg);
				}
			}

			//Create the I table
			StringBuilder qvars_i = new StringBuilder();
			StringBuilder insertVars_i = new StringBuilder();

			sep = "";
			for(int pos = 0;  pos < head.getPredicate().getArity(); ++pos){
				int arg = head.getArgs().get(pos);
				if(arg > 0){
					qvars_i.append(sep);
					insertVars_i.append(sep);
					qvars_i.append("var" + arg + " INT ");
					insertVars_i.append("var" + arg);
					sep = ",";
				}
			}

			lp.getDB().execute("create MEMORY table "  + getClauseTableName() + "_1(sat INT, " + qvars_i.toString() + ")");

			insertOn = new StringBuilder();
			insertWhere = new StringBuilder();

			for(int pos = 0; pos < a.getPredicate().getArity(); ++pos){
				Integer arg = a.getArgs().get(pos);
				if(arg != null){
					if(arg > 0){
						if(insertOn.length() > 0){
							insertOn.append(" AND ");
						}
						insertOn.append("arg" + pos + "=var" + arg);
					}
					else{
						if(insertWhere.length() > 0){
							insertOn.append(" AND ");
						}
						insertWhere.append("arg" + pos + "=" + -arg);
					}
				}
			}

			String q = "";

			joinString = new StringBuilder(
					String.format("(Select %s FROM %s %s join atom_%s %s %s)",
							selectVars_k.toString(),
							joinString.length() == 0 ? "ROOT" : "(" + joinString.toString() + ")",
									(insertOn.length() > 0 ? "":"cross"),
									a.getPredicate().getName(),
									(insertOn.length() > 0 ? (" on " + insertOn.toString()):""),
									(insertWhere.length() > 0 ? (" where " + insertWhere.toString()):""))
					);

			
			String source = joinString.toString().replaceFirst("ROOT", old_table_name + "_1");
			
			// Populate the new I set
			q = "Insert into " + getClauseTableName() + "_1 "
					+ " Select count(*), " + insertVars_i.toString()
					+ " FROM " + source + " GROUP BY " + insertVars_i.toString()
					+ ";";
			
			lp.getDB().execute(q);

			if(Config.debug){
				System.out.println("======= The atom table ");
				DBTablePrinter.printResultSet(
						lp.getDB().executSelectQuery(
								" Select * "
										+ " FROM " + "atom_" + a.getPredicate().getName()
								)
						);
			}			
		}

		calcWeightAndScore();
	}

	/** @return the list of all atoms of this horn clause */
	public ArrayList<Atom> getAllAtoms(){
		ArrayList<Atom> allAtoms = new ArrayList<>();
		allAtoms.add(head);
		allAtoms.addAll(body);

		return allAtoms;
	}

	public String getClauseTableName(){
		return ("c_" + (table_id < 0 ? ("n" + (-table_id)) : table_id));
	}

	/** @return the head of this horn clause */
	public Atom getHead(){
		return head;
	}

	public HashSet<Predicate> getPreds(){
		HashSet<Predicate> preds = new HashSet<>();
		preds.add(head.getPredicate());

		for(Atom a : body){
			preds.add(a.p);
		}

		return preds;
	}

	public double getScore(){
		return score;
	}

	/** @return number of literals in this clause. */
	public int getSize(){
		return body.size() + 1;
	}

	public HashSet<Integer> getVars(){
		return vars;
	}

	public double getWeight(){
		return weight;
	}

	/**
	 * @see Object#hashCode()
	 * provides unique hash for this horn clause
	 * that takes into account all the atoms and their
	 * assignments. Does not take into account the weight
	 * of the clause
	 * 
	 * @return hash of this horn clause
	 */
	@Override
	public int hashCode() {
		HashFunction hf = Hashing.goodFastHash(1);
		Hasher h = hf.newHasher();

		h.putInt(head.hashCode());

		for(Atom a : body){
			h.putInt(a.hashCode());
		}

		return h.hash().asInt();
	}

	public int hashCode(Atom new_a){
		HashFunction hf = Hashing.goodFastHash(1);
		Hasher h = hf.newHasher();

		h.putInt(head.hashCode());
		TreeSet<Atom> body = new TreeSet<>(this.body);

		body.add(new_a);
		for(Atom a : body){
			h.putInt(a.hashCode());
		}

		return h.hash().asInt();
	}

	public void setScore(double score){
		this.score = score;
	}

	public double getPrecision() {
		return precision;
	}

	/**
	 * Should be only called only from the constructor.
	 * 
	 * If not done already it sets up the database 
	 * using the head of this clause.
	 */
	private void setupDBtable(){
		Iterator<Integer> it = head.getArgs().iterator();

		StringBuilder insertCols = new StringBuilder();
		StringBuilder insertVars = new StringBuilder();
		StringBuilder insertWhere = new StringBuilder();

		Predicate p = head.getPredicate();

		for(int arg = 0; arg < p.getArity(); ++arg){
			int next = it.next();

			if(next > 0){
				if(insertCols.length() > 0){
					insertVars.append(",");
					insertCols.append(",");
				}

				insertVars.append(" var" + next);
				insertCols.append("arg" + arg);
			}
			else{
				if(insertWhere.length() > 0){
					insertWhere.append(" AND");
				}
				insertWhere.append(String.format("arg%d=%d", arg,  -next));
			}
		}

		lp.getDB().execute(String.format("create view %s_1 (%s) AS Select %s FROM atom_%s %s",
				getClauseTableName(), insertVars.toString(),
				insertCols.toString(), p.getName(), 
				(insertWhere.length() > 0 ? (" where " + insertWhere.toString()):"")));
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	/** @see Object#toString() */
	@Override
	public String toString() {
		return "head:[" +  head + "], body:" + body + ", weight="
				+ weight + ", score= " + score + ", precision = " + precision;
	}

	public String getPrologString(){
		StringBuilder sb = new StringBuilder();

		Predicate p = head.getPredicate();
		sb.append("(");
		sb.append(head.getPredicate().getName() + "(");

		String separator = "";
		for(int i = 0; i < p.getArity(); ++i){
			Integer arg = head.getArgs().get(i);
			sb.append(separator);
			if(arg == null){
				sb.append("_");
			}
			else{
				sb.append(
						arg > 0 ? String.valueOf((char) ('A' + (arg) - 1))
								: lp.type2const2id.get(p.getType(i)).inverse().get(arg)
						);
			}
			separator = ", ";
		}
		sb.append(", " + weight + ") :- ");

		separator = "";
		for(Atom a : body){
			sb.append(separator);

			p = a.getPredicate();

			String separator2 = "";
			sb.append(p.getName() + "(");

			for(int i = 0; i < p.getArity(); ++i){
				sb.append(separator2);
				Integer arg = a.getArgs().get(i);
				if(arg == null){
					sb.append("_");
				}
				else{
					sb.append(
							arg > 0 ? String.valueOf((char) ('A' + (arg) - 1))
									: lp.type2const2id.get(p.getType(i)).inverse().get(arg)

							);
				}
				separator2 = ", ";
			}
			separator = ", ";
			sb.append(")");
		}
		sb.append(").");

		return sb.toString();
	}

	/** Evaluates the given candidate literals 
	 * @param candLits
	 * @return
	 */
	public ArrayList<HornClause> processCandLits(HashSet<Atom> candLits){
		ArrayList<HornClause> newClauses = new ArrayList<>(candLits.size());

		for(Atom a : candLits){
			HornClause nc = new HornClause(this);
			nc.expandBody(a);
			newClauses.add(nc);
		}

		return newClauses;
	}
}