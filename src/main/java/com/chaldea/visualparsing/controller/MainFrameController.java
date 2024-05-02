package com.chaldea.visualparsing.controller;

import com.chaldea.visualparsing.Main;
import com.chaldea.visualparsing.grammar.Grammar;
import com.chaldea.visualparsing.gui.DialogShower;
import com.chaldea.visualparsing.parsing.LRParsingTable;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class MainFrameController {
    public static String TITLE_SUFFIX = "——可视化分析器";

    @FXML
    private VBox topVBox;
    @FXML
    private TabPane tabPane;
    @FXML
    private Menu syntacticAnalysisMenu;
    @FXML
    private Tab ll1Tab;
    @FXML
    private Tab lrTab;
    private static final Logger logger = LoggerFactory.getLogger(MainFrameController.class);

    public MainFrameController() {
        ControllerMediator.getInstance().setMainFrameController(this);
        logger.debug("已经注册MainFrameController");
    }

    @FXML
    private void initialize() {
        Screen screen = Screen.getPrimary();
        double topVBoxWidth = screen.getVisualBounds().getWidth() - 60;
        double topVBoxHeight = screen.getVisualBounds().getHeight() - 120;
        topVBox.setPrefSize(topVBoxWidth, topVBoxHeight);
        topVBox.setMinSize(400, 300);
        tabPane.prefHeightProperty().bind(topVBox.heightProperty().subtract(35));
    }

    VBox getTopVBox() {
        return topVBox;
    }

    /**
     * 修改窗口标题前缀
     * @param titlePrefix 标题前缀
     */
    void setStageTitlePrefix(String titlePrefix) {
        ((Stage) topVBox.getScene().getWindow()).setTitle(titlePrefix + TITLE_SUFFIX);
    }

    /**
     * Gets stage title prefix.
     *
     * @return the stage title prefix
     */
    String getStageTitlePrefix() {
        Stage stage = ((Stage) topVBox.getScene().getWindow());
        int index = stage.getTitle().indexOf(TITLE_SUFFIX);
        return stage.getTitle().substring(0, index);
    }

    /**
     * 打开用户手册对话框
     */
    @FXML
    private void openUserManual() {
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
            DialogShower.showExceptionDialog(e);
        }
    }

    @FXML
    private void createGrammar() {
        ControllerMediator.getInstance().getGrammarViewController().createGrammar();
        syntacticAnalysisMenu.setDisable(false);
        syntacticAnalysisMenu.getItems().forEach(item -> {
            item.setDisable(false);
        });
    }

    @FXML
    private void openGrammar() {
        GrammarViewController grammarViewController =
                ControllerMediator.getInstance().getGrammarViewController();
        grammarViewController.openGrammar();
        if (grammarViewController.hasGrammarFile()) {
            syntacticAnalysisMenu.setDisable(false);
            syntacticAnalysisMenu.getItems().forEach(item -> {
                item.setDisable(false);
            });
        }
    }

    @FXML
    private void saveGrammar() {
        ControllerMediator.getInstance().getGrammarViewController().saveGrammar();
        Grammar grammar = ControllerMediator.getInstance().getGrammar();
        if (grammar != null && !grammar.isEmpty()) {
            ControllerMediator.getInstance().getLl1ViewController().loadGrammar();
        }
    }

    @FXML
    private void openLL1Tab() {
        ll1Tab.setDisable(false);
        ControllerMediator.getInstance().getLl1ViewController().loadGrammar();
        tabPane.getSelectionModel().select(ll1Tab);
    }

    @FXML
    private void setSLRTab() {
        ControllerMediator.getInstance().getLrViewController()
                .setLRType(LRParsingTable.Type.SLR);
        openLRTab();
    }

    @FXML
    private void setLR0Tab() {
        ControllerMediator.getInstance().getLrViewController()
                .setLRType(LRParsingTable.Type.LR0);
        openLRTab();
    }

    private void openLRTab() {
        lrTab.setDisable(false);
        tabPane.getSelectionModel().select(lrTab);
    }
}
