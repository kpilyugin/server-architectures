package launcher;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class BenchmarkApp extends Application {
  @Override
  public void start(Stage primaryStage) throws Exception {
    FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("benchmark.fxml"));
    Pane pane = loader.load();
    Scene scene = new Scene(pane);
    primaryStage.setScene(scene);
    primaryStage.setTitle("Servers benchmark");
    primaryStage.show();
  }

  public static void main(String[] args) {
    Application.launch(BenchmarkApp.class, args);
  }
}
