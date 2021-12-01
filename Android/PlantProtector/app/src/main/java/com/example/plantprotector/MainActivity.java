package com.example.plantprotector;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.plantprotector.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "KBK";
    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    TcpClient tcpClient;

    private boolean run;
    private boolean connected;

    private long plant1PreviousSeqNum = 0;
    private long plant1PacketLoss = 0;
    private long plant1Latency = -1;
    private String plant1Message;
    private long plant2Latency = -1;
    private String plant2Message;
    private long plant2PreviousSeqNum = 0;
    private long plant2PacketLoss = 0;

    public class ProgressUpdateWrapper {
        public String message;
        public long time;
        public long seqnum;
        public long plantnum;

        public ProgressUpdateWrapper(String message, long time, long seqnum, long plantnum) {
            this.message = message;
            this.time = time;
            this.seqnum = seqnum;
            this.plantnum = plantnum;
        }
    }

    public class PlantInfo {
        public String message;
        public long latency;
        public long packetLoss;

        public PlantInfo(String message, long latency, long packetLoss) {
            this.message = message;
            this.latency = latency;
            this.packetLoss = packetLoss;
        }
    }

    public boolean isRunning() {
        return run;
    }

    public boolean isConnected() {
        return connected;
    }

    // ConnectTask starts up the TCP Client and connects it to the server. The IP address and port
    // used can be found in the TcpClient.java class. It also houses the message received method that
    // calls publishProgress which calls onProgressUpdate. That is where the the code that does stuff
    // with the server messages should go. connected() just changes the text giving the connection
    // status. ConnectTask creates an asynchronous task that works in the background, not on the main
    // UI thread, this needs to stay this way.
    @SuppressLint("StaticFieldLeak")
    public class ConnectTask extends AsyncTask<String, ProgressUpdateWrapper, TcpClient> {
        @Override
        protected TcpClient doInBackground(String... message) {
            tcpClient = new TcpClient(new TcpClient.OnMessageReceived() {
                @Override
                public void messageReceived(String message, long time, long seqnum, long plantnum) {
                    publishProgress(new ProgressUpdateWrapper(message, time, seqnum, plantnum));
                }
            }, new TcpClient.OnConnected() {
                @Override
                public void connected() {
                    connected = true;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (getSupportFragmentManager().findFragmentById(R.id.FirstFragment) != null && getSupportFragmentManager().findFragmentById(R.id.FirstFragment).getView() != null){
                                ((FirstFragment)getSupportFragmentManager().findFragmentById(R.id.FirstFragment)).updateInfo();
                            }
                        }
                    });
                }
            });
            tcpClient.run();

            return null;
        }

        @Override
        protected void onProgressUpdate(ProgressUpdateWrapper... values) {
            super.onProgressUpdate(values);

            ProgressUpdateWrapper progressUpdate = values[0];
            Log.d(TAG, "Server Message --------> " + "\n\tMessage: " + progressUpdate.message + "\n\tTime: " + progressUpdate.time + "\n\tSequence Number: " + progressUpdate.seqnum + "\n\tPlant Number: " + progressUpdate.plantnum);
            switch ((int) progressUpdate.plantnum) {
                case 1:
                    plant1PacketLoss += progressUpdate.seqnum - plant1PreviousSeqNum - 1;
                    plant1PreviousSeqNum = progressUpdate.seqnum;
                    plant1Message = progressUpdate.message;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (getSupportFragmentManager().findFragmentById(R.id.SecondFragment) != null){
                                ((SecondFragment)getSupportFragmentManager().findFragmentById(R.id.SecondFragment)).updateInfo(getPlantInfo(1));
                            }
                        }
                    });
                case 2:
                    plant2PacketLoss += progressUpdate.seqnum - plant2PreviousSeqNum - 1;
                    plant2PreviousSeqNum = progressUpdate.seqnum;
                    plant2Message = progressUpdate.message;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (getSupportFragmentManager().findFragmentById(R.id.ThirdFragment) != null){
                                ((ThirdFragment)getSupportFragmentManager().findFragmentById(R.id.ThirdFragment)).updateInfo(getPlantInfo(2));
                            }
                        }
                    });
                default:
            }
        }
    }

    public PlantInfo getPlantInfo(int plant) {
        switch(plant) {
            case 1:
                return new PlantInfo(plant1Message, plant1Latency, plant1PacketLoss);
            case 2:
                return new PlantInfo(plant2Message, plant2Latency, plant2PacketLoss);
            default:
                return new PlantInfo(null, -1, -1);
        }
    }

    // A simple method that creates the ConnectTask class which creates the new thread and starts up
    // the client and connects to the server. It is called in FirstFragment.java so when a button is
    // clicked. This is done by using getActivity() which returns the activity that the fragment is
    // bound to (in this case MainActivity) which allows us to not make this static, and it needs to
    // stay that way.
    public void startClient(){
        run = true;
        new ConnectTask().execute();
    }

    // Similar to startClient(), stops the client and is called from the FirstFragment.java class.
    public void stopClient(){
        if (tcpClient != null) {
            tcpClient.stopClient();
        }

        run = false;

        if (getSupportFragmentManager().findFragmentById(R.id.SecondFragment) != null && getSupportFragmentManager().findFragmentById(R.id.SecondFragment).getView() != null){

        }
        if (getSupportFragmentManager().findFragmentById(R.id.ThirdFragment) != null && getSupportFragmentManager().findFragmentById(R.id.ThirdFragment).getView() != null){
            ((TextView)getSupportFragmentManager().findFragmentById(R.id.ThirdFragment).getView().findViewById(R.id.plant_2_status)).setText(R.string.connect_for_info);
            ((TextView)getSupportFragmentManager().findFragmentById(R.id.ThirdFragment).getView().findViewById(R.id.plant_2_latency)).setText(R.string.connect_for_info);
            ((TextView)getSupportFragmentManager().findFragmentById(R.id.ThirdFragment).getView().findViewById(R.id.plant_2_packet_loss)).setText(R.string.connect_for_info);
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
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}