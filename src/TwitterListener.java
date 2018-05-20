
import java.util.Iterator;

import twitter4j.JSONArray;
import twitter4j.JSONException;
import twitter4j.JSONObject;
import twitter4j.Status;
import twitter4j.StatusAdapter;
import twitter4j.TwitterStream;
import twitter4j.TwitterObjectFactory;


public class TwitterListener extends StatusAdapter {

	protected final TweetManager manager;
	boolean stop = false;
	TwitterStream twitterStream;

	public TwitterListener(TweetManager manager) {
		super();
		this.manager = manager;
	}

	public void stop(TwitterStream twitterStream) {
		this.twitterStream = twitterStream;
		stop = true;		
	}

	/**
	 * See https://dev.twitter.com/docs/platform-objects/tweets to get the JSon
	 * format of a tweet.
	 */
	@Override
	public void onStatus(Status status) {
		if(stop){
			System.out.println("Shutting down connection...");
			twitterStream.shutdown();
			twitterStream.cleanUp();
			System.out.println("Terminated");
			return;
		}
		String statusJson = TwitterObjectFactory.getRawJSON(status);
		JSONObject tweet;
		try {
			tweet = new JSONObject(statusJson);
			String id = extractID(tweet);
			work(tweet,id);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	protected void work(Object json, String id) throws JSONException {
		
		if (json instanceof JSONObject) {
			work((JSONObject) json, id);
			return;
		}
		if (json instanceof JSONArray) {
			work((JSONArray) json, id);
			return;
		}

	}

	protected void work(JSONArray json, String id) throws JSONException {
		for (int i = 0; i < json.length(); i++) {
			work(json.get(i), id);
		}
	}

	protected void work(JSONObject json, String id) throws JSONException {
		Iterator<?> keys = json.keys();
		
		while (keys.hasNext()) {

			String key = (String) keys.next();

			Object value = json.get(key);

			if("hashtags".equals(key)){
				extractHashtags((JSONArray)value, id);
			}
			else{
				work(value, id);
			}
		}

	}
	
	protected void extractHashtags(JSONArray tags, String id) throws JSONException {
		for (int i = 0; i < tags.length(); ++i) {
			JSONObject tag = (JSONObject) tags.get(i);
			manager.processHashtag((String)tag.get("text"), id);
		}
	}
	
	protected String extractID(JSONObject tweet) {
		String ret = "";
		try {
			ret = (String) tweet.get("id_str");
		} catch (JSONException e) {e.printStackTrace();}
		return ret;
	}

}
