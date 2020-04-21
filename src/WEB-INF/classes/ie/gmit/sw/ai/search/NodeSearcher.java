package ie.gmit.sw.ai.search;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import ie.gmit.sw.ai.search.WordDatabase;
import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.FunctionBlock;
import net.sourceforge.jFuzzyLogic.rule.Variable;

/*	Ths class searches DDG for the search term, adds the results of that search
*	to a word database and calculates a fuzzy score
*/
public class NodeSearcher implements Runnable, Searcher {
	private File fuzzyFile; // The fcl file used for the fuzzy heuristic
	private String url; // The url we want to connect to
	private String link; // The absolute url of the edges
	private static int MAX = 30; // The max number of nodes to visit
	private int score; // The fuzzy score of a node
	private int highRelevance = 0; // The fuzzy variables scores
	private int mediumRelevance = 0;
	private int lowRelevance = 0;
	private double defuzzifiedValue = 0; // The defuzzified scoring
	private WordDatabase wordDatabase = WordDatabase.getInstance(); // Singleton

	private List<Document> queue = new ArrayList<Document>(); // list of nodes to visit
	private List<String> closedList = new ArrayList<String>(); // list of nodes already visited
	private List<String> searchList = new ArrayList<String>(); // List of initial search terms

	// Takes in the fuzzy file, the url and the query
	public NodeSearcher(File file, String url, String term) {
		this.fuzzyFile = file;
		this.url = url;
		this.searchList = processSearchTerms(term);
	}

	@Override
	public void run() {
		// System.out.println("Beginning search for " + this.term);
		try {
			// Connect to the document at the url given
			Document doc = Jsoup.connect(this.url).get();
			// Add the document node to the queue
			queue.add(doc);
			// Begin the search
			search();
		} catch (Exception exception) {
			exception.printStackTrace();
		}

	}

	/*	Search the web for the term */
	@Override
	public void search() throws IOException {
		// While queue isn't empty and we haven't gotten to MAX
		while (!queue.isEmpty() && closedList.size() <= MAX) {
			// Remove a document at random from the queue
			Document doc = getRandomElement(queue);
			// A link on the document
			Elements edges = doc.select("a[href]");
			// System.out.println(doc.body().text());
			// Add the body text to the word database
			insertWord(doc.body().text());

			// For each element in edges get teh absolute url and set it as 'link'
			for (Element e : edges) {
				link = e.absUrl("href");

				if (link != null && !closedList.contains(link) && closedList.size() <= MAX) {
					// check if each term in searchList contains the term.
					// If true, try and create a new connection to the absolute URL
					for (String term : searchList) {
						if (link.contains(term)) {
							// Child element from link
							Document child = Jsoup.connect(link).get();
							// If the fuzzy score of the child is good, give body text to insertWord
							// Add the link to the closedlist and the child to the queue
							if (getFuzzyHeuristic(child) >= score) {
								insertWord(child.body().text());

								closedList.add(link);
								queue.add(child);
							}
						}
					}
				}
			}

		}

	}
	/*	Removes and returns a random element from the queue	*/
	public Document getRandomElement(List<Document> queue) {
		Random rand = new Random();
		return queue.remove(rand.nextInt(queue.size()));
	}

	/*	Inserts text to the database	*/
	@Override
	public void insertWord(String text) {
		// \\W just gets words except alphanumerical ones
		String[] words = text.split("\\W+");

		// For every word in the array, add to the database and trim( remove whitespace etc.)
		for (String word : words) {
			wordDatabase.addToMap(word.trim());
		}
	}

	/*	This method calculates the fuzzy value of the title, heading and body of the document (child url)
	*	being passed into it.
	*/
	@Override
	public double getFuzzyHeuristic(Document doc) {
		// Load and parse the FCL file
		FIS fis = FIS.load(fuzzyFile.getAbsolutePath(), true);
		// Get the function block from the file
		FunctionBlock fb = fis.getFunctionBlock("relevanceRate");
		// Create heading and bosy elements out of what's on the doc
		Elements heading = doc.select("h1");	//	can add h2, h3 etc
		Elements body = doc.select("p");

		// For every term in the searchList, check if the title element contains it
		// If it does, add to highRelevance
		// Do the same for the headings and body but with 'mediumScore' and 'lowScore'
		// respectively
		for (String term : searchList) {
			if (doc.title().toString().contains(term)) {
				highRelevance++;
			}
		}
		// Checking for each element in heading as well as each term in search
		for (Element h : heading) {
			for (String term : searchList) {
				if (h.toString().contains(term)) {
					mediumRelevance++;
				}
			}
		}
		for (Element p : body) {
			for (String term : searchList) {
				if (p.toString().contains(term)) {
					lowRelevance++;
				}
			}
		}
		// Set inputs
		fis.setVariable("title", highRelevance);
		fis.setVariable("headings", mediumRelevance);
		fis.setVariable("body", lowRelevance);

		// Evaluate
		fis.evaluate();
		//	Set output variable
		Variable variable = fb.getVariable("relevance");
		//	Set defuzzified value
		defuzzifiedValue = variable.getLatestDefuzzifiedValue();
		
		// Return the defuzzified value of variable
		return defuzzifiedValue;
	}
	/*	Process the search term
	*/
	@Override
	public List<String> processSearchTerms(String initialTerm) {
		List<String> processedSearchTerms = new ArrayList<String>();
		String[] words = initialTerm.split("\\W+");

		//	compares every word in words to the list of ignore words
		//	in the word database
		for (String word : words) {
			// Dont ignore the word
			if (!wordDatabase.ignore().contains(word)) {
				// Add it to the searc terms
				processedSearchTerms.add(word.trim());
			}
		}

		// Return processedSearchTerms
		return processedSearchTerms;
	}

}
