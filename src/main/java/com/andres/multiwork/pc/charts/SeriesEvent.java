package com.andres.multiwork.pc.charts;

import javafx.scene.chart.XYChart;
import javafx.scene.input.MouseEvent;

public interface SeriesEvent {

    public void onSeriesClick(XYChart.Series series, MouseEvent mouseEvent);

}
