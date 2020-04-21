package ie.gmit.sw.ai.search;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import ie.gmit.sw.ai.cloud.WordFrequency;

/*	Have all searches populate this database	
 * 	Singleton design pattern
 */
public class WordDatabase implements WordDB {

	private static WordDatabase database = null;
	private WordFrequency[] wf = new WordFrequency[20];
	private ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<String, Integer>();
	private List<WordFrequency> mostCommonWords = new ArrayList<WordFrequency>();
	private Set<String> ignoreSet = new ConcurrentSkipListSet<String>();
	private BufferedReader reader = null;
	private String line;
	private String myWord;
	private int value = 0;

	private WordDatabase() {

	}

	public static WordDatabase getInstance() {
		if (database == null) {
			database = new WordDatabase();
		}
		return database;
	}

	/*	This method adds the given word to the map */
	@Override
	public void addToMap(String word) {
		//	This is just managing the casing. Makes first index of substring capital
		//	then concats the rest onto it
		myWord = word.substring(0, 1).toUpperCase().concat(word.substring(1));

		//	If the set doesn't contain the word given and map does
		if (!ignoreSet.contains(myWord) && map.containsKey(myWord)) {
			//	Get value for myWord
			value = map.get(myWord);
			//	Increment it
			value++;
			//	Map the word given to the value count
			map.put(myWord, value);
		} else {
			//	Or add it for first time
			map.put(word, 1);
		}
	}

	/*	Returns the word frequency array to populate the word cloud	*/
	@Override
	public WordFrequency[] getSearchResults() {
		//	For each of the mappings contained in this map
		this.map.entrySet().forEach(entry -> {
			mostCommonWords.add(new WordFrequency(entry.getKey(), entry.getValue()));
		});
		for (int i = 0; i < 20; i++) {
			wf[i] = mostCommonWords.get(i);
		}
		return wf;
	}

	/*	Empty the map and the most common words list	*/
	@Override
	public void empty() {
		map.clear();
		mostCommonWords.clear();
	}

	/*	Used for ignoring words from the file given	*/
	@Override
	public void ignoreWordsFromFile(File fileIgnore) throws IOException {
		try {
			reader = new BufferedReader(new FileReader(fileIgnore));

			while ((line = reader.readLine()) != null) {
				ignoreSet.add(line.substring(0, 1).toUpperCase().concat(line.substring(1)));
			}
		} catch (FileNotFoundException fileNotFoundException) {
			fileNotFoundException.printStackTrace();
		}
	}
	/*	Method to organise thread terms	*/
	public Set<String> ignore() {
		return ignoreSet;
	}


}
