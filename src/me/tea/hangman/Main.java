package me.tea.hangman;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    private static Stage stage;

    /** TODO:
     *
     * Implement connection status'. (lost, etc.)
     * Fix it to allow the same person go again, if their input was correct.
     * Add back buttons.
     * Possibly allow keyboard support.
     *
     */

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Hangman - TheEvilArchitect");
        primaryStage.setScene(new Scene(root, 522, 353));
        primaryStage.setResizable(false);

        primaryStage.show();
        stage = primaryStage;
    }

    public static Stage getStage(){
        return stage;
    }

    public static void main(String[] args) {
        launch(args);
    }


}
