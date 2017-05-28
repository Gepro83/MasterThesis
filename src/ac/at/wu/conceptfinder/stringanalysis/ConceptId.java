package ac.at.wu.conceptfinder.stringanalysis;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public final class ConceptId implements Serializable {
	
	private static final long serialVersionUID = 2804586414281656929L;

	public ConceptId(String ID){
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
		if (!(obj instanceof ConceptId))
	            return false;
	        if (obj == this)
	            return true;
	    ConceptId ID = (ConceptId) obj;
	    
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
