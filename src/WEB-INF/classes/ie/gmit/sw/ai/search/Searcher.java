package ie.gmit.sw.ai.search;

import java.io.IOException;
import java.util.List;
import org.jsoup.nodes.Document;

/*	Interface for NodeSearcher	*/
public interface Searcher {
	public void search() throws IOException;
	public void insertWord(String text);
	public double getFuzzyHeuristic(Document doc);
	public List<String> processSearchTerms(String searchTerm);


}
