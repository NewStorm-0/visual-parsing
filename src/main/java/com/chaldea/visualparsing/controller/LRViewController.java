package com.chaldea.visualparsing.controller;

import com.chaldea.visualparsing.ArrayHelper;
import com.chaldea.visualparsing.debug.LRParsingAlgorithm;
import com.chaldea.visualparsing.debug.StepwiseAlgorithmDebugger;
import com.chaldea.visualparsing.exception.LRConflictException;
import com.chaldea.visualparsing.grammar.Grammar;
import com.chaldea.visualparsing.grammar.Nonterminal;
import com.chaldea.visualparsing.grammar.Terminal;
import com.chaldea.visualparsing.gui.DialogShower;
import com.chaldea.visualparsing.parsing.*;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class LRViewController {
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
    private AnchorPane rightAnchorPane;
    @FXML
    private TextField inputStringTextField;
    @FXML
    private Button resumeButton;
    @FXML
    private  Button stepButton;

    /**
     * The Table view.
     * Integer 是状态，ActionItem 是表项。在 GOTO 列的表项，
     * ActionItem 中的 action 无效，只使用 number 代表转换的状态
     *
     * <p>也许应该将ActionItem换为LRParsingTable，这样就可以更方便
     * 的添加表格数据和设置每列的显示</p>
     */
    @FXML
    private TableView<Pair<Integer, ActionItem[]>> tableView;
    @FXML
    private TableColumn<Pair<Integer, ActionItem[]>, Integer> stateColumn;
    @FXML
    private TableColumn<Pair<Integer, ActionItem[]>, String> actionColumn;
    @FXML
    private TableColumn<Pair<Integer, ActionItem[]>, Integer> gotoColumn;

    private Grammar grammar;
    private LRParsingTable lrParsingTable;
    private final StepwiseAlgorithmDebugger algorithmDebugger;
    private static final Logger logger = LoggerFactory.getLogger(LRViewController.class);


    public LRViewController() {
        algorithmDebugger = new StepwiseAlgorithmDebugger();
        algorithmDebugger.setStepwiseAlgorithm(new LRParsingAlgorithm());
        ControllerMediator.getInstance().setLrViewController(this);
        logger.debug("已经注册LRViewController");
    }

    @FXML
    private void initialize() {
        setStateColumnCellFactory();
        Platform.runLater(this::setLayout);
    }

    /**
     * Sets grammar. 设置相应文法及具体LR文法处理技术
     */
    void setLRType(LRParsingTable.Type parsingTableType) {
        grammar = ControllerMediator.getInstance().getGrammar();
        LRParsingAlgorithm lrParsingAlgorithm =
                (LRParsingAlgorithm) algorithmDebugger.getStepwiseAlgorithm();
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
        } catch (LRConflictException e) {
            DialogShower.showErrorDialog("LR分析冲突");
        }
        lrParsingAlgorithm.setLrParsingTable(lrParsingTable);
        setActionColumns();
        setGotoColumns();
        setTableViewDate();
    }

    @FXML
    private void showLrCollection() {
        DialogShower.showInformationDialog(lrParsingTable.getLrCollection().toString());
    }

    @FXML
    private void processInputString() {

    }

    @FXML
    private void resumeDebugger() {

    }

    @FXML
    private void stepDebugger() {

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
        tableView.getItems().clear();
        tableView.getItems().addAll(dataCollection);
    }

    private void setLayout() {
        horizontalSplitPane.prefWidthProperty().bind(rootHBox.widthProperty());
        tableView.prefWidthProperty().bind(verticalSplitPane.widthProperty().subtract(25));
        tableView.prefHeightProperty().bind(lrTableAnchorPane.heightProperty().subtract(40));
    }
}
