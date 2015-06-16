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
	//TODO die spieler hand wird nicht gelert
	private String name;
	private float coins = 1000; //TODO 
	private List<Cards> hand;
	private boolean dealer = false; 

	public Player(String name){
		this.hand   = new ArrayList<Cards>();
		this.name   = name;
	}

	public void setDealer(boolean dealerButton){
		this.dealer = dealerButton;
	}

	public String getName(){
		return this.name;
	}

	public float getCoinsCount(){
		return this.coins;
	}

	public List<Cards> getHand(){
		return this.hand;
	}

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

	//Eine Karte waehlen, die anstatt den Jocker sein wird.
/**	public Cards chosseJocker(){
		return null;
	}
*/
	public String toString(){
		return this.name;
	}


}
