package com.andres.multiwork.pc.charts;

import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;

public class FXUtils {

    public static <T extends Number> T maxY(ObservableList<XYChart.Data<T,T>> data){
        T maxY = data.get(0).getYValue();

        for(XYChart.Data xydata : data){
            if((double)xydata.getYValue() > maxY.doubleValue()){
                maxY = (T)xydata.getYValue();
            }
        }

        return maxY;
    }

    public static <T extends Number> T minY(ObservableList<XYChart.Data<T,T>> data){
        T minY = data.get(0).getYValue();

        for(XYChart.Data xydata : data){
            if((double)xydata.getYValue() < minY.doubleValue()){
                minY = (T)xydata.getYValue();
            }
        }

        return minY;
    }

}
