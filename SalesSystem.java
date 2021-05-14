import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.util.Scanner;


public class SalesSystem extends Application {
    private static final String DELIMITER = ",";
    private static final int PERCENTAGE_UPPERBOUND = 32, PERCENTAGE_LOWERBOUND = 29;

    private static BufferedReader csvReader = null;
    private static int[] digitCount   = new int[9];
    private static double[] distribution = new double[9];
    private static int dataTotal = 0;
    private static String salesFileLocation;

    private static final int CHART_HEIGHT = 500, CHART_WIDTH = 500;
    private static void calculateData(){
        String rowData;
        try {
            csvReader.readLine(); // Eat the starting line
            while ((rowData = csvReader.readLine()) != null) {
                String[] rowArray = rowData.split(DELIMITER);
                int digit = rowArray[1].charAt(0) - '0'; //Turn digit to integer.
                digitCount[digit-1]++; // Update counts
                dataTotal++;
            }
            csvReader.close();
        } catch (Exception e){
            System.out.println("IO ERROR");
            System.exit(-1);
        }
        for(int i = 0; i < digitCount.length; i++) {
            distribution[i] = Math.round((double) digitCount[i] / dataTotal * 1000.0)/10.0; // Round the display percentage
        }
    }
    private static void exportData(){
        try {
            String exportLocation =  String.format("%s\\results.csv", salesFileLocation);
            PrintWriter dataWriter = new PrintWriter(new File(exportLocation));
            StringBuilder stringBuilder = new StringBuilder(); // Use a string builder.
            stringBuilder.append("Digit, Distribution Percentage\n"); // Append the header.
            for(int i = 0; i < distribution.length; i++) {
                stringBuilder.append(String.format("%d,%s\n", i+1, String.format("%.1f%%", distribution[i])));
            }
            dataWriter.write(stringBuilder.toString()); // Write to file.
            dataWriter.close(); // Close the reader.
        } catch (FileNotFoundException e) { // Catch the exception
            System.out.println("DATA EXPORT HAS FAILED: THIS COULD BE BECAUSE THE FILE ALREADY EXISTS OR THAT THE LOCATION IS INVALID");
        }
    }
    public static void main(String[] args){
        Scanner scan = new Scanner(System.in);
        System.out.print("Please enter the location of the file sales.csv: ");
        salesFileLocation = scan.next();
        try {
            csvReader = new BufferedReader(new FileReader(String.format("%s/sales.csv", salesFileLocation)));
        } catch (Exception e){
            System.out.println("FILE LOCATION IS INVALID. PLEASE TRY AGAIN.");
            System.exit(-1);
        }
        // READ AND CALCULATE DATA //
        calculateData();
        if ((int)(distribution[0] + 0.5) <= PERCENTAGE_UPPERBOUND && distribution[0] >= PERCENTAGE_LOWERBOUND){
            System.out.println("Fraud likely did not occur");
        }else{
            System.out.println("Fraud likely did occur");
        }
        exportData();
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // SET AXIS //
        primaryStage.setTitle("Generated Distribution");
        CategoryAxis  xAxis = new CategoryAxis();
        xAxis.setLabel("Digit");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Percent");

        BarChart<String, Number> dataChart = new BarChart<>(xAxis, yAxis);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Distribution");
        dataChart.setTitle("Benford's Law Distribution Leading Digit");

        for(int i = 0; i < digitCount.length; i++) {
            series.getData().add(new XYChart.Data<String, Number>(String.format("%d (%.1f%%)", i+1, distribution[i]),  (int)(distribution[i] + 0.5))); // Add to series while rounding
        }
        dataChart.getData().add(series);


        // Create the frame //
        VBox vbox = new VBox(dataChart);

        Scene scene = new Scene(vbox, CHART_WIDTH, CHART_HEIGHT);

        primaryStage.setScene(scene);
        primaryStage.setWidth(CHART_WIDTH);
        primaryStage.setHeight(CHART_HEIGHT);

        primaryStage.show();
    }
}
