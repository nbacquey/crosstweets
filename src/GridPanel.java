import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.KeyboardFocusManager;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JPanel;


public class GridPanel extends JPanel{

	private static final long serialVersionUID = 1L;
	private static final int INITIAL_SIZE = 15;

	public Crosswords grid;
	public Map<Pair<Integer,Integer>,CrosswordSquare> squares;

	private int xmin, ymin, xmax, ymax;

	private CrosswordFrame parent;

	public GridPanel(Crosswords grid, CrosswordFrame parent, boolean visibility){
		super();
		this.grid = grid;
		this.parent = parent;
		squares = new ConcurrentHashMap<Pair<Integer,Integer>,CrosswordSquare>();

		xmin = -INITIAL_SIZE;
		ymin = -INITIAL_SIZE;
		xmax = INITIAL_SIZE;
		ymax = INITIAL_SIZE;

		refresh(visibility);
	}

	public void highlightWord(){

		for(CrosswordSquare square : squares.values())
			square.downlight();

		Word currentWord = Word.getCurrentWord();
		CrosswordSquare currentSquare = CrosswordSquare.currentSquare;

		if(currentWord == null)
			return;

		for(Pair<Integer,Integer> pos : currentWord.positions){
			CrosswordSquare square = squares.get(pos);
			if(square == null)
				System.out.println(square);
			else
				square.highlight();
		}
		if(currentSquare != null)
			currentSquare.superHighlight();
	}

	public void updateWord(){
		parent.updateWord();
	}

	public void validateWord(){
		Word current = Word.getCurrentWord();
		if(current == null)
			return;

		for(Pair<Integer,Integer> pos : current.positions){
			CrosswordSquare square = squares.get(pos);
			if(!square.checkCorrect())
				square.setText("");
		}
	}

	public Dimension refresh(boolean visibility) {

		synchronized(grid){

			Component comp = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();

			for(Pair<Integer,Integer> pos : grid.grid.keySet()){
				Word hWord = grid.words.get(pos).x;
				Word vWord = grid.words.get(pos).y;
				if(!squares.containsKey(pos)){
					char c = grid.grid.get(pos);
					CrosswordSquare square;
					
					if(c == '@'){
						square = CrosswordSquare.makeDefinitionSquare(hWord, vWord, c, pos, this);
					}
					else{
						square = new CrosswordSquare(hWord, vWord, c, pos, this);
					}					
										
					square.setValueHidden(!visibility);
					squares.put(pos, square);
				}
				else{
					CrosswordSquare square = squares.get(pos);
					square.setHWord(hWord);
					square.setVWord(vWord);
				}
			}

			int gridXmin = grid.getXmin();
			int gridXmax = grid.getXmax();
			int gridYmin = grid.getYmin();
			int gridYmax = grid.getYmax();

			int newXmin = Math.min(gridXmin, xmin);
			int newXmax = Math.max(gridXmax, xmax);
			int newYmin = Math.min(gridYmin, ymin);
			int newYmax = Math.max(gridYmax, ymax);

			this.removeAll();
			this.setLayout(new GridLayout(newYmax-newYmin+1,newXmax-newXmin+1,0,0));

			for(int y = newYmin; y <= newYmax; ++y){
				for(int x = newXmin; x <= newXmax; ++x){
					Pair<Integer,Integer> pos = new Pair<Integer,Integer>(x,y);

					if(squares.containsKey(pos)){
						this.add(squares.get(pos));
					}
					else{
						this.add(CrosswordSquare.makeBlankSquare());
					}
				}
			}

			int deltaX = xmin - newXmin;
			int deltaY = ymin - newYmin;

			xmin = newXmin;
			xmax = newXmax;
			ymin = newYmin;
			ymax = newYmax;

			if(comp != null)
				comp.requestFocusInWindow();

			return new Dimension(deltaX*CrosswordSquare.preferredSide,deltaY*CrosswordSquare.preferredSide);
		}
	}

	public void reset(Crosswords grid, boolean visibility){
		
		this.grid = grid;
		squares = new ConcurrentHashMap<Pair<Integer,Integer>,CrosswordSquare>();

		xmin = -INITIAL_SIZE;
		ymin = -INITIAL_SIZE;
		xmax = INITIAL_SIZE;
		ymax = INITIAL_SIZE;

		refresh(visibility);
	}



}