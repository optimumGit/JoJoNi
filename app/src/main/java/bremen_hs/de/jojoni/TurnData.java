package bremen_hs.de.jojoni;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import bremen_hs.de.jojoni.seka.Player;

/**
 *  The TurnData class for persisting and unpersisting the game data
 */
public class TurnData {

    private final String TAG = "TurnData";
    private String playerName = null;
    private String playerId = null;
    private String broadcastAction = null;
    private String action = null;
    private String nextTurnId = null;
    private float playerSetCoins = 0.0f;
    private int cardType;
    private int cardCount;


    // default constructor
    public TurnData(){

    }

    // getter & setter
    public String getPlayerId(){
        return this.playerId;
    }

    public int getCardType(){
        return this.cardType;
    }

    public String getAction(){
        return this.action;
    }

    public void setAction(String action){
        this.action = action;
    }

    public int getCardCount(){
        return this.cardCount;
    }

    public String getPlayerName(){
        return this.playerName;
    }

    public String getNextTurnId(){
        return this.nextTurnId;
    }

    public String getBroadcastAction(){
        return this.broadcastAction;
    }

    public float getPlayerCoins(){
        return this.playerSetCoins;
    }

    public String getData() {
        return playerName;
    }

    public void setData(String data) {
        this.playerName = data;
    }

    /**
     * Unpersists the byte array to a JSON Object and instantiate a new {@link TurnData} Object which is returned
     * @param data which was received
     * @return a TurnData Object with the received information
     */
    public TurnData unpersist(byte[] data){
        if (data == null) {
            Log.d(TAG, "Empty array---possible bug.");
            return new TurnData();
        }

        String st;
        try {
            st = new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
            return null;
        }

        Log.d(TAG, "====UNPERSIST \n" + st);

        TurnData turnData = new TurnData();

        try {
            JSONObject obj = new JSONObject(st);
            if(obj.has("action")){
                // deal cards
                if(obj.get("action").equals("newCard")) {
                    turnData.cardCount = obj.getInt("card count");
                    turnData.cardType = obj.getInt("card type");
                    turnData.nextTurnId = obj.getString("next turn");
                    turnData.setAction(obj.getString("action"));
                    return turnData;
            } else if(obj.get("action").equals("raise")){
                    return getTurnData(turnData, obj);
                }else if(obj.get("action").equals("call")){
                    return getTurnData(turnData, obj);
                }else if(obj.get("action").equals("fold")){
                    return getTurnData(turnData, obj);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return turnData;
    }

    /**
     * Getting the {@link TurnData} for the three turn actions
     * @param turnData
     * @param obj the JSONObject
     * @return the turnData
     * @throws JSONException
     */
    private TurnData getTurnData(TurnData turnData, JSONObject obj) throws JSONException {
        turnData.broadcastAction = obj.getString("action");
        if(obj.has("coins")) {
            turnData.playerSetCoins = Float.parseFloat(obj.getString("coins"));
        }
        turnData.playerName = obj.getString("player name");
        turnData.playerId = obj.getString("player id");
        turnData.nextTurnId = obj.getString("next turn");
        turnData.setAction(obj.getString("action"));
        return turnData;
    }

    /**
     * Getting the byte array for dealing the cards.
     *
     * @param cardType as integer 0=karo; 1=herz; 2=kreuz; 3=pik;
     * @param cardCount as integer 0=6; 1=7; 2=8; 3=9; 4=10; 5=bube; 6=dame; 7=king; 8=As
     * @param rounds as integer for the number of the dealt card
     * @return the data as a byte array
     */
    public byte[] cardJson
    (int cardType, int cardCount, int rounds, String playerid){
        String newCard = new String("newCard");
        JSONObject cardJSON = new JSONObject();
        try {
            cardJSON.put("new card",   rounds);
            cardJSON.put("card type",  cardType);
            cardJSON.put("card count", cardCount);
            cardJSON.put("action", newCard);
            cardJSON.put("next turn", playerid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
       return cardJSON.toString().getBytes();
    }

    /**
     * Creating the {@link JSONObject} and build the byte array with the information which should be sent.
     *
     * @param player the player name who made the turn
     * @param nextParticitpantId the next player
     * @param coins the set value
     * @param action which action the player made (raise, call, fold)
     * @return the data as a byte array
     */
    public byte[] sendGameBroadcast(Player player, String nextParticitpantId, float coins, String action){
        JSONObject cardJSON = new JSONObject();
        try {
            cardJSON.put("player name", player.getPlayerName());
            cardJSON.put("player id", player.getPlayerID());
            cardJSON.put("next turn", nextParticitpantId);
            if(!action.equals("fold")) {
                cardJSON.put("coins", coins);
            }else{
                cardJSON.put("coins", null);
            }
            cardJSON.put("action", action);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return cardJSON.toString().getBytes();
    }

}
