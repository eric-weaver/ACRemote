package com.weaver.eric.acremote.services;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.enums.PNStatusCategory;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;
import com.weaver.eric.acremote.R;
import com.weaver.eric.acremote.Constants;

import java.util.Collections;

public class PubNubService extends Service {

    private Callbacks activity;
    private PubNub pubnub;
    private final IBinder mBinder = new PubNubBinder();

    private SubscribeCallback subscribeCallback = new SubscribeCallback() {
        @Override
        public void status(PubNub pubnub, PNStatus status) {
            if (status.getCategory() == PNStatusCategory.PNConnectedCategory) {
                if (status.getCategory() == PNStatusCategory.PNConnectedCategory) {
                    activity.onPNConnected();
                }
                else if (status.getCategory() == PNStatusCategory.PNDisconnectedCategory) {
                    activity.onPNDisconnected();
                }
            }
        }

        @Override
        public void message(PubNub pubnub, PNMessageResult message) {
            if (message.getChannel() != null) {
                String msg = message.getMessage().getAsString();
                activity.onPNMessageReceived(msg);
            }
        }

        @Override
        public void presence(PubNub pubnub, PNPresenceEventResult presence) {

        }
    };

    public class PubNubBinder extends Binder {
        public PubNubService getService() {
            return PubNubService.this;
        }
    }

    public PubNub getPubNub() {
        if(null == pubnub){
            PNConfiguration pnConfiguration = new PNConfiguration();
            pnConfiguration.setSubscribeKey(getString(R.string.pubnub_subscribe_key));
            pnConfiguration.setPublishKey(getString(R.string.pubnub_publish_key));
            pnConfiguration.setSecure(true);

            pubnub = new PubNub(pnConfiguration);
        }

        return pubnub;
    }

    public void publishMessage(String msg){
        this.getPubNub().publish().channel(Constants.CHANNEL_PUBLISH).message(msg).async(
                new PNCallback<PNPublishResult>() {
            @Override
            public void onResponse(PNPublishResult result, PNStatus status) {
                if (!status.isError()){
                    Log.d(Constants.LOG_TAG, "Successfully published message");
                }
                // Request processing failed.
                else {
                    activity.onPNMessagePublishError(status.getStatusCode());
                    Log.d(Constants.LOG_TAG, "Failed to publish message");
                }
            }
        });
    }

    @Override
    public void onCreate() {
        super.onCreate();

        this.getPubNub().addListener(subscribeCallback);
        this.getPubNub().subscribe().channels(Collections.singletonList(Constants.CHANNEL_SUBSCRIBE)).execute();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (pubnub != null) pubnub.destroy();
        return super.onUnbind(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void registerClient(Activity activity){
        this.activity = (Callbacks)activity;
    }

    public interface Callbacks{
        void onPNConnected();
        void onPNDisconnected();
        void onPNMessageReceived(String msg);
        void onPNMessagePublishError(int statusCode);
    }
}
