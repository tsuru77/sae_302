package com.bouvard.linky;

import androidx.appcompat.app.AppCompatActivity;
//import Socket;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientActivity extends AppCompatActivity {

    private TextView tvReceivedData;
    private EditText etServer_ip, etServer_port;
    private Button connect_server_btn;
    private onClickConnect connect;
    private String serverName;
    private int serverPort;
    private Button switchButton;


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
                connect = new onClickConnect(serverName,serverPort,tvReceivedData);
                connect.run();
            }
        });

    }
};

class onClickConnect extends Thread implements Runnable{

    private String server_ip;
    private int server_port;
    private ClientActivity clientact;
    private TextView tvReceivedData;



    onClickConnect(String ip, int port, TextView temp){
        server_ip = ip;
        server_port = port;
        tvReceivedData = temp;
    }



        @Override
        public void run() {
            try {
                Socket socket = new Socket(server_ip,server_port);

                BufferedReader buffer_input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String txtFromServer = buffer_input.readLine();



                clientact.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvReceivedData.setText(txtFromServer);
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }


    }

}

