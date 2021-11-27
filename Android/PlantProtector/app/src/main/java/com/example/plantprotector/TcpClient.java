package com.example.plantprotector;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class TcpClient {
    // The TAG is just for Log usage. If you run the app through AS you can go to the bottom of AS
    // and there is a Logcat option in the bottom left, this is where logging lines show up.
    // To add a log message simple do Log.d(TAG, "Message you want to see"); then in logcat
    // you can search for "KBK" and see the message when it shows up. (Yes KBK is one of my coworkers
    // initials and he had me use it for one thing and now I just use it for everything)
    public static final String TAG = "KBK";
    public static final String SERVER_IP = "138.67.190.221";
    public static final int SERVER_PORT = 8888;
    // message to send to the server
    private String mServerMessage;
    // sends message received notifications
    private OnMessageReceived mMessageListener = null;
    private OnConnected mConnectedListener = null;
    // while this is true, the server will continue running
    private boolean mRun = false;
    // used to send messages
    private PrintWriter mBufferOut;
    // used to read messages from the server
    private BufferedReader mBufferIn;

    /**
     * Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public TcpClient(OnMessageReceived listener, OnConnected connectedListener) {
        mMessageListener = listener;
        mConnectedListener = connectedListener;
    }

    /**
     * Sends the message entered by client to the server
     *
     * @param message text entered by client
     */
    public void sendMessage(final String message) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (mBufferOut != null) {
                    Log.d(TAG, "Sending: " + message);
                    mBufferOut.println(message);
                    mBufferOut.flush();
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    /**
     * Close the connection and release the members
     */
    public void stopClient() {

        mRun = false;

        if (mBufferOut != null) {
            mBufferOut.flush();
            mBufferOut.close();
        }

        mMessageListener = null;
        mBufferIn = null;
        mBufferOut = null;
        mServerMessage = null;
    }

    // This is what gets called when we start up the client. It houses most of the needed stuff
    public void run() {

        mRun = true;

        try {
            // serverAddr is just the IP address, SERVER_IP is instantiated above on class creation
            InetAddress serverAddr = InetAddress.getByName(SERVER_IP);

            // I just have this here for testing to make sure that the IP Address is reachable, when
            // we confirm that the app can connect to our server we can remove it.
            if (serverAddr.isReachable(5000)) {
                Log.d(TAG, "Connected");
            } else {
                Log.d(TAG, "Connection Failed");
            }

            // Log line to say that we starting connecting to the server
            Log.d(TAG, "C: Connecting...");

            // Creating the socket here should make the socket for our client as well as connect it
            // to our server.
            Socket socket = new Socket(serverAddr, SERVER_PORT);

            // Log line to say that we have created the socket and thus connected to the server.
            Log.d(TAG, "Socket created");

            if (socket.isConnected()){
                Log.d(TAG, "Connected");
            } else {
                Log.d(TAG, "Not Connected");
            }

            // Callback to the main thread so that the connection status text in the app changes to
            // connected.
            // mConnectedListener.connected();

            try {

                Log.d(TAG, "Create Message Buffers");

                //sends the message to the server
                mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                //receives the message which the server sends back
                mBufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                InputStream stream = socket.getInputStream();

                // Listen for messages from the server while the client is active
                while (mRun) {
                    byte[] data = new byte[40];
                    Log.d(TAG, "Listening For Message");

                    //mServerMessage = mBufferIn.readLine();
                    stream.read(data);

                    byte[] bmessage = Arrays.copyOfRange(data, 0, 20);
                    byte[] btime = Arrays.copyOfRange(data, 21, 28);
                    byte[] bseqnum = Arrays.copyOfRange(data, 29, 36);

                    String message = new String(bmessage);

                    long time = 0;
                    for (int i = 0; i < btime.length; i++) {
                        time += ((long) btime[i] & 0xffL) << (8 * i);
                    }

                    long seqnum = 0;
                    for (int i = 0; i < bseqnum.length; i++) {
                        seqnum += ((long) bseqnum[i] & 0xffL) << (8 * i);
                    }

                    // If there is a message, use the callback to send the message back to the main
                    // thread so that it can be handled
                    if (data != null && mMessageListener != null) {
                        mMessageListener.messageReceived(message);
                        mMessageListener.messageReceived(String.valueOf(seqnum));
                        mMessageListener.messageReceived(String.valueOf(time));
                    }

                    Thread.sleep(2000);
                }

            } catch (Exception e) {
                Log.e(TAG, "S: Error", e);
            } finally {
                //the socket must be closed. It is not possible to reconnect to this socket
                // after it is closed, which means a new socket instance has to be created.
                socket.close();
            }

        } catch (Exception e) {
            Log.e(TAG, "C: Error", e);
        }

    }

    //Declare the interface. The method messageReceived(String message) will must be implemented in the Activity
    //class at on AsyncTask doInBackground
    public interface OnMessageReceived {
        public void messageReceived(String message);
    }

    public interface OnConnected {
        public void connected();
    }
}
