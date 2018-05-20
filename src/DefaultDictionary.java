import java.io.File;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import de.tudarmstadt.ukp.jwktl.JWKTL;
import de.tudarmstadt.ukp.jwktl.api.IWiktionaryEdition;
import de.tudarmstadt.ukp.jwktl.api.IWiktionaryEntry;
import de.tudarmstadt.ukp.jwktl.api.IWiktionaryPage;
import de.tudarmstadt.ukp.jwktl.api.IWiktionarySense;
import de.tudarmstadt.ukp.jwktl.api.PartOfSpeech;
import de.tudarmstadt.ukp.jwktl.api.util.Language;

/**
 * @author mnds
 * 
 * Default implementation of the IDictionary interface
 * Gets its data from Wiktionary, using the JWKTL API
 */
public class DefaultDictionary implements IDictionary{

	// Location of the raw Wiktionary sources
	public static String EN_WIKTIONARY_DUMP_PATH = "resources/en/enwiktionary-20180120-pages-articles.xml.bz2";
	public static String FR_WIKTIONARY_DUMP_PATH = "resources/fr/frwiktionary-20180201-pages-articles.xml.bz2";

	// Directory where Wiktionary generated files will be stored
	public static String EN_WIKTIONARY_DB_TARGET_DIRECTORY = "resources/en/generated";
	public static String FR_WIKTIONARY_DB_TARGET_DIRECTORY = "resources/fr/generated";

	// Enable (or deactivate) the overwrite option of the JWKTL parser
	public static Boolean OVERWRITE_EXISTING_DB = false;

	// Allows or deactivates case insensitive search
	public static Boolean CASE_INSENSITIVE_SEARCH = true; 

	public static File EN_wiktionaryDirectory;
	public static File FR_wiktionaryDirectory;

	// Allows accessing the Wiktionary database
	private IWiktionaryEdition ENwkt;
	private IWiktionaryEdition FRwkt;

	// Array for grammatical relevance, sorted by decreasing order
	private static final List<PartOfSpeech> grammaticalRelevance = Arrays.asList(
			PartOfSpeech.ACRONYM,
			PartOfSpeech.INITIALISM,
			PartOfSpeech.NOUN,
			PartOfSpeech.VERB,
			PartOfSpeech.ADJECTIVE,
			PartOfSpeech.ADVERB,
			PartOfSpeech.PROPER_NOUN
			);

	private DefaultDictionary() {
		ENwkt = JWKTL.openEdition(EN_wiktionaryDirectory);
		//FRwkt = JWKTL.openEdition(FR_wiktionaryDirectory);
	}

	public static DefaultDictionary getInstance(){
		initialize();
		return new DefaultDictionary();
	}


	public static void initialize() {
		EN_wiktionaryDirectory = new File(EN_WIKTIONARY_DB_TARGET_DIRECTORY);
		//FR_wiktionaryDirectory = new File(FR_WIKTIONARY_DB_TARGET_DIRECTORY);

		// Checking whether the database files have been already generated  
		if( !EN_wiktionaryDirectory.exists() ) {
			System.out.println("Unpacking english dictionary...");
			File dumpFile = new File(EN_WIKTIONARY_DUMP_PATH);
			try{
				JWKTL.parseWiktionaryDump(dumpFile, EN_wiktionaryDirectory, OVERWRITE_EXISTING_DB);
			}
			catch(Exception ex){ex.printStackTrace();}
			System.out.println("Dictionary unpacking finished. Resuming execution.");
		}

		/*if( !FR_wiktionaryDirectory.exists() ) {
			System.out.println("Unpacking le french dictionary...");
			File dumpFile = new File(FR_WIKTIONARY_DUMP_PATH);
			try{
				JWKTL.parseWiktionaryDump(dumpFile, FR_wiktionaryDirectory, OVERWRITE_EXISTING_DB);
			}
			catch(Exception ex){}
			System.out.println("Le dictionary unpacking finished. Resuming execution.");
		}*/
	}



	public String getDefinition(String word) {

		// For sorting entries relatively to grammatical relevance
		SortedSet<IWiktionaryEntry> entrySet = new TreeSet<IWiktionaryEntry>(new Comparator<IWiktionaryEntry>(){
			public int compare(IWiktionaryEntry arg0, IWiktionaryEntry arg1) {
				PartOfSpeech part0 = arg0.getPartOfSpeech(), part1 = arg1.getPartOfSpeech();
				int i0 = grammaticalRelevance.indexOf(part0), i1 = grammaticalRelevance.indexOf(part1);

				i0 = (i0 == -1) ? grammaticalRelevance.size() : i0;
				i1 = (i1 == -1) ? grammaticalRelevance.size() : i1;
				return i1 - i0;
			}				
		});

		List<IWiktionaryPage> pagesList = ENwkt.getPagesForWord( word, CASE_INSENSITIVE_SEARCH );

		for(IWiktionaryPage page : pagesList){
			for(IWiktionaryEntry entry : page.getEntries()){
				if(Language.ENGLISH.equals(entry.getWordLanguage())){
					entrySet.add(entry);
				}				
			}
		}

		if(entrySet.isEmpty())
			return null;

		IWiktionaryEntry selectedEntry = entrySet.last();
		IWiktionarySense selectedSense = null;

		for(IWiktionarySense sense : selectedEntry.getSenses()){
			if(!"".equals(sense.getGloss().getPlainText())){
				selectedSense = sense;
				break;
			}
		}

		if(selectedSense == null)
			return null;

		String htmlString = selectedSense.getGloss().getPlainText();
		return cleanHTML(htmlString);
	}

	// For processing specific html syntax (such as &eacute) back to normal text
	private String cleanHTML(String rawString){
		HTMLDocument doc = new HTMLDocument();
		try{
			new HTMLEditorKit().read( new StringReader( "<html><body>" + rawString ), doc, 0 );
			return doc.getText( 1, doc.getLength() );
		}
		catch(Exception e){
			return rawString;
		}
	}

	/*
	 * Destructor method used to close the database connection
	 */
	@Override
	public void finalize() {
		ENwkt.close();
		FRwkt.close();
	}

}
