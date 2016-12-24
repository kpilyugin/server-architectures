package launcher.gui;

import benchmark.BenchmarkParams;
import benchmark.BenchmarkResult;
import benchmark.SingleResult;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import launcher.BenchmarkLauncher;
import server.Server;
import server.ServerType;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URL;
import java.util.OptionalInt;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public class BenchmarkController implements Initializable {
  @FXML
  private Button startButton;
  @FXML
  private TextField ipAddressField;
  @FXML
  private ChoiceBox<ServerType> serverTypeChoice;
  @FXML
  private TextField arraySizeField;
  @FXML
  private TextField numRequestsField;
  @FXML
  private TextField numClientsField;
  @FXML
  private TextField delayField;
  @FXML
  private ChoiceBox<BenchmarkParams.VaryingType> varyingTypeChoice;
  @FXML
  private TextField varyingFromField;
  @FXML
  private TextField varyingToField;
  @FXML
  private TextField stepField;
  @FXML
  private Label statusLabel;
  @FXML
  public VBox chartContainer;

  private final ExecutorService executor = Executors.newSingleThreadExecutor();
  private Task<BenchmarkResult> benchmarkTask;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    startButton.setOnMouseClicked(mouseEvent -> {
      if (benchmarkTask != null) {
        benchmarkTask.cancel();
      }
      chartContainer.getChildren().clear();
      benchmarkTask = new Task<BenchmarkResult>() {
        @Override
        protected BenchmarkResult call() throws Exception {
          return runBenchmark();
        }
      };
      executor.execute(benchmarkTask);
      statusLabel.setText("Running");
      benchmarkTask.setOnFailed(event -> {
        //noinspection ThrowableResultOfMethodCallIgnored
        Throwable throwable = benchmarkTask.getException();
        throwable.printStackTrace();
        if (throwable instanceof ConnectException) {
          statusLabel.setText("No connection");
        } else {
          statusLabel.setText("Failed");
        }
      });
      benchmarkTask.setOnSucceeded(event -> {
        BenchmarkResult result = benchmarkTask.getValue();
        addResultChart(result, SingleResult::getServerRequestTime, "Request handling time on server");
        addResultChart(result, SingleResult::getServerClientTime, "Client handling time on server");
        addResultChart(result, SingleResult::getClientWorkingTime, "Client working time");
        statusLabel.setText("Finished");
      });

    });

    serverTypeChoice.setItems(FXCollections.observableArrayList(ServerType.values()));
    serverTypeChoice.setValue(ServerType.TCP_SINGLE_THREAD);

    ipAddressField.setText("localhost");
    String regex = makePartialIpRegex();
    UnaryOperator<TextFormatter.Change> ipAddressFilter = c -> {
      String text = c.getControlNewText();
      return text.matches(regex) || "localhost".startsWith(text) ? c : null;
    };
    ipAddressField.setTextFormatter(new TextFormatter<>(ipAddressFilter));

    initIntField(arraySizeField, 1000);
    initIntField(numRequestsField, 10);
    initIntField(numClientsField, 5);
    initIntField(delayField, 0);

    varyingTypeChoice.setItems(FXCollections.observableArrayList(BenchmarkParams.VaryingType.values()));
    varyingTypeChoice.setValue(BenchmarkParams.VaryingType.NUM_CLIENTS);
    initIntField(varyingFromField, 1);
    initIntField(varyingToField, 5);
    initIntField(stepField, 2);
  }

  public void shutdown() {
    executor.shutdownNow();
  }

  private BenchmarkResult runBenchmark() throws IOException, InterruptedException {
    BenchmarkParams parameters = BenchmarkParams.builder()
        .type(serverTypeChoice.getValue())
        .hostName(ipAddressField.getText())
        .port(Server.PORT)
        .numClients(getIntValue(numClientsField))
        .arraySize(getIntValue(arraySizeField))
        .delay(getIntValue(delayField))
        .numRequests(getIntValue(numRequestsField))
        .varyingType(varyingTypeChoice.getValue())
        .varyingFrom(getIntValue(varyingFromField))
        .varyingTo(getIntValue(varyingToField))
        .varyingStep(getIntValue(stepField))
        .build();
    return new BenchmarkLauncher(parameters).run();
  }

  private void addResultChart(BenchmarkResult benchmarkResult, Function<SingleResult, Double> selector, String metrics) {
    NumberAxis xAxis = new NumberAxis();
    NumberAxis yAxis = new NumberAxis();
    BenchmarkParams params = benchmarkResult.getParams();
    xAxis.setLabel(params.getVaryingType().toString());
    LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
    chart.setTitle(metrics);

    XYChart.Series<Number, Number> series = new XYChart.Series<>();
    int varyingValue = params.getVaryingFrom();
    for (SingleResult result : benchmarkResult.getResults()) {
      series.getData().add(new XYChart.Data<>(varyingValue, selector.apply(result)));
      varyingValue += params.getVaryingStep();
    }
    chart.getData().add(series);
    chartContainer.getChildren().add(chart);
  }

  private static void initIntField(TextField field, int defaultValue) {
    UnaryOperator<TextFormatter.Change> positiveIntFilter = change -> {
      OptionalInt nonDigit = change.getText().chars()
          .filter(ch -> !Character.isDigit(ch)).findFirst();
      return nonDigit.isPresent() ? null : change;
    };
    field.setTextFormatter(new TextFormatter<String>(positiveIntFilter));
    field.setText(Integer.toString(defaultValue));
  }

  private static String makePartialIpRegex() {
    String partialBlock = "(([01]?[0-9]{0,2})|(2[0-4][0-9])|(25[0-5]))";
    String subsequentPartialBlock = "(\\." + partialBlock + ")";
    String ipAddress = partialBlock + "?" + subsequentPartialBlock + "{0,3}";
    return "^" + ipAddress;
  }

  private static int getIntValue(TextField field) {
    return Integer.parseInt(field.getText());
  }
}
