package edu.soic.Learning.Boosting;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

import edu.soic.Database.Database;
import edu.soic.FOL.Predicate;
import edu.soic.Util.Config;
import edu.soic.Util.Util;

/**
 * Representation of a learning problem
 * 
 * @author Marcin Malec
 * 
 * Last Modified: November 4, 2015
 */

public class LearningProblem {

	int cacheType = 0;
	String output_dir;

	//TODO
	int posCount = 0;

	// Mapping from type name to mapping of constant value to corresponding int ids
	public HashMap<String, BiMap<String, Integer>> type2const2id = new HashMap<>();

	private int gradientSteps = 20;
	private int clausesPerGS = 3;
	private int maxClauseLen = 3;
	private int beamLength = 10;
	private int maxExp = 10;

	// Predicate's name to predicate object
	private HashMap<String, Predicate> name2preds = new HashMap<>();

	// Query predicates
	private HashSet<Predicate> qPreds = new HashSet<>();

	// Database handle
	private Database db = new Database();

	// HornClauses that are stored in DB
	HashSet<Integer> isInDB = new HashSet<>();

	// HornClauses that were expanded
	HashSet<Integer> wasExpanded = new HashSet<>();

	public HashSet<Integer> storedEvid = new HashSet<>();

	public LearningProblem(){
		db.setup();
	}

	public boolean HCinDB(int hash){
		return isInDB.contains(hash);
	}

	public void markHCinDB(int hash){
		isInDB.add(hash);
	}

	public void addPredicate(String name, Predicate p, boolean isQuery){
		name2preds.put(name, p);

		//=== store the type info in the db
		for(int i = 0; i < p.getArity(); ++i){
			String tname = p.getType(i) ;
			if(!type2const2id.containsKey(tname)){
				String stmt = "create MEMORY table type_" + tname
						+ "(ID int, PRIMARY KEY (ID))";
				getDB().execute(stmt);
				type2const2id.put(tname, HashBiMap.create());
			}
		}
		//=== store atom info in the database
		StringBuilder query = new StringBuilder();
		StringBuilder pk_query = new StringBuilder();
		StringBuilder ref_query = new StringBuilder();
		ref_query.append(", foreign key (arg0) references type_" + p.getType(0)); 

		query.append("create MEMORY table atom_" + p.getName() + "(");

		if(isQuery){
			qPreds.add(p);
			query.append("TRUTH TINYINT DEFAULT 0, PSUM DOUBLE DEFAULT " 
					+ Config.PSUM_INIT + ", G DOUBLE,");
		}

		query.append("arg0 int");
		pk_query.append(",PRIMARY KEY(arg0");
		for(int i = 1; i < p.getArity(); ++i){
			query.append(", arg" + i + " INT");
			pk_query.append(", arg" + i);

			ref_query.append(", foreign key (arg" + i
					+ ") references type_" + p.getType(i) + "(ID)");
		}

		getDB().execute(query.toString() + pk_query.toString() + ")" + ref_query.toString() + ");");

		//Add indexes to the table
		for(int i = 0; i < p.getArity(); ++i){
			getDB().execute("CREATE INDEX " +  p.getName() + "arg" + i + "idx ON atom_"
					+ p.getName() + " (arg" + i + ");");
		}

		//Add the index on the truth value
		if(isQuery){
			getDB().execute("CREATE INDEX " +  p.getName() + "truth" + "idx ON atom_"
					+ p.getName() + " (truth);");
		}
	}

	public void addQueryPredicate(String pName){
		qPreds.add(name2preds.get(pName));
	}

	public void addEvidence(String pname, String[] args){
		Predicate p = getPred(pname);
		int[] argsint = new int[p.getArity()];

		for(int i = 0; i < p.getArity(); ++i){

			String key_type = p.getType(i);
			String key_constant = args[i];

			BiMap<String, Integer> constant2id = type2const2id.get(key_type);
			if(!constant2id.containsKey(key_constant)){
				int id = constant2id.size();
				constant2id.put(key_constant, id);
				String q = "Insert into type_" + key_type
						+ " Values(" + id + ")";
				getDB().execute(q);
			}
			argsint[i] = constant2id.get(key_constant);
		}

		//get hash of the fact
		HashFunction hf = Hashing.goodFastHash(1);
		Hasher h = hf.newHasher();
		h.putInt(p.getName().hashCode());

		for(int arg : argsint){
			h.putInt(arg);
		}

		int hash = h.hash().asInt();

		if(!storedEvid.contains(hash)){
			storedEvid.add(hash);
		}
		else{
			return;
		}
		StringBuffer query = new StringBuffer();
		query.append("Insert into atom_" + pname + "(");

		boolean isQuery = qPreds.contains(p);
		if(isQuery){
			++posCount;
			query.append("truth,");
		}

		for(int i = 0; i < p.getArity(); ++i){
			if(i != 0){
				query.append(",");
			}
			query.append("arg" + i);
		}

		query.append(") ");
		query.append("Values(" + (isQuery ? "1,":"") + Util.intArrayToString(argsint) + ");");

		getDB().execute(query.toString());
	}

