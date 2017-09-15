package com.craftsman.websockets;

import android.util.Log;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import de.tavendo.autobahn.Autobahn;
import de.tavendo.autobahn.AutobahnConnection;

/**
 * Created by ALI SHADAÏ (Software Craftman) on 15/09/2017.
 */

public class WsImpl implements Ws {

    final String TAG = "Web Socket Impl";

    AutobahnConnection autobahnConnection = new AutobahnConnection();
    final List<Payload> subscriptions = new ArrayList<>();
    String serverUrl;

    public WsImpl(String websocketServerUri) {
        serverUrl = websocketServerUri;
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
                Log.i(TAG,"Disconnected");
            }
        });
        return this;
    }

    @Override
    public <T> Ws on(final String channelPath, final Class<T> exceptedDataType, final WsListner<T> wsListner) {

        if(!autobahnConnection.isConnected()){
            subscriptions.add(new Payload<>(channelPath,exceptedDataType,wsListner));
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
        if(!autobahnConnection.isConnected()){
            subscriptions.add(new Payload<>(channelPath,null,wsListner));
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
            autobahnConnection = null;
        }
    }

    final private class Payload<T>{
        String channel;
        Class<T> objectType;
        WsListner listner;



        public Payload(String channel, Class<T> objectType, WsListner listner) {
            this.channel = channel;
            this.objectType = objectType;
            this.listner = listner;
        }
    }
}
