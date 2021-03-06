package bremen_hs.de.jojoni.seka;

import android.util.Log;

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
	private Results results = null;
	private float minCall = 50;
	/**
	 *
	 */
	public GameManager(HashMap<String, Player> mParticipants) {
		this.pot = new Pot();
		this.cardStack   = new CardStack();
		this.gameHistory = new HashMap<String, Object>();
		this.mParticipants = mParticipants;
		this.results = new Results();
	}

	public void setCardToPlayer(Cards card){
		this.player.setCard(card);
	}

	/**
	 *
	 * @return
	 */
	public HashMap<String, Object> getGameHistoryList(){
		return this.gameHistory;
	}

	public List<Cards> getStack(){
		return shuffle();
	}

	public void raise(float coins){
		this.pot.raise(coins);
	}

	public void call(float coins){
		this.pot.call(coins);
	}

	public float getMinCall(){
		return  this.minCall;
	}

	public void setMinCall(float minCall){
		this.minCall = minCall;
	}

	public float getCardsResult(List<Cards> hand){
		return this.results.results(hand);
	}

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

	public List<Cards> getPlayerHand(){
		return player.getHand();
	}
    /**
     *
     *
     * @param player
     * @param coins
     */
    public void playerRaise(Player player, float coins){
        this.pot.playerRaise(player, coins);
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
    public void playerCall(Player player, float coins){
		this.pot.playerCall(player, coins);
    }

	public void playerWin(float coins){
		this.player.winCoints(coins);
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

	public boolean gameOver(HashMap<String, Player> mParticipants){
		boolean gameOver = true;
		int lastPlayer   = 1;
		int players = mParticipants.size();
		int raise   = 0;
		int call    = 0;
		int fold    = 0;

		for (Player participant : mParticipants.values()) {
			if (participant.getAction().equals("call")) {
				call++;
			}else if(participant.getAction().equals("raise")){
				raise++;
			}else if(participant.getAction().equals("fold")){
				fold++;
			}
		}

		int playerImGame = players - fold;

		if(playerImGame == call){
			return gameOver;
		}else if(raise == 1 && fold == players - 1){//todo
			return gameOver;
		}
		return false;
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
