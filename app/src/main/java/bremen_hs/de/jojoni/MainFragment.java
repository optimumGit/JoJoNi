package bremen_hs.de.jojoni;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class MainFragment extends Fragment implements View.OnClickListener{

    MainListener mainListener = null;

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main, container, false);
        final int[] BUTTONS = {R.id.btnCreateGame, R.id.btnJoinGame, R.id.btnShowRules};
        for (int i : BUTTONS) {
            v.findViewById(i).setOnClickListener(this);
        }
        return v;
    }

    public void setMainListener(MainListener listener){
        this.mainListener = listener;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnCreateGame:
                mainListener.onCreateGameClicked();
                break;
            case R.id.btnJoinGame:
                mainListener.onJoinGameClicked();
                break;
            case R.id.btnShowRules:
                mainListener.onShowRulesClicked();
                break;
        }

    }

    public interface MainListener{
        void onCreateGameClicked();
        void onJoinGameClicked();
        void onShowRulesClicked();
    }
}
