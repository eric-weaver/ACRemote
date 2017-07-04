package com.weaver.eric.acremote;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.weaver.eric.acremote.services.PubNubService;

public class MainActivity extends AppCompatActivity implements PubNubService.Callbacks {

    private PubNubService mService;
    private Button btnKeyPower;
    private Button btnKeyUp;
    private Button btnKeyDown;
    private Button btnKeyLeft;
    private Button btnKeyRight;


    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(Constants.LOG_TAG, "connecting to service");
            PubNubService.PubNubBinder binder = (PubNubService.PubNubBinder) service;
            mService = binder.getService();
            mService.registerClient(MainActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(Constants.LOG_TAG, "disconnecting from service");
        }
    };

    private View.OnClickListener onBtnPower = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mService.publishMessage(Constants.KEY_POWER);
        }
    };

    private View.OnClickListener onBtnUp = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mService.publishMessage(Constants.KEY_UP);
        }
    };

    private View.OnClickListener onBtnDown = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mService.publishMessage(Constants.KEY_DOWN);
        }
    };

    private View.OnClickListener onBtnLeft = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mService.publishMessage(Constants.KEY_LEFT);
        }
    };

    private View.OnClickListener onBtnRight = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mService.publishMessage(Constants.KEY_RIGHT);
        }
    };

    @Override
    protected void onDestroy() {
        unbindService(serviceConnection);
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, PubNubService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnKeyPower = (Button) findViewById(R.id.key_power);
        btnKeyUp = (Button) findViewById(R.id.key_up);
        btnKeyDown = (Button) findViewById(R.id.key_down);
        btnKeyLeft = (Button) findViewById(R.id.key_left);
        btnKeyRight = (Button) findViewById(R.id.key_right);
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

    public void showToast(final String toast)
    {
        runOnUiThread(new Runnable() {
            public void run()
            {
                Toast.makeText(getApplicationContext(), toast, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onPNDisconnected() {
        showToast("Disconnected");
    }

    @Override
    public void onPNConnected() {
        showToast("Connected");
        btnKeyPower.setOnClickListener(onBtnPower);
        btnKeyDown.setOnClickListener(onBtnDown);
        btnKeyUp.setOnClickListener(onBtnUp);
        btnKeyLeft.setOnClickListener(onBtnLeft);
        btnKeyRight.setOnClickListener(onBtnRight);
    }

    @Override
    public void onPNMessageReceived(String msg) {
        showToast("Successfully received " + msg + " confirmation");
    }

    @Override
    public void onPNMessagePublishError(int statusCode) {
        showToast("Failed to publish message. Status code: " + statusCode);
    }
}
