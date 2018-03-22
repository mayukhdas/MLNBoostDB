package edu.soic.Learning.Boosting;

import java.util.ArrayList;

import edu.soic.FOL.Atom;

public class Partition {
	ArrayList<Atom> lits = new ArrayList<Atom>();
	public int var;
	public int argPos;
	public String atomName;
	
	public Partition(int var, int argPos, String atomName){
		this.var = var;
		this.argPos = argPos;
		this.atomName = atomName;
	}
	
	public void add(Atom a){
		lits.add(a);
	}
	
	public ArrayList<Atom> getLits(){
		return lits;
	}
	
	public Atom getFirstLit(){
		return lits.get(0);
	}
	
	public String toString(){
		return lits.toString();
	}
	
	public int size(){
		return lits.size();
	}
}
