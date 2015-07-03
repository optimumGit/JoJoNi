package bremen_hs.de.jojoni;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * Created by johan_000 on 02.06.2015.
 */
public class TurnData {

    private final String TAG     = "TurnData";
    private String playerName    = null;
    private String playerAction  = null;
    private boolean isNewCard    = false;
    private float playerSetCoins = new Integer(null);
    private int cardType  = new Integer(null);
    private int cardCount = new Integer(null);
    private int turn      = new Integer(null);



    public TurnData(){

    }

    public int getCardType(){
        return this.cardType;
    }

    public boolean isNewCard(){
        return this.isNewCard;
    }

    public int getCardCount(){
        return this.cardCount;
    }


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
            }else if(obj.has("new card")){
                turnData.isNewCard = true;
                turnData.cardCount = Integer.parseInt(obj.getString("card count"));
                turnData.cardType  = Integer.parseInt(obj.getString("card type"));
            }

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return turnData;

    }

    public byte[] dealCards(String cardType, String cardCount){
        JSONObject json = new JSONObject();
        try {
            json.put("card typ",   cardType);
            json.put("card count", cardCount);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        String st = json.toString();

        Log.d(TAG, "==== DEAL CARDS\n" + st);

        return st.getBytes(Charset.forName("UTF-8"));
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
