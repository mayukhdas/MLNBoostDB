package edu.soic.FOL;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Representation of a FOL predicate.
 * 
 * @author Marcin Malec
 * 
 * Last Modified: November 5, 2015
 */

public class Predicate {
	/*
	 * Mode definitions
	 */
	public static final int PLUS = 0;
	public static final int MINUS = 1;
	public static final int NEW = 2;
	public static final int CONST = 3;

	ArrayList<Integer[]> modes = new ArrayList<>();

	/** Predicate's name */
	String name;

	/** Predicate's argument count */
	int arity;

	/** Predicate's argument types */
	String[] types;	

	Integer groundCount = 0;
	
	public Predicate(String name, int arity){
		this.name = name;
		this.arity = arity;
		types = new String[arity];
	}

	/** @return arity of this predicate i.e. argument count */
	public int getArity(){
		return arity;
	}

	/** @return Type object at position @param i */
	public String getType(int i){
		return types[i];
	}

	public String[] getTypes(){
		return types;
	}

	/** Sets Type object at position @param i to type object @param t */
	public void setType(int i, String t){
		types[i] = t;
	}

	public String getName(){
		return name;
	}

	public void addMode(String[] s_mode){
		Integer[] mode = new Integer[arity];

		for(int i = 0; i < arity; ++i){
			String val = s_mode[i];
			if(val.equals("+")){
				mode[i] = PLUS;
			}
			else if(val.equals("-")){
				mode[i] = MINUS;
			}
			else if(val.equals("#")){
				mode[i] = CONST;
			}
			else if(val.equals("_")){
				mode[i] = NEW;
			}
		}
		modes.add(mode);
	}

	public ArrayList<Integer[]> getModes() {
		return modes;
	}

	public Integer getGroundCount() {
		return groundCount;
	}

	public void setGroundCount(Integer groundCount) {
		this.groundCount = groundCount;
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
		Predicate other = (Predicate) obj;
		if (arity != other.arity)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (!Arrays.equals(types, other.types))
			return false;
		return true;
	}

	/** @see Object#hashCode() */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + arity;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + Arrays.hashCode(types);
		return result;
	}

	/** @see Object#toString() */
	@Override
	public String toString() {
		return "Predicate [name=" + name + ", arity=" + arity + ", types="
				+ Arrays.toString(types) + "]";
	}
}