package ac.at.wu.conceptfinder.application;

import java.util.Comparator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class Globals {

	public static final int MAX_DISAMBIGUATION_SCORE = 1000;
	public static final int MAX_RELEVANCE_SCORE = 1000;
	public static final int MAX_COHERENCE_SCORE = 1000;
	
	public static final int MAX_BABELFYCALLS = 50;
	public static final String KEYWORDS_MARKER = "~kyws~";
	
	public Globals() {
		// TODO Auto-generated constructor stub
	}
	
	/*
	 * Sort a map by its values
	 */
	public static <K,V extends Comparable<? super V>>
	SortedSet<Map.Entry<K,V>> entriesSortedByValues(Map<K,V> map) {
	    SortedSet<Map.Entry<K,V>> sortedEntries = new TreeSet<Map.Entry<K,V>>(
	        new Comparator<Map.Entry<K,V>>() {
	            @Override public int compare(Map.Entry<K,V> e1, Map.Entry<K,V> e2) {
	                int res = e1.getValue().compareTo(e2.getValue());
	                return res != 0 ? res : 1;
	            }
	        }
	    );
	    sortedEntries.addAll(map.entrySet());
	    return sortedEntries;
	}

}
