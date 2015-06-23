package bremen_hs.de.jojoni;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
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
import com.google.android.gms.games.multiplayer.turnbased.OnTurnBasedMatchUpdateReceivedListener;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatchConfig;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer;
import com.google.android.gms.plus.Plus;
import com.google.example.games.basegameutils.BaseGameUtils;

import java.util.ArrayList;

import bremen_hs.de.jojoni.seka.GameManager;


public class MainActivity extends FragmentActivity implements MainFragment.MainListener,
        GameActivityFragment.GameListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        OnInvitationReceivedListener, OnTurnBasedMatchUpdateReceivedListener {

    private static final String TAG = "SekaCardGame";
    private static final int INVITE_PLAYERS_REQUEST = 10000;
    private static final int SIGN_IN_REQUEST = 9001;
    final static int RC_LOOK_AT_MATCHES = 10001;
    private boolean mResolvingConnectionFailure = false;
    private boolean mAutoStartSignInFlow = true;
    private TurnBasedMatch currentMatch;
    private TurnData turnData;

    private GoogleApiClient apiClient;

    //seka
    GameManager gameManager = new GameManager();

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

        if(apiClient.isConnected()) {
            Games.Invitations.registerInvitationListener(apiClient, this);
            Games.TurnBasedMultiplayer.registerMatchUpdateListener(apiClient, this);
        }
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
        if(apiClient.isConnected()){
            Games.Invitations.registerInvitationListener(apiClient, this);
            Games.TurnBasedMultiplayer.registerMatchUpdateListener(apiClient, this);
        }
        if (currentMatch != null) {
            if (apiClient == null || !apiClient.isConnected()) {
                Log.d(TAG, "Warning: accessing TurnBasedMatch when not connected");
            }
            updateMatch(currentMatch);
            return;
        }
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


    // implementing the GameActivityFragment interface
    @Override
    public void onRaiseButtonClicked() {
        // call the gameManager functions
        playerRaise();
    }

    @Override
    public void onCallButtonClicked() {
        playerCall();
    }

    @Override
    public void onFoldButtonClicked() {
        playerFold();
    }

    // implementing the mainFragment interface
    @Override
    public void onCreateGameClicked() {
        onStartMatchClicked();
    }

    @Override
    public void onJoinGameClicked() {
        onCheckGamesClicked();
    }

    @Override
    public void onShowRulesClicked() {
        getFragmentManager().beginTransaction().add(R.id.fragment, gameActivityFragment).commit();
    }

    public void onCheckGamesClicked() {
        Intent intent = Games.TurnBasedMultiplayer.getInboxIntent(apiClient);
        startActivityForResult(intent, RC_LOOK_AT_MATCHES);
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
        } else if (request == RC_LOOK_AT_MATCHES) {
            // Returning from the 'Select Match' dialog

            if (response != Activity.RESULT_OK) {
                // user canceled
                return;
            }

            TurnBasedMatch match = data
                    .getParcelableExtra(Multiplayer.EXTRA_TURN_BASED_MATCH);

            if (match != null) {
               updateMatch(match);
            }
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

            TurnBasedMatchConfig tbmc = TurnBasedMatchConfig.builder()
                    .addInvitedPlayers(invitees).build();

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

    private void updateMatch(TurnBasedMatch match) {
        currentMatch = match;

        int status = match.getStatus();
        int turnStatus = match.getTurnStatus();

        switch (status) {
            case TurnBasedMatch.MATCH_STATUS_CANCELED:
                Toast.makeText(this, "Canceled!", Toast.LENGTH_LONG).show();
                return;
            case TurnBasedMatch.MATCH_STATUS_EXPIRED:
                Toast.makeText(this, "Expired!", Toast.LENGTH_LONG).show();
                return;
            case TurnBasedMatch.MATCH_STATUS_AUTO_MATCHING:
                Toast.makeText(this, "Waiting for auto-match...", Toast.LENGTH_LONG).show();
                return;
            case TurnBasedMatch.MATCH_STATUS_COMPLETE:
                if (turnStatus == TurnBasedMatch.MATCH_TURN_STATUS_COMPLETE) {
                    Toast.makeText(this, "Complete! This game is over", Toast.LENGTH_LONG).show();
                    break;
                }
        }

        // OK, it's active. Check on turn status.
        switch (turnStatus) {
            case TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN:
                turnData = turnData.unpersist(currentMatch.getData());
                updateUi();
                return;
            case TurnBasedMatch.MATCH_TURN_STATUS_THEIR_TURN:
                // Should return results.
                Toast.makeText(this, "It's not your turn.", Toast.LENGTH_LONG).show();
                break;
            case TurnBasedMatch.MATCH_TURN_STATUS_INVITED:
                Toast.makeText(this, "Good inititative! Still waiting for invitations.\n\nBe patient!", Toast.LENGTH_LONG).show();
        }

        turnData = null;


    }

    private void startMatch(TurnBasedMatch match) {
        turnData = new TurnData();
        // Some basic turn data
        //  mTurnData.data = "First turn";

        currentMatch = match;

        String playerId = Games.Players.getCurrentPlayerId(apiClient);
        com.google.android.gms.games.Player part = Games.Players.getCurrentPlayer(apiClient);
        Toast.makeText(this, "playerId " + playerId + " participant " + part.getDisplayName(), Toast.LENGTH_LONG).show();
        String myParticipantId = currentMatch.getParticipantId(playerId);

        FragmentManager fragmentManager = getFragmentManager();

            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.replace(R.id.fragment, gameActivityFragment);

            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.commit();

        Games.TurnBasedMultiplayer.takeTurn(apiClient, match.getMatchId(),
                turnData.persist(), myParticipantId).setResultCallback(
                new ResultCallback<TurnBasedMultiplayer.UpdateMatchResult>() {
                    @Override
                    public void onResult(TurnBasedMultiplayer.UpdateMatchResult result) {
                        processResult(result);
                    }
                });
    }

    // Start a new Match
    private void processResult(TurnBasedMultiplayer.InitiateMatchResult result) {

        TurnBasedMatch match = result.getMatch();
        if (match.getData() != null) {
            // This is a game that has already started, so I'll just start
            updateMatch(match);
            return;
        }
        Toast.makeText(this, "Game started", Toast.LENGTH_LONG).show();
        startMatch(match);
    }

    private void processResult(TurnBasedMultiplayer.UpdateMatchResult result) {
        TurnBasedMatch match= result.getMatch();
        boolean isDoingTurn = (match.getTurnStatus() == TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN);

        if (isDoingTurn) {
            updateMatch(match);
            return;
        }
        updateUi();
        Toast.makeText(this, "Game updated", Toast.LENGTH_LONG).show();
    }

    private void updateUi() {

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

    //******************* spiel logik************************
    //TODO player object mit uebergeben
    private void playerFold(){

        //gameManager.playerFold(Player);

        Context context = getApplicationContext();
        CharSequence text = "playerFold";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }
    private void playerRaise(){

        //gameManager.playerRais(Player, coins);

        Context context = getApplicationContext();
        CharSequence text = "playerRaise";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }
    private void playerCall(){

        //gameManager.playerCall(Player);

        Context context = getApplicationContext();
        CharSequence text = "playerCall";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }
}
