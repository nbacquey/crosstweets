import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;


public class Crosswords {
	
	public static final int RANDOM_TRIES_NUMBER = 2000;

	Map<Pair<Integer,Integer>,Character> grid;
	Map<Character,Map<Pair<Integer,Integer>,Boolean>> matches;
	Map<Pair<Integer,Integer>,Pair<Word,Word>> words;
	Collection<Word> wordSet;
	
	int xmin, xmax, ymin, ymax;

	public static Crosswords test(){
		Crosswords ret = new Crosswords();
		ret.placeWord(new Word("JABCDEF"));
		ret.placeWord(new Word("ZJZYZZ"));
		ret.placeWord(new Word("XCXTXX"));
		ret.placeWord(new Word("MFMOPQ"));
		ret.placeWord(new Word("ZEEXC"));

		return ret;		
	}

	public Crosswords(){
		grid = new ConcurrentHashMap<Pair<Integer,Integer>, Character>();
		matches = new ConcurrentHashMap<Character,Map<Pair<Integer,Integer>,Boolean>>();
		words = new ConcurrentHashMap<Pair<Integer,Integer>, Pair<Word,Word>>();
		wordSet = new Vector<Word>();

		for(Character c : "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray()){
			matches.put(c, new ConcurrentHashMap<Pair<Integer,Integer>,Boolean>());
		}
		
		xmin = ymin = Integer.MAX_VALUE;
		xmax = ymax = Integer.MIN_VALUE;
	}

	public int getXmin(){
		return xmin;
	}

	public int getXmax(){
		return xmax;
	}

	public int getYmin(){
		return ymin;
	}

	public int getYmax(){
		return ymax;
	}

	public boolean placeWord(Word fullWord){

		synchronized(this){

			String word = fullWord.getValue();

			if(grid.isEmpty()){
				addWord(fullWord, new Pair<Integer,Integer>(0,0), true);
				return true;
			}		

			int bestX = 0, bestY = 0, bestCross = 0, bestDelta = word.length();
			boolean orientation = true;

			for(int i = 0; i < word.length(); ++i){
				char c = word.charAt(i);

				for(Pair<Integer,Integer> crossPos : matches.get(c).keySet()){
					boolean isHorizontal = matches.get(c).get(crossPos);
					int initX = isHorizontal ? crossPos.x - i : crossPos.x;
					int initY = isHorizontal ? crossPos.y : crossPos.y - i;
					Pair<Integer,Integer> initPos = new Pair<Integer,Integer>(initX,initY);

					int delta = 0;
					if (isHorizontal)
						delta = (   Math.max(getXmin() - initX,0)
							      + Math.max(initX + word.length() - getXmax(),0)
							    )*(getXmax() - getXmin()+1);
					else
						delta = (   Math.max(getYmin() - initY,0) 
								  + Math.max(initY + word.length() - getYmax(),0)
								)*(getYmax() - getYmin()+1);

					int crossNumber = getCrosses(word, initPos, isHorizontal);

					if(crossNumber > bestCross || (crossNumber == bestCross && delta < bestDelta)){
						bestX = initX;
						bestY = initY;
						bestCross = crossNumber;
						bestDelta = delta;
						orientation = isHorizontal;
					}
				}			
			}

			if(bestCross > 0){
				addWord(fullWord, new Pair<Integer,Integer>(bestX,bestY), orientation);
				return true;
			}

			return false;	
		}
	}



	public int getCrosses(String word, Pair<Integer, Integer> initPos, boolean isHorizontal) {

		int crosses = 0;

		int beforeStartX = isHorizontal ? initPos.x - 1 : initPos.x;
		int beforeStartY = isHorizontal ? initPos.y : initPos.y - 1;

		int afterEndX = isHorizontal ? initPos.x + word.length() : initPos.x;
		int afterEndY = isHorizontal ? initPos.y : initPos.y + word.length();

		Pair<Integer,Integer> beforeStartPos = new Pair<Integer,Integer>(beforeStartX,beforeStartY);
		Pair<Integer,Integer> afterEndPos = new Pair<Integer,Integer>(afterEndX,afterEndY);

		if(grid.containsKey(beforeStartPos) || grid.containsKey(afterEndPos))
			return -1;

		for(int i = 0 ; i < word.length(); ++i){
			char c = word.charAt(i);
			int curX = isHorizontal ? initPos.x+i : initPos.x;
			int curY = isHorizontal ? initPos.y : initPos.y+i;

			Pair<Integer,Integer> curPos = new Pair<Integer,Integer>(curX,curY);

			if(grid.containsKey(curPos)){
				if(!grid.get(curPos).equals(c))
					return -1;
				++crosses;
			}

			else{			
				int adjX1 = isHorizontal ? curX : curX - 1;
				int adjX2 = isHorizontal ? curX : curX + 1;
				int adjY1 = isHorizontal ? curY - 1 : curY;
				int adjY2 = isHorizontal ? curY + 1 : curY;

				Pair<Integer,Integer> adjPos1 = new Pair<Integer,Integer>(adjX1,adjY1);
				Pair<Integer,Integer> adjPos2 = new Pair<Integer,Integer>(adjX2,adjY2);

				if(grid.containsKey(adjPos1) || grid.containsKey(adjPos2))
					return -1;
			}

		}

		return crosses;
	}

	public void addWord(Word fullWord, Pair<Integer,Integer> firstPosition, boolean isHorizontal){
		String word = "@"+fullWord.getValue(); //the asperand is a placeholder for the cell containing the definition of the word
		int x = firstPosition.x;
		int y = firstPosition.y;
		
		for(int i = 0 ; i < word.length(); ++i){
			// the -1 in x or y is for the '@' character
			Pair<Integer,Integer> currentPos = isHorizontal ? new Pair<Integer,Integer>(x+i-1,y) : new Pair<Integer,Integer>(x,y+i-1);
			Character currentChar = word.charAt(i);

			fullWord.addPosition(currentPos);
			
			if(grid.containsKey(currentPos)){
				if(!grid.get(currentPos).equals(currentChar)){
					System.err.println("Non-matching existing character");
				}
				matches.get(currentChar).remove(currentPos);

				if(isHorizontal)
					words.get(currentPos).x = fullWord; 
				else 
					words.get(currentPos).y = fullWord;
			}

			else{				
				grid.put(currentPos, currentChar);
				
				if(!currentChar.equals('@'))
					matches.get(currentChar).put(currentPos, !isHorizontal);	

				if(isHorizontal)
					words.put(currentPos, new Pair<Word,Word>(fullWord,null));
				else
					words.put(currentPos, new Pair<Word,Word>(null,fullWord));
			}
		}
		wordSet.add(fullWord);
		updateBounds();
	}
	
	public void updateBounds() {
		for(Pair<Integer,Integer> pos : grid.keySet()) {
			if(pos.x < xmin)
				xmin = pos.x;
			if(pos.x > xmax)
				xmax = pos.x;
			if(pos.y < ymin)
				ymin = pos.y;
			if(pos.y > ymax)
				ymax = pos.y;
		}
	}
	
	public Collection<Word> getWords(){
		return wordSet;
	}
	

	@Override
	public String toString(){
		String ret = "";

		if(grid.isEmpty())
			return "#\n";

		for(int y = getYmin() ; y <= getYmax(); ++y){
			for(int x = getXmin() ; x <= getXmax(); ++x){
				Pair<Integer,Integer> pos = new Pair<Integer,Integer>(x,y);

				if(grid.containsKey(pos))
					ret += grid.get(pos);
				else
					ret += " ";
			}

			ret += "\n";
		}

		return ret;
	}
	
	public static Crosswords makePackedCrosswords(Collection<Word> wordSet) {
		Crosswords bestTry = null;
		
		int minScore = Integer.MAX_VALUE;
		Collection<Word> oldSet = wordSet;
		
			
		for(int i = 0; i < RANDOM_TRIES_NUMBER; ++i) {
			Crosswords crosswords = new Crosswords();
			List<Word> newSet = new ArrayList<Word>();
			for(Word w : oldSet)
				newSet.add(w.quasiClone());
			
			Collections.shuffle(newSet);
			
			boolean allPlaced = true;
			
			for(Word w : newSet) {
				allPlaced = allPlaced && crosswords.placeWord(w);
			}
			
			if(!allPlaced) {
				--i;
				oldSet = newSet;
				continue;
			}
			
			int score = Math.max((crosswords.getXmax() - crosswords.getXmin())
							    ,(crosswords.getYmax() - crosswords.getYmin()));
			if(score < minScore) {
				minScore = score;
				bestTry = crosswords;
			}
			oldSet = newSet;
		}
		
		return bestTry;		
	}

}
