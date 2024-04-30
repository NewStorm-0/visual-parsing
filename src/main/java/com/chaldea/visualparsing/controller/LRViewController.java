package com.chaldea.visualparsing.controller;

import com.chaldea.visualparsing.debug.LRParsingAlgorithm;
import com.chaldea.visualparsing.debug.StepwiseAlgorithmDebugger;
import com.chaldea.visualparsing.grammar.Grammar;
import com.chaldea.visualparsing.grammar.Nonterminal;
import com.chaldea.visualparsing.grammar.Terminal;
import com.chaldea.visualparsing.gui.DialogShower;
import com.chaldea.visualparsing.parsing.ActionItem;
import com.chaldea.visualparsing.parsing.LRParsingTable;
import com.chaldea.visualparsing.parsing.SLRParsingTable;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    /**
     * The Table view.
     * Integer 是状态，ActionItem 是表项。在 GOTO 列的表项，
     * ActionItem 中的 action 无效，只使用 number 代表转换的状态
     */
    @FXML
    private TableView<Pair<Integer, ActionItem>> tableView;
    @FXML
    private TableColumn<Pair<Integer, ActionItem>, Integer> stateColumn;
    @FXML
    private TableColumn<Pair<Integer, ActionItem>, String> actionColumn;
    @FXML
    private TableColumn<Pair<Integer, ActionItem>, Integer> gotoColumn;

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
    public void initialize() {
        setStateColumnCellFactory();
        setLayout();
    }

    /**
     * Sets grammar. 设置相应文法及具体LR文法处理技术
     */
    public void setLRType(LRParsingTable.Type parsingTableType) {
        grammar = ControllerMediator.getInstance().getGrammar();
        LRParsingAlgorithm lrParsingAlgorithm =
                (LRParsingAlgorithm) algorithmDebugger.getStepwiseAlgorithm();
        switch (parsingTableType) {
            case SLR:
                grammarTypeLabel.setText("SLR");
                lrParsingTable = new SLRParsingTable(grammar);
                break;
            case LR0:
                grammarTypeLabel.setText("LR(0)");
                break;
            case LR1:
                grammarTypeLabel.setText("LR(1)");
                break;
            case LALR:
                grammarTypeLabel.setText("LALR");
                break;
            default:
                DialogShower.showErrorDialog("未知的LR类型" + parsingTableType);
                break;
        }
        lrParsingAlgorithm.setLrParsingTable(lrParsingTable);
        setActionColumns();
        setGotoColumns();
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
        List<TableColumn<Pair<Integer, ActionItem>, String>> actionColumnsList
                = new ArrayList<>(grammar.getTerminals().size() + 1);
        for (Terminal terminal : lrParsingTable.getActionColumnsHeader()) {
            TableColumn<Pair<Integer, ActionItem>, String> temp =
                    new TableColumn<>(terminal.getValue());
            temp.setCellValueFactory(cellData -> {
                ActionItem actionItem = cellData.getValue().getValue();
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
        actionColumn.getColumns().addAll(actionColumnsList);
    }

    private void setGotoColumns() {
        List<TableColumn<Pair<Integer, ActionItem>, Integer>> gotoColumnsList
                = new ArrayList<>(grammar.getNonterminals().size());
        for (Nonterminal nonterminal : lrParsingTable.getGotoColumnsHeader()) {
            TableColumn<Pair<Integer, ActionItem>, Integer> temp =
                    new TableColumn<>(nonterminal.getValue());
            temp.setCellValueFactory(cellData ->
                    new ReadOnlyObjectWrapper<>(cellData.getValue().getValue().number()));
            temp.setEditable(false);
            temp.setSortable(false);
            gotoColumnsList.add(temp);
        }
        gotoColumn.getColumns().addAll(gotoColumnsList);
    }

    private void setLayout() {
        horizontalSplitPane.prefWidthProperty().bind(rootHBox.widthProperty());
        tableView.prefWidthProperty().bind(verticalSplitPane.widthProperty().subtract(25));
        tableView.prefHeightProperty().bind(lrTableAnchorPane.heightProperty().subtract(40));
    }
}
