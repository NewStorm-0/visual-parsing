package com.chaldea.visualparsing.controller;

import com.chaldea.visualparsing.grammar.Grammar;
import com.chaldea.visualparsing.grammar.Nonterminal;
import com.chaldea.visualparsing.grammar.Terminal;
import javafx.scene.Scene;

import java.util.Set;

/**
 * controller 中介类
 * <p>协调各个controller之间的通信</p>
 */
public class ControllerMediator {
    private GrammarViewController grammarViewController;
    private UserManualController userManualController;
    private MainFrameController mainFrameController;
    private LL1ViewController ll1ViewController;
    private LRViewController lrViewController;

    public GrammarViewController getGrammarViewController() {
        return grammarViewController;
    }

    public void setGrammarViewController(GrammarViewController grammarViewController) {
        this.grammarViewController = grammarViewController;
    }

    public UserManualController getUserManualController() {
        return userManualController;
    }

    public void setUserManualController(UserManualController userManualController) {
        this.userManualController = userManualController;
    }

    public MainFrameController getMainFrameController() {
        return mainFrameController;
    }

    public void setMainFrameController(MainFrameController mainFrameController) {
        this.mainFrameController = mainFrameController;
    }

    public LL1ViewController getLl1ViewController() {
        return ll1ViewController;
    }

    public void setLl1ViewController(LL1ViewController ll1ViewController) {
        this.ll1ViewController = ll1ViewController;
    }

    /**
     * 获取当前主窗口的 Scene
     * @return Scene
     */
    public Scene getScene() {
        return mainFrameController.getTopVBox().getScene();
    }

    /**
     * 修改主窗口标题前缀
     * @param titlePrefix 标题前缀
     */
    public void setStageTitlePrefix(String titlePrefix) {
        mainFrameController.setStageTitlePrefix(titlePrefix);
    }

    /**
     * Gets stage title prefix.
     *
     * @return the stage title prefix
     */
    public String getStageTitlePrefix() {
        return mainFrameController.getStageTitlePrefix();
    }

    /**
     * Gets lr view controller.
     *
     * @return the lr view controller
     */
    public LRViewController getLrViewController() {
        return lrViewController;
    }

    /**
     * Sets lr view controller.
     *
     * @param lrViewController the lr view controller
     */
    public void setLrViewController(LRViewController lrViewController) {
        this.lrViewController = lrViewController;
    }

    /**
     * Gets grammar.
     *
     * @return the grammar
     */
    public Grammar getGrammar() {
        return grammarViewController.getGrammar();
    }

    public Set<Nonterminal> getNonterminalCopy() {
        return grammarViewController.getNonterminalCopy();
    }

    public Set<Terminal> getTerminalCopy() {
        return grammarViewController.getTerminalCopy();
    }

    /**
     * Everything below here is in support of Singleton pattern
     */
    private ControllerMediator() {}

    public static ControllerMediator getInstance() {
        return ControllerMediatorHolder.INSTANCE;
    }

    private static class ControllerMediatorHolder {
        private static final ControllerMediator INSTANCE = new ControllerMediator();
    }
}
