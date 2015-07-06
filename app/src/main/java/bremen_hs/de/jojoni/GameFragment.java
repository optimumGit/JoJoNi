package bremen_hs.de.jojoni;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;

/**
 * A placeholder fragment containing a simple view.
 */
public class GameFragment extends Fragment implements View.OnClickListener {

    private static String TAG = "GAME_FRAGMENT";

    MainFragment mainFragment;
    ImageButton raise_button;
    ImageButton call_button;
    ImageButton fold_button;
    ListView listView;

    static int[] BUTTONS = {
            R.id.raise_button, R.id.call_button, R.id.fold_button
    };

    GameListener gameListener = null;


    public GameFragment() {
        mainFragment = new MainFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_game, container, false);
        for (int i : BUTTONS) {
            ((ImageButton) v.findViewById(i)).setOnClickListener(this);
        }

        call_button = (ImageButton) v.findViewById(R.id.call_button);
        fold_button = (ImageButton) v.findViewById(R.id.fold_button);
        raise_button = (ImageButton) v.findViewById(R.id.raise_button);
        listView = (ListView) v.findViewById(R.id.playerHistory);
        return v;



    }

    public void setGameListener(GameListener listener){
        this.gameListener = listener;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateUi();
    }

    void updateUi() {
        if (getActivity() == null){
            return;
        }
    }

    @Override
    public void onClick(View view) {

        switch(view.getId()){
            case R.id.raise_button:
                gameListener.onRaiseButtonClicked();
                break;
            case R.id.call_button:
                gameListener.onCallButtonClicked();
                break;
            case R.id.fold_button:
                gameListener.onFoldButtonClicked();
                break;
        }
    }

    public void setEnabled(boolean enabled){
        Log.d(TAG, "Buttons enabled" + enabled);
        if(!enabled) {
            raise_button.setImageResource(R.drawable.button_erhoehen_deaktiviert);
            fold_button.setImageResource(R.drawable.button_passen_deaktiviert);
            call_button.setImageResource(R.drawable.button_mitgehen_deaktiviert);
        } else {
            raise_button.setImageResource(R.drawable.button_erhoehen);
            fold_button.setImageResource(R.drawable.button_passen);
            call_button.setImageResource(R.drawable.button_mitgehen);
        }
        raise_button.setEnabled(enabled);
        fold_button.setEnabled(enabled);
        call_button.setEnabled(enabled);
    }

    // The functions for the three buttons, implemented by the MainActivity
    public interface GameListener{
        void onRaiseButtonClicked();
        void onCallButtonClicked();
        void onFoldButtonClicked();

    }
}
