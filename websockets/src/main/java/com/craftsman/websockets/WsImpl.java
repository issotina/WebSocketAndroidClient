package com.craftsman.websockets;

import android.os.Handler;
import android.util.Log;

import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import de.tavendo.autobahn.Autobahn;
import de.tavendo.autobahn.AutobahnConnection;

@SuppressWarnings("unchecked")
public class WsImpl implements Ws {
    private final String TAG = "Web Socket Impl";
    private final List<Payload> subscriptions = new ArrayList<>();
    private Handler mainHandler = new Handler();
    private AutobahnConnection autobahnConnection = new AutobahnConnection();
    private String serverUrl;
    private Runnable handleSocketReconnection = new Runnable() {
        @Override
        public void run() {
            try {
                if (autobahnConnection != null && !autobahnConnection.isConnected())
                    connect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    WsImpl(String websocketServerUri) {
        serverUrl = websocketServerUri;
    }

    public void changeSocketURI(String host, String port) throws Exception {

        if (serverUrl != null && !serverUrl.isEmpty()) {

            if (autobahnConnection != null && autobahnConnection.isConnected()) {
                end();
            }

            String[] spliter = serverUrl.split(":");
            if (host != null && !host.isEmpty()) {
                spliter[1] = "//" + host;
            }

            if (port != null && !port.isEmpty()) {
                spliter[2] = port;
            }

            serverUrl = StringUtils.join(spliter, ":");

            connect();
        }
    }

    @Override
    public Ws connect() throws Exception {
        if(serverUrl == null || !serverUrl.startsWith("ws")){
            throw new Exception("Right server url is not provided");
        }
        autobahnConnection.connect(serverUrl, new Autobahn.SessionHandler() {
            @Override
            public void onOpen() {
                Log.i(TAG,"Connected");

                for (final Payload payload: subscriptions) {
                    //setup listener for all subscribed channel
                    if(payload == null || payload.channel == null || payload.channel.isEmpty()) continue;
                    else if(payload.channel.startsWith("/")) payload.channel = payload.channel.substring(1);
                    autobahnConnection.subscribe(payload.channel, Object.class, new Autobahn.EventHandler() {
                        @Override
                        public void onEvent(String s, Object o) {
                            try {
                                payload.listner.onEvent(s,
                                        (payload.objectType != null) ?
                                                new Gson().fromJson(o.toString(),payload.objectType)
                                                 : o);
                            }
                            catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
            @Override
            public void onClose(int i, String s) {
                //force recnnection to web socket
                Log.i(TAG, "Disconnected; Code " + i);

                if (i == 1 || i == 3 || i == 2 || i == 4 || i == 5) {
                    mainHandler.removeCallbacks(handleSocketReconnection);
                    mainHandler.postDelayed(handleSocketReconnection, 15000);
                }
            }
        });
        return this;
    }

    @Override
    public <T> Ws on(final String channelPath, final Class<T> exceptedDataType, final WsListner<T> wsListner) {
        subscriptions.add(new Payload<>(channelPath, exceptedDataType, wsListner));

        if (!autobahnConnection.isConnected()) {
            return this;
        }
        else {
            autobahnConnection.subscribe(channelPath, Object.class, new Autobahn.EventHandler() {
                @Override
                public void onEvent(String s, Object o) {
                    wsListner.onEvent(s,new Gson().fromJson(o.toString(),exceptedDataType));
                }
            });
        }

        return this;
    }


    @Override
    public Ws on(String channelPath, final WsListner wsListner) {
        subscriptions.add(new Payload<>(channelPath, null, wsListner));
        if (!autobahnConnection.isConnected()) {
            return this;
        }
        else autobahnConnection.subscribe(channelPath, Object.class, new Autobahn.EventHandler() {
            @Override
            public void onEvent(String s, Object o) {
                wsListner.onEvent(s, o);
            }
        });

        return this;
    }

    @Override
    public Ws unsubscribe(String channelPath) {
        for (Payload payload : subscriptions) {
            if (StringUtils.equals(payload.channel, channelPath)) {
                if (autobahnConnection != null) {
                    if (autobahnConnection.isConnected())
                        autobahnConnection.unsubscribe(channelPath);

                    subscriptions.remove(payload);
                }
            }
        }
        return this;
    }

    @Override
    public Ws unsubscribe(List<String> channelPath) {
        for (String payload : channelPath) {
            unsubscribe(payload);
        }
        return this;
    }

    @Override
    public void send( String text) {
        if(autobahnConnection.isConnected())
            autobahnConnection.sendTextMessage(text);
    }

    @Override
    public void send( byte[] binary) {
        if(autobahnConnection.isConnected())
            autobahnConnection.sendBinaryMessage(binary);
    }

    @Override
    public void send(String channelPath, Object o) {
        if(autobahnConnection.isConnected())
            autobahnConnection.publish(channelPath,o);
    }

    @Override
    public void end() {
        if(autobahnConnection != null && autobahnConnection.isConnected()) {
            autobahnConnection.unsubscribe();
            autobahnConnection.disconnect();
        }
    }

    final private class Payload<T>{
        private String channel;
        private Class<T> objectType;
        private WsListner listner;

        Payload(String channel, Class<T> objectType, WsListner listner) {
            this.channel = channel;
            this.objectType = objectType;
            this.listner = listner;
        }
    }
}