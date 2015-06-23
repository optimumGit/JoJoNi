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
	private Pot pot = null;

	public GameManager() {
		this.pot = new Pot();
	}
	/**
	 * 
	 * 
	 * @param player player
	 * @return Boolean 
	 */
	public boolean playerJoinGame(Player player){
		if(players.size() <= 8){
			players.add(player);
			return true;
		}else{
			return false;
		}
	}
    /**
     *
     *
     * @param player
     * @param coins
     */
    public void playerRais(Player player, float coins){
        //TODO hier überprüfen ob der player genug coins hat?!
        this.pot.playerRais(player, coins);
    }
    /**
     *
     *
     * @param player
     */
    public void playerFold(Player player){
        this.pot.playerFold(player);
    }

    /**
     *
     * @param player
     */
    public void playerCall(Player player){

    }

    /**
	 * 
	 * 
	 * @return List<Player> players
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
	 * shuffle cards stack
	 * 
	 * @return List<Cards> stack
	 */
	private List<Cards> shuffle(){
		List<Cards> stack = this.cardStack.getStack();
		Collections.shuffle(stack);
		return stack;
	}
	

	public HashMap<Player, Float> results(){
		Results rs = new Results();
		HashMap<Player, Float> gameResults = new HashMap<Player, Float>();
		float result = 0.0f;
		for (int i = 0; i < players.size(); i++) {
			result = rs.results(players.get(i).getHand());
			gameResults.put(players.get(i), result);
		}
		return gameResults;
	}

	// --------------------test methoden-------------------------
	
	public void creatNewPlayer(String name){
		//Player pl = new Player(name);
	//	this.playerJoinGame(pl);
	}
	
	public float testResult(List<Cards> hand){
		Results rs = new Results();
		return rs.results(hand);
	}
}
