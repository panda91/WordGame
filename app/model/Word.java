package model;

import java.io.Serializable;
import java.util.List;

public class Word implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String word;
	private List<String> dependents;
	
	public String getWord() {
		return word;
	}
	public void setWord(String word) {
		this.word = word;
	}
	public List<String> getDependents() {
		return dependents;
	}
	public void setDependents(List<String> dependents) {
		this.dependents = dependents;
	}

}
