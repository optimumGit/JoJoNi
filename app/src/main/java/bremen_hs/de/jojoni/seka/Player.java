package bremen_hs.de.jojoni.seka;

import com.google.android.gms.games.multiplayer.Participant;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * 20150216
 * @author optimum
 *
 */

public class Player {

	private String playerName = null;
    private String playerID   = null;
	private float playerCoins = 1000; //TODO start coins??
	private List<Cards> hand  = null;
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

	/**
	 *
	 * @param dealerButton
	 */
	public void setDealer(boolean dealerButton){
		this.dealer = dealerButton;
	}

	/**
     *
     * @return
     */
	public boolean isDealer(){
		return this.dealer;
	}
	/**
	 * add card to player hand
	 * 
	 * @param card
	 */
	public void getNewCards(Cards card){
		this.hand.add(card);
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
			return 0;//TODO
		}
	}

	/**
	 *
	 * @param coins
	 */
	public void winCoints(float coins){
		this.playerCoins =+ coins;
	}

}
