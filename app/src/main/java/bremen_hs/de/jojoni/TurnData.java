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
    private float playerSetCoins = 0;
    private int turn = 0;

    public TurnData(){

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
            }

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return turnData;

    }

    public byte[] dealCards(ArrayList<Integer> cards){
        JSONObject json = new JSONObject();
        try {
            json.put("first card typ",    cards.get(0));
            json.put("first card count",  cards.get(1));
            json.put("second card typ",   cards.get(2));
            json.put("second card count", cards.get(3));
            json.put("third card typ",    cards.get(4));
            json.put("third card count",  cards.get(5));
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        String st = json.toString();

        Log.d(TAG, "==== PERSISTING\n" + st);

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
