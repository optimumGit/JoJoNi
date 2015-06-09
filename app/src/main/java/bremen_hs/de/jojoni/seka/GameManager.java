package bremen_hs.de.jojoni.seka;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;



/**
 * @author  Johann Luziv
 * @since   20150513
 * @version 0.1
 * 
 */
public class GameManager {
	private CardStack cardStack = new CardStack();
	private List<Player> players = new ArrayList<Player>();
	private Pot pot;

	public GameManager() {
		this.pot = new Pot();
	}
	/**
	 * 
	 * 
	 * @param player
	 */
	public void playerJoinGame(Player player){
		players.add(player);
	}
	/**
	 * 
	 * 
	 * @return List<Player>
	 */
	public List<Player> getPlayerList(){
		return players;
	}	
	/**
	 * 
	 * 
	 * @param List<playerList> playerList
	 */
	public void dealCards(List<Player> playerList){//TODO fehler abfangen... java.lang.IndexOutOfBoundsException.... nur 36 karten stack 
		List<Cards> currentStack = this.shuffle();
		int y = 0;
		
		for(int e = 0; e < playerList.size(); e++){
			playerList.get(e).getHand().clear();
		}
		
		for(int i = 0; i < 3; i++){
			while(y < playerList.size()){
				playerList.get(y).getNewCards(currentStack.get(0));
				currentStack.remove(0);
				y++;
			}
			y = 0;
		}
	}
	/**
	 * 
	 * 
	 * @param player
	 * @param coins
	 */
	public void playerRais(Player player, float coins){
		this.pot.playerRais(player, coins);
	}
	/**
	 * 
	 * 
	 * @param player
	 */
	public void playerPass(Player player){
		this.pot.playerPass(player);
	}
		
	/**
	 * shuffle card stack
	 * 
	 * @return List<Cards> stack
	 */
	private List<Cards> shuffle(){
		List<Cards> stack = this.cardStack.getStack();
		Collections.shuffle(stack);
		return stack;
	}
	
	public HashMap<Player, Integer> result(){
		
		return null;
	}

	// --------------------test methoden-------------------------
	
	public void creatNewPlayer(String name){
		Player pl = new Player(name);
		this.playerJoinGame(pl);
	}
	
	public float testResult(List<Cards> hand, int typeOfCheck){
		Results rs = new Results();
		float result = 0.0f;
		if(typeOfCheck == 0){
			result = rs.pair(hand);
		}else if(typeOfCheck == 1){
			result = rs.allSameCount(hand);
		}else if(typeOfCheck == 2){
			result = rs.allSameType(hand);
		}else if(typeOfCheck == 3){
			result = rs.twoAs(hand);
		}else if(typeOfCheck == 4){
			result = rs.highestCard(hand);
		}
		return result;
	}
}
