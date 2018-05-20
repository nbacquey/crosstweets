import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import twitter4j.Status;
import twitter4j.StatusAdapter;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.util.function.Consumer;

public class CredentialsManager {

	private static volatile Integer waiting_status = 0;

	private static final int CRED_NUMBER = 3;
	private static final String[][] credentials = new String[CRED_NUMBER][4]; 

	private String consumerKey;
	private String consumerSecret;
	private String accessToken;
	private String accessTokenSecret;

	private CredentialsManager(int credNum) {
		consumerKey = credentials[credNum][0];
		consumerSecret = credentials[credNum][1];
		accessToken = credentials[credNum][2];
		accessTokenSecret = credentials[credNum][3];
	}
	
	private static void parseCredentials() throws FileNotFoundException, IOException {
		
		BufferedReader reader = new BufferedReader(new FileReader("credentials.txt"));
		
		for(int i = 0; i < CRED_NUMBER; ++i) 
			for(int j = 0; j < 4; ++j)
				credentials[i][j] = reader.readLine();
		
		reader.close();
		
	}

	private static boolean testTwitterStream(TwitterStream stream) {

		stream.onException(new Consumer<Exception>(){
			public void accept(Exception e) {
				stream.clearListeners();
				stream.cleanUp();
				stream.shutdown();
				CredentialsManager.waiting_status = -1;
				e.printStackTrace();
			}			
		});

		stream.addListener(new StatusAdapter(){
			@Override
			public void onStatus(Status s) {				
				stream.clearListeners();
				stream.cleanUp();
				stream.shutdown();
				CredentialsManager.waiting_status = 1;
			}
		});

		waiting_status = 0;
		stream.sample();

		while(waiting_status == 0) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1){}
		}

		return waiting_status == 1;
	}

	public static TwitterStream getTwitterStream() {
		
		TwitterStream ret = null;
		
		try {
			parseCredentials();			
		}catch(Exception e) {e.printStackTrace();}

		for(int i = 0; i < CRED_NUMBER ; ++i) {
			CredentialsManager cred = new CredentialsManager(i);
			ConfigurationBuilder cb = new ConfigurationBuilder();

			cb.setDebugEnabled(false);
			cb.setOAuthConsumerKey(cred.consumerKey);
			cb.setOAuthConsumerSecret(cred.consumerSecret);
			cb.setOAuthAccessToken(cred.accessToken);
			cb.setOAuthAccessTokenSecret(cred.accessTokenSecret);
			cb.setJSONStoreEnabled(true);
			TwitterStreamFactory tf = new TwitterStreamFactory(cb.build());

			ret = tf.getInstance();

			if(testTwitterStream(ret))
				break;
		}		

		return ret;
	}
}
