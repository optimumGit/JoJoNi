package bremen_hs.de.jojoni;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import bremen_hs.de.jojoni.seka.Player;

/**
 * Created by johan_000 on 02.06.2015.
 */
public class TurnData {

    private final String TAG     = "TurnData";
    private String playerName    = null;
    private String playerAction  = null;
    private String broadcastAction = null;
    private String isNewCard     = null;
    private String action = null;
    private float playerSetCoins = 0.0f;
    private int cardType;//new Integer(null);
    private int cardCount;//new Integer(null);
    private int turn;//new Integer(null);



    public TurnData(){

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


    public String getBroadcastAction(){
        return this.broadcastAction;
    }

    public float getPlayerCoins(){
        return this.playerSetCoins;
    }

    /**
     *
     * @return
     */
    public byte[] persist(){
        JSONObject json = new JSONObject();
        try {
            json.put("player name",      playerName);
            json.put("player action",    playerAction);
            json.put("player set coins", playerSetCoins);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        String st = json.toString();

        Log.d(TAG, "==== PERSISTING\n" + st);

        return st.getBytes(Charset.forName("UTF-8"));
    }

    /**
     *
     * @param data
     * @return
     */
    public TurnData unpersist(byte[] data){
        if (data == null) {
            Log.d(TAG, "Empty array---possible bug.");
            return new TurnData();
        }

        String st = null;
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
            if (obj.has("player name")) {
                turnData.playerName = obj.getString("player name");
                return turnData;
            }else if(obj.has("action")){
                if(obj.get("action").equals("newCard")) {
                    turnData.isNewCard = obj.getString("new card");
                    turnData.cardCount = obj.getInt("card count");
                    turnData.cardType = obj.getInt("card type");
                    String action = obj.getString("action");
                    turnData.setAction(action);
                    return turnData;
            } else if(obj.get("action").equals("raise")){
                    turnData.broadcastAction = obj.getString("action");
                    turnData.playerSetCoins  = Float.parseFloat(obj.getString("coins"));
                    turnData.playerName      = obj.getString("player name");
                    turnData.setAction(obj.getString("action"));
                    return turnData;
                }else if(obj.get("action").equals("call")){
                    turnData.broadcastAction = obj.getString("action");
                    turnData.playerSetCoins  = Float.parseFloat(obj.getString("coins"));
                    turnData.playerName      = obj.getString("player name");
                    turnData.setAction(obj.getString("action"));
                    return turnData;
                }else if(obj.get("action").equals("fold")){
                    turnData.broadcastAction = obj.getString("action");
                    turnData.playerName      = obj.getString("player name");
                    turnData.setAction(obj.getString("action"));
                    return turnData;
                }
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return turnData;
    }

    /**
     *
     * @param cardType
     * @param cardCount
     * @param rounds
     * @return
     */
    public byte[] CardJson(int cardType, int cardCount, int rounds){
        String newCard = new String("newCard");
        JSONObject cardJSON = new JSONObject();
        try {
            cardJSON.put("new card",   rounds);
            cardJSON.put("card type",  cardType);
            cardJSON.put("card count", cardCount);
            cardJSON.put("action", newCard);
        } catch (JSONException e) {
            e.printStackTrace();
        }
       return cardJSON.toString().getBytes();
    }

    /**
     *
     * @param name
     * @param coins
     * @param action
     * @return
     */
    public byte[] receiveGameBroadcast(Player name, float coins, String action){
        JSONObject cardJSON = new JSONObject();
        try {
            cardJSON.put("player name", name);
            cardJSON.put("next turn", name.getPlayerID());
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


    public String getData() {
        return playerName;
    }

    public void setData(String data) {
        this.playerName = data;
    }

    public int getTurn() {
        return turn;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }
}
