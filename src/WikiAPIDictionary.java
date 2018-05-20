import java.io.IOException;

import org.wikipedia.Wiki;

public class WikiAPIDictionary implements IDictionary {

	public static final String FR_WIKI_URL = "fr.wiktionary.org";
	public static final String EN_WIKI_URL = "en.wiktionary.org";
	
	public Wiki FRWiki;
	Wiki ENWiki;
	
	private WikiAPIDictionary() {
		FRWiki = Wiki.createInstance(FR_WIKI_URL);
		ENWiki = Wiki.createInstance(EN_WIKI_URL);
	}
	
	public static void initialize() {
		
	}
	
	public static WikiAPIDictionary getInstance(){
		initialize();
		return new WikiAPIDictionary();
	}
	
	@Override
	public String getDefinition(String word) {
		String ret = null;
		try {
			ret = FRWiki.getRenderedText(word);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}

}
