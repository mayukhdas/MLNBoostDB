package edu.soic.Learning.Boosting;
import com.google.common.collect.MinMaxPriorityQueue;

import edu.soic.FOL.HornClause;

public class Beam {
	private int maxSize;
	
	MinMaxPriorityQueue<HornClause> pq = MinMaxPriorityQueue.create();
	
	public Beam(int maxSize){
		this.maxSize = maxSize;
	}
	
	public boolean isEmpty(){
		return pq.isEmpty();
	}
	
	public void add(HornClause c){
		pq.add(c);
		
		if(pq.size() > maxSize){
			pq.removeLast();
		}
	}
	
	public HornClause popFront(){
		return pq.pollFirst();
	}
	
	public HornClause popBack(){
		return pq.removeLast();
	}
	
	public int getLength(){
		return pq.size();
	}
}
