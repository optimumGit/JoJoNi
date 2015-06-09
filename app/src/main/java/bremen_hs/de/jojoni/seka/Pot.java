package bremen_hs.de.jojoni.seka;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * @author  Johann Luziv
 * @version 0.1
 * @since   20150513
 */
public class Pot {
	private float pot = 0;
	private HashMap<Player, Float> potHistory; 
	
	
	public Pot(){}
	
	/**
	 * 
	 * @return
	 */
	public float getPotSize() {
		return this.pot;
	}
	/**
	 * 
	 * @return
	 */
	public HashMap<Player, Float> getPotHistory() {
		return this.potHistory;
	}
	
	public void setPotHistory(HashMap<Player, Float> potHistory) {
		this.potHistory = potHistory;
	}
	/**
	 * 
	 * @param player
	 * @param coins
	 */
	public void playerRais(Player player, float coins){
		pot =+ coins;
		writteToHistory(player, coins);
	}
	
	public void playerPass(Player player){
		player.getHand().clear();
	}
	
	/**
	 * 
	 * @param player
	 * @param coins
	 */
	private void writteToHistory(Player player, float coins){
		this.potHistory.put(player, coins);
	}
	

	
}
