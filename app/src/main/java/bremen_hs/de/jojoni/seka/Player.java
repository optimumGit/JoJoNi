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
	private float coins = 1000; 
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
	public Cards chosseJocker(){
		return null;
	}
	/**
	 * 
	 * 
	 * @param hand
	 * @return float hand result
	 */
	/*public float getCardsResult(List<Cards> hand){
		float result = 0.0f;
		if(hand.get(0).getCardTyp() == hand.get(1).getCardTyp()){
			if(hand.get(0).getCardCount() > 0 && hand.get(0).getCardCount() < 4){
				result += hand.get(0).getCardCount() + 6;
			}else if(hand.get(0).getCardCount() > 3){
				result += 10;
			}else if(hand.get(0).getCardCount() == 8){
				result += 11;	
			}
			if(hand.get(1).getCardCount() > 0 && hand.get(1).getCardCount() < 4){
				result += hand.get(1).getCardCount() + 6;
			}else if(hand.get(1).getCardCount() > 3){
				result += 10;
			}else if(hand.get(1).getCardCount() == 8){
				result += 11;		
			}
		}
		if(hand.get(0).getCardTyp() == hand.get(2).getCardTyp()){
			if(hand.get(0).getCardCount() >= 0 && hand.get(0).getCardCount() < 4){
				result += hand.get(0).getCardCount() + 6;
			}else if(hand.get(0).getCardCount() > 3){
				result += 10;
			}else if(hand.get(0).getCardCount() == 8){
				result += 11;	
			}if(hand.get(2).getCardCount() > 0 && hand.get(2).getCardCount() < 4){
				result += hand.get(2).getCardCount() + 6;
			}else if(hand.get(2).getCardCount() > 3){
				result += 10;
			}else if(hand.get(2).getCardCount() == 8){
				result += 11;		
			}
		}
		if(hand.get(1).getCardTyp() == hand.get(2).getCardTyp()){
			if(hand.get(1).getCardCount() > 0 && hand.get(1).getCardCount() < 4){
				result += hand.get(1).getCardCount() + 6;
			}else if(hand.get(1).getCardCount() > 3){
				result += 10;
			}else if(hand.get(1).getCardCount() == 8){
				result += 11;	
			}	
			if(hand.get(2).getCardCount() > 0 && hand.get(2).getCardCount() < 4){
				result += hand.get(2).getCardCount() + 6;
			}else if(hand.get(2).getCardCount() > 3){
				result += 10;
			}else if(hand.get(2).getCardCount() == 8){
				result += 11;		
			}
		}//TODO abhier else fälle überprüfen


		/*		for(int i = 1; i < hand.size(); i++){
			if(hand.get(i-1).getCardTyp() == hand.get(i).getCardTyp()){
				if(hand.get(i-1).getCardCount() > 3 && hand.get(0).getCardCount() < 8){
					this.result += 10;
				}else if(hand.get(i-1).getCardCount() == 8){
					this.result += 11;
				}
				this.result += hand.get(i-1).getCardCount();

			}
			//	this.result = hand.get(0).getCardCount() + hand.get(1).getCardCount(); 

		}

		if(hand.get(0).getCardTyp() == hand.get(1).getCardTyp() || hand.get(0).getCardTyp() == hand.get(2).getCardTyp() || hand.get(1).getCardTyp() == hand.get(2).getCardTyp()){

		}


		/*  TODO: rekrusiv versuchen

			public void rekrusiv(List<Cards> hand){
				System.out.println("Hand");
				//rechnen
				hand.remove(0);
				if(hand.size() != 0){
					rekrusiv(hand);
				}
			}

		 * 
		 */

		/*else if(hand.get(0).getCardTyp() == hand.get(2).getCardTyp()){
			this.result = hand.get(0).getCardCount() + hand.get(2).getCardCount();
		}else if(hand.get(1).getCardTyp() == hand.get(3).getCardTyp()){
			this.result = hand.get(1).getCardCount() + hand.get(2).getCardCount();
		}else{
			this.result = 0.0f;//sonst hoehste karte als result rechnen

		}
		System.out.println("result methode: " + result);
		return result;
	}*/

	public String toString(){
		return this.name;
	}


}
