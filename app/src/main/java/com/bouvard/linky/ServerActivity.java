package com.bouvard.linky;

import static java.net.InetAddress.getLocalHost;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ServerActivity extends Activity {

    private TextView tvServerIP, tvServerPort, tvStatus;
    private String serverIP;
    private int serverPort = 1234; // il faudra peut etre changer le port
    private Button switchButton;
    private Button StartServer;
    private ServerThread launch;
    private Button StopServer;
    private ServerThread stop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipInt = wifiInfo.getIpAddress();
        serverIP = String.format("%d.%d.%d.%d", (ipInt & 0xff), (ipInt >> 8 & 0xff), (ipInt >> 16 & 0xff), (ipInt >> 24 & 0xff));

        tvServerIP = findViewById(R.id.tvServerIP);
        tvServerPort = findViewById(R.id.tvServerPort);
        tvStatus = findViewById(R.id.tvStatus);



        tvServerIP.setText(serverIP);
        tvServerPort.setText(String.valueOf(serverPort));

        switchButton = findViewById(R.id.client_switch_btn);

        StartServer = findViewById(R.id.btnStart);

        StopServer = findViewById(R.id.btnStop);


        StartServer.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                launch = new ServerThread();
                launch.startServer();
            }
        });

        StopServer.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                stop.stopServer();
            }
        });


        switchButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(ServerActivity.this, ClientActivity.class);
                startActivity(intent);
            }
        });

    }//on create

    //public void onClickStopServer(View view){
    //   serverThread.stopServer();
    //  }




    class ServerThread extends Thread implements Runnable {

        private boolean serverRunning;
        private ServerSocket serverSocket;
        private int count=0;

        public void startServer(){

            //commence le thread
            serverRunning=true;
            start();
        }

        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(serverPort);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvStatus.setText("waiting for client");
                    }
                });

                while(serverRunning){

                    Socket socket = serverSocket.accept(); //on accepte un nouveau cliuent
                    count++;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvStatus.setText("connected to : " + socket.getInetAddress() + ":" + socket.getLocalPort());
                        }
                    });

                    PrintWriter output_server = new PrintWriter(socket.getOutputStream());
                    output_server.write("Welcome to server :"+count);
                    output_server.flush();

                    socket.close();



                }



            } catch (IOException e) {
                e.printStackTrace();
            }


        }

        public void stopServer(){
            serverRunning=false;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if(serverSocket!=null){
                        try {
                            serverSocket.close();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tvStatus.setText("server is stopped");
                                }
                            });



                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }// stop server



    }//server thread



}