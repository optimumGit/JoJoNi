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
public class GameActivityFragment extends Fragment implements View.OnClickListener {

    static int[] BUTTONS = {
            R.id.raise_button, R.id.call_button, R.id.fold_button
    };

    GameListener gameListener = null;


    public GameActivityFragment() {
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
                gameListener.onFinishedTurn();
                return;
            case R.id.call_button:
                gameListener.onFinishedTurn();
                return;
            case R.id.fold_button:
                gameListener.onFinishedTurn();
                return;
        }
    }

    public interface GameListener{
        void onFinishedTurn();
    }
}
