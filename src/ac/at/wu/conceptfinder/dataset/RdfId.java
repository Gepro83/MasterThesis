package ac.at.wu.conceptfinder.dataset;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/*
 * stores a DatasetID
 */
public final class RdfId implements Serializable{

	private static final long serialVersionUID = 8519768770288073492L;
	
	public RdfId(String ID){ m_ID = ID;	}
	public String value(){	return m_ID; }
	
	@Override
	public boolean equals(Object obj){
		if(obj == null) return false;
		if (!(obj instanceof RdfId))
	            return false;
	        if (obj == this)
	            return true;
	    RdfId ID = (RdfId) obj;
	    
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