package com.andres.multiwork.pc.utils;

import javafx.scene.layout.Pane;

public abstract class SettingsPane{

    final int id;
    int channel;
    private Pane pane;

    public abstract void enable();

    public abstract void disable();

    public SettingsPane(int id){
        this.id = id;
    }

    /**
     * Notify channel changed
     * @param newChannel channel number from 1 to {@link com.andres.multiwork.pc.GlobalValues#channelsNumber}
     */
    public void notifiyChannelChanged(int newChannel){
        channel = newChannel;
    }

    public int getChannel(){
        return channel;
    }

    public Pane getPane() {
        return pane;
    }

    public void setPane(Pane pane) {
        this.pane = pane;
    }

    public int getID() {
        return id;
    }

}
