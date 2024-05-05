package com.example.shooterlab2;


public class MSG {
    public MsgAction action;

    public String text = null;

    public MSG(MsgAction action) {
        this.action = action;
    }

    public MSG(MsgAction action, String text) {
        this.action = action;
        this.text = text;
    }

    public MsgAction getAction() {
        return action;
    }

    @Override
    public String toString() {
        if (text == null) {
            return "ClientMsg { " + "action = " + action + " }";
        } else {
            return "ClientMsg { " + "action = " + action + ", text = " + text + " }";
        }
    }
}
