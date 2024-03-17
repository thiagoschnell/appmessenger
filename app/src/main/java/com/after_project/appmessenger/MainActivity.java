package com.after_project.appmessenger;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
public class MainActivity extends AppCompatActivity {
    private AppMessenger appMessenger;
    final String TAG = this.getClass().getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ((Button)findViewById(R.id.OpenMainActivity2)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,MainActivity2.class);
                startActivity(intent);
            }
        });
        ((Button)findViewById(R.id.SendMessageTest1)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                {
                    try {
                        appMessenger.sendMsgTest(Message.obtain(null, Messenger.MSG_CLIENT_TEST));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        mainActivityMessenger(MainActivity.this,"main");
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(appMessenger.getStatus()==AppMessenger.MESSENGER_CLIENT_STATUS_CONNECTED){
            try {
                appMessenger.disconnect();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    private void mainActivityMessenger(Context context, String name){
        if(appMessenger==null)
        {
            appMessenger = new AppMessenger(context, name);
            try {
                appMessenger.setCallback(new AppMenssengerCallback() {
                    @Override
                    public void onAttached() {
                        System.out.println("The client("+TAG+") has attached to the server");
                    }

                    @Override
                    public void onClose() {
                        System.out.println("The client("+TAG+") connection to the server has been terminated");
                    }

                    @Override
                    public void onBinding() {
                        System.out.println("The client("+TAG+") has been linked to the server");
                    }

                    @Override
                    public void onDisconnected(Message msg) {
                        System.out.println("Client("+TAG+") has disconncted from the server");
                    }

                    @Override
                    public void onUnbinding() {
                        System.out.println("The client("+TAG+") is detaching from the server");
                    }
                    @Override
                    public void onConnected(Message msg) {
                        System.out.println("The client("+TAG+") has connected to the server");
                    }

                    @Override
                    public void onConnectionChanges(MessengerConnection.ConnectionState state, MessengerConnection.ConnectionStatus status, String textStatus) {
                        System.out.println("The client("+TAG+") connection has change. State: " + String.valueOf(state) + " Status: " + String.valueOf(status) + " Description: " + textStatus);
                    }

                    @Override
                    public void onReceiveMessage(Message msg) {
                        switch (msg.what){
                            case Messenger.MSG_CLIENT_TEST:{
                                System.out.println("Client("+TAG+") Received MSG_CLIENT_TEST successfully");
                                break;
                            }
                            case Messenger.MSG_CLIENT_TEST2:{
                                System.out.println("Client("+TAG+") Received MSG_CLIENT_TEST2 message: " + msg.getData().getString("my_message"));
                                break;
                            }

                        }
                    }
                });
                appMessenger.connect();
            } catch (Exception e) {
                if(e instanceof ClientIsBinding){
                }else if(e instanceof ClientIsConnecting){
                }else if(e instanceof ClientAlreadyConnected){
                }else if(e instanceof ClientConnectException){
                }
                e.printStackTrace();
            }
        }
    }
}