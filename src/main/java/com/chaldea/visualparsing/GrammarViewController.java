package com.chaldea.visualparsing;

import com.chaldea.visualparsing.grammar.*;
import com.chaldea.visualparsing.gui.ExceptionDialogUtils;
import com.chaldea.visualparsing.gui.ExpressionHBox;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Screen;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javafx.stage.Stage;
import org.controlsfx.dialog.ExceptionDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrammarViewController {

    public static String TITLE_SUFFIX = "——可视化分析器";
    @FXML
    protected VBox topVBox;
    @FXML
    protected TabPane tabPane;
    @FXML
    protected VBox scrollVBox;
    @FXML
    protected ScrollPane grammarScrollPane;
    @FXML
    protected Button addExpressionButton;
    private List<ExpressionHBox> expressionHBoxList;
    private Grammar grammar;
    private static final Logger logger = LoggerFactory.getLogger(GrammarViewController.class);
    /**
     * 当前打开的语法文件
     */
    private File grammarFile;
    private boolean unsaved = false;

    public GrammarViewController() {
        expressionHBoxList = new ArrayList<>(32);
    }

    @FXML
    public void initialize() {
        Screen screen = Screen.getPrimary();
        double topVBoxWidth = screen.getVisualBounds().getWidth() - 60;
        double topVBoxHeight = screen.getVisualBounds().getHeight() - 120;
        topVBox.setPrefSize(topVBoxWidth, topVBoxHeight);
        topVBox.setMinSize(400, 300);
        tabPane.prefHeightProperty().bind(topVBox.heightProperty().subtract(35));
//        tabPane.setPrefSize(topVBoxWidth, topVBoxHeight - 35);
//        grammarScrollPane.setPrefSize(topVBoxWidth, topVBoxHeight - 125);
        grammarScrollPane.prefHeightProperty().bind(topVBox.heightProperty().subtract(125));
    }

    /**
     * 清理现场。
     * <p>检查当前是否新建或者修改了文法且还没有保存，并且对用户进行询问是否保存
     * 根据用户选择来决定是否保存之前进行的修改。然后将现有的一些数据进行清除，以
     * 便后面新的操作。</p>
     */
    private void cleanupPreviousData() {
        // 检查是否有之前未保存的修改
        if (unsaved) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("未保存");
            alert.setHeaderText("保存已经进行的修改？");
            alert.setContentText("当前文件还未保存，在进行新的操作之前，是否保存？如果您未保存，会丢失之前的修改");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                saveGrammar();
                logger.debug("OK了，家人们");
            } else if (result.isPresent()) {
                unsaved = false;
                logger.debug(result.get().getButtonData().toString());
            }
        }
        // 清理数据
        grammar = null;
        grammarFile = null;
        scrollVBox.getChildren().clear();
    }

    /**
     * 修改窗口标题前缀
     * @param titlePrefix 标题前缀
     */
    private void setStageTitlePrefix(String titlePrefix) {
        ((Stage) topVBox.getScene().getWindow()).setTitle(titlePrefix + TITLE_SUFFIX);
    }

    /**
     * 新建一个文法
     */
    @FXML
    protected void createGrammar() {
        cleanupPreviousData();
        grammar = new Grammar();
        setStageTitlePrefix("未命名文法");
        addExpressionButton.setDisable(false);
    }

    /**
     * 选择一个文件，并从该文件中读出一个文法
     */
    @FXML
    protected void openGrammar() {
        cleanupPreviousData();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("打开文法文件");
        fileChooser.setInitialDirectory(new File("./"));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("所有文件", "*.*"),
                new FileChooser.ExtensionFilter("文法文件", "*.gra")
        );
        grammarFile = fileChooser.showOpenDialog(topVBox.getScene().getWindow());
        // 判断用户有没有选择文件
        if (grammarFile == null) {
            logger.debug("用户没有选择文件");
            return;
        }
        logger.debug(grammarFile.getAbsolutePath());
        try {
            grammar = GrammarReaderWriter.readGrammarFromFile(grammarFile);
            logger.debug(grammar.toString());
            setStageTitlePrefix(grammarFile.getName());
            // 将文法中所有产生式进行添加
            for (Production production : grammar.getProductions()) {
                Nonterminal head = production.getHead();
                for (Expression expression : production.getBody()) {
                    expressionHBoxList.add(new ExpressionHBox(head, expression));
                }
            }
            scrollVBox.getChildren().addAll(expressionHBoxList);
            addExpressionButton.setDisable(false);
        } catch (FileNotFoundException e) {
            grammarFile = null;
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("错误");
            alert.setHeaderText("文件不存在");
            alert.setContentText("无法读取文件！");
            alert.showAndWait();
        } catch (StreamCorruptedException e) {
            grammarFile = null;
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("错误");
            alert.setHeaderText("读取文法错误");
            alert.setContentText("获取文法失败！");
            alert.showAndWait();
        } catch (IOException e) {
            grammarFile = null;
            ExceptionDialogUtils.showExceptionDialog(e);
            logger.error("打开文法文件错误", e);
        }
    }

    /**
     * 保存当前文法到文件中
     * <p>用户新建文件后，第一次保存要弹出对话框选择文件位置，后续就成为第三种情况</p>
     */
    @FXML
    protected void saveGrammar() {
        if (grammarFile == null && !unsaved && grammar == null) {
            // 没有打开、新建、修改文法文件，则无需进行任何操作
            return;
        }
        if (grammarFile == null && grammar != null && !grammar.isEmpty()) {
            // 新建文件，并且已经修改
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("保存为 gra 文件");
            fileChooser.setInitialFileName("未命名文法");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("文法文件 (*.gra)", "*.gra")
            );
            grammarFile = fileChooser.showSaveDialog(topVBox.getScene().getWindow());
            // 判断用户有没有选择文件
            if (grammarFile == null) {
                return;
            }
            setStageTitlePrefix(grammarFile.getName());
            // 若选择了文件，则一定符合下列判定
        }
        if (grammarFile != null && unsaved) {
            if (grammar.isEmpty()) {
                // 如果文法产生式个数为 0
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("错误");
                alert.setHeaderText("保存文件错误");
                alert.setContentText("文法不能没有任何产生式！");
                alert.showAndWait();
            }
            // 打开文件并且进行了修改
            try {
                GrammarReaderWriter.writeGrammarToFile(grammar, grammarFile);
                unsaved = false;
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("错误");
                alert.setHeaderText("写入文件错误");
                alert.setContentText("向文件写入文法失败！");
                alert.showAndWait();
                logger.error("写入文法文件错误", e);
            }
        }
    }

    /**
     * 增加表达式
     */
    @FXML
    protected void addExpression() {
        // TODO:
        expressionHBoxList.add(new ExpressionHBox());
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

}