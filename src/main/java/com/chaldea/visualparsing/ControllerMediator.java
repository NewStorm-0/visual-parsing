package com.chaldea.visualparsing;

import com.chaldea.visualparsing.controller.GrammarViewController;
import com.chaldea.visualparsing.controller.MainFrameController;
import com.chaldea.visualparsing.controller.UserManualController;
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

    /**
     * 获取当前 Scene
     * @return Scene
     */
    public Scene getScene() {
        return mainFrameController.getTopVBox().getScene();
    }

    /**
     * 修改窗口标题前缀
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
     * Gets grammar.
     *
     * @return the grammar
     */
    @Deprecated
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
