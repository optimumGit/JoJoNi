package bremen_hs.de.jojoni.seka;

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

	public Player(String playerName, String playerID){
		this.hand = new ArrayList<Cards>();
		this.playerName = playerName;
        this.playerID   = playerID;
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
    public float getCoinsCount(){
		return this.playerCoins;
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
     */
    public void winCoints(float coins){
        this.playerCoins =+ coins;
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

	//Eine Karte waehlen, die anstatt den Jocker sein wird.
/**	public Cards chosseJocker(){
		return null;
	}
*/

}
