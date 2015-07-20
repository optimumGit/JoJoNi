package bremen_hs.de.jojoni.seka;


/**
 * @author  Johann Luziv
 * @version 0.1
 * @since   20150513
 */
public class Pot {
	private float pot = 0;
    private float lastRaise = 0;
	
	
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
	 * @param player
	 * @param coins
	 */
	public void playerRaise(Player player, float coins){
		if(player.setCoinsInGame(coins) != 0) {
			this.pot = this.pot + coins;
			this.lastRaise = coins;
		}
	}

	public void raise(float coins){
		this.pot = this.pot + coins;
	}
    /**
     *
     * @param player
     */
	public void playerFold(Player player){
		player.getHand().clear();
		this.pot = 0;
	}

    /**
     *
     * @param player
     */
    public void playerCall(Player player, float coins){
		if(player.setCoinsInGame(coins) != 0) {
			this.pot = this.pot + coins;

		}
    }

	/**
	 *
	 * @param coins
	 */
	public void call(float coins){
		this.pot =+ coins;
	}
}
