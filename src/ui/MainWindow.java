package ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;

public class MainWindow extends Application {
    public static int SCENE_WIDTH = 750;
    public static int SCENE_HEIGHT = 500;
    private static Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception{
        MainWindow.primaryStage = primaryStage;
        Parent root = FXMLLoader.load(getClass().getResource("main_window.fxml"));

        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("app_icon.png")));
        primaryStage.setTitle("Google Scholar Profiles Parser");
        primaryStage.setScene(new Scene(root, SCENE_WIDTH, SCENE_HEIGHT));
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
