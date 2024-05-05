package com.example.shooterlab2;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class mainServer {
    public static final int DEFAULT_SERVER_PORT = 3456;
    public final Model model = Model.Builder.build();
    InetAddress ip = null;
    ExecutorService service = Executors.newCachedThreadPool();
    ArrayList<ClAtServer> clients = new ArrayList<>();

    boolean isUpdaterRunning = false;

    Runnable gameUpdaterRunnable = () -> {
        isUpdaterRunning = true;
        while (isUpdaterRunning) {
            isUpdaterRunning = model.gameUpdate();
            try {
                //noinspection BusyWait
                Thread.sleep(30);
            } catch (InterruptedException e) {
                isUpdaterRunning = false;
            }
        }
    };

    Thread updaterThread;

    public void start(int portNumber) {
        MyDB db = new MyDB();
        db.getRatingsTable().forEach(dbRecord ->
                System.out.println(dbRecord.name + ": " + dbRecord.winsCount));
        System.out.println(".................");


        ServerSocket ss;
        try {
            model.addObserver(this::onModelUpdated);

            ip = InetAddress.getLocalHost();
            ss = new ServerSocket(portNumber, 0, ip);
            System.out
                    .append("Сервер: работает. Порт: ")
                    .append(String.valueOf(portNumber))
                    .append("\n");

            Socket cs;
            while (true) {
                cs = ss.accept();
                System.out.append("Клиент подключился.\n");

                int newPlayerId = model.regNewPlayer("");
                ClAtServer client = new ClAtServer(this, cs, newPlayerId);
                client.sendModelStateToClient();
                clients.add(client);
                service.submit(client);
            }

        } catch (IOException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    void onModelUpdated() {
        for (ClAtServer clAtServer : clients) {
            clAtServer.sendModelStateToClient();
        }
        if (!model.isPaused && !isUpdaterRunning) {
            updaterThread = new Thread(gameUpdaterRunnable);
            updaterThread.start();
        } else if (model.isPaused && isUpdaterRunning) {
            updaterThread.interrupt();
        }
    }

    public static void main(String[] args) {
        mainServer server = new mainServer();
        server.start(DEFAULT_SERVER_PORT);
    }
}
