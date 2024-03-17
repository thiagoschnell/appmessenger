# appmessenger
App Messenger is a part of [**Web App Api for android easier request your Api and supports for using (CORS)**](https://github.com/thiagoschnell/webappapi).

<h1>USAGE</h1>


Copy ![AppMessenger.java](https://github.com/thiagoschnell/appmessenger/blob/main/app/src/main/java/com/after_project/appmessenger/AppMessenger.java), ![Messenger.java](https://github.com/thiagoschnell/appmessenger/blob/main/app/src/main/java/com/after_project/appmessenger/Messenger.java), ![MessengerConnectionManager.java](https://github.com/thiagoschnell/appmessenger/blob/main/app/src/main/java/com/after_project/appmessenger/MessengerConnectionManager.java) to youur project.

Then go to ![manifest file](https://github.com/thiagoschnell/appmessenger/blob/main/app/src/main/AndroidManifest.xml)

and add 
```
<service android:name=".Messenger" />
```
go to MessengerConnectionManager.java and move to the his constructor 

add a new connection police for MainActivity.class
```
 connectionPolicies.add(new ConnectionPolicy(MainActivity.class, CONNECTION_NORMAL,
                1, new ConnectionRules(false,0, new ConnectionNormalMatchByNames("main")))  );
```

go to MainActivity.java

add this method
```
private void mainActivityAppMessenger(Context context, String name){       
        {
            AppMessenger appMessenger = new AppMessenger(context, name);
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
                        {
                            try {
                                appMessenger.sendMsgTest(Message.obtain(null, Messenger.MSG_CLIENT_TEST));
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
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

```

now execute the method by calling
```
mainActivityAppMessenger(MainActivity.this,"main");
```
