package main;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

public class ConvexHull extends Application{

	@Override
	public void start(Stage primaryStage) throws Exception {
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Number of Month");

        final LineChart<Number,Number> lineChart = new LineChart<Number,Number>(xAxis,yAxis);

        XYChart.Series<Number,Number> series = new XYChart.Series<>();
        series.setName("My portfolio");

        series.getData().add(new XYChart.Data(1, 23));
        series.getData().add(new XYChart.Data(2, 14));
        series.getData().add(new XYChart.Data(3, 15));

        lineChart.getData().add(series);
        
        primaryStage.setScene(new Scene(lineChart));
        primaryStage.show();
	}
	
	public static void main(String args[]) {
		super();
	}

}
