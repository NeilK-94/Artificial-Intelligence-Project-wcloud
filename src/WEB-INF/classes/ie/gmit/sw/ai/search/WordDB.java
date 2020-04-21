package ie.gmit.sw.ai.search;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import ie.gmit.sw.ai.cloud.WordFrequency;

/*	Interface for WordDatabase	*/
public interface WordDB {
	abstract public void addToMap(String word);
	abstract public WordFrequency[] getSearchResults();
	abstract public void empty();
	abstract public void ignoreWordsFromFile(File fileIgnore) throws IOException;
}
