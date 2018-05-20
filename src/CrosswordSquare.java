import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.FocusEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseAdapter;

import javax.swing.BorderFactory;
import javax.swing.JTextField;


public class CrosswordSquare extends JTextField {

	public static final int preferredSide = 25;

	//sets the behaviour when the automatic navigation on the grid encounters a validated cell
	public static final boolean JUMP_CORRECT_SQUARES = false;
	private static final long serialVersionUID = 1L;

	public static CrosswordSquare currentSquare = null;

	private Word hWord, vWord;
	public Character c;
	public Pair<Integer,Integer> position;
	public GridPanel parent;

	private boolean isCorrect;
	private boolean isDefinition;

	public CrosswordSquare(Word hWord, Word vWord, Character c, Pair<Integer,Integer> position, GridPanel parent){
		super("");

		this.hWord = hWord;
		this.vWord = vWord;
		this.c = c;
		this.position = position;
		this.parent = parent;

		isCorrect = false;
		isDefinition = false;

		setEditable(false);
		setHorizontalAlignment(JTextField.CENTER);
		Dimension size = new Dimension(preferredSide,preferredSide);
		setPreferredSize(size);
		this.downlight();

		Font font = this.getFont();
		this.setFont(new Font(font.getFontName(),Font.BOLD,font.getSize()));

		setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));

		this.addFocusListener(new FocusAdapter(){
			@Override
			public void focusGained(FocusEvent evt) {
				updateSquare();	
			}
		});

		this.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent evt) {				
				updateWord();
			}		
		});

		this.addKeyListener(new KeyAdapter(){
			@Override
			public void keyTyped(KeyEvent evt) {

				Word currentWord = Word.getCurrentWord();

				CrosswordSquare square = (CrosswordSquare)evt.getComponent();
				if(evt.getKeyChar() == '\n'){
					square.parent.validateWord();		
					return;
				}

				String c = ""+Character.toUpperCase(evt.getKeyChar());				
				if(!isCorrect && "ABCDEFGHIJKLMNOPQRSTUVWXYZ".contains(c)){					
					square.setText(c);
				}

				Pair<Integer,Integer> nextPosition = currentWord.nextPosition(square.position);
				CrosswordSquare nextSquare = square;

				while(nextPosition != null){
					nextSquare = ((GridPanel)evt.getComponent().getParent()).squares.get(nextPosition);
					if(!JUMP_CORRECT_SQUARES)
						break;
					if(nextSquare.isCorrect)
						nextPosition = currentWord.nextPosition(nextSquare.position);
					else
						break;
				}


				if(nextPosition != null){
					nextSquare.requestFocusInWindow();
				}
			}				
		});

		updateTooltip();
	}

	public void setVWord(Word vWord){
		this.vWord = vWord;
		updateTooltip();
	}

	public void setHWord(Word hWord){
		this.hWord = hWord;
		updateTooltip();
	}

	public void updateTooltip(){
		String tooltip = "";
		if(hWord != null){
			tooltip += "Horizontal :<BR/>" + hWord.definition;
			if(vWord != null)
				tooltip += "<BR/>------<BR/>";
		}
		if(vWord != null){
			tooltip += "Vertical :<BR/>" + vWord.definition;
		}

		if("".equals(tooltip)){
			this.setToolTipText(null);
		}
		else{
			this.setToolTipText("<HTML><p width=\"300\">"+tooltip+"</p><</HTML>");
		}
	}

	public void highlight(){
		if(isDefinition)
			return;
		this.setBackground(new Color(180,218,255));
	}

	public void superHighlight(){
		if(isDefinition)
			return;
		this.setBackground(new Color(254,254,51));
	}

	public void downlight(){
		if(isDefinition)
			return;
		this.setBackground(Color.WHITE);
	}

	public void updateSquare(){
		currentSquare = this;
		parent.highlightWord();
	}

	public void updateWord(){
		Word currentWord = Word.getCurrentWord();
		if(hWord == null){
			Word.setCurrentWord(vWord);
		}
		else if(vWord == null){
			Word.setCurrentWord(hWord);
		}
		else{
			if(currentWord == hWord)
				Word.setCurrentWord(vWord);
			else
				Word.setCurrentWord(hWord);
		}
		currentSquare = this;

		parent.highlightWord();
		parent.updateWord();
	}

	public static CrosswordSquare makeBlankSquare(){
		CrosswordSquare blank = new CrosswordSquare(null,null,null,null,null);
		blank.setEditable(false);
		blank.setEnabled(false);
		blank.setBackground(Color.BLACK);

		for(MouseListener lst : blank.getMouseListeners())
			blank.removeMouseListener(lst);

		return blank;
	}

	public static CrosswordSquare makeDefinitionSquare(Word hWord, Word vWord, Character c, Pair<Integer,Integer> position, GridPanel parent){
		CrosswordSquare def = new CrosswordSquare(hWord,vWord,c,position,parent);

		def.setEditable(false);
		def.isDefinition = true;
		def.setBackground(Color.ORANGE);

		for(MouseListener lst : def.getMouseListeners())
			def.removeMouseListener(lst);

		return def;
	}

	public boolean checkCorrect(){
		if(isDefinition)
			return false;
		
		isCorrect = this.getText().equals(c.toString());
		if(isCorrect){
			this.setForeground(Color.BLUE);
		}
		return isCorrect;
	}

	public void setValueHidden(boolean hide){
		if(isDefinition)
			return;
		if(hide && !isCorrect){
			this.setText("");
		}
		else
			this.setText(c.toString());
	}

}
