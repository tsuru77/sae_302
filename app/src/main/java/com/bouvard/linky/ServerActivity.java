package com.bouvard.linky;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;
import java.util.ArrayList;

public class ServerActivity extends Activity {

    private TextView tvServerIP, tvServerPort, tvStatus;
    private String serverIP;
    private int serverPort = 4343;
    private Button switchButton;
    private Button StartServer;
    private ServerThread launch;
    private Button StopServer;
    private ServerThread stop;
    private ArrayList<ClientHandler> clients;
    private TextView tvIncomingMessages;

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

        clients = new ArrayList<ClientHandler>();

        StartServer.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                launch = new ServerThread();
                launch.start();
            }
        });

        StopServer.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (launch != null) {
                    launch.stopServer();
                }
            }
        });



        switchButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(ServerActivity.this, ClientActivity.class);
                startActivity(intent);
            }
        });
    }

    public class ServerThread extends Thread {
        private boolean running = false;
        private ServerSocket serverSocket;

        public void run() {
            try {
                serverSocket = new ServerSocket(serverPort);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvStatus.setText("Server started");
                    }
                });
                while (running) {
                    Socket client = serverSocket.accept();
                    ClientHandler clientThread = new ClientHandler(client, tvIncomingMessages);
                    clients.add(clientThread);
                    clientThread.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void startServer() {
            try {
                serverSocket = new ServerSocket(serverPort);
                running = true;
                tvStatus.setText("Waiting for clients...");
                while (running) {
                    Socket socket = serverSocket.accept();
                    ClientHandler clientHandler = new ClientHandler(socket, tvIncomingMessages);
                    clientHandler.start();
                    clients.add(clientHandler);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void stopServer() {
            try {
                running = false;
                for (ClientHandler clientHandler : clients) {
                    clientHandler.stopClient();
                }
                serverSocket.close();
                tvStatus.setText("Server stopped");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    private class ClientHandler extends Thread {
        private Socket client;
        private BufferedReader reader;
        private PrintWriter writer;
        private TextView tvIncomingMessages;

        public ClientHandler(Socket client, TextView tvIncomingMessages) {
            this.client = client;
            this.tvIncomingMessages = tvIncomingMessages;
            try {
                reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                writer = new PrintWriter(client.getOutputStream(), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            try {
                String message;
                while ((message = reader.readLine()) != null) {
                    final String finalMessage = message;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvIncomingMessages.append(finalMessage + "\n");
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void stopClient() {
            try {
                client.close();
                reader.close();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}


