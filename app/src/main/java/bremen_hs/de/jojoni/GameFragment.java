package bremen_hs.de.jojoni;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

/**
 * A placeholder fragment containing a simple view.
 */
public class GameFragment extends Fragment implements View.OnClickListener {

    MainFragment mainFragment;

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

        // TODO implement methods
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

    // The functions for the three buttons, implemented by the MainActivity
    public interface GameListener{
        void onFinishedTurn();
        void onRaiseButtonClicked();
        void onCallButtonClicked();
        void onFoldButtonClicked();

    }
}
