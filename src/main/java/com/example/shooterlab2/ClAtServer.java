package com.example.shooterlab2;

import com.google.gson.Gson;


import java.io.*;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClAtServer implements Runnable {
    private final mainServer myServer;
    private final Socket clientSocket;
    private final int id;
    private DataInputStream dInputStream;
    private DataOutputStream dOutputStream;
    private final Gson gson = new Gson();

    public ClAtServer(mainServer myServer, Socket clientSocket, int id) {
        this.clientSocket = clientSocket;
        this.myServer = myServer;
        this.id = id;

        try {
            OutputStream outputStream = clientSocket.getOutputStream();
            dOutputStream = new DataOutputStream(outputStream);
        } catch (IOException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        System.out.println("Thread for client started");
        try {
            InputStream inputStream = clientSocket.getInputStream();
            dInputStream = new DataInputStream(inputStream);

            while (true) {
                // Reading messages from client
                String s = dInputStream.readUTF();
                System.out.println("Received: " + s + " from client " + id);

                MSG msg = gson.fromJson(s, MSG.class);

                if (id != -1) {
                    if (msg.getAction() == MsgAction.CL_HELLO) {
                        myServer.model.players[id].name = msg.text;
                        myServer.onModelUpdated();
                    } else if (msg.getAction() == MsgAction.CL_READY) {
                        myServer.model.setPlayerReady(id, true);
                    } else if (msg.getAction() == MsgAction.CL_PAUSE) {
                        myServer.model.setPlayerReady(id, false);
                    } else if (msg.getAction() == MsgAction.CL_SHOOT) {
                        myServer.model.shoot(id);
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    void sendModelStateToClient() {
        try {
            dOutputStream.writeUTF(gson.toJson(new ServerMsg(id, myServer.model)));
        } catch (IOException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }
}
