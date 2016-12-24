package launcher.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class BenchmarkApp extends Application {
  @Override
  public void start(Stage stage) throws Exception {
    FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("benchmark.fxml"));
    Pane pane = loader.load();
    Scene scene = new Scene(pane);
    //noinspection ConstantConditions
    scene.getStylesheets().add(getClass().getClassLoader().getResource("style.css").toExternalForm());
    stage.setScene(scene);
    stage.setTitle("Server architectures benchmark");
    stage.show();
    BenchmarkController controller = loader.getController();
    stage.setOnCloseRequest(event -> controller.shutdown());
  }

  public static void main(String[] args) {
    Application.launch(BenchmarkApp.class, args);
  }
}
