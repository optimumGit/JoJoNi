package bremen_hs.de.jojoni;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMultiplayer;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;
import com.google.android.gms.plus.Plus;
import com.google.example.games.basegameutils.BaseGameUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import bremen_hs.de.jojoni.seka.Cards;
import bremen_hs.de.jojoni.seka.GameManager;
import bremen_hs.de.jojoni.seka.Player;


public class MainActivity extends FragmentActivity implements MainFragment.MainListener,
        GameFragment.GameListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        OnInvitationReceivedListener,
        RoomUpdateListener,
        RealTimeMessageReceivedListener,
        RoomStatusUpdateListener,
        RealTimeMultiplayer.ReliableMessageSentCallback {

    private static final String TAG = "SekaCardGame";
    private static final int INVITE_PLAYERS_REQUEST = 10000;
    private static final int SIGN_IN_REQUEST = 9001;
    final static int WAITING_ROOM_REQUEST = 10001;
    final static int RC_INVITATION_INBOX = 20000;

    private boolean mResolvingConnectionFailure = false;
    private boolean mAutoStartSignInFlow = true;
    private TurnData turnData = new TurnData();
    private HashMap<String, Player> mParticipants = new HashMap<>();
    private Room mRoom;
    private AlertDialog mAlertDialog;
    // The match turn number, monotonically increasing from 0
    private int mMatchTurnNumber = 0;
    // It is the player's turn when (match turn number % num participants == my turn index)
    private int mMyTurnIndex;

    private String mMyPersistentId;

    private GoogleApiClient apiClient;

    //seka
    GameManager gameManager = new GameManager(mParticipants);

    // Fragments
    GameFragment gameFragment;
    MainFragment mainFragment;
    RuleFragment ruleFragment;


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
        ruleFragment = new RuleFragment();

        gameFragment.setGameListener(this);
        mainFragment.setMainListener(this);

        updateUi();

        if(apiClient.isConnected()) {
            Games.Invitations.registerInvitationListener(apiClient, this);
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
        String playerID = Games.Players.getCurrentPlayerId(apiClient);
        String playerName = Games.Players.getCurrentPlayer(apiClient).getDisplayName();

        gameManager.playerJoinGame(playerName, playerID);
        Games.Invitations.registerInvitationListener(apiClient, this);


        if (bundle != null) {
            Invitation inv =
                    bundle.getParcelable(Multiplayer.EXTRA_INVITATION);

            if (inv != null) {
                acceptInvitation(inv);
            }
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

    }

    @Override
    public void onCallButtonClicked() {
        buildInputWindow();
    }

    @Override
    public void onFoldButtonClicked() {
        dealCards();
    }

    public void onDoneClicked() {
    // Increment turn number
        Log.d(TAG, "DoneClicked");
        mMatchTurnNumber = mMatchTurnNumber + 1;
        sendReliableMessageToOthers(turnData.persist());

    }

    private void sendReliableMessageToOthers(byte[] data) {
        Log.d(TAG, "sendRliableMessage");
        Player me = mParticipants.get(mMyPersistentId);
        for (Player participant : mParticipants.values()) {
            if (!participant.equals(me)) {
                Log.d(TAG, "reliablemessage to:" + participant.getPlayerName() + participant.getPlayerID());
                Log.d(TAG, "reliablemessage to:" + mRoom.getRoomId());
                Games.RealTimeMultiplayer.sendReliableMessage(apiClient, null,
                        data, mRoom.getRoomId(), participant.getPlayerID());
            }
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

    private void onCheckGamesClicked() {
        Intent intent = Games.Invitations.getInvitationInboxIntent(apiClient);
        startActivityForResult(intent, RC_INVITATION_INBOX);
    }

    @Override
    public void onShowRulesClicked() {
        getFragmentManager().beginTransaction().replace(R.id.fragment, ruleFragment).commit();

    }

    // Instantiate a new TurnBasedMatch - Getting to the Lobby
    private void onStartMatchClicked() {
        int minPlayers = 1;
        int maxPlayers = 4;
        Intent intent = Games.RealTimeMultiplayer.getSelectOpponentsIntent(apiClient,
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
        if (request == RC_INVITATION_INBOX) {
            if (response != Activity.RESULT_OK) {
                // canceled
                return;
            }

            // get the selected invitation
            Bundle extras = data.getExtras();
            Invitation invitation = extras.getParcelable(Multiplayer.EXTRA_INVITATION);
            if(invitation != null) {
                acceptInvitation(invitation);
            }
        }
        if (request == WAITING_ROOM_REQUEST) {
            // Coming back from a RealTime Multiplayer waiting room

                Room room = data.getParcelableExtra(Multiplayer.EXTRA_ROOM);
                if (response == RESULT_OK) {
                    Log.d(TAG, "Waiting Room: Success");
                    mRoom = room;
                    startMatch();
                } else if (response == RESULT_CANCELED) {
                    Log.d(TAG, "Waiting Room: Canceled");
                    leaveRoom();
                } else if (response == GamesActivityResultCodes.RESULT_LEFT_ROOM) {
                    Log.d(TAG, "Waiting Room: Left Room");
                    leaveRoom();
                } else if (response == GamesActivityResultCodes.RESULT_INVALID_ROOM) {
                    Log.d(TAG, "Waiting Room: Invalid Room");
                    leaveRoom();
                }
            }
        if (request == INVITE_PLAYERS_REQUEST) {
            if (response != Activity.RESULT_OK) {
                // user canceled
                Log.d(TAG, "onActivityResult: user canceled player selection.");
                return;
            }

            // Create a basic room configuration
            RoomConfig.Builder roomConfigBuilder = RoomConfig.builder(this)
                    .setMessageReceivedListener(this)
                    .setRoomStatusUpdateListener(this);

            // Set the invitees
            final ArrayList<String> invitees = data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);
            if (invitees != null && invitees.size() > 0) {
                roomConfigBuilder.addPlayersToInvite(invitees);
            }

            // Build the room and start the match
            Games.RealTimeMultiplayer.create(apiClient, roomConfigBuilder.build());
        }
    }

    private void leaveRoom() {
        if (mRoom != null) {
            Games.RealTimeMultiplayer.leave(apiClient, this, mRoom.getRoomId());
            mRoom = null;
            updateUi();
        }
    }

    private void startMatch() {
        turnData = new TurnData();
        // Some basic turn data
        turnData.setData("First turn");

        for(String id : mRoom.getParticipantIds()){
            Player p = new Player(mRoom.getParticipant(id));
            mParticipants.put(id, p);
            Toast.makeText(getApplicationContext(), "Player: " + p.getPlayerName(),Toast.LENGTH_LONG);
        }
        // TODO: Karten austeilen
        sendReliableMessageToOthers(turnData.persist());
        updateUi();
    }


    private boolean isMyTurn() {
        int numParticipants = mParticipants.size();
        if (numParticipants == 0) {
            Log.w(TAG, "isMyTurn: no participants - default to true.");
            return true;
        }
        int participantTurnIndex = mMatchTurnNumber % numParticipants;

        Log.d(TAG, String.format("isMyTurn: %d participants, turn #%d, my turn is #%d",
                numParticipants, mMatchTurnNumber, mMyTurnIndex));
        return (mMyTurnIndex == participantTurnIndex);
    }



    private void updateUi() {
        if(mRoom == null){
            Log.d(TAG, "room null");
            getFragmentManager().beginTransaction().replace(R.id.fragment, mainFragment).commit();
            return;
        }
        if(mRoom.getStatus() == Room.ROOM_STATUS_ACTIVE && !gameFragment.isVisible()) {
            Log.d(TAG, "Room Active And mainfragmentvisible");
            getFragmentManager().beginTransaction().replace(R.id.fragment, gameFragment).commitAllowingStateLoss();
        }else if(mRoom.getStatus() == Room.ROOM_STATUS_ACTIVE && gameFragment.isVisible()){
            Log.d(TAG, "Room Active And gamefragment visible");
            return;
        } else if(mRoom.getStatus() == Room.ROOM_STATUS_INVITING){
            Log.d(TAG, "Room inviting");
            showWaitingRoom(mRoom);
        } else if(mRoom == null && !mainFragment.isVisible()){
            Log.d(TAG, "room null");
            getFragmentManager().beginTransaction().replace(R.id.fragment, mainFragment).commit();
        } else {
            //getFragmentManager().beginTransaction().replace(R.id.fragment, mainFragment).commit();
        }
    }



    /**
     * RTMP Participant joined, register the DrawingParticipant if the Participant is connected.
     * @param p the Participant from the Real-Time Multiplayer match.
     */
    private void onParticipantConnected(Participant p) {
        if (p.isConnectedToRoom()) {
            onParticipantConnected(new Player(p));
        }
    }

    /**
     * Add a DrawingParticipant to the ongoing game and update turn order. If the
     * DrawingParticipant is a duplicate, this method does nothing.
     * @param dp the DrawingParticipant to add.
     */
    private void onParticipantConnected(Player dp) {
        Log.d(TAG, "onParticipantConnected: " + dp.getPlayerID());
        if (!mParticipants.containsKey(dp.getPlayerID())) {
            mParticipants.put(dp.getPlayerID(), dp);
        }

        updateTurnIndices();
        //updateUi();
    }

    private void updateTurnIndices() {
        // Turn order is determined by sorting participant IDs, which are consistent across
        // devices (but not across sessions)
        ArrayList<String> ids = new ArrayList<>();
        ids.addAll(mParticipants.keySet());
        Collections.sort(ids);

        // Get your turn order
        mMyTurnIndex = ids.indexOf(mMyPersistentId);
        Log.d(TAG, "My turn index: " + mMyTurnIndex);
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
    public void onInvitationReceived(final Invitation invitation) {
        final String inviterName = invitation.getInviter().getDisplayName();

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this)
                .setTitle("Invitation")
                .setMessage("Would you like to play a new game with " + inviterName + "?")
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Games.RealTimeMultiplayer.declineInvitation(apiClient,
                                invitation.getInvitationId());
                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        acceptInvitation(invitation);
                    }
                });

        mAlertDialog = alertDialogBuilder.create();
        mAlertDialog.show();
    }

    private void acceptInvitation(Invitation invitation) {
        RoomConfig.Builder roomConfigBuilder = RoomConfig.builder(this)
                .setMessageReceivedListener(this)
                .setRoomStatusUpdateListener(this)
                .setInvitationIdToAccept(invitation.getInvitationId());
        RoomConfig room = roomConfigBuilder.build();

        Games.RealTimeMultiplayer.join(apiClient, room);
    }

    @Override
    public void onInvitationRemoved(String s) {
        // The invitation is no longer valid, so dismiss the dialog asking if they'd like to
        // accept and show a Toast.
        if (mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
        }
        Toast.makeText(this, "The invitation was removed.", Toast.LENGTH_SHORT).show();
    }



    @Override
    public void onRoomCreated(int statusCode, Room room) {
        Log.d(TAG, "onRoomCreated: " + statusCode + ":" + room);
        if (statusCode != GamesStatusCodes.STATUS_OK) {
            Log.w(TAG, "Error in onRoomCreated: " + statusCode);
            Toast.makeText(this, "Error creating room.", Toast.LENGTH_SHORT).show();
            return;
        }
        showWaitingRoom(room);
    }

    private void showWaitingRoom(Room room) {
        // Require all players to join before starting
        final int MIN_PLAYERS = Integer.MAX_VALUE;

        Intent i = Games.RealTimeMultiplayer.getWaitingRoomIntent(apiClient, room, MIN_PLAYERS);
        startActivityForResult(i, WAITING_ROOM_REQUEST);
    }

    @Override
    public void onJoinedRoom(int statusCode, Room room) {
        Log.d(TAG, "onJoinedRoom: " + statusCode + ":" + room);
    }

    @Override
    public void onLeftRoom(int statusCode, String s) {
        mRoom = null;
        updateUi();
    }

    @Override
    public void onRoomConnected(int statusCode, Room room) {
        Log.d(TAG, "onRoomConnected: " + statusCode + ":" + room);
        if (statusCode != GamesStatusCodes.STATUS_OK) {
            Log.w(TAG, "Error in onRoomConnected: " + statusCode);
            return;
        }
        mRoom = room;
        updateUi();
    }

    @Override
    public void onRealTimeMessageReceived(RealTimeMessage realTimeMessage) {
        Log.d(TAG, "Message received " + realTimeMessage.toString());
        byte[] data = realTimeMessage.getMessageData();
        onMessageReceived(data);
    }

    private void onMessageReceived(byte[] data) {
        turnData = turnData.unpersist(data);
        if(turnData.isNewCard()){
            Cards card = new Cards(turnData.getCardType(), turnData.getCardCount());
            gameManager.setCardToPlayer(card);
        }
        Log.d(TAG, "Message received " + turnData.getData());
        gameFragment.listView.setText(turnData.getData());
        updateUi();
    }

    @Override
    public void onRealTimeMessageSent(int statusCode, int tokenId, String participantId) {
        Log.d(TAG, "onRealTimeMessageSent: " + statusCode + ":" + participantId);
    }

    @Override
    public void onRoomConnecting(Room room) {
        Log.d(TAG, "onRoomConnecting: " + room);
    }

    @Override
    public void onRoomAutoMatching(Room room) {
        Log.d(TAG, "onRoomAutoMatching: " + room);
    }

    @Override
    public void onPeerInvitedToRoom(Room room, List<String> list) {
        Log.d(TAG, "onPeerInvitedToRoom: "  + room + ":" + list);
    }

    @Override
    public void onPeerDeclined(Room room, List<String> list) {
        Log.d(TAG, "onPeerDeclined: " + room + ":" + list);
    }

    @Override
    public void onPeerJoined(Room room, List<String> list) {
        Log.d(TAG, "onPeerJoined: " + room + ":" + list);
        mRoom = room;
        for (String pId : list) {
            onParticipantConnected(mRoom.getParticipant(pId));
        }
    }


    @Override
    public void onPeerLeft(Room room, List<String> list) {
        Log.d(TAG, "onPeerLeft: " + room + ":" + list);
    }

    @Override
    public void onConnectedToRoom(Room room) {
        mRoom = room;

        // Add self to participants
        mMyPersistentId = mRoom.getParticipantId(Games.Players.getCurrentPlayerId(apiClient));
        Participant me = mRoom.getParticipant(mMyPersistentId);
        onParticipantConnected(me);
        updateTurnIndices();
    }

    @Override
    public void onDisconnectedFromRoom(Room room) {
        Log.d(TAG, "onDisconnectedFromRoom: " + room);
        leaveRoom();
    }

    @Override
    public void onPeersConnected(Room room, List<String> list) {
        Log.d(TAG, "onPeersConnected:" + room + ":" + list);
        mRoom = room;
        for (String pId : list) {
            onParticipantConnected(mRoom.getParticipant(pId));
        }
    }

    @Override
    public void onPeersDisconnected(Room room, List<String> list) {
        Log.d(TAG, "onPeersDisconnected: " + room + ":" + list);
        for (String pId : list) {
            onParticipantDisconnected(pId, pId);
        }
    }

    private void onParticipantDisconnected(String messagingId, String persistentId) {
            Log.d(TAG, "onParticipantDisconnected:" + messagingId);
            Player dp = mParticipants.remove(persistentId);
            if (dp != null) {
                // Display disconnection toast
                Toast.makeText(this, dp.getPlayerName() + " disconnected.", Toast.LENGTH_SHORT).show();
                if (mRoom != null && mParticipants.size() <= 1) {
                    // Last player left in an RTMP game, leave
                    leaveRoom();
                } else {
                    updateTurnIndices();
                }
            }
    }

    @Override
    public void onP2PConnected(String s) {
        Log.d(TAG, "onP2PConnected: " + s);
    }

    @Override
    public void onP2PDisconnected(String s) {
        Log.d(TAG, "onP2PDisconnected: " + s);
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
        AlertDialog.Builder exitPopUpBuilder = new AlertDialog.Builder(this);

        LayoutInflater inflater = this.getLayoutInflater();

        View view = inflater.inflate(R.layout.exit_app_popup, null);
        exitPopUpBuilder.setView(view);

        final AlertDialog PopUp = exitPopUpBuilder.create();

        ImageButton btnExit = (ImageButton)view.findViewById(R.id.btnExitApp);
        btnExit.setOnClickListener(new ImageButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.exit(0);
                PopUp.dismiss();
            }
        });

        ImageButton btnDontExit = (ImageButton)view.findViewById(R.id.btnDontExitApp);
        btnDontExit.setOnClickListener(new ImageButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopUp.dismiss();
            }
        });

        PopUp.show();
    }


    private void showExitGamePopUp() {
        AlertDialog.Builder exitPopUpBuilder = new AlertDialog.Builder(this);

        LayoutInflater inflater = this.getLayoutInflater();

        View view = inflater.inflate(R.layout.exit_game_popup, null);
        exitPopUpBuilder.setView(view);

        final AlertDialog PopUp = exitPopUpBuilder.create();

        ImageButton btnExit = (ImageButton)view.findViewById(R.id.btnExitGame);
        btnExit.setOnClickListener(new ImageButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                leaveRoom();
                PopUp.dismiss();
            }
        });

        ImageButton btnDontExit = (ImageButton)view.findViewById(R.id.btnDontExitGame);
        btnDontExit.setOnClickListener(new ImageButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopUp.dismiss();
            }
        });
        PopUp.show();
    }

    public void onBackPressed(){

        if (mainFragment.isVisible()){
            showExitAppPopUp();
        }
        else if (gameFragment.isVisible()){
            showExitGamePopUp();
        }
        else if (ruleFragment.isVisible()){
            getFragmentManager().beginTransaction().replace(R.id.fragment, mainFragment).commit();
        }

    }
    //+++++++++++++++++++++ game ++++++++++++++++++++++

    public byte[] dealCards(){
        List<Cards> stack = gameManager.getStack();

        int count  = 0;//while zaehler
        int cards  = 0;//aktuelle karte
        int rounds = 3;//Anzahl der runden

        while(count < rounds) {

            for (Player participant : mParticipants.values()) {
                if (!participant.equals(mMyPersistentId)) {
                    JSONObject cardJSON = new JSONObject();
                    try {
                        cardJSON.put("new card",   rounds);
                        cardJSON.put("card type",  stack.get(cards).getCardTyp());
                        cardJSON.put("card count", stack.get(cards).getCardCount());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG, "reliablemessage to:" + participant.getPlayerName() + participant.getPlayerID());
                    Log.d(TAG, "reliablemessage to:" + mRoom.getRoomId());
                    Games.RealTimeMultiplayer.sendReliableMessage(apiClient, null,
                            cardJSON.toString().getBytes(), mRoom.getRoomId(), participant.getPlayerID());
                }else{
                    gameManager.setCardToPlayer(stack.get(cards));
                }
                cards++;
            }
            count++;
        }
        return null;
    }

}
