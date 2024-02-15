package com.chaldea.visualparsing;

import com.chaldea.visualparsing.controller.GrammarViewController;
import com.chaldea.visualparsing.controller.MainFrameController;
import com.chaldea.visualparsing.controller.UserManualController;
import javafx.scene.Scene;

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
