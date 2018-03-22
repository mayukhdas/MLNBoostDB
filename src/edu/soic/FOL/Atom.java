package edu.soic.FOL;

import java.util.ArrayList;
import java.util.Iterator;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

/**
 * Representation of a FOL atom
 * 
 * @author Marcin Malec
 * 
 * Last Modified: November 7, 2015
 */

public class Atom implements Comparable<Atom>{

	// Predicate associated with this atom
	Predicate p;

	/* 	Atom's arguments: 
	 *  Positive integers correspond to variables
	 *  and negative integers correspond to constants */
	ArrayList<Integer> args;

	//	HashSet<Integer> vars = new HashSet<>();
	/**
	 * Constructs new Atom:
	 * @param p - atom's predicate
	 * @param integers - atom's arguments
	 */
	public Atom(Predicate p, Integer[] integers){
		this.p = p;
		this.args = new ArrayList<>(p.arity);

		for(Integer arg : integers){
			//			if(arg > 0){
			//				vars.add(arg);
			//			}
			this.args.add(arg);
		}
	}

	public Predicate getPredicate(){
		return p;
	}

	public ArrayList<Integer> getArgs(){
		return args;
	}

	//	public boolean containsVar(int var){
	//		return vars.contains(var);
	//	}

	public boolean containsArg(int arg){
		return args.contains(arg);
	}

	/** @see Object#equals(Object) */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Atom other = (Atom) obj;

		if(args.size() != other.args.size()){
			return false;
		}

		Iterator<Integer> i1 = args.iterator();
		Iterator<Integer> i2 = other.args.iterator();

		for(int i = 0; i < args.size(); ++i){
			Integer ai1 = i1.next();
			Integer ai2 = i2.next();
			if(ai1 == null || ai2 ==null){
				if(ai1 != null || ai2 != null){
					return false;
				}
			}
			else if(!ai1.equals(ai2)){
				return false;
			}
		}
		if (p == null) {
			if (other.p != null)
				return false;
		} else if (!p.equals(other.p))
			return false;
		return true;
	}

	/** @see Object#toString() */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer(p.getName() + "(");

		String separator = "";
		for(Integer arg : args){
			sb.append(separator);

			if(arg == null){
				sb.append("_");
			}
			else{
				sb.append(
						arg > 0 ? String.valueOf((char) ('A' + (arg) - 1))
								: arg
						);
			}
			separator = ", ";
		}
		sb.append(")");
		return sb.toString();
	}

	/** @see Object#hashCode() */
	@Override
	public int hashCode() {
		HashFunction hf = Hashing.goodFastHash(1);
		Hasher h = hf.newHasher();
		h.putInt(p.hashCode());

		for(Integer arg : args){
			if(arg == null){
				//TODO:might cause conflict
				h.putInt(100);
			}
			else{
				h.putInt(arg);
			}
		}

		return h.hash().asInt();
	}

	@Override
	public int compareTo(Atom other) {
		return p.toString().compareTo(other.toString());
	}
}