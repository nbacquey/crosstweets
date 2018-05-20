/**
 * @author mnds
 * Checks the definition of words and poses them in the grid
 */
public class WordPoser implements Runnable {

	public static final String URIbase = "https://twitter.com/statuses/";
	private TweetManager tweetManager;
	private String tag;
	private String id;
	
	public WordPoser(TweetManager tweetManager, String word, String id) {
		this.tweetManager = tweetManager;
		this.tag = word;
		this.id = id;
	}
	
	public void run() {
		//System.out.println("Looking for definition of " + tag); 

		String wordDefinition = tweetManager.getDictionary().getDefinition(tag);
		// Testing if the definition is not empty... Words that have no definition are not added
		if( (wordDefinition != null) && (!wordDefinition.trim().isEmpty()) ) {
			synchronized( tweetManager ) {
				//System.out.println(tag + ":" + wordDefinition);

				Word w = new Word();
				w.setValue(tag);
				w.setDefinition(wordDefinition);
				w.setUrl(URIbase + id);

				tweetManager.getGrid().placeWord(w);
			}
			
		}
		else{
			//System.out.println(tag + ": not added");
		}

	}
}
