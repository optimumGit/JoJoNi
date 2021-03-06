package bremen_hs.de.jojoni;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
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
    final static String NEW_CARD  = new String("newCard");
    final static String RESULT    = new String("result");
    final static String RAISE     = new String("raise");
    final static String CALL      = new String("call");
    final static String FOLD      = new String("fold");


    private boolean mResolvingConnectionFailure = false;
    private boolean mAutoStartSignInFlow = true;

    // The game information
    private TurnData turnData = new TurnData();

    // game participants with the id and a Player object
    private HashMap<String, Player> mParticipants = new HashMap<>();

    // ids of all participating Players, used for the turn order
    private ArrayList<String> playerIds = new ArrayList<String>();

    // array of players, used for the listview
    private ArrayList<Player> arrayOfPlayers = new ArrayList<Player>();

    // The game Room instance
    private Room mRoom;
    private AlertDialog mAlertDialog;

    private String mMyPersistentId;

    private String nextPlayerId;

    int cardCounter = 1;

    private GoogleApiClient apiClient;

    // used to format the listview
    HistoryListAdapter adapter;


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

        adapter = new HistoryListAdapter(this, arrayOfPlayers);
        updateUi();

        // check for invitations when starting the app
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

        findViewById(R.id.btnCreateGame).setEnabled(true);
        // check for invitations
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

        // Launch the sign-in flow if auto sign-in is enabled
        if (mAutoStartSignInFlow) {
            mAutoStartSignInFlow = false;
            mResolvingConnectionFailure = BaseGameUtils.resolveConnectionFailure(this,
                    apiClient, connectionResult, SIGN_IN_REQUEST,
                    getString(R.string.signin_other_error));
        }
    }
    // implementing the GameFragment interface
    @Override
    public void onRaiseButtonClicked() {
        // open a popup for setting the raise value
        buildRaiseButtonWindow();

        mParticipants.get(mMyPersistentId).setAction(RAISE);
        boolean gameOver = this.gameManager.gameOver(mParticipants);
        if(gameOver){
            Toast.makeText(this, " GAME OVER", Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "Own Hand Result: " +gameManager.getCardsResult(gameManager.getPlayerHand()), Toast.LENGTH_SHORT).show();

            leaveRoom();//TODO runde neustarten ohne das spiel zu beenden
        }
    }

    @Override
    public void onCallButtonClicked() {
        nextPlayerId = getNextPlayerId();

        // update the list with my own action
        updateListAfterButtonClick(CALL);

        // set the icon of the next player in the list
        updateListForActivePlayer();

        // instantiate the information byte array
        byte [] data = this.turnData.sendGameBroadcast(mParticipants.get(mMyPersistentId), nextPlayerId, 1.0f/*todo call coins*/, CALL);//

        // and broadcast the data to the other players
        this.sendGameBroadcast(data);

        // update the buttons
        gameFragment.setEnabled(isMyTurn());
        synchronizeOwnCoins();
        synchronizePot();
        mParticipants.get(mMyPersistentId).setAction(CALL);

        //float lastPlayerSetCoins = arrayOfPlayers.get(1).

        this.gameManager.playerCall(mParticipants.get(mMyPersistentId), 1.0f/*todo coins holen*/);


        boolean gameOver = this.gameManager.gameOver(mParticipants);
        if(gameOver){
            Toast.makeText(this, " GAME OVER", Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "Own Hand Result: " + gameManager.getCardsResult(gameManager.getPlayerHand()), Toast.LENGTH_SHORT).show();
            leaveRoom();
        }

    }

    @Override
    public void onFoldButtonClicked() {
        float playerOut = -1.0f;

        // remove the player from the list, so he can't take a turn anymore
        this.playerIds.remove(mMyPersistentId);
        nextPlayerId = getNextPlayerId();
        updateListAfterButtonClick(FOLD);
        updateListForActivePlayer();
        byte [] data = this.turnData.sendGameBroadcast(mParticipants.get(mMyPersistentId), nextPlayerId, playerOut, FOLD);//
        gameFragment.setEnabled(isMyTurn());
        this.sendGameBroadcast(data);
        mParticipants.get(mMyPersistentId).setAction(FOLD);



        boolean gameOver = this.gameManager.gameOver(mParticipants);
        if(gameOver){
            Toast.makeText(this, " GAME OVER", Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "Own Hand Result: " + gameManager.getCardsResult(gameManager.getPlayerHand()), Toast.LENGTH_SHORT).show();
            leaveRoom();
        }
    }

    /**
     * Updates the player history with the belonging action.
     *
     *
     * @param action which was made
     */
    private void updateListAfterButtonClick(String action){
        for(int i = 0; i< arrayOfPlayers.size(); i++){
            if(arrayOfPlayers.get(i).getPlayerID().equals(mMyPersistentId)){
                arrayOfPlayers.get(i).setAction(action);
            }
        }
        adapter.notifyDataSetChanged();
        gameFragment.listView.setAdapter(adapter);
    }

    /**
     * Updates the player history and sets the icon of the acive player
     */
    private void updateListForActivePlayer(){
        for(int i = 0; i< arrayOfPlayers.size(); i++){
            if(arrayOfPlayers.get(i).getPlayerID().equals(nextPlayerId)){
                arrayOfPlayers.get(i).setHasTurn(true);
            } else {
                arrayOfPlayers.get(i).setHasTurn(false);
            }
        }
        adapter.notifyDataSetChanged();
        gameFragment.listView.setAdapter(adapter);
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

    /**
     *  Opens the google play services invitation inbox view.
     */
    private void onCheckGamesClicked() {
        Intent intent = Games.Invitations.getInvitationInboxIntent(apiClient);
        startActivityForResult(intent, RC_INVITATION_INBOX);
    }

    @Override
    public void onShowRulesClicked() {
        getFragmentManager().beginTransaction().replace(R.id.fragment, ruleFragment).commit();
    }

    /**
     * Instantiate a new Realtime Multiplayer - Getting to the Invitation Lobby.
     */
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

            arrayOfPlayers.clear();
            // Build the room and start the match
            Games.RealTimeMultiplayer.create(apiClient, roomConfigBuilder.build());
        }
    }

    /**
     * Player left the room.
     */
    private void leaveRoom() {
        if (mRoom != null) {
            Games.RealTimeMultiplayer.leave(apiClient, this, mRoom.getRoomId());
            mRoom = null;
            updateUi();
        }
    }

    /**
     * Starting the match. Next Player is the hist
     */
    private void startMatch() {
        for(String id : mRoom.getParticipantIds()){
            Player p = new Player(mRoom.getParticipant(id));
            mParticipants.put(id, p);
        }
        nextPlayerId = mMyPersistentId;
        // deal cards to all players
        this.dealCards();
        updateUi();
    }

    /**
     * Retrieves the next player ID.
     *
     * @return the next player ID
     */
    private String getNextPlayerId(){
        int pos = playerIds.indexOf(mMyPersistentId);
        if(playerIds.size() == pos +1) {
            return playerIds.get(0);
        }
        return playerIds.get(pos + 1);
    }

    /**
     * Check whether my id is the next Players Id
     *
     * @return is my turn
     */
    private boolean isMyTurn() {
        if(nextPlayerId.equals(mMyPersistentId)){
            return true;
        }
        return false;
    }


    /**
     * Updating the App Fragments
     */
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
        } else if(mRoom == null && !mainFragment.isVisible()) {
            Log.d(TAG, "room null");
            getFragmentManager().beginTransaction().replace(R.id.fragment, mainFragment).commit();
        }
    }


    /**
     * RTMP Participant joined, register the Player if the Participant is connected.
     *
     * @param p the Participant from the Real-Time Multiplayer match.
     */
    private void onParticipantConnected(Participant p) {
        if (p.isConnectedToRoom()) {
            onParticipantConnected(new Player(p));
        }
    }

    /**
     * Add a Player to the ongoing game and update turn order. If the
     * Player is a duplicate, this method does nothing.
     * @param dp the Player to add.
     */
    private void onParticipantConnected(Player dp) {
        Log.d(TAG, "onParticipantConnected: " + dp.getPlayerID());
        if (!mParticipants.containsKey(dp.getPlayerID())) {
            mParticipants.put(dp.getPlayerID(), dp);

        }
        if(!arrayOfPlayers.contains(dp)){
            arrayOfPlayers.add(dp);
            playerIds.add(dp.getPlayerID());
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
    public void onInvitationReceived(final Invitation invitation) {
        final String inviterName = invitation.getInviter().getDisplayName();

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this)
                .setTitle("Einladung")
                .setMessage("Möchtest du Seka mit " + inviterName + " spielen?")
                .setNegativeButton("Nein", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Games.RealTimeMultiplayer.declineInvitation(apiClient,
                                invitation.getInvitationId());
                    }
                })
                .setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        acceptInvitation(invitation);
                    }
                });

        mAlertDialog = alertDialogBuilder.create();
        mAlertDialog.show();
    }

    /**
     * Accept the invitation and build RoomConfig with the right settings. Join the game.
     */
    private void acceptInvitation(Invitation invitation) {
        arrayOfPlayers.clear();
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
        // show waiting room until all players joined
        showWaitingRoom(room);
    }

    /**
     * Google Play Services Waiting Room.
     *
     * @param room the belonging game room
     */
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

    /**
     * Receiving a RealtimeMessage and process the data. Unpersisting the bytearray to JSON Objects and differ the actions.
     *
     * @param data bytearray
     */
    private void onMessageReceived(byte[] data) {
        turnData = turnData.unpersist(data);
        String karte;

        // deal the cards
        if(turnData.getAction().equals(NEW_CARD)){
            Cards card = new Cards(turnData.getCardType(), turnData.getCardCount());
            gameManager.setCardToPlayer(card);

            if (cardCounter % 3 == 1){
                ImageView vw = (ImageView) findViewById(R.id.imgVwSlot1);
                karte = checkCards(card);
                int resID = getResources().getIdentifier(karte, "drawable", getPackageName());
                vw.setImageResource(resID);
                Collections.sort(playerIds);
            } else if (cardCounter % 3 == 2){
                ImageView vw = (ImageView) findViewById(R.id.imgVwSlot2);
                karte = checkCards(card);
                int resID = getResources().getIdentifier(karte, "drawable", getPackageName());
                vw.setImageResource(resID);
            } else if (cardCounter % 3 == 0){
                ImageView vw = (ImageView) findViewById(R.id.imgVwSlot3);
                karte = checkCards(card);
                int resID = getResources().getIdentifier(karte, "drawable", getPackageName());
                vw.setImageResource(resID);
            }
            nextPlayerId = turnData.getNextTurnId();
            gameFragment.listView.setAdapter(adapter);
            gameFragment.setEnabled(isMyTurn());

            // Raise action
        } else if(turnData.getAction().equals(RAISE)) {
            Log.d(TAG, "Message received " + RAISE);
            updateList(RAISE);
            updateListForActivePlayer();
            gameManager.raise(turnData.getPlayerCoins());
            synchronizePot();

            // fold action
        } else if(turnData.getAction().equals((FOLD))) {
            Log.d(TAG, "Message received " + FOLD);
           for(int i = 0; i < playerIds.size(); i++){
               if(playerIds.get(i).equals(turnData.getPlayerId())){
                   playerIds.remove(i);
               }
           }

            updateList(FOLD);
            updateListForActivePlayer();

            // call action
        } else if(turnData.getAction().equals((CALL))) {
            Log.d(TAG, "Message received " + CALL);
            updateList(CALL);
            updateListForActivePlayer();
            gameManager.call(turnData.getPlayerCoins());
            synchronizePot();
        }
        if(!turnData.getAction().equals("newCard")) {
            mParticipants.get(turnData.getPlayerId()).setAction(turnData.getAction());
            Log.d(TAG, "Player Action: " + mParticipants.get(turnData.getPlayerId()).getAction());
        }
        boolean gameOver = gameManager.gameOver(mParticipants);

        if(gameOver){
            Toast.makeText(this, " GAME OVER", Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "Own Hand Result: " +gameManager.getCardsResult(gameManager.getPlayerHand()), Toast.LENGTH_SHORT).show();

            leaveRoom();//TODO runde neustarten ohne das spiel zu beenden
        }

        updateUi();
        cardCounter ++;
    }

    private void synchronizePot(){
        TextView vwPot = (TextView) findViewById(R.id.txtVwPot);
        String text = Float.toString(gameManager.getPotSize());
        vwPot.setText(text);
    }

    private void synchronizeOwnCoins(){
        TextView vwCoins = (TextView) findViewById(R.id.txtVwOwnCoins);
        String text = Float.toString(mParticipants.get(mMyPersistentId).getPlayerCoins());
        vwCoins.setText(text);
    }
    /**
     * updating the player history with the action when a message was received
     *
     * @param action to set
     */
    public void updateList(String action){
        for(int i = 0; i < arrayOfPlayers.size(); i++){
            if(arrayOfPlayers.get(i).getPlayerName().equals(turnData.getPlayerName())){
                arrayOfPlayers.get(i).setAction(action);
            }
        }
        // set the next player
        nextPlayerId = turnData.getNextTurnId();
        adapter.notifyDataSetChanged();
        gameFragment.listView.setAdapter(adapter);
        // update the buttons
        gameFragment.setEnabled(isMyTurn());
    }

    /**
     * Checks which card should be displayed
     *
     * @param card the card object to be displayed
     * @return the belonging string to the card
     */
    public String checkCards(Cards card){

        Cards ca = card;

        int wert = ca.getCardCount();
        int symbol = ca.getCardTyp();

        String karte = null;

        if (symbol == 0) {

            switch (wert){
                case 0:
                    karte = "karo_sechs";
                    break;
                case 1:
                    karte = "karo_sieben";
                    break;
                case 2:
                    karte = "karo_acht";
                    break;
                case 3:
                    karte = "karo_neun";
                    break;
                case 4:
                    karte = "karo_zehn";
                    break;
                case 5:
                    karte = "karo_bube";
                    break;
                case 6:
                    karte = "karo_dame";
                    break;
                case 7:
                    karte = "karo_koenig";
                    break;
                case 8:
                    karte = "karo_ass";
                    break;
            }
        }
        else if (symbol == 1){

            switch (wert){
                case 0:
                    karte = "herz_sechs";
                    break;
                case 1:
                    karte = "herz_sieben";
                    break;
                case 2:
                    karte = "herz_acht";
                    break;
                case 3:
                    karte = "herz_neun";
                    break;
                case 4:
                    karte = "herz_zehn";
                    break;
                case 5:
                    karte = "herz_bube";
                    break;
                case 6:
                    karte = "herz_dame";
                    break;
                case 7:
                    karte = "herz_koenig";
                    break;
                case 8:
                    karte = "herz_ass";
                    break;
            }
        }
        else if (symbol == 2){

            switch (wert){
                case 0:
                    karte = "kreuz_sechs";
                    break;
                case 1:
                    karte = "kreuz_sieben";
                    break;
                case 2:
                    karte = "kreuz_acht";
                    break;
                case 3:
                    karte = "kreuz_neun";
                    break;
                case 4:
                    karte = "kreuz_zehn";
                    break;
                case 5:
                    karte = "kreuz_bube";
                    break;
                case 6:
                    karte = "kreuz_dame";
                    break;
                case 7:
                    karte = "kreuz_koenig";
                    break;
                case 8:
                    karte = "kreuz_ass";
                    break;
            }
        }
        else if (symbol == 3){

            switch (wert){
                case 0:
                    karte = "pik_sechs";
                    break;
                case 1:
                    karte = "pik_sieben";
                    break;
                case 2:
                    karte = "pik_acht";
                    break;
                case 3:
                    karte = "pik_neun";
                    break;
                case 4:
                    karte = "pik_zehn";
                    break;
                case 5:
                    karte = "pik_bube";
                    break;
                case 6:
                    karte = "pik_dame";
                    break;
                case 7:
                    karte = "pik_koenig";
                    break;
                case 8:
                    karte = "pik_ass";
                    break;
            }
        }
        return karte;

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

    /**
     * Player left the room and the game. Updating the lists and the playerIDs for the turn order.
     *
     * @param messagingId
     * @param persistentId
     */
    private void onParticipantDisconnected(String messagingId, String persistentId) {
            Log.d(TAG, "onParticipantDisconnected:" + messagingId);
            Player dp = mParticipants.remove(persistentId);
            for(int i = 0; i<arrayOfPlayers.size(); i++){
                if(arrayOfPlayers.get(i).getPlayerID().equals(persistentId)){
                    arrayOfPlayers.remove(i);
                }
            }
            if (dp != null) {
                // Display disconnection toast
                Toast.makeText(this, dp.getPlayerName() + " disconnected.", Toast.LENGTH_SHORT).show();
                if (mRoom != null && mParticipants.size() <= 1) {
                    // Last player left in an RTMP game, leave
                    leaveRoom();
                } else {
                    playerIds.remove(persistentId);
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

    /**
     * The raise Button Popup. The player can type a raise value.
     */
    private void buildRaiseButtonWindow() {

        AlertDialog.Builder raiseWindowBuilder = new AlertDialog.Builder(this);

        LayoutInflater inflater = this.getLayoutInflater();

        View view = inflater.inflate(R.layout.raise_window, null);
        raiseWindowBuilder.setView(view);

        final AlertDialog Window = raiseWindowBuilder.create();

        final EditText raise = (EditText) view.findViewById(R.id.edtTxtRaiseWert);
        raise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                raise.setText("");
            }
        });
        final ImageButton button = (ImageButton) view.findViewById(R.id.raise_button_popup);
        button.setOnClickListener(new ImageButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                final float raiseCoin = Float.valueOf(raise.getText().toString());
                if (raiseCoin < gameManager.getMinCall()) {
                    Toast.makeText(getApplicationContext(), "Bitte geben Sie mindestens den Startbetrag ein!", Toast.LENGTH_LONG).show();
                    raise.setText("");
                } else if (raiseCoin > mParticipants.get(mMyPersistentId).getPlayerCoins()) {
                    Toast.makeText(getApplicationContext(), "Sie k?nnen nur so viel einsetzen, wie Sie besitzen!", Toast.LENGTH_LONG).show();
                    raise.setText("");
                } else {
                    nextPlayerId = getNextPlayerId();
                    updateListAfterButtonClick(RAISE);
                    updateListForActivePlayer();
                    byte [] data = turnData.sendGameBroadcast(mParticipants.get(mMyPersistentId), nextPlayerId, raiseCoin/*set coins*/, RAISE);
                    sendGameBroadcast(data);
                    gameFragment.setEnabled(isMyTurn());
                    gameManager.playerRaise(mParticipants.get(mMyPersistentId), raiseCoin);
                    synchronizePot();
                    synchronizeOwnCoins();
                    Window.dismiss();
                }
            }
        });
        Window.show();

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

    /**
     * Deal the cards to all participants.
     *
     * @return
     */
    public byte[] dealCards(){
        List<Cards> stack = gameManager.getStack();

        int count  = 0;//while zaehler
        int cards  = 0;//aktuelle karte
        int rounds = 3;//Anzahl der runden
        String karte;

        while(count < rounds) {

            for (Player participant : mParticipants.values()) {
                if (!participant.getPlayerID().equals(mMyPersistentId)) {
                    byte[] data = turnData.cardJson(stack.get(cards).getCardTyp(), stack.get(cards).getCardCount(), rounds, mMyPersistentId);
                    Log.d(TAG, "reliablemessage to:" + participant.getPlayerName() + participant.getPlayerID());
                    Games.RealTimeMultiplayer.sendReliableMessage(apiClient, null,
                           data , mRoom.getRoomId(), participant.getPlayerID());
                }else{
                    gameManager.setCardToPlayer(stack.get(cards));

                }

                cards++;
            }
            count++;
        }
        hostCardDeal();
        return null;
    }

    /**
     * deal the cards to the host, because the sender can't get a reliable message.
     */
    public void hostCardDeal(){
        String[] karten = new String[3];
        List<Cards> li = gameManager.getPlayerHand();
        for(int i = 0; i < li.size(); i++){
            karten[i] = checkCards(li.get(i));
        }
        gameFragment.listView.setAdapter(adapter);
        gameFragment.setEnabled(isMyTurn());
        Collections.sort(playerIds);
        ImageView vw1 = (ImageView) findViewById(R.id.imgVwSlot1);
        ImageView vw2 = (ImageView) findViewById(R.id.imgVwSlot2);
        ImageView vw3 = (ImageView) findViewById(R.id.imgVwSlot3);
        int Id1 = getResources().getIdentifier(karten[0], "drawable", getPackageName());
        int Id2 = getResources().getIdentifier(karten[1], "drawable", getPackageName());
        int Id3 = getResources().getIdentifier(karten[2], "drawable", getPackageName());
        vw1.setImageResource(Id1);
        vw2.setImageResource(Id2);
        vw3.setImageResource(Id3);
    }

    /**
     * Broadcast the turn information to all players.
     *
     * @param data the information to be send.
     */
    private void sendGameBroadcast(byte[] data){
        for (Player participant : mParticipants.values()) {
            if (!participant.getPlayerID().equals(mMyPersistentId)) {
                Log.d(TAG, "reliablemessage to:" + participant.getPlayerName() + participant.getPlayerID());
                Games.RealTimeMultiplayer.sendReliableMessage(apiClient, null,
                        data, mRoom.getRoomId(), participant.getPlayerID());
            }
        }
        //TODO listen eintrag
    }

    /**
     * finishing the game
     */
    private void gameFinished(){
        int call   = 0;
        int raise  = 0;
        int player = playerIds.size();

        for(int i = 0; i < player; i++){
            if(mParticipants.get(playerIds.get(i)).getAction().equals("call")){
                call++;
            }else if(mParticipants.get(playerIds.get(i)).getAction().equals("raise")){
                raise++;
            }
        }
        if(call == player){
            float result = gameManager.getCardsResult(mParticipants.get(mMyPersistentId).getHand());
            mParticipants.get(mMyPersistentId).setHandResult(result);
            this.sendGameBroadcast(turnData.sendGameBroadcast(mParticipants.get(mMyPersistentId), null, result, RESULT));
            //todo call game over method and put result in own list
            Log.d(TAG, "GAME OVER:" + "all player call");
        }else if(player == raise){
            //todo call game over method
            Log.d(TAG, "GAME OVER:" + "all player fold");
        }
    }
}
