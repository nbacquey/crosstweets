import java.text.Normalizer;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class TweetManager {
	
	private static final int MINIMUM_WORD_LENGTH = 3;

	private Collection<String> tags;
	private Crosswords grid;
	private IDictionary dictionary;
	private ExecutorService executor;
	
	public TweetManager(){
		tags = new HashSet<String>();
		grid = new Crosswords();
		dictionary = DefaultDictionary.getInstance();
		executor = Executors.newCachedThreadPool();
	}
	
	
	public void processHashtag(String tag, String id){
		String upperTag = Normalizer.normalize(tag,Normalizer.Form.NFD).toUpperCase();
		upperTag = upperTag.replaceAll("[^\\p{ASCII}]", "");

		//Quick fix to avoid 2-letter words, which often have an inconsistent definition and just clog up the grid
		if(upperTag.length() < MINIMUM_WORD_LENGTH)
			return;
		
		if(isWord(upperTag) && tags.add(upperTag)){
			// Looking concurrently for the definition of words
			executor.execute( new WordPoser(this, upperTag, id) ); 
		}
	}
	
	public boolean isWord(String word){
		for(Character c : word.toCharArray())
			if(!Character.isLetter(c))
				return false;
		return true;
	}
	
	public synchronized Crosswords getGrid(){
		return grid;
	}
	
	public synchronized void setGrid(Crosswords grid) {
		this.grid = grid;
	}
	
	public Collection<String> getTags() {
		return tags;
	}

	public IDictionary getDictionary() {
		return dictionary;
	}
	
	public void addTag(String upperTag) {
		tags.add(upperTag);
	}
	
}
