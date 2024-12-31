package com.chaldea.visualparsing.controller;

import com.chaldea.visualparsing.exception.grammar.IllegalSymbolException;
import com.chaldea.visualparsing.exception.grammar.RepeatedSymbolException;
import com.chaldea.visualparsing.grammar.*;
import com.chaldea.visualparsing.gui.DialogShower;
import com.chaldea.visualparsing.gui.ExpressionHBox;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Grammar view controller.
 */
public class GrammarViewController {
    @FXML
    private VBox topVBox;
    @FXML
    private VBox scrollVBox;
    @FXML
    private SplitPane splitPane;
    @FXML
    private ScrollPane grammarScrollPane;
    @FXML
    private Button addExpressionButton;
    @FXML
    private ListView<Nonterminal> nonterminalListView;
    @FXML
    private ListView<Terminal> terminalListView;
    @FXML
    private VBox nonterminalVBox;
    @FXML
    private VBox terminalVBox;
    @FXML
    private Button nonterminalNewButton;
    @FXML
    private Button nonterminalDeleteButton;
    @FXML
    private Button terminalNewButton;
    @FXML
    private Button terminalDeleteButton;
    @FXML
    private Label startSymbolLabel;
    private final ObservableList<ExpressionHBox> expressionHBoxList;

    private Grammar grammar;
    private static final Logger logger = LoggerFactory.getLogger(GrammarViewController.class);
    /**
     * 当前打开的语法文件
     */
    private File grammarFile;
    private final BooleanProperty unsaved = new SimpleBooleanProperty(false);

    public GrammarViewController() {
        expressionHBoxList = FXCollections.observableArrayList();
        expressionHBoxList.addListener(this::expressionHBoxListListener);
        unsaved.addListener(this::unsavedListener);
        ControllerMediator.getInstance().setGrammarViewController(this);
        logger.debug("已经注册GrammarViewController");
    }

    Grammar getGrammar() {
        return grammar;
    }

    @FXML
    private void initialize() {
        nonterminalListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Nonterminal nonterminal, boolean empty) {
                super.updateItem(nonterminal, empty);
                if (empty || nonterminal == null) {
                    setText(null);
                } else {
                    setText(nonterminal.getValue());
                }
            }
        });
        terminalListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Terminal terminal, boolean empty) {
                super.updateItem(terminal, empty);
                if (empty || terminal == null) {
                    setText(null);
                } else {
                    setText(terminal.getValue());
                }
            }
        });
        splitPane.prefHeightProperty().bind(topVBox.heightProperty().subtract(60));
        splitPane.getDividers().forEach(this::splitPaneDividersHandler);
        grammarScrollPane.prefWidthProperty().bind(topVBox.widthProperty().subtract(20));
        nonterminalListView.prefHeightProperty().bind(splitPane.heightProperty().subtract(90));
        nonterminalListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        terminalListView.prefHeightProperty().bind(splitPane.heightProperty().subtract(90));
        terminalListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }

    /**
     * 清理现场。
     * <p>检查当前是否新建或者修改了文法且还没有保存，并且对用户进行询问是否保存
     * 根据用户选择来决定是否保存之前进行的修改。然后将现有的一些数据进行清除，以
     * 便后面新的操作。</p>
     */
    private void cleanupPreviousData() {
        // 检查是否有之前未保存的修改
        if (unsaved.get()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("未保存");
            alert.setHeaderText("保存已经进行的修改？");
            alert.setContentText("当前文件还未保存，在进行新的操作之前，是否保存？如果您未保存，会丢失之前的修改");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                saveGrammar();
                unsaved.set(false);
                logger.debug("OK了，家人们");
            } else if (result.isPresent()) {
                unsaved.set(false);
                logger.debug(result.get().getButtonData().toString());
            }
        }
        // 清理数据
        grammar = null;
        grammarFile = null;
        scrollVBox.getChildren().clear();
