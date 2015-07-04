package bremen_hs.de.jojoni;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import bremen_hs.de.jojoni.seka.Player;

/**
 * Created by optimum on 04.07.15.
 */
public class HistoryListAdapter extends ArrayAdapter {
    public HistoryListAdapter(Context context, ArrayList<Player> players) {
        super(context, 0, players);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Player player = (Player)getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.playerhistory_list, parent, false);
        }
        // Lookup view for data population
        TextView tvName = (TextView) convertView.findViewById(R.id.playerName);
        TextView tvHome = (TextView) convertView.findViewById(R.id.action);
        // Populate the data into the template view using the data object
        tvName.setText(player.getPlayerName());
        tvHome.setText(player.getPlayerID());
        // Return the completed view to render on screen
        return convertView;
    }
}
