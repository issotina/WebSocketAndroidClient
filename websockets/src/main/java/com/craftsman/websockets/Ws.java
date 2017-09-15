package com.craftsman.websockets;

/**
 * Created by ALI SHADAÏ (Software Craftman) on 15/09/2017.
 */

public interface Ws {

    /**
     *
     * @return
     */
     Ws connect() throws Exception;


    /**
     *
     * @param channelPath
     * @param wsListner
     */
    <T> Ws on(String channelPath,Class<T> exceptedDataType, WsListner<T> wsListner);


    /**
     *
     * @param channelPath
     * @param wsListner
     * @return
     */
     Ws on(String channelPath, WsListner wsListner);

    /**
     *
     * @param channelPath
     * @param text
     */
    void send(String channelPath,String text);


    /**
     *
     * @param channelPath
     * @param binary
     */
    void send(String channelPath,byte[] binary);

    /**
     *
     */
    interface WsListner<T> {

        void onEvent(String eventUri,T data);
    }


    class Builder {
        public Builder(){
        }

        public WsImpl from(String websocketServerUri){
            return new WsImpl(websocketServerUri);
        }
    }
}
