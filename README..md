AI Assignment

I have used thread pooling for the multithreaded aspect of the project.

In ServiceHandler I perform an initial search of the duck duck go page, this gives back 10 links/results, from there each of those links is passed in to the NodeSearcher to be parsed and scored. Each node is it's own thread in the thread pool.

In the NodeSearcher I used a regular queue rather than a priority queue. Documents are removed from the queue at random to be searched.

The body text of each node is passed into a word database. Here I check to see if the word is in the ignore words file then add it as a key to a hashMap and update its value(if it's already present).

I then check the links of the node from the queue for the search term, if they compare, connect to that child link and score it using the fuzzyHeuristic. If it's good, add to database, add the child to the queue.

The fuzzy logic file doesn't stray very far from the lab examples. There are 3 variable inputs, 'title', 'headings' and 'body' with one output, the 'relevance' of each variable(score). Each input variable can have one of 3 scores, 'poor', 'good' or 'excellent'. I used the center of gravity defuzzification technique. I have 6 inference rules. I think maybe this is where the project suffers mostly. I get results back on a search however they only sometimes seem relevant to the query.

I then compare each of the titles, headings and bodies of the child node to the search query, and add to the corresponding variable input score for every match.

After the thread times out the WordFrequency class calls the getSearchResults method on the database instance. The word cloud is then populated with the 20 most common words from the database.





