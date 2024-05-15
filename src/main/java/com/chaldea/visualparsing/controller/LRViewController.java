package com.chaldea.visualparsing.controller;

import com.chaldea.visualparsing.ArrayHelper;
import com.chaldea.visualparsing.debug.LRParsingAlgorithm;
import com.chaldea.visualparsing.debug.LRParsingObserver;
import com.chaldea.visualparsing.debug.StepwiseAlgorithmDebugger;
import com.chaldea.visualparsing.exception.LRConflictException;
import com.chaldea.visualparsing.exception.LRParsingException;
import com.chaldea.visualparsing.grammar.*;
import com.chaldea.visualparsing.gui.DialogShower;
import com.chaldea.visualparsing.gui.LRParsingStepData;
import com.chaldea.visualparsing.parsing.*;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class LRViewController implements LRParsingObserver {
    @FXML
    private HBox rootHBox;
    @FXML
    private SplitPane horizontalSplitPane;
    @FXML
    private SplitPane verticalSplitPane;
    @FXML
    private Label grammarTypeLabel;
    @FXML
    private AnchorPane lrTableAnchorPane;
    @FXML
    private TextField inputStringTextField;
    @FXML
    private Button resumeButton;
    @FXML
    private Button stepButton;

    /**
     * The Table view.
     * Integer 是状态，ActionItem 是表项。在 GOTO 列的表项，
     * ActionItem 中的 action 无效，只使用 number 代表转换的状态
     *
     * <p>也许应该将ActionItem换为LRParsingTable，这样就可以更方便
     * 的添加表格数据和设置每列的显示</p>
     */
    @FXML
    private TableView<Pair<Integer, ActionItem[]>> parsingTableView;
    @FXML
    private TableColumn<Pair<Integer, ActionItem[]>, Integer> stateColumn;
    @FXML
    private TableColumn<Pair<Integer, ActionItem[]>, String> actionColumn;
    @FXML
    private TableColumn<Pair<Integer, ActionItem[]>, Integer> gotoColumn;

    @FXML
    private VBox algorithmVBox;
    @FXML
    private TableView<LRParsingStepData> stepDataTableView;
    @FXML
    private TableColumn<LRParsingStepData, Integer> numberColumn;
    @FXML
    private TableColumn<LRParsingStepData, String> stateStackColumn;
    @FXML
    private TableColumn<LRParsingStepData, String> symbolStackColumn;
    @FXML
    private TableColumn<LRParsingStepData, String> inputColumn;
    @FXML
    private TableColumn<LRParsingStepData, String> actionTakenColumn;

    @FXML
    private WebView webView;

    private Grammar grammar;
    private LRParsingTable lrParsingTable;
    private final StepwiseAlgorithmDebugger algorithmDebugger;
    private static final Logger logger = LoggerFactory.getLogger(LRViewController.class);


    public LRViewController() {
        algorithmDebugger = new StepwiseAlgorithmDebugger();
        ControllerMediator.getInstance().setLrViewController(this);
        logger.debug("已经注册LRViewController");
    }

    @FXML
    private void initialize() {
        setStateColumnCellFactory();
        setStepDataColumnsCellFactory();
        Platform.runLater(this::setLayout);
        bindCheckBoxOnAction();
        initializeWebView();
    }

    /**
     * Sets grammar. 设置相应文法及具体LR文法处理技术
     */
    void setLRType(LRParsingTable.Type parsingTableType) {
        grammar = ControllerMediator.getInstance().getGrammar();
        LRParsingStepData.setGrammar(grammar);
        try {
            switch (parsingTableType) {
                case SLR:
                    grammarTypeLabel.setText("SLR");
                    lrParsingTable = new SLRParsingTable(grammar);
                    break;
                case LR0:
                    grammarTypeLabel.setText("LR(0)");
                    lrParsingTable = new LR0ParsingTable(grammar);
                    break;
                case LR1:
                    grammarTypeLabel.setText("LR(1)");
                    lrParsingTable = new LR1ParsingTable(grammar);
                    break;
                case LALR:
                    grammarTypeLabel.setText("LALR");
                    lrParsingTable = new LALRParsingTable(grammar);
                    break;
                default:
                    DialogShower.showErrorDialog("未知的LR类型" + parsingTableType);
                    break;
            }
            setActionColumns();
            setGotoColumns();
            setTableViewDate();
        } catch (LRConflictException e) {
            logger.error(e.toString());
            DialogShower.showErrorDialog(grammarTypeLabel.getText() + "分析表构建冲突");
        }
    }

    @FXML
    private void showLrCollection() {
        DialogShower.showInformationDialog(lrParsingTable.getLrCollection().toString());
    }

    @FXML
    private void processInputString() {
        if (inputStringTextField.getText().isBlank()) {
            DialogShower.showErrorDialog("输入不能为空");
            return;
        }
        // 清除原先的数据
        resetWebView();
        stepDataTableView.getItems().clear();
        // 将输入字符串转换为Terminal的列表
        List<Terminal> inputSymbolList = Grammars
                .convertStringToTerminalList(grammar, inputStringTextField.getText());
        LRParsingAlgorithm algorithm = new LRParsingAlgorithm(
                lrParsingTable, inputSymbolList
        );
        algorithm.addObserver(this);
        algorithmDebugger.setStepwiseAlgorithm(algorithm);
        LRParsingStepData.setInputSymbols(inputSymbolList);
        // 设置 继续按钮、向前一步按钮 可点击
        resumeButton.setDisable(false);
        stepButton.setDisable(false);
        // 开始执行debugger
        algorithmDebugger.start();
    }

    @FXML
    private void resumeDebugger() {
        algorithmDebugger.resume();
    }

    @FXML
    private void stepDebugger() {
        algorithmDebugger.step();
    }

    /**
     * Sets state column cell factory.
     * 设置表格状态列的数据显示格式
     */
    private void setStateColumnCellFactory() {
        stateColumn.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(cellData.getValue().getKey()));
    }

    /**
     * Sets action columns.
     */
    private void setActionColumns() {
        List<TableColumn<Pair<Integer, ActionItem[]>, String>> actionColumnsList
                = new ArrayList<>(grammar.getTerminals().size() + 1);
        Terminal[] actionColumnsHeader = lrParsingTable.getActionColumnsHeader();
        for (Terminal terminal : actionColumnsHeader) {
            TableColumn<Pair<Integer, ActionItem[]>, String> temp =
                    new TableColumn<>(terminal.getValue());
            temp.setCellValueFactory(cellData -> {
                int index = ArrayHelper.findIndex(actionColumnsHeader, terminal);
                ActionItem actionItem = cellData.getValue().getValue()[index];
                if (actionItem == null) {
                    return null;
                }
                String displayText = switch (actionItem.action()) {
                    case SHIFT -> "s" + actionItem.number();
                    case REDUCE -> "r" + actionItem.number();
                    case ACCEPT -> "acc";
                };
                return new SimpleStringProperty(displayText);
            });
            temp.setEditable(false);
            temp.setSortable(false);
            actionColumnsList.add(temp);
        }
        actionColumn.getColumns().clear();
        actionColumn.getColumns().addAll(actionColumnsList);
    }

    private void setGotoColumns() {
        List<TableColumn<Pair<Integer, ActionItem[]>, Integer>> gotoColumnsList
                = new ArrayList<>(grammar.getNonterminals().size());
        Nonterminal[] gotoColumnsHeader = lrParsingTable.getGotoColumnsHeader();
        for (Nonterminal nonterminal : gotoColumnsHeader) {
            TableColumn<Pair<Integer, ActionItem[]>, Integer> temp = getGotoNonterminalColumn(nonterminal, gotoColumnsHeader);
            gotoColumnsList.add(temp);
        }
        gotoColumn.getColumns().clear();
        gotoColumn.getColumns().addAll(gotoColumnsList);
    }

    private TableColumn<Pair<Integer, ActionItem[]>, Integer> getGotoNonterminalColumn(Nonterminal nonterminal, Nonterminal[] gotoColumnsHeader) {
        TableColumn<Pair<Integer, ActionItem[]>, Integer> temp =
                new TableColumn<>(nonterminal.getValue());
        int index = ArrayHelper.findIndex(gotoColumnsHeader, nonterminal)
                + actionColumn.getColumns().size();
        temp.setCellValueFactory(cellData -> {
            ActionItem actionItem = cellData.getValue().getValue()[index];
            return actionItem.number() == -1 ?
                    null : new ReadOnlyObjectWrapper<>(actionItem.number());
        });
        temp.setEditable(false);
        temp.setSortable(false);
        return temp;
    }

    private void setTableViewDate() {
        Collection<Pair<Integer, ActionItem[]>> dataCollection = new LinkedList<>();
        for (int i = 0; i < lrParsingTable.getLrCollection().size(); i++) {
            ActionItem[] actionItems = new ActionItem[actionColumn.getColumns().size()
                    + gotoColumn.getColumns().size()];
            System.arraycopy(lrParsingTable.getActionTable()[i], 0,
                    actionItems, 0, lrParsingTable.getActionTable()[i].length);
            int index = lrParsingTable.getActionTable()[i].length;
            for (ItemSet itemSet : lrParsingTable.getGotoTable()[i]) {
                int number = lrParsingTable.getLrCollection().getItemSetNumber(itemSet);
                actionItems[index++] = new ActionItem(null, number);
            }
            dataCollection.add(new Pair<>(i, actionItems));
        }
        parsingTableView.getItems().clear();
        parsingTableView.getItems().addAll(dataCollection);
    }

    private void setLayout() {
        horizontalSplitPane.prefWidthProperty().bind(rootHBox.widthProperty());
        parsingTableView.prefWidthProperty().bind(verticalSplitPane.widthProperty().subtract(25));
        parsingTableView.prefHeightProperty().bind(lrTableAnchorPane.heightProperty().subtract(40));
        stepDataTableView.prefHeightProperty().bind(horizontalSplitPane.heightProperty().subtract(405));
        AnchorPane anchorPane = (AnchorPane) stepDataTableView.getParent();
        stepDataTableView.prefWidthProperty().bind(anchorPane.widthProperty().subtract(14));
    }

    private void setStepDataColumnsCellFactory() {
        numberColumn.setCellValueFactory(x -> x.getValue().getNumber());
        stateStackColumn.setCellValueFactory(x -> x.getValue().getStateStack());
        symbolStackColumn.setCellValueFactory(x -> x.getValue().getSymbolStack());
        inputColumn.setCellValueFactory(x -> x.getValue().getInputSymbols());
        actionTakenColumn.setCellValueFactory(x -> x.getValue().getAction());
    }

    @Override
    public void addStepData(ActionItem actionItem) {
        LRParsingAlgorithm algorithm =
                (LRParsingAlgorithm) algorithmDebugger.getStepwiseAlgorithm();
        LRParsingStepData stepData = new LRParsingStepData(
                algorithm.getSymbolIndex(),
                stepDataTableView.getItems().size() + 1,
                algorithm.getStateStack(),
                algorithm.getSymbolStack(),
                actionItem
        );
        stepDataTableView.getItems().add(stepData);
    }

    @Override
    public void showNextAlgorithmStep(int index) {
        String styleClass = "next-statement";
        algorithmVBox.getChildren().filtered(node -> node instanceof CheckBox)
                .forEach(node -> node.getStyleClass().remove(styleClass));
        int stepNumbers = (int) algorithmVBox.getChildren().stream()
                .filter(node -> node instanceof CheckBox).count();
        if (index == -1 || index >= stepNumbers) {
            return;
        }
        algorithmVBox.getChildren().get(index + 1).getStyleClass().add(styleClass);
    }

    @Override
    public void showException(Exception e) {
        if (e instanceof LRParsingException e1) {
            DialogShower.showErrorDialog("LR语法分析错误：" + e1.getMessage());
        }
    }

    @Override
    public void completeExecution() {
        DialogShower.showInformationDialog("分析完毕");
        executeJavaScript("recalculateLevel()");
    }

    @Override
    public void addNodeToTree(Terminal terminal) {
        executeJavaScript("addNodeToTree('" + escapeString(terminal.getValue()) + "');");
    }

    @Override
    public void addParentNodeToTree(Nonterminal nonterminal, ProductionSymbol... symbols) {
        StringBuilder script = new StringBuilder();
        script.append("addParentNodeToTree('").append(escapeString(nonterminal.getValue()))
                .append("'");
        for (ProductionSymbol symbol : symbols) {
            script.append(", '").append(escapeString(symbol.getValue())).append("'");
        }
        script.append(");");
        executeJavaScript(script.toString());
    }

    @Override
    public void initializeParserState(int state) {
        executeJavaScript("initializeParserStateNode('" + state + "');");
    }

    @Override
    public void addNodeToState(String state, ProductionSymbol symbol) {
        executeJavaScript("addNodeToState('" + state + "', '" +
                escapeString(symbol.getValue()) + "');");
    }

    @Override
    public void rollbackState(Production production) {
        executeJavaScript("rollbackState("
                + production.getBody().get(0).getValue().length + ");");
    }

    /**
     * 为checkbox伪代码行绑定处理断点事件
     */
    private void bindCheckBoxOnAction() {
        int index = 0;
        List<Node> nodes =
                algorithmVBox.getChildren().filtered(n -> n instanceof CheckBox);
        for (Node node : nodes) {
            CheckBox checkBox = (CheckBox) node;
            final int i = index++;
            checkBox.setOnAction(event -> algorithmDebugger.toggleBreakPoint(i));
        }
    }

    private void initializeWebView() {
        webView.getEngine().load(Objects.requireNonNull(getClass().getResource("graph.html")).toExternalForm());
    }

    /**
     * 清除现有节点图及语法翻译器状态显示
     */
    private void resetWebView() {
        webView.getEngine().reload();
    }

    /**
     * Execute java script.
     * <p>该方法能否保证script的执行顺序目前是不得而知的</p>
     *
     * @param script the script
     */
    private void executeJavaScript(String script) {
        WebEngine webEngine = webView.getEngine();
        // 如果页面已经加载完成
        if (webEngine.getLoadWorker().getState() == javafx.concurrent.Worker.State.SUCCEEDED) {
            // 直接执行脚本
            logger.debug("执行js：{}", script);
            webEngine.executeScript(script);
            return;
        }
        // 否则等待页面加载完成后执行脚本
        // 创建一个ChangeListener
        ChangeListener<Worker.State> listener = new ChangeListener<>() {
            @Override
            public void changed(ObservableValue<? extends Worker.State> observable, javafx.concurrent.Worker.State oldValue, javafx.concurrent.Worker.State newState) {
                if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                    logger.debug("执行js：{}", script);
                    // 页面加载成功后执行的脚本
                    webEngine.executeScript(script);
                    // 执行完毕后移除监听器
                    observable.removeListener(this);
                }
            }
        };
        // 添加监听器等待页面加载完成
        webEngine.getLoadWorker().stateProperty().addListener(listener);
    }

    /**
     * Escape string string.对字符串进行转义。
     * <p>转义其中的'与"及其它特殊符号</p>
     *
     * @param string the string
     * @return the string
     */
    private String escapeString(String string) {
        return string.replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                .replace("\b", "\\b")
                .replace("\f", "\\f");
    }
}
