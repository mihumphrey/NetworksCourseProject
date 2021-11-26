package com.example.plantprotector;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.plantprotector.databinding.ActivityMainBinding;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "KBK";
    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    TcpClient mTcpClient;


    // ConnectTask starts up the TCP Client and connects it to the server. The IP address and port
    // used can be found in the TcpClient.java class. It also houses the message received method that
    // calls publishProgress which calls onProgressUpdate. That is where the the code that does stuff
    // with the server messages should go. connected() just changes the text giving the connection
    // status. ConnectTask creates an asynchronous task that works in the background, not on the main
    // UI thread, this needs to stay this way.
    @SuppressLint("StaticFieldLeak")
    public class ConnectTask extends AsyncTask<String, String, TcpClient> {
        @Override
        protected TcpClient doInBackground(String... message) {
            mTcpClient = new TcpClient(new TcpClient.OnMessageReceived() {
                @Override
                public void messageReceived(String message) {
                    publishProgress(message);
                }
            }, new TcpClient.OnConnected() {
                @Override
                public void connected() {
                    ((TextView)getFragmentManager().findFragmentById(R.id.FirstFragment).getView().findViewById(R.id.connection_status)).setText(R.string.connected);
                }
            });
            mTcpClient.run();

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            Log.d(TAG, "Server Message: " + values[0]);
            //process server response here....
        }
    }

    // A simple method that creates the ConnectTask class which creates the new thread and starts up
    // the client and connects to the server. It is called in FirstFragment.java so when a button is
    // clicked. This is done by using getActivity() which returns the activity that the fragment is
    // bound to (in this case MainActivity) which allows us to not make this static, and it needs to
    // stay that way.
    public void startClient(){
        new ConnectTask().execute();
    }

    // Similar to startClient(), stops the client and is called from the FirstFragment.java class.
    public void stopClient(){
        if (mTcpClient != null) {
            mTcpClient.stopClient();
        }
    }

    // onCreate is called when our application boots up, in it we do all of the setup for the app.
    // The onClick() method is attached to the "+" button on the home screen, we currently don't use
    // it and it was from the example app given. It can either be given functionality or removed. If
    // it is removed there will be other stuff elsewhere in the project that needs to be deleted I
    // believe. Everything else below here is stuff from the default android project I used and can
    // either be utilized and added to or potentially removed depending on what we need.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
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
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}