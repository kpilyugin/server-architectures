package gui;

import launcher.BenchmarkLauncher;
import launcher.BenchmarkParams;
import client.ClientType;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import launcher.ClientsLauncher;
import server.Server;
import server.ServerFactory;
import server.ServerType;

import java.io.IOException;
import java.net.URL;
import java.util.OptionalInt;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.UnaryOperator;

public class MainController implements Initializable {
  @FXML
  private Button startButton;
  @FXML
  private TextField ipAddressField;
  @FXML
  private TextField portField;
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
  private Label logLabel;

  private final ExecutorService executor = Executors.newSingleThreadExecutor();

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    startButton.setOnMouseClicked(event -> executor.submit(() -> {
      try {
        start();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }));

    serverTypeChoice.setItems(FXCollections.observableArrayList(ServerType.values()));
    serverTypeChoice.setValue(ServerType.TCP_SINGLE_THREAD);
    initTextField(arraySizeField, 1000);
    initTextField(numRequestsField, 100);
    initTextField(numClientsField, 5);
    initTextField(delayField, 100);
  }

  private void initTextField(TextField field, int defaultValue) {
    UnaryOperator<TextFormatter.Change> positiveIntFilter = change -> {
      OptionalInt nonDigit = change.getText().chars()
          .filter(ch -> !Character.isDigit(ch)).findFirst();
      return nonDigit.isPresent() ? null : change;
    };
    field.setTextFormatter(new TextFormatter<String>(positiveIntFilter));
    field.setText(Integer.toString(defaultValue));
  }

  private int getIntValue(TextField field) {
    return Integer.parseInt(field.getText());
  }

  private void start() throws IOException, InterruptedException {
    ServerType serverType = serverTypeChoice.getValue();
    BenchmarkParams parameters = BenchmarkParams.builder()
        .type(serverType)
        .numClients(getIntValue(numClientsField))
        .arraySize(getIntValue(arraySizeField))
        .delay(getIntValue(delayField))
        .numRequests(getIntValue(numRequestsField))
        .build();
    new BenchmarkLauncher(parameters).run();
  }
}
