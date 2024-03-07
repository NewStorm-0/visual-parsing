package com.chaldea.visualparsing.debug;

import java.util.HashSet;
import java.util.Set;

/**
 * The type Stepwise algorithm debugger.
 */
public class StepwiseAlgorithmDebugger {
    private StepwiseAlgorithm stepwiseAlgorithm;
    private final Set<Integer> breakpointSet;

    public StepwiseAlgorithmDebugger(StepwiseAlgorithm stepwiseAlgorithm) {
        this();
        this.stepwiseAlgorithm = stepwiseAlgorithm;
    }

    public StepwiseAlgorithmDebugger() {
        breakpointSet = new HashSet<>();
    }

    public StepwiseAlgorithm getStepwiseAlgorithm() {
        return stepwiseAlgorithm;
    }

    public void setStepwiseAlgorithm(StepwiseAlgorithm stepwiseAlgorithm) {
        this.stepwiseAlgorithm = stepwiseAlgorithm;
    }

    /**
     * Start. 从头开始执行算法
     */
    public void start() {
        stepwiseAlgorithm.reset();
        while (!breakpointSet.contains(stepwiseAlgorithm.currentStepIndex) &&
                !stepwiseAlgorithm.isExecutionComplete()) {
            stepwiseAlgorithm.executeStep();
        }
    }

    /**
     * Step. 算法向下执行一步
     */
    public void step() {
        if (stepwiseAlgorithm.isExecutionComplete()) {
            return;
        }
        stepwiseAlgorithm.executeStep();
    }

    /**
     * Resume.继续运行算法直到碰到断点
     */
    public void resume() {
        if (!stepwiseAlgorithm.isExecutionComplete()) {
            stepwiseAlgorithm.executeStep();
        }
        while (!breakpointSet.contains(stepwiseAlgorithm.getCurrentStepIndex()) &&
                !stepwiseAlgorithm.isExecutionComplete()) {
            stepwiseAlgorithm.executeStep();
        }
    }

    /**
     * Toggle break point.修改断点
     * <p>若已经包含该断点，则删除；若不包含，则添加</p>
     *
     * @param index the index
     */
    public void toggleBreakPoint(int index) {
        if (!breakpointSet.remove(index)) {
            breakpointSet.add(index);
        }
    }

}
