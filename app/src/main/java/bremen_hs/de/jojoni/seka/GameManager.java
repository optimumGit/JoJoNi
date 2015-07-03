package bremen_hs.de.jojoni.seka;

import com.google.android.gms.games.multiplayer.Participant;

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
	private HashMap<String, Object> gameHistory = null;
	private HashMap<String, Player> mParticipants = null;
	private CardStack cardStack = null;
	private Player player = null;
	private Pot pot = null;

	/**
	 *
	 */
	public GameManager(HashMap<String, Player> mParticipants) {
		this.pot = new Pot();
		this.cardStack   = new CardStack();
		this.gameHistory = new HashMap<String, Object>();
		this.mParticipants = mParticipants;
	}

	public HashMap<String, Object> getGameHistoryList(){
		return this.gameHistory;
	}

	/**
	 *
	 * @param playerName
	 * @param playerID
	 */
	public void playerJoinGame(Participant participent){
		if(!this.gameHistory.containsKey(participent.getParticipantId())){
			this.gameHistory.put(participent.getDisplayName(), "join");
			player = new Player(participent);
		}else{
			return;
		}
	}


	/**
	 *
	 * @param playerName
	 * @param playerID
	 */
	public void playerJoinGame(String playerName, String playerID){
		if(!this.gameHistory.containsKey(playerID)){
			this.gameHistory.put(playerName, "join");
			player = new Player(playerName, playerID);
		}else{
			return;
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
    public void playerFold(Player player){
        this.pot.playerFold(player);
    }

    /**
     *
     * @param player
     */
    public void playerCall(Player player){
		this.pot.playerCall(player);
    }

	public void playerWin(float coins){
		this.player.winCoints(coins);
	}
	/**
	 * 
	 * 
	 * @param list<playerList> playerList
	 */
	public byte[] dealCards(List<Player> playerList) {//TODO fehler abfangen... java.lang.IndexOutOfBoundsException.... nur 36 karten stack
		List<Cards> currentStack = this.shuffle();





/*		int y = 0;

		for (int e = 0; e < playerList.size(); e++) {
			playerList.get(e).getHand().clear();
		}


		for (int i = 0; i < 3; i++) {
			while (y < playerList.size()) {
				playerList.get(y).getNewCards(currentStack.get(0));
				currentStack.remove(0);
				y++;
			}
			y = 0;
		}*/
		return null;
	}

	public float getPotSize(){
		return this.pot.getPotSize();
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
	
/*
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
*/
	// --------------------test methoden-------------------------

	public float testResult(List<Cards> hand){
		Results rs = new Results();
		return rs.results(hand);
	}

	public Player getPlayer(){
		return this.player;
	}
}
