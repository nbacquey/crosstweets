import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.util.Scanner;
import java.util.Timer;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.ToolTipManager;

import twitter4j.FilterQuery;
import twitter4j.JSONArray;
import twitter4j.JSONObject;
import twitter4j.TwitterStream;

import java.util.TimerTask;

public class CrosswordFrame extends JFrame {


	private static final long serialVersionUID = 1L;
	private static final String INITIAL_WORDS = "#INRIA\n#startup";
	private static final boolean SHOW_PACK_BUTTON = false;

	private static final long INITIAL_TIME = 5000;
	private static final long REFRESH_TIME = 5000;

	private static TwitterStream twitterStream = null;
	
	private TweetManager manager;

	GridPanel gridPanel;
	JScrollPane scroll;
	JTextArea wordDefinitionArea;
	JButton linkButton;
	JButton validateButton;
	JTextArea buzzArea;
	JButton saveButton;
	JButton loadButton;

	JFileChooser fileChooser;

	boolean visibility;

	public CrosswordFrame(){
		super("Linking Dynamic Tweets");		

		visibility = false;

		fileChooser = new JFileChooser();

		this.setLayout(new BorderLayout());

		gridPanel = new GridPanel(new Crosswords(), this, visibility);
		gridPanel.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent arg0) {
				updateWord();			
			}
		});

		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new FlowLayout());
		rightPanel.setPreferredSize(new Dimension(200,600));

		scroll = new JScrollPane(gridPanel);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);	
		scroll.setPreferredSize(new Dimension(800,800));
		this.add(scroll,BorderLayout.CENTER);

		JPanel wordPanel = new JPanel();
		wordPanel.setLayout(new BoxLayout(wordPanel, BoxLayout.PAGE_AXIS));
		wordPanel.setBorder(BorderFactory.createTitledBorder("Selected word"));

		wordDefinitionArea = new JTextArea("Definition",4,15);
		wordDefinitionArea.setLineWrap(true);
		wordDefinitionArea.setWrapStyleWord(true);
		wordDefinitionArea.setEditable(false);	

		JScrollPane scrollText = new JScrollPane(wordDefinitionArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS ,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollText.setPreferredSize(new Dimension(180,100));

		wordPanel.add(scrollText);

		JPanel btnPanel = new JPanel();
		btnPanel.setLayout(new GridLayout(0,1,0,2));
		btnPanel.setMaximumSize(new Dimension(160,50));

		linkButton = new JButton("View source");
		linkButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		linkButton.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		linkButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				if (Desktop.isDesktopSupported()) {
					Desktop desktop = Desktop.getDesktop();
					try {
						URI uri = Word.getCurrentWord().getUrl();
						desktop.browse(uri);
					} catch (Exception e) {e.printStackTrace();}
				}
			}			
		});
		linkButton.setEnabled(false);
		btnPanel.add(linkButton);

		validateButton = new JButton("Validate word");
		validateButton.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		validateButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0){
				gridPanel.validateWord();
			}
		});
		validateButton.setEnabled(false);
		btnPanel.add(validateButton);

		wordPanel.add(btnPanel);

		rightPanel.add(wordPanel);

		JPanel generationPanel = new JPanel();
		generationPanel.setLayout(new BoxLayout(generationPanel, BoxLayout.PAGE_AXIS));
		generationPanel.setBorder(BorderFactory.createTitledBorder("Crosswords generation"));

		buzzArea = new JTextArea(INITIAL_WORDS);
		JScrollPane scrollBuzz = new JScrollPane(buzzArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS ,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollBuzz.setPreferredSize(new Dimension(180,60));
		generationPanel.add(scrollBuzz);

		
		JPanel genBtnPanel = new JPanel();
		genBtnPanel.setLayout(new GridLayout(0,1,0,2));
		
		JButton generateButton = new JButton("New Twitter request");
		generateButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				generateNewGrid();
			}			
		});
		generateButton.setPreferredSize(new Dimension(150,25));
		generateButton.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		genBtnPanel.add(generateButton);
		
		JButton clearButton = new JButton("Clear grid");
		clearButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				clearGrid();
			}			
		});
		clearButton.setMinimumSize(new Dimension(150,25));
		clearButton.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		genBtnPanel.add(clearButton);
		
		generationPanel.add(genBtnPanel);

		rightPanel.add(generationPanel);

		JCheckBox showButton = new JCheckBox("Show all",false);
		showButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt) {
				JCheckBox btn = (JCheckBox) evt.getSource();
				visibility = btn.isSelected();
				updateVisibility();
			}			
		});
		rightPanel.add(showButton);

		JPanel filePanel = new JPanel();
		filePanel.setLayout(new GridLayout(0,1,0,2));
		filePanel.setBorder(BorderFactory.createTitledBorder("File management"));
		filePanel.setPreferredSize(new Dimension(160,80));

		saveButton = new JButton("Save to file");
		saveButton.setPreferredSize(new Dimension(150,25));
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
					File selectedFile = fileChooser.getSelectedFile();
					saveToFile(selectedFile);
				}
			}
		});
		filePanel.add(saveButton);
		saveButton.setEnabled(false);

		loadButton = new JButton("Load from file");
		loadButton.setPreferredSize(new Dimension(150,25));
		loadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					File selectedFile = fileChooser.getSelectedFile();
					loadFromFile(selectedFile);
				}
			}
		});
		filePanel.add(loadButton);

		rightPanel.add(filePanel);

		JButton packBtn = new JButton("Pack grid");
		packBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				packGrid();
			}
			
		});
		if(SHOW_PACK_BUTTON)
			rightPanel.add(packBtn);

		this.add(rightPanel,BorderLayout.EAST);
		pack();
		
		clearGrid();

		this.addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosing(WindowEvent evt){
				twitterStream.shutdown();
				twitterStream.cleanUp();

				evt.getWindow().dispose();
			}
		});

		ToolTipManager.sharedInstance().setInitialDelay(0);
		ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
		
		setLocationRelativeTo(null);
		setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);

		new Timer().scheduleAtFixedRate(new TimerTask() {
			public void run(){
				refresh();
			}
		}, INITIAL_TIME, REFRESH_TIME);
	}

	public void updateWord(){
		Word current = Word.getCurrentWord();
		if(current == null){
			linkButton.setEnabled(false);
			validateButton.setEnabled(false);
			wordDefinitionArea.setText("Definition");
		}
		else{
			linkButton.setEnabled(true);
			validateButton.setEnabled(true);			
			wordDefinitionArea.setText(current.definition);
		}
	}

	public void updateVisibility(){
		for(CrosswordSquare square : gridPanel.squares.values()){
			square.setValueHidden(!visibility);
		}
	}


	private void refresh(){

		JViewport view = scroll.getViewport();
		Point origin = view.getViewPosition();

		Dimension delta = gridPanel.refresh(visibility);
		origin.translate(delta.width,delta.height);
		view.setViewPosition(origin);

		pack();
		repaint();
	}

	private void buildTwitterStream(){
		
		if(twitterStream != null){
			twitterStream.clearListeners();
			twitterStream.cleanUp();
			twitterStream.shutdown();
		}
		
		twitterStream = CredentialsManager.getTwitterStream();
		
	}
	
	public void clearGrid() {
		
		buildTwitterStream();
		
		manager = new TweetManager();
		TwitterListener listener = new TwitterListener(manager);
		twitterStream.addListener(listener);
		
		saveButton.setEnabled(false);
		
		gridPanel.reset(manager.getGrid(), visibility);
		Word.setCurrentWord(null);
		updateWord();
		refresh();		
	}

	public void generateNewGrid(){	

		//clearGrid();

		String txt = buzzArea.getText().replace(" ","");
		String[] buzzwords = txt.split("\n");	

		FilterQuery query = new FilterQuery();
		query.track(buzzwords);
		twitterStream.filter(query);

		saveButton.setEnabled(true);		
	}
	
	public void packGrid(){
		new Thread(new Runnable(){
			public void run() {
				Crosswords grid = manager.getGrid();
				manager.setGrid(Crosswords.makePackedCrosswords(grid.wordSet));
				gridPanel.reset(manager.getGrid(), visibility);				
			}			
		}).start();		
	}

	public void saveToFile(File file) {
		JSONArray json = new JSONArray();
		try {
			for(Word w : gridPanel.grid.getWords()) {
				json.put(w.toJsonObject());
			}

			FileWriter writer = new FileWriter(file);
			writer.write(json.toString(1));
			writer.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void loadFromFile(File file) {
		try {
			Scanner scanner = new Scanner(file);
			String jsonString = scanner.useDelimiter("\\Z").next();
			scanner.close();
			
			JSONArray json = new JSONArray(jsonString);
			
			clearGrid();
			Crosswords grid = gridPanel.grid;
			
			for(int i = 0; i < json.length(); ++i) {
				JSONObject obj = json.getJSONObject(i);
				Word w = Word.fromJsonObject(obj);
				grid.addWord(w, w.getPositions().get(0), w.isHorizontal());
				manager.addTag(w.value);
			}
			
			refresh();			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

}
