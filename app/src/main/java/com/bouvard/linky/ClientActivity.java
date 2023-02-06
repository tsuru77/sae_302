package com.bouvard.linky;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import com.google.gson.Gson;
import java.util.ArrayList;


public class ClientActivity extends AppCompatActivity {

    private TextView tvReceivedData;
    private EditText etServer_ip, etServer_port;
    private Button connect_server_btn;
    private String serverName;
    private int serverPort;
    private onClickConnect connect;
    private Button switchButton;
    private ArrayList<onClickConnect.MessageData> messagesList = new ArrayList<>();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        tvReceivedData = findViewById(R.id.rec_data);
        etServer_ip = findViewById(R.id.etServer_ip);
        etServer_port = findViewById(R.id.etServer_port);
        connect_server_btn = findViewById(R.id.server_connect_btn);

        switchButton = findViewById(R.id.server_switch_btn);

        switchButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(ClientActivity.this, ServerActivity.class);
                startActivity(intent);
            }
        });

        connect_server_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                serverName = etServer_ip.getText().toString();
                serverPort = Integer.valueOf(etServer_port.getText().toString());
                new Thread(() -> {
                    connect = new onClickConnect(serverName, serverPort, tvReceivedData, ClientActivity.this);
                    connect.run();
                }).start();
            }
        });


    }

    public ArrayList<onClickConnect.MessageData> getMessagesList() {
        return messagesList;
    }

    class onClickConnect extends Thread implements Runnable {

        private String server_ip;
        private int server_port;
        private TextView tvReceivedData;
        private ClientActivity clientActivity;
        private ArrayList<String> chatMessages = new ArrayList<>();

        onClickConnect(String ip, int port, TextView temp, ClientActivity clientActivity) {
            server_ip = ip;
            server_port = port;
            tvReceivedData = temp;
            this.clientActivity = clientActivity;
        }

        @Override
        public void run() {
            try {
                Socket socket = new Socket(server_ip, server_port);

                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                Gson gson = new Gson();

                while (true) {
                    // Read incoming messages
                    String txtFromServer = in.readLine();
                    if (txtFromServer != null) {
                        chatMessages.add(txtFromServer);
                        ContactsContract.Contacts.Data receivedData = gson.fromJson(txtFromServer, ContactsContract.Contacts.Data.class);

                        clientActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvReceivedData.setText(String.join("\n", chatMessages));
                            }
                        });
                    }

                    // Send outgoing messages
                    MessageData data = new MessageData("test"); // example data object
                    String json = gson.toJson(data);
                    out.println(json);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        public class MessageData {
            private String message;

            public MessageData(String message) {
                this.message = message;
            }

            public String getMessage() {
                return message;
            }
        }
    }

}
