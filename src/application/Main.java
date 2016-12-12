package application;

import application.controller.LoggerController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Main extends Application {

  public static Stage stage;
  public static LoggerController loggerController = new LoggerController();

  @Override
  public void start(Stage primaryStage) throws Exception {
    Parent root = FXMLLoader.load(getClass().getResource("main.fxml"));
    primaryStage.setTitle("StashTool");
    Scene scene = new Scene(root, 800, 400);
    primaryStage.setScene(scene);
    primaryStage.show();
    Main.stage = primaryStage;

    loggerController.setScene(scene);
  }


  public static void main(String[] args) {
    launch(args);
  }
}
