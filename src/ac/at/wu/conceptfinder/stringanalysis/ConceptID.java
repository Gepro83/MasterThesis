package ac.at.wu.conceptfinder.stringanalysis;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import ac.at.wu.conceptfinder.dataset.RdfId;

public final class ConceptID implements Serializable {
	
	private static final long serialVersionUID = 2804586414281656929L;

	public ConceptID(String ID){
		m_ID = ID;
	}
	
	public String value(){
		return m_ID;
	}
	
	public String toString(){
		return m_ID;
	}
		
	@Override
	public boolean equals(Object obj){
		if(obj == null) return false;
		if (!(obj instanceof ConceptID))
	            return false;
	        if (obj == this)
	            return true;
	    ConceptID ID = (ConceptID) obj;
	    
	    return new EqualsBuilder().
	    		append(m_ID, ID.value()).
	    		isEquals();
		 
	}
	
	@Override
	public int hashCode(){
		return new HashCodeBuilder(17, 31).
				append(m_ID).
				toHashCode();
	}
	
	private final String m_ID;

}
