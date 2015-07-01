package bremen_hs.de.jojoni;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
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
import bremen_hs.de.jojoni.seka.Player;


public class MainActivity extends FragmentActivity implements MainFragment.MainListener,
        GameFragment.GameListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
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
    GameFragment gameFragment;
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

        gameFragment = new GameFragment();
        mainFragment = new MainFragment();

        gameFragment.setGameListener(this);
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
        String playerID   = Games.Players.getCurrentPlayerId(apiClient);
        String playerName = Games.Players.getCurrentPlayer(apiClient).getDisplayName();

        gameManager.playerJoinGame(playerName, playerID);


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


    // implementing the GameFragment interface
    @Override
    public void onRaiseButtonClicked() {
        // call the gameManager functions
        playerRaise();
    }

    @Override
    public void onCallButtonClicked() {
        playerCall();
        buildInputWindow();
    }

    @Override
    public void onFoldButtonClicked() {
        playerFold();
    }

    public void onDoneClicked() {

        String nextParticipantId = getNextParticipantId();
        // Create the next turn
        turnData.setTurn(turnData.getTurn() + 1);
        int turn =  turnData.getTurn();


        Games.TurnBasedMultiplayer.takeTurn(apiClient, currentMatch.getMatchId(),
                turnData.persist(), nextParticipantId).setResultCallback(
                new ResultCallback<TurnBasedMultiplayer.UpdateMatchResult>() {
                    @Override
                    public void onResult(TurnBasedMultiplayer.UpdateMatchResult result) {
                        processResult(result);
                    }
                });
        gameFragment.setListView(turnData.getData());
        gameFragment.setEnabled(false);
    }

    private String getNextParticipantId() {
        String playerId = Games.Players.getCurrentPlayerId(apiClient);
        String myParticipantId = currentMatch.getParticipantId(playerId);

        ArrayList<String> participantIds = currentMatch.getParticipantIds();

        int desiredIndex = -1;

        for (int i = 0; i < participantIds.size(); i++) {
            if (participantIds.get(i).equals(myParticipantId)) {
                desiredIndex = i + 1;
            }
        }

        if (desiredIndex < participantIds.size()) {
            return participantIds.get(desiredIndex);
        }

        if (currentMatch.getAvailableAutoMatchSlots() <= 0) {
            // You've run out of automatch slots, so we start over.
            return participantIds.get(0);
        } else {
            // You have not yet fully automatched, so null will find a new
            // person to play against.
            return null;
        }
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
        getFragmentManager().beginTransaction().replace(R.id.fragment, gameFragment).commit();
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
                getFragmentManager().beginTransaction().replace(R.id.fragment, gameFragment).commit();
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
                gameFragment.setListView(turnData.getData());
                Toast.makeText(this, "turnData: " + turnData.getData(), Toast.LENGTH_LONG).show();
                updateUi();
                return;
            case TurnBasedMatch.MATCH_TURN_STATUS_THEIR_TURN:
                // Should return results.
                gameFragment.setListView(turnData.getData());
                Toast.makeText(this, "It's not your turn.", Toast.LENGTH_LONG).show();
                break;
            case TurnBasedMatch.MATCH_TURN_STATUS_INVITED:
                Toast.makeText(this, "Good inititative! Still waiting for invitations.\n\nBe patient!", Toast.LENGTH_LONG).show();
        }

     //   turnData = null;


    }

    private void startMatch(TurnBasedMatch match) {
        turnData = new TurnData();
        // Some basic turn data
        turnData.setData("First turn");

        currentMatch = match;

        String playerId = Games.Players.getCurrentPlayerId(apiClient);
        String myParticipantId = currentMatch.getParticipantId(playerId);

        FragmentManager fragmentManager = getFragmentManager();

            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.replace(R.id.fragment, gameFragment);

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
            gameFragment.setEnabled(true);
            updateMatch(match);
            return;
        }
        updateUi();
        Toast.makeText(this, "Game updated", Toast.LENGTH_LONG).show();
    }

    private void updateUi() {

    }

    private void buildInputWindow() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Title");
        alert.setMessage("Message");

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Editable value = input.getText();
                turnData.setData(turnData.getData() + value);
                onDoneClicked();
            }
        });


        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();

    }

    private void showExitAppPopUp() {

        AlertDialog.Builder exitPopUp = new AlertDialog.Builder(this);
        exitPopUp.setTitle("App Beenden");
        exitPopUp.setMessage("Möchsten Sie das Spiel beenden?");

        exitPopUp.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        System.exit(0);
                    }
        })
                .setNegativeButton("Nein", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //Nicht beenden
                    }
                });

        AlertDialog PopUp = exitPopUp.create();
        PopUp.show();
    }


    private void showExitGamePopUp() {

        AlertDialog.Builder exitPopUp = new AlertDialog.Builder(this);
        exitPopUp.setTitle("Partie Beenden");
        exitPopUp.setMessage("Möchten Sie diese Partie beenden?");

        exitPopUp.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        getFragmentManager().beginTransaction().replace(R.id.fragment, mainFragment).commit();
                    }
                })
                .setNegativeButton("Nein", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //Nicht beenden
                    }
                });

        AlertDialog PopUp = exitPopUp.create();
        PopUp.show();
    }

    public void onBackPressed(){

        if (mainFragment.isVisible()){
            showExitAppPopUp();
        }
        else if (gameFragment.isVisible()){

            showExitGamePopUp();


        }

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
        gameFragment.setListView(turnData.getData());
        if(turnBasedMatch.getTurnStatus() == TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN){
            gameFragment.setEnabled(true);
            updateMatch(turnBasedMatch);
        }
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
        Player pl = gameManager.getPlayer();
        Context context = getApplicationContext();
        CharSequence text = pl.getPlayerName() + " : " + pl.getPlayerID();
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }
    private void playerCall(){



        Context context = getApplicationContext();
        CharSequence text = "playerCall";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }
}
