package org.smartfrog.sfcore.languages.sf.constraints.propositions;


public class Exists extends Proposition {
	
	Boolean decide_prop(Boolean result, int rem) {
		if (result.booleanValue()) return result;  //failed...
		else if (rem==0) return new Boolean(false); //success...
		else return null; 
	}
}
