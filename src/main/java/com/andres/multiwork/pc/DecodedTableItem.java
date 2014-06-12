package com.andres.multiwork.pc;

import javafx.beans.property.SimpleStringProperty;

public class DecodedTableItem {

    private final SimpleStringProperty dataString;
    private final SimpleStringProperty startTime;
    private final SimpleStringProperty endTime;

    public DecodedTableItem(String dataString, String t1, String t2){
        this.dataString = new SimpleStringProperty(dataString);
        this.startTime = new SimpleStringProperty(t1);
        this.endTime = new SimpleStringProperty(t2);
    }

    public String getDataString() {
        return dataString.get();
    }

    public SimpleStringProperty dataStringProperty() {
        return dataString;
    }

    public void setDataString(String dataString) {
        this.dataString.set(dataString);
    }

    public String getStartTime() {
        return startTime.get();
    }

    public SimpleStringProperty startTimeProperty() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime.set(startTime);
    }

    public String getEndTime() {
        return endTime.get();
    }

    public SimpleStringProperty endTimeProperty() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime.set(endTime);
    }
}
