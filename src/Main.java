import java.io.IOException;

import javax.swing.SwingUtilities;

import twitter4j.TwitterException;


public class Main {



	public static void main(String[] args) throws TwitterException, IOException {

		DefaultDictionary.initialize();
		
		final CrosswordFrame mainFrame = new CrosswordFrame();

		SwingUtilities.invokeLater(new Runnable(){
	        public void run(){
	        	mainFrame.setVisible(true);
	        }
	    });
				

	}

}
