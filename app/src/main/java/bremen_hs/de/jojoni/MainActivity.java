package bremen_hs.de.jojoni;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.turnbased.OnTurnBasedMatchUpdateReceivedListener;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatchConfig;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer;
import com.google.android.gms.plus.Plus;
import com.google.example.games.basegameutils.BaseGameUtils;

import java.util.ArrayList;


public class MainActivity extends FragmentActivity implements MainFragment.MainListener, GameActivityFragment.GameListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        OnInvitationReceivedListener, OnTurnBasedMatchUpdateReceivedListener {

    private static final String TAG = "SekaCardGame";
    private static final int INVITE_PLAYERS_REQUEST = 10000;
    private static final int SIGN_IN_REQUEST = 9001;
    private boolean mResolvingConnectionFailure = false;
    private boolean mAutoStartSignInFlow = true;
    private TurnBasedMatch currentMatch;
    private TurnData turnData;

    private GoogleApiClient apiClient;

    // Fragments
    GameActivityFragment gameActivityFragment;
    MainFragment mainFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create the Google API Client with access to Plus and Games
        apiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();

        gameActivityFragment = new GameActivityFragment();
        mainFragment = new MainFragment();

        gameActivityFragment.setGameListener(this);
        mainFragment.setMainListener(this);

        getFragmentManager().beginTransaction().add(R.id.fragment, mainFragment).commit();

    //    Games.Invitations.registerInvitationListener(apiClient, this);

    //    Games.TurnBasedMultiplayer.registerMatchUpdateListener(apiClient, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "Connect ApiClient ... ");
        apiClient.connect();

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "Disconnect ApiClient ... ");
        if (apiClient.isConnected()) {
            apiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "Connected");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        Log.d(TAG, "Connection failed" + GooglePlayServicesUtil.getErrorString(connectionResult.getErrorCode()));
        Log.d(TAG, "onConnectionFailed(): attempting to resolve");
        if (mResolvingConnectionFailure) {
            // Already resolving
            Log.d(TAG, "onConnectionFailed(): ignoring connection failure, already resolving.");
            return;
        }

        // Launch the sign-in flow if the button was clicked or if auto sign-in is enabled
        if (mAutoStartSignInFlow) {
            mAutoStartSignInFlow = false;
            mResolvingConnectionFailure = BaseGameUtils.resolveConnectionFailure(this,
                    apiClient, connectionResult, SIGN_IN_REQUEST,
                    getString(R.string.signin_other_error));
        }
    }


    @Override
    public void onFinishedTurn() {

    }

    @Override
    public void onCreateGameClicked() {
        onStartMatchClicked();
    }

    @Override
    public void onJoinGameClicked() {

    }

    @Override
    public void onShowRulesClicked() {

    }


    // Instantiate a new TurnBasedMatch - Getting to the Lobby
    private void onStartMatchClicked() {
        int minPlayers = 1;
        int maxPlayers = 8;
        Intent intent = Games.TurnBasedMultiplayer.getSelectOpponentsIntent(apiClient,
                minPlayers, maxPlayers, true);
        startActivityForResult(intent, INVITE_PLAYERS_REQUEST);
    }

    // Handle the ActivityRequests
    @Override
    public void onActivityResult(int request, int response, Intent data) {
        super.onActivityResult(request, response, data);
        if( request == SIGN_IN_REQUEST){
            apiClient.connect();
        }
        if (request == INVITE_PLAYERS_REQUEST) {
            if (response != Activity.RESULT_OK) {
                // user canceled
                Log.d(TAG, "onActivityResult: user canceled player selection.");
                return;
            }

            // get the invitee list
            final ArrayList<String> invitees = data
                    .getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);

            // get automatch criteria
            Bundle autoMatchCriteria = null;

            int minAutoMatchPlayers = data.getIntExtra(
                    Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
            int maxAutoMatchPlayers = data.getIntExtra(
                    Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);

            if (minAutoMatchPlayers > 0) {
                autoMatchCriteria = RoomConfig.createAutoMatchCriteria(
                        minAutoMatchPlayers, maxAutoMatchPlayers, 0);
            } else {
                autoMatchCriteria = null;
            }

            TurnBasedMatchConfig tbmc = TurnBasedMatchConfig.builder()
                    .addInvitedPlayers(invitees)
                    .setAutoMatchCriteria(autoMatchCriteria).build();

            // Start the match
            Games.TurnBasedMultiplayer.createMatch(apiClient, tbmc).setResultCallback(
                    new ResultCallback<TurnBasedMultiplayer.InitiateMatchResult>() {
                        @Override
                        public void onResult(TurnBasedMultiplayer.InitiateMatchResult result) {
                            processResult(result);
                        }
                    });
        }
    }

    // Start a new Match
    private void processResult(TurnBasedMultiplayer.InitiateMatchResult result) {

        TurnBasedMatch match = result.getMatch();
        Toast.makeText(this, "Game started", Toast.LENGTH_LONG).show();
        startMatch(match);
    }

    private void startMatch(TurnBasedMatch match) {
        turnData = new TurnData();
        // Some basic turn data
        //  mTurnData.data = "First turn";

        currentMatch = match;

        String playerId = Games.Players.getCurrentPlayerId(apiClient);
        String myParticipantId = currentMatch.getParticipantId(playerId);

        FragmentManager fragmentManager = getFragmentManager();

            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.replace(R.id.fragment, gameActivityFragment);

            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.commit();



        /*Games.TurnBasedMultiplayer.takeTurn(apiClient, match.getMatchId(),
                turnData.persist(), myParticipantId).setResultCallback(
                new ResultCallback<TurnBasedMultiplayer.UpdateMatchResult>() {
                    @Override
                    public void onResult(TurnBasedMultiplayer.UpdateMatchResult result) {
                        //processResult(result);
                    }
                });*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onInvitationReceived(Invitation invitation) {
        Toast.makeText(
                this,
                "An invitation has arrived from "
                        + invitation.getInviter().getDisplayName(), Toast.LENGTH_LONG)
                .show();

    }

    @Override
    public void onInvitationRemoved(String s) {

    }

    @Override
    public void onTurnBasedMatchReceived(TurnBasedMatch turnBasedMatch) {

    }

    @Override
    public void onTurnBasedMatchRemoved(String s) {

    }
}
