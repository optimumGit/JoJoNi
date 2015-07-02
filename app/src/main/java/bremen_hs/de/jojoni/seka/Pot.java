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
    private float lastRaise = 0;
	private HashMap<Player, Float> potHistory = null;
	
	
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

	/**
	 * 
	 * @param player
	 * @param coins
	 */
	public void playerRais(Player player, float coins){
		if(player.setCoinsInGame(coins) != 0) {
			this.pot =+ coins;
			this.lastRaise = coins;
			this.writeToHistory(player, coins);
		}
	}
    /**
     *
     * @param player
     */
	public void playerFold(Player player){
		player.getHand().clear();
        writeToHistory(player);
	}

    /**
     *
     * @param player
     */
    public void playerCall(Player player){
		float coins = player.setCoinsInGame(this.lastRaise);
		if(coins != 0) {
			this.pot =+ coins;
			this.writeToHistory(player, coins);
		}
    }
	/**
	 * 
	 * @param player
	 * @param coins
	 */
	private void writeToHistory(Player player, float coins){
		this.potHistory.put(player, coins);
	}

    /**
     *
     * @param player
     */
    private void writeToHistory(Player player){
        this.potHistory.put(player, null);
    }
	
}