//        nonterminalListView.getItems().clear();
//        terminalListView.getItems().clear();
        nonterminalListView.setItems(null);
        terminalListView.setItems(null);
        expressionHBoxList.clear();
        startSymbolLabel.setText("开始符号：");
        addExpressionButton.setDisable(true);
        nonterminalNewButton.setDisable(true);
        nonterminalDeleteButton.setDisable(true);
        terminalNewButton.setDisable(true);
        terminalDeleteButton.setDisable(true);
    }

    /**
     * 新建一个文法
     */
    void createGrammar() {
        cleanupPreviousData();
        Optional<String> startSymbolOptional =
                DialogShower.showInputDialog("请输入开始符号", "非终结符：");
        if (startSymbolOptional.isEmpty()) {
            return;
        }
        if (startSymbolOptional.get().isBlank()) {
            DialogShower.showErrorDialog("不能为空白符号");
            return;
        }
        grammar = new Grammar(startSymbolOptional.get());
        ControllerMediator.getInstance().setStageTitlePrefix("未命名文法");
        startSymbolLabel.setText("开始符号：" + startSymbolOptional.get());
//        scrollVBox.getChildren().addAll(expressionHBoxList);
        initializeListView(grammar.getNonterminals(), grammar.getTerminals());
        addExpressionButton.setDisable(false);
        nonterminalNewButton.setDisable(false);
        nonterminalDeleteButton.setDisable(false);
        terminalNewButton.setDisable(false);
        terminalDeleteButton.setDisable(false);
    }

    /**
     * 选择一个文件，并从该文件中读出一个文法
     */
    void openGrammar() {
        cleanupPreviousData();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("打开文法文件");
        fileChooser.setInitialDirectory(new File("./"));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("所有文件", "*.*"),
                new FileChooser.ExtensionFilter("文法文件", "*.gra")
        );
        grammarFile = fileChooser.showOpenDialog(ControllerMediator
                .getInstance().getScene().getWindow());
        // 判断用户有没有选择文件
        if (grammarFile == null) {
            logger.debug("用户没有选择文件");
            return;
        }
        logger.debug(grammarFile.getAbsolutePath());
        try {
            grammar = GrammarReaderWriter.readGrammarFromFile(grammarFile);
            logger.debug("打开文法：{}", grammar);
            // 将文法中所有产生式进行添加
            for (Production production : grammar.getProductions()) {
                Nonterminal head = production.getHead();
                for (Expression expression : production.getBody()) {
                    expressionHBoxList.add(new ExpressionHBox(head, expression));
                }
            }
            // 添加所有非终结符和终结符
            initializeListView(grammar.getNonterminals(), grammar.getTerminals());
            // 其余操作
            ControllerMediator.getInstance().setStageTitlePrefix(grammarFile.getName());
            startSymbolLabel.setText("开始符号：" + grammar.getStartSymbol().getValue());
            addExpressionButton.setDisable(false);
            nonterminalNewButton.setDisable(false);
            nonterminalDeleteButton.setDisable(false);
            terminalNewButton.setDisable(false);
            terminalDeleteButton.setDisable(false);
        } catch (FileNotFoundException e) {
            grammarFile = null;
            DialogShower.showErrorDialog("文件不存在", "无法读取文件！");
        } catch (StreamCorruptedException e) {
            grammarFile = null;
            DialogShower.showErrorDialog("读取文法错误", "获取文法失败！");
        } catch (InvalidClassException e) {
            grammarFile = null;
            DialogShower.showErrorDialog("文法文件版本与本程序版本不兼容");
        } catch (IOException e) {
            grammarFile = null;
            DialogShower.showExceptionDialog(e);
            logger.error("打开文法文件错误", e);
        }
    }

    /**
     * 保存当前文法到文件中
     * <p>用户新建文件后，第一次保存要弹出对话框选择文件位置，后续就成为第三种情况</p>
     */
    void saveGrammar() {
        if (grammarFile == null && !unsaved.get() && grammar == null) {
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
            grammarFile = fileChooser.showSaveDialog(ControllerMediator
                    .getInstance().getScene().getWindow());
            // 判断用户有没有选择文件
            if (grammarFile == null) {
                return;
            }
            ControllerMediator.getInstance().setStageTitlePrefix(grammarFile.getName());
            // 若选择了文件，则一定符合下列判定
        }
        if (grammarFile != null && unsaved.get()) {
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
                // 提取文法左公因子
                Grammars.extractingLeftCommonFactors(grammar);
                // 消除文法左递归
                Grammars.eliminateLeftRecursion(grammar);
                GrammarReaderWriter.writeGrammarToFile(grammar, grammarFile);
                unsaved.set(false);
                DialogShower.showInformationDialog("保存到文件成功");
                logger.debug("保存文法：{} 到 {}", grammar, grammarFile.getAbsolutePath());
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
     * Gets nonterminal copy.
     *
     * @return the nonterminal copy
     */
    Set<Nonterminal> getNonterminalCopy() {
        return new HashSet<>(grammar.getNonterminals());
    }

    /**
     * Gets terminal copy.
     *
     * @return the terminal copy
     */
    Set<Terminal> getTerminalCopy() {
        return new HashSet<>(grammar.getTerminals());
    }

    /**
     * Delete expressionHBox.
     *
     * @param expressionHBox the expression h box
     */
    public void deleteExpressionHBox(ExpressionHBox expressionHBox) {
        expressionHBoxList.remove(expressionHBox);
        expressionHBoxList.forEach(ExpressionHBox::setNumberLabel);
    }

    public void addExpressionToGrammar(Nonterminal head, Expression body) {
        grammar.addExpression(head, body);
        unsaved.set(true);
    }

    public void deleteExpressionFromGrammar(Nonterminal head, Expression body) {
        grammar.deleteExpression(head, body);
        unsaved.set(true);
    }

    /**
     * Has grammar file boolean.
     * 检测打开文法后，是否成功打=开了选择的文法文件
     *
     * @return the boolean
     */
    boolean hasGrammarFile() {
        return grammarFile != null;
    }

    /**
     * 增加表达式。向界面中增加一组表达式的相关组件。
     */
    @FXML
    private void addExpressionHBox() {
        if (expressionHBoxList.isEmpty()) {
            Platform.runLater(() -> expressionHBoxList.add(new ExpressionHBox(1)));
            return;
        }
        ExpressionHBox lastExpressionHBox = expressionHBoxList.get(expressionHBoxList.size() - 1);
        if (lastExpressionHBox != null && lastExpressionHBox.isEmpty()) {
            // 上一个增加的表达式内容还没有填
            DialogShower.showWarningDialog("请在填写当前产生式后再添加新的表达式");
            new Thread(() -> {
                lastExpressionHBox.toWarningState();
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    DialogShower.showExceptionDialog(e);
                }
                lastExpressionHBox.toNormalState();
            }).start();
        } else {
            Platform.runLater(() ->
                    expressionHBoxList.add(new ExpressionHBox(expressionHBoxList.size() + 1)));
        }

    }

    /**
     * 新增非终结符
     */
    @FXML
    private void newNonterminal() {
        TextInputDialog inputDialog = new TextInputDialog();
        inputDialog.setTitle("提示");
        inputDialog.setHeaderText("请输入一个非终结符");
        inputDialog.setContentText("符号：");
        inputDialog.showAndWait().ifPresent(symbol -> {
            if (symbol.isBlank()) {
                DialogShower.showErrorDialog("输入不可以为空");
                return;
            }
            try {
                Nonterminal nonterminal = new Nonterminal(symbol);
                grammar.addNonterminal(nonterminal);
                nonterminalListView.getItems().add(nonterminal);
            } catch (RepeatedSymbolException | IllegalSymbolException e) {
                DialogShower.showErrorDialog(e.getMessage());
            }
        });
    }

    /**
     * 删除非终结符
     */
    @FXML
    private void deleteNonterminal() {
        Nonterminal removedNonterminal =
                nonterminalListView.getSelectionModel().getSelectedItem();
        grammar.removeNonterminal(removedNonterminal);
        nonterminalListView.getItems().remove(removedNonterminal);
        logger.debug("Grammar Nonterminals: {}", grammar.getNonterminals());
    }

    /**
     * 新增终结符
     */
    @FXML
    private void newTerminal() {
        TextInputDialog inputDialog = new TextInputDialog();
        inputDialog.setTitle("提示");
        inputDialog.setHeaderText("请输入一个终结符");
        inputDialog.setContentText("符号：");
        inputDialog.showAndWait().ifPresent(symbol -> {
            if (symbol.isBlank()) {
                DialogShower.showErrorDialog("输入不可以为空");
                return;
            }
            try {
                Terminal terminal = new Terminal(symbol);
                grammar.addTerminal(terminal);
                terminalListView.getItems().add(terminal);
            } catch (RepeatedSymbolException | IllegalSymbolException e) {
                DialogShower.showErrorDialog(e.getMessage());
            }
        });
    }

    /**
     * 删除终结符
     */
    @FXML
    private void deleteTerminal() {
        Terminal removedTerminal = terminalListView.getSelectionModel().getSelectedItem();
        grammar.removeTerminal(removedTerminal);
        terminalListView.getItems().remove(removedTerminal);
        logger.debug("Grammar Terminals: {}", grammar.getTerminals());
    }

    /**
     * 初始化 nonterminalListView 和 terminalListView
     *
     * @param nonterminalCollection 非终结符集合
     * @param terminalCollection    终结符集合
     */
    private void initializeListView(Collection<Nonterminal> nonterminalCollection,
                                    Collection<Terminal> terminalCollection) {
        // 添加所有非终结符
        ObservableList<Nonterminal> nonterminalObservableList;
        nonterminalObservableList =
                FXCollections.observableArrayList(nonterminalCollection);
        nonterminalListView.setItems(nonterminalObservableList);
        // 添加所有终结符
        ObservableList<Terminal> terminalObservableList;
        terminalObservableList =
                FXCollections.observableArrayList(terminalCollection);
        terminalListView.setItems(terminalObservableList);
    }

    /**
     * 设置分割条最小最大宽度，动态调整符号展示部分宽度
     *
     * @param divider 分割条
     */
    private void splitPaneDividersHandler(SplitPane.Divider divider) {
        // 防止 divider.setPosition 再次引起监听器动作
        AtomicBoolean stop = new AtomicBoolean(false);
        divider.positionProperty().addListener(((observable, oldValue, newValue) -> {
            if (stop.get()) {
                return;
            }
            // 设置分割条最小最大宽度
            double minDividerWidth = 350;
            double maxDividerWidth = topVBox.getWidth() - 250;
            double minPosition = minDividerWidth / splitPane.getWidth();
            double maxPosition = maxDividerWidth / splitPane.getWidth();
            stop.set(true);
            if (newValue.doubleValue() < minPosition) {
                divider.setPosition(minPosition);
            } else if (newValue.doubleValue() > maxPosition) {
                divider.setPosition(maxPosition);
            }
            stop.set(false);
            // 动态调整符号展示部分
            double rightWidth = splitPane.getWidth() * (1 - divider.getPosition());
            nonterminalVBox.setPrefWidth(rightWidth / 2);
            terminalVBox.setPrefWidth(rightWidth / 2);
        }));
    }

    /**
     * expressionHBoxList 监听器，向 scrollVBox 中动态增减 expressionHBox
     *
     * @param change the change
     */
    private void expressionHBoxListListener(
            ListChangeListener.Change<? extends ExpressionHBox> change) {
        while (change.next()) {
            if (change.wasAdded()) {
                for (ExpressionHBox expressionHBox : change.getAddedSubList()) {
                    scrollVBox.getChildren().add(expressionHBox);
                }
            }

            if (change.wasRemoved()) {
                for (ExpressionHBox expressionHBox : change.getRemoved()) {
                    scrollVBox.getChildren().remove(expressionHBox);
                }
            }
        }
    }

    private void unsavedListener(ObservableValue<? extends Boolean> observable,
                                 Boolean oldValue, Boolean newValue) {
        String stageTitlePrefix = ControllerMediator.getInstance().getStageTitlePrefix();
        if (oldValue && !newValue) {
            // 由 true 变为 false
            ControllerMediator.getInstance().setStageTitlePrefix(stageTitlePrefix
                    .substring(0, stageTitlePrefix.length() - 1));
        } else if (!oldValue && newValue) {
            // 由 false 变为 true
            ControllerMediator.getInstance().setStageTitlePrefix(stageTitlePrefix + "*");
        }
    }

}