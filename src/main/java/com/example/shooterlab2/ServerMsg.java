package com.example.shooterlab2;

public class ServerMsg {
    public int clientId;
    public Model model;

    public ServerMsg(int clientId, Model model) {
        this.clientId = clientId;
        this.model = model;
    }
}

