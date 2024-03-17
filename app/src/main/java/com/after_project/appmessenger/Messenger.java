package com.after_project.appmessenger;
// Copyright (c) Thiago Schnell | https://github.com/thiagoschnell/appmessenger/blob/main/LICENSE
// Licensed under the MIT License.
import static com.after_project.appmessenger.MessengerConnection.ConnectionState.CONNECTION_STATE_NOT_ADDED;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import androidx.annotation.NonNull;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
public class Messenger extends Service {
    protected static final int MSG_REGISTER_CLIENT = 1;
    protected static final int MSG_UNREGISTER_CLIENT = 2;
    protected static final int MSG_CLIENT_CONNECTED = 10;
    protected static final int MSG_CLIENT_DISCONNECTED = 11;
    protected static final int MSG_CLIENT_CONNECTION_STATE_CHANGE = 12;
    protected static final int MSG_CLIENT_TEST = 40;
    protected static final int MSG_CLIENT_TEST2 = 41;
    protected static final int MSG_CLIENT_TEST3 = 42;
    private MessengerConnectionManager messengerConnectionManager = null;
    private Map<android.os.Messenger, MessengerConnection> mClients = new HashMap<>();
    private final android.os.Messenger mMessenger = new android.os.Messenger(new IncomingHandler());
    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            try{
                switch (msg.what){
                    case MSG_REGISTER_CLIENT:{
                        final MessengerConnection reqquestConnection = new Gson().fromJson(JsonParser.parseString(msg.getData().getString("config")).getAsJsonObject(),MessengerConnection.class);
                            messengerConnectionManager.addConnection(reqquestConnection, msg, new MessengerConnection.MessengerConnectionCallback() {
                                @Override
                                public void onConnectionSuccess(MessengerConnection connection, MessengerConnection.ConnectionStatus connectionStatus) {
                                    addClient(connection, msg.replyTo);
                                    Message message = Message.obtain(null,MSG_CLIENT_CONNECTED);
                                    message.getData().putInt("connectionId",connection.getConnectionId());
                                    message.getData().putInt("hashcode",msg.getData().getInt("hashcode"));
                                    try {
                                        replyMsg(message,msg.replyTo);
                                    } catch (Exception e) {
                                        throw new RuntimeException(e);
                                    }
                                    {
                                        setConnectionChanges(msg,connection.getConnectionState(),connection.getConnectionStatus());
                                    }
                                }
                                @Override
                                public void onConnectionError(MessengerConnection.ConnectionStatus connectionStatus) {
                                    setConnectionChanges(msg, CONNECTION_STATE_NOT_ADDED, connectionStatus);
                                }
                            });
                        break;
                    }
                    case MSG_UNREGISTER_CLIENT:{
                        removeClient(msg.replyTo);
                        {
                        }
                        replyMsg(Message.obtain(null,MSG_CLIENT_DISCONNECTED), msg.replyTo);
                        if(msg.getData().containsKey("connectionId")) {
                            JsonObject jsonObjectChanges = messengerConnectionManager.endConnection(msg.getData().getInt("connectionId"));
                            setConnectionChanges(msg, jsonObjectChanges.get("connectionState").getAsString(), jsonObjectChanges.get("connectionStatus").getAsString());
                        }
                        break;
                    }
                    default:{
                        onReceiveMessage(msg);
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    private void setConnectionChanges(final android.os.Message msg, final MessengerConnection.ConnectionState state, final MessengerConnection.ConnectionStatus status){
        setConnectionChanges(msg,String.valueOf(state),String.valueOf(status));
    }
    private void setConnectionChanges(final android.os.Message msg, final String state, final String status){
        {
            Message message = Message.obtain(null,MSG_CLIENT_CONNECTION_STATE_CHANGE);
            message.getData().putString("connectionStatus", status);
            message.getData().putString("connectionState", state);
            try{
                replyMsg(message,msg.replyTo);
            }catch (Exception e){
                throw new RuntimeException(e);
            }
        }
    }
    private void onReceiveMessage(Message msg){
        {
            if (msg.what == MSG_CLIENT_TEST) {
                try {
                    msg.replyTo.send(Message.obtain(null,MSG_CLIENT_TEST));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }else
            if (msg.what == MSG_CLIENT_TEST2) {
                final int connectionId = msg.getData().getInt("connectionId");
                MultiClientOptions multiClientOptions = null;
                if(msg.getData().containsKey("multiClientOptions")) {
                    multiClientOptions = new Gson().fromJson(msg.getData().getString("multiClientOptions"),MultiClientOptions.class);
                }
                Message message = Message.obtain(null,MSG_CLIENT_TEST2);
                message.getData().putString("my_message",msg.getData().getString("my_message"));
                try {
                    sendMessageToClient(message,connectionId,multiClientOptions);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }else
            if (msg.what == MSG_CLIENT_TEST3) {
            }
        }
    }
    @Override
    public void onCreate() {
        super.onCreate();
        {
            try{
                if(messengerConnectionManager==null){
                    messengerConnectionManager = new MessengerConnectionManager();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    private void addClient(final MessengerConnection connection, final android.os.Messenger msg){
        mClients.put(msg,connection);
    }
    private void removeClient(final android.os.Messenger msg){
        mClients.remove(msg);
    }
    private int getClientsCount(){
        return mClients.size();
    }
    private boolean containsClient(final android.os.Messenger client){
        return mClients.containsKey(client);
    }
    private Map.Entry<android.os.Messenger,MessengerConnection> getConnection(int connectionId){
        Iterator<Map.Entry<android.os.Messenger,MessengerConnection>> myVeryOwnIterator = mClients.entrySet().iterator();
        HashMap.Entry<android.os.Messenger,MessengerConnection> entry = null;
        while(myVeryOwnIterator.hasNext()) {
            entry = myVeryOwnIterator.next();
            if(entry.getValue().getConnectionId()==connectionId){
                break;
            }
        }
        return entry;
    }
    private void sendMessageToClient(android.os.Message msg, int connectionId, MultiClientOptions multiClientOptions) throws Exception {
        final HashMap.Entry<android.os.Messenger,MessengerConnection> entry = getConnection(connectionId);
        if (!entry.getValue().isMultiClient()) {
            if(entry!=null) {
                Message message = new Message();
                message.copyFrom(msg);
                entry.getKey().send(message);
            }
        } else {
            if(multiClientOptions==null){
            }else if(multiClientOptions!=null){
                sendMessageToClients(msg, multiClientOptions);
            }
        }
    }
    private void sendMessageToClients(final android.os.Message msg, final MultiClientOptions multiClientOptions) throws Exception{
        Iterator<Map.Entry<android.os.Messenger,MessengerConnection>> myVeryOwnIterator = mClients.entrySet().iterator();
        while(myVeryOwnIterator.hasNext()) {
            HashMap.Entry<android.os.Messenger,MessengerConnection> entry = myVeryOwnIterator.next();
            Message message = new Message();
            message.copyFrom(msg);
            for(ClientRulesMatch clientRulesMatch : multiClientOptions.clientRulesMatches){
                if (clientRulesMatch.rulesMatchType.equals(RulesMatchType.MATCH_TYPE_NAME)) {
                    if (!clientRulesMatch.matchWith(clientRulesMatch.toArray(), entry.getValue().getName())) {
                    } else {
                        entry.getKey().send(message);
                    }
                }
                if (clientRulesMatch.rulesMatchType.equals(RulesMatchType.MATCH_TYPE_TAG)) {
                    if (!clientRulesMatch.matchWith(clientRulesMatch.toArray(), entry.getValue().getTag())) {
                    } else {
                        entry.getKey().send(message);
                    }
                }
            }
        }
    }
    private void replyMsg(final Message msg, final android.os.Messenger messenger) throws Exception {
        Message message = new Message();
        message.copyFrom(msg);
        messenger.send(message);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }
}
class MultiClientOptions{
    ClientRulesMatch[] clientRulesMatches = null;
    MultiClientOptions(ClientRulesMatch... clientRulesMatches ){
        this.clientRulesMatches = clientRulesMatches;
    }
}
class ClientRulesMatch extends RulesMatch {
    ClientRulesMatch(String...stringsToMatch){
        super(stringsToMatch);
    }
}
class ClientMatchByTags extends ClientRulesMatch {
    ClientMatchByTags(String...matchs){
        super(matchs);
        this.rulesMatchType = RulesMatchType.MATCH_TYPE_TAG;
    }
}
class ClientMatchByNames extends ClientRulesMatch {
    ClientMatchByNames(String...matchs){
        super(matchs);
        this.rulesMatchType = RulesMatchType.MATCH_TYPE_NAME;
    }
}
enum RulesMatchType{
    MATCH_TYPE_NAME,
    MATCH_TYPE_TAG
}
abstract class AbstractRulesMatch{
    RulesMatchType rulesMatchType = null;
    abstract String[] toArray();
}
class RulesMatch extends AbstractRulesMatch{
    private String[] stringsToMatch = null;
    RulesMatch(String...stringsToMatch){
        this.stringsToMatch = stringsToMatch;
    }
    String[] getStringsToMatch() {
        return stringsToMatch;
    }
    @Override
    String[] toArray() {
        return stringsToMatch;
    }
    boolean matchWith(String[] strings, String with){
        return Arrays.asList(strings).indexOf(with) > -1;
    }
}
class MessengerConnectionImpl{
    private String tag = null;
    private Integer clientId = null;
    private String name = null;
    Integer getClientId() {
        return clientId;
    }
    String getTag() {
        return tag;
    }
    String getName() {
        return name;
    }
    MessengerConnectionImpl(@NonNull String tag, @NonNull Integer clientId, @NonNull String name) {
        this.tag = tag;
        this.name = name;
        this.clientId = clientId;
    }
}
abstract class AbstractMessengerConnection extends MessengerConnectionImpl{
     AbstractMessengerConnection(@NonNull String tag, @NonNull Integer clientId, @NonNull String name) {
         super(tag, clientId, name);
     }
     abstract Integer getConnectionId();
}
class MessengerConnection extends AbstractMessengerConnection {
    private Integer connectionId = null;
    @Override
    Integer getConnectionId() {
        return connectionId;
    }
    enum ConnectionState{
        CONNECTION_STATE_OK,
        CONNECTION_STATE_CANCELLED,
        CONNECTION_STATE_ENDED,
        CONNECTION_STATE_ADDED,
        CONNECTION_STATE_NOT_ADDED,
        //CONNECTION_STATE_REMOVED
    }
    enum ConnectionStatus{
        ERROR_MAX_CONNECTION_EXCEEDED,
        ERROR_CONNECTION_UNAVAILABLE,
        ERROR_CONNECTION_MULTCLIENT_PARALLEL_LIMIT_EXCEEDED,
        ERROR_CONNECTION_NORMAL_PARALLEL_LIMIT_EXCEEDED,
        ERROR_CONNECTION_MULTCLIENT_NO_MATCHS_FOUND,
        ERROR_CONNECTION_NORMAL_NO_MATCHS_FOUND,
        ERROR_CONNECTION_MULTCLIENT_ALREADY_CONNECTED,
        ERROR_CONNECTION_NORMAL_ALREADY_CONNECTED,
        ERROR_CONNECTION_TAG_NOT_REGISTERED,
        ERROR_CONNECTION_RULES_INCOMPLETE_MATCH_ARGUMENTS,
        CONNECTION_CLIENT_SUCCESS,
        CONNECTION_MULTICLIENT_SUCCESS,
        CONNECTION_CLIENT_DISCONNECTED,
        ERROR_UNKOWN
    }
    private ConnectionStatus connectionStatus = null;
    private ConnectionState connectionState = null;
    private Boolean isMultiClient = null;
    private Boolean isParallel = null;
    MessengerConnection(@NonNull String tag, @NonNull Integer clientId, @NonNull String name, Integer connectionId, boolean isMultiClient, boolean isParallel) {
        super(tag,clientId,name);
        this.connectionId = connectionId;
        this.isMultiClient = isMultiClient;
        this.isParallel = isParallel;
    }
    protected boolean isMultiClient() {
        return isMultiClient;
    }
    protected Boolean isParallel() {
        return isParallel;
    }
    protected ConnectionState getConnectionState() {
        return connectionState;
    }
    protected ConnectionStatus getConnectionStatus() {
        return connectionStatus;
    }
    protected interface MessengerConnectionCallback {
        void onConnectionSuccess(MessengerConnection connection, ConnectionStatus connectionStatus);
        void onConnectionError(ConnectionStatus connectionStatus);
    }
}