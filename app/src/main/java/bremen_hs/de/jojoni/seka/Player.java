package bremen_hs.de.jojoni.seka;

import com.google.android.gms.games.multiplayer.Participant;

import java.util.ArrayList;
import java.util.List;

/**
 * 20150216
 * @author optimum
 *
 */

public class Player {

	private String playerName = null;
    private String playerID   = null;
	private String action = "no action yet";
	private float playerCoins = 1000; //TODO start coins??
	private List<Cards> hand  = new ArrayList<Cards>();
	private boolean dealer    = false;

	/**
	 *
	 * @param p
	 */
	public Player(Participant p){
		this.playerID   = p.getParticipantId();
		this.playerName = p.getDisplayName();
	}

	/**
	 *
 	 * @param playerName
	 * @param playerID
	 */
	public Player(String playerName, String playerID){
		this.playerName = playerName;
        this.playerID   = playerID;
	}

    /**
     *
     * @return
     */
	public String getPlayerName(){
		return this.playerName;
	}
    /**
     *
     * @return
     */
    public String getPlayerID(){
        return this.playerID;
    }

	/**
     *
     * @return
     */
	public List<Cards> getHand(){
		return this.hand;
	}

	public float getPlayerCoins(){
		return this.playerCoins;
	}

	/**
	 *
	 * @param dealerButton
	 */
	public void setDealer(boolean dealerButton){
		this.dealer = dealerButton;
	}

	/**
	 * add card to player hand
	 * @param card
	 */
	public void setCard(Cards card){
		this.hand.add(card);
	}
	/**
     *
     * @return
     */
	public boolean isDealer(){
		return this.dealer;
	}

	/**
	 *
	 * @param coins
	 * @return
	 */
	public float setCoinsInGame(float coins){
		if(this.playerCoins >= coins){
			this.playerCoins =- coins;
			return coins;
		}else{
			return 0;
		}
	}


	/**
	 *
	 * @param coins
	 */
	public void winCoints(float coins){
		this.playerCoins =+ coins;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}
}