	public void generateNegatives(String posNegRatio){
		String limit = "";
		if(posNegRatio != null){
			limit = " limit " + (int)(posCount * Double.parseDouble(posNegRatio));
			System.out.println("Generating " + posCount * Double.parseDouble(posNegRatio) + " negatives");
		}

		for(Predicate p : qPreds){
			String insertInto = "atom_" + p.getName();

			if(posNegRatio != null){
				insertInto = "sample";
				StringBuilder sb = new StringBuilder();
				sb.append("CREATE CACHED TABLE sample(" );
				for(int i = 0; i < p.getArity(); ++i){
					if(i != 0){
						sb.append(",");
					}
					sb.append("arg" + i + " INT ");
				}
				sb.append(")");
				db.execute(sb.toString());
			}
			StringBuffer query = new StringBuffer();
			StringBuffer temp = new StringBuffer();
			query.append("Insert into " + insertInto + "(");
			for(int i = 0; i < p.getArity(); ++i){
				if(i != 0){
					query.append(",");
					temp.append(",");
				}
				query.append("arg" + i);
				temp.append("arg" + i);
			}
			query.append(") Select * FROM ");

			for(int i = 0; i < p.getArity(); ++i){
				if(i != 0){
					query.append(" cross join ");
				}
				String id = String.valueOf((char)('A' + i));
				query.append("type_" + p.getType(i) + " " + id);
			}
			query.append(" EXCEPT Select " + temp.toString() + " FROM atom_" + p.getName());
			db.execute(query.toString());

			if(posNegRatio != null){
				db.execute("Insert into atom_" + p.getName() + "(" + temp.toString() + ") " +
						"Select " + temp.toString() + " FROM sample ORDER by RAND()" + limit);
				db.execute("DROP TABLE sample");
			}

			//update the initial gradient
			getDB().execute("Update atom_" + p.getName() + " SET G = truth - (1.0 / (1.0 + exp(-PSUM)))");
		}
	}

	public void computeGroundCount(){
		int groundCount = 1;
		for(Predicate p : qPreds){
			for(int i = 0; i < p.getArity(); ++i){
				groundCount *= type2const2id.get(p.getType(i)).size();
				p.setGroundCount(groundCount);
			}
		}
	}
	//  =======  GETTERS  =======  \\

	public int getCacheType(){
		return cacheType;
	}

	public void setCacheType(int type){
		cacheType = type;
	}

	public Predicate getPred(String name){
		return name2preds.get(name);
	}

	public Database getDB(){
		return db;
	}

	public Collection<Predicate> getPreds(){
		return name2preds.values();
	}

	public Collection<Predicate> getQPreds(){
		return qPreds;
	}

	public int getGradientSteps() {
		return gradientSteps;
	}

	public void setGradientSteps(int gradientSteps) {
		this.gradientSteps = gradientSteps;
	}

	public int getClausesPerGS() {
		return clausesPerGS;
	}

	public void setClausesPerGS(int clausesPerGS) {
		this.clausesPerGS = clausesPerGS;
	}

	public int getMaxClauseLen() {
		return maxClauseLen;
	}

	public void setMaxClauseLen(int maxClauseLen) {
		this.maxClauseLen = maxClauseLen;
	}

	public int getBeamLength() {
		return beamLength;
	}

	public void setBeamLength(int beamLength) {
		this.beamLength = beamLength;
	}

	public void setOutputDir(String outDir){
		this.output_dir = outDir;
	}

	public boolean getWasExpanded(int id) {
		return wasExpanded.contains(id);
	}

	public void setWasExpanded(int id) {
		this.wasExpanded.add(id);
	}

	public int getMaxExp() {
		return maxExp;
	}

	public void setMaxExp(int maxExp) {
		this.maxExp = maxExp;
	}

}