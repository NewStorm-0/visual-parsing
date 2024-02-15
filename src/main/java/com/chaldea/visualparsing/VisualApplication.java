package com.chaldea.visualparsing;

import com.chaldea.visualparsing.controller.GrammarViewController;
import com.chaldea.visualparsing.controller.MainFrameController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class VisualApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(VisualApplication.class
                .getResource("main-frame.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("未打开" + MainFrameController.TITLE_SUFFIX);
        stage.setMinHeight(400);
        stage.setMinWidth(600);
        stage.setScene(scene);
        stage.show();
    }

    public static void run(String[] args) {
        launch(args);
    }
}