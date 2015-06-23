package bremen_hs.de.jojoni.seka;

/**
 * 20150216
 * @author optimum
 *
 */

public class Cards {
	private int cardType;  /*0=karo; 1=herz; 2=kreuz; 3=pik;*/
	private int cardCount; /*0=6; 1=7; 2=8; 3=9; 4=10; 5=bube; 6=dame; 7=king; 8=As*/
	
	public Cards (int type, int count){
		this.cardType  = type;
		this.cardCount = count;
		
	}
	
	public void setCardTyp(int type){
		this.cardType = type;
	}
	
	public int getCardTyp(){
		return this.cardType;
	}
	
	public void setCardCount(int count){
		this.cardCount = count;
	}
	
	public int getCardCount(){
		return this.cardCount;
	}
	
	public String toString(){
		return "card type: " + this.cardType + " card count: " + this.cardCount; 
	}
}