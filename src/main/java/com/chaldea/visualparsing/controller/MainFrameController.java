package com.chaldea.visualparsing.controller;

import com.chaldea.visualparsing.ControllerMediator;
import com.chaldea.visualparsing.Main;
import com.chaldea.visualparsing.controller.GrammarViewController;
import com.chaldea.visualparsing.gui.ExceptionDialogUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class MainFrameController {
    public static String TITLE_SUFFIX = "——可视化分析器";

    @FXML
    protected VBox topVBox;
    @FXML
    protected TabPane tabPane;
    private static final Logger logger = LoggerFactory.getLogger(MainFrameController.class);

    public MainFrameController() {
        ControllerMediator.getInstance().setMainFrameController(this);
        logger.debug("已经注册MainFrameController");
    }

    @FXML
    public void initialize() {
        Screen screen = Screen.getPrimary();
        double topVBoxWidth = screen.getVisualBounds().getWidth() - 60;
        double topVBoxHeight = screen.getVisualBounds().getHeight() - 120;
        topVBox.setPrefSize(topVBoxWidth, topVBoxHeight);
        topVBox.setMinSize(400, 300);
        tabPane.prefHeightProperty().bind(topVBox.heightProperty().subtract(35));
    }

    public VBox getTopVBox() {
        return topVBox;
    }

    /**
     * 修改窗口标题前缀
     * @param titlePrefix 标题前缀
     */
    public void setStageTitlePrefix(String titlePrefix) {
        ((Stage) topVBox.getScene().getWindow()).setTitle(titlePrefix + TITLE_SUFFIX);
    }

    /**
     * 打开用户手册对话框
     */
    @FXML
    protected void openUserManual() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("使用说明");
        ButtonType buttonType = new ButtonType("好的", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(buttonType);
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("user-manual.fxml"));
        try {
            dialog.getDialogPane().setContent(loader.load());
            dialog.showAndWait();
        } catch (IOException e) {
            logger.error("", e);
            ExceptionDialogUtils.showExceptionDialog(e);
        }
    }

    @FXML
    protected void createGrammar() {
        ControllerMediator.getInstance().getGrammarViewController().createGrammar();
    }

    @FXML
    protected void openGrammar() {
        ControllerMediator.getInstance().getGrammarViewController().openGrammar();
    }

    @FXML
    protected void saveGrammar() {
        ControllerMediator.getInstance().getGrammarViewController().saveGrammar();
    }
}
