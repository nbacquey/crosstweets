import java.net.URI;
import java.net.URISyntaxException;
import java.util.Vector;

import twitter4j.JSONException;
import twitter4j.JSONObject;

public class Word {
	
	private static Word currentWord;

	String value;
	String definition;
	URI url;
	Vector<Pair<Integer,Integer>> positions;
	
	public Word(String value){
		this.value = value;
		this.definition = value;
		
		try {
			this.url = new URI("https://twitter.com");
		} catch (URISyntaxException e) {e.printStackTrace();}
		
		positions = new Vector <Pair<Integer,Integer>>();
	}
	
	public Word(){
		this("");
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getDefinition() {
		return definition;
	}

	public void setDefinition(String definition) {
		this.definition = definition.replace('\n', ' ');
	}

	public URI getUrl() {
		return url;
	}

	public void setUrl(String url) {
		try {
			this.url = new URI(url);
		} catch (URISyntaxException e) {e.printStackTrace();}
	}

	public Vector<Pair<Integer, Integer>> getPositions() {
		return positions;
	}

	public void addPosition(Pair<Integer, Integer> position) {
		positions.add(position);
	}
	
	public void clearPositions() {
		positions.clear();
	}
	
	public Pair<Integer,Integer> nextPosition(Pair<Integer,Integer> current){
		for(int i = 0; i < positions.size() -1; ++i)
			if(positions.get(i).equals(current))
				return positions.get(i+1);
		return null;
	}
	
	public boolean isHorizontal() {
		return positions.get(0).y == positions.get(1).y;
	}
	
	public static Word getCurrentWord(){
		return currentWord;
	}
	
	public static void setCurrentWord(Word word){
		currentWord = word;
	}
	
	public JSONObject toJsonObject() throws JSONException{
		JSONObject json = new JSONObject();
		json.put("value",this.value);
		json.put("definition", this.definition);
		json.put("URL", this.url.toString());
		json.put("position", this.positions.get(1).toString());
		//position starts at 1 because of the '@' character
		json.put("isHorizontal", this.isHorizontal());
		
		return json;
	}
	
	public static Word fromJsonObject(JSONObject json) throws JSONException{
		Word w = new Word();
		String value = json.getString("value");
		w.setValue(value);
		w.setDefinition(json.getString("definition"));
		w.setUrl(json.getString("URL"));
		
		boolean isHorizontal = Boolean.parseBoolean(json.getString("isHorizontal"));
		String posString = json.getString("position");
		posString = posString.substring(1, posString.length()-1);
		String[] pos = posString.split(",");
		
		Pair<Integer,Integer> initPos = new Pair<Integer,Integer>(Integer.parseInt(pos[0]),Integer.parseInt(pos[1]));
		for(int i = 0; i < value.length(); ++i) {
			int newX = isHorizontal ? initPos.x + i : initPos.x;
			int newY = isHorizontal ? initPos.y : initPos.y + i;
			w.addPosition(new Pair<Integer,Integer>(newX,newY));
		}
		
		return w;
	}
	
	public Word quasiClone() {
		Word ret = new Word(this.value);
		ret.url = this.url;
		ret.definition = this.definition;
		
		return ret;
	}

}
