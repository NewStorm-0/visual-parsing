package com.chaldea.visualparsing;

import com.chaldea.visualparsing.controller.MainFrameController;
import com.chaldea.visualparsing.gui.DialogShower;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class VisualApplication extends Application {
    private static final Logger logger = LoggerFactory.getLogger(VisualApplication.class);

    @Override
    public void start(Stage stage) throws IOException {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            logger.error("未捕获的异常在线程 " + thread.getName(), throwable);
            DialogShower.showExceptionDialog(throwable);
        });
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