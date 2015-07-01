package bremen_hs.de.jojoni;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

/**
 * Created by johan_000 on 02.06.2015.
 */
public class TurnData {

    private final String TAG = "TurnData";
    private String data;
    private int turn = 0;

    public TurnData(){

    }


    public byte[] persist(){
        JSONObject json = new JSONObject();
        try {
            json.put("data", data);

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

            if (obj.has("data")) {
                turnData.data = obj.getString("data");
            }

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return turnData;

    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getTurn() {
        return turn;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }
}
