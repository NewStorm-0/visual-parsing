const treeNodes = new vis.DataSet([]);

const treeEdges = new vis.DataSet([]);

// provide the data in the vis format
const treeData = {
    nodes: treeNodes,
    edges: treeEdges
};
let treeOptions = {
    layout: {
        improvedLayout: true,
        hierarchical: {
            enabled: true,
            levelSeparation: 150,
            nodeSpacing: 100,
            treeSpacing: 100,
            direction: 'UD',
            sortMethod: 'hubsize'
        }
    },
    nodes: {
        borderWidthSelected: 2.5,
        font: {
            size: 40
        }
    },
    edges: {
        width: 5,
    }
};

const treeSymbolNumberMap = new Map();
const treeNodeStack = [];

/**
 * 向语法分析树中加一个节点
 * @param symbolValue 节点代表的文法符号的值
 * @returns {string} 节点的id
 */
function addNodeToTree(symbolValue) {
    symbolValue = String(symbolValue);
    let value;
    if (treeSymbolNumberMap.has(symbolValue)) {
        value = symbolValue + treeSymbolNumberMap.get(symbolValue);
        treeSymbolNumberMap.set(symbolValue, treeSymbolNumberMap.get(symbolValue) + 1);
    } else {
        treeSymbolNumberMap.set(symbolValue, 2);
        value = symbolValue + '1';
    }
    treeNodes.add({id: value, label: symbolValue, level: 0});
    treeNodeStack.push(treeNodes.get(value));
    return value;
}

/**
 * 向语法分析树中的children节点加同一个父节点parent，并且连接相应的边
 * @param parent 父节点对应的文法符号的值
 * @param children 子节点们对应的文法符号的值
 */
function addParentNodeToTree(parent, ...children) {
    parent = String(parent);
    const childrenNodes = [];
    let minLevel = 0;
    for (const childrenNode of children) {
        const temp = treeNodeStack.pop()
        childrenNodes.push(temp);
        minLevel = Math.min(minLevel, temp.level)
    }
    let parentNodeId = addNodeToTree(parent);
    // 更新父节点的level
    const temp = treeNodes.get(parentNodeId);
    temp.level = minLevel - 1;
    treeNodes.update(temp);

    for (const childrenNode of childrenNodes) {
        // 此处添加的边用于记录父子关系
        treeEdges.add({from: parentNodeId, to: childrenNode.id});
    }
}

/**
 * 从根节点开始计算level，使树更美丽
 */
function recalculateLevel() {
    const root = treeNodeStack.pop();
    _recalculateLevel(root);
    // 刷新边的布局
    treeNetwork.setOptions(treeOptions);
}

function _recalculateLevel(root) {
    const childrenId = findChildrenNodes(root.id, treeEdges);
    for (const childId of childrenId) {
        const childNode = treeNodes.get(childId);
        childNode.level = root.level + 1;
        treeNodes.update(childNode);
        _recalculateLevel(childNode);
    }
}

/**
 * 获取一个节点的所有子节点的id
 * @param nodeId
 * @param edges 边的dataset
 * @returns {*[]}
 */
function findChildrenNodes(nodeId, edges) {
    let childrenId = [];
    // 遍历所有边以找到子节点
    edges.forEach((edge) => {
        if (edge.from === nodeId) {
            childrenId.push(edge.to);
        }
    });
    return childrenId;
}

const parseTreeContainer = document.getElementById('parse-tree');
const treeNetwork = new vis.Network(parseTreeContainer, treeData, treeOptions);
treeNetwork.setOptions(treeOptions);


const stateNodes = new vis.DataSet([]);
const stateEdges = new vis.DataSet([]);

const stateData = {
    nodes: stateNodes,
    edges: stateEdges
}

let stateOptions = {
    layout: {
        improvedLayout: true,
        hierarchical: {
            enabled: false,
            levelSeparation: 220,
            nodeSpacing: 100,
            treeSpacing: 100,
            direction: 'DU',
            sortMethod: 'hubsize'
        }
    },
    nodes: {
        borderWidthSelected: 2.5,
        font: {
            size: 40
        }
    },
    edges: {
        width: 5,
        arrows: 'to',
        font: {
            size: 40,
            align: 'horizontal'
        },
        length: 300
    }
};

const stateSymbolNumberMap = new Map();
const stateNodeStack = [];

const parserStateContainer = document.getElementById('parser-state');
const stateNetwork = new vis.Network(parserStateContainer, stateData, stateOptions);
stateNetwork.setOptions(stateOptions);

/**
 * 添加语法翻译器最初的状态节点
 * @param initialState 最初的状态
 */
function initializeParserStateNode(initialState) {
    initialState = String(initialState);
    stateSymbolNumberMap.set(initialState, 2);
    stateNodes.add({id: initialState + '1', label: initialState});
    stateNodeStack.push(stateNodes.get(initialState + '1'));
}

/**
 * 向语法分析器状态中加一个节点
 * @param state 节点代表的分析器的状态
 * @param symbol 文法符号的值，通过该文法符号语法分析器的状态改变
 * @returns {string} 节点的id
 */
function addNodeToState(state, symbol) {
    state = String(state);
    symbol = String(symbol);
    let value;
    if (stateSymbolNumberMap.has(state)) {
        value = state + stateSymbolNumberMap.get(state);
        stateSymbolNumberMap.set(state, stateSymbolNumberMap.get(state) + 1);
    } else {
        stateSymbolNumberMap.set(state, 2);
        value = state + '1';
    }
    stateNodes.add({id: value, label: state});
    // 连接与上一个状态的边
    const lastStateNode = stateNodeStack[stateNodeStack.length - 1];
    stateEdges.add({from: lastStateNode.id, to: value, label: symbol});
    stateNodeStack.push(stateNodes.get(value));
    return value;
}

/**
 * 回退语法分析器状态，在归约时发生
 * @param number 回退的状态的个数
 */
function rollbackState(number) {
    number = Number(number);
    for (let i = 0; i < number; i++) {
        const current = stateNodeStack.pop();
        // const last = stateNodeStack[stateNodeStack.length - 1];
        // const edgeToRemove = stateEdges.get({
        //     filter: function (item) {
        //         return (item.from === last.id && item.to === current.id);
        //     }
        // });
        // if (edgeToRemove.length > 1) {
        //     throw new Error("有多条从" + last.id + "到" + current.id + "的边");
        // }
        // stateEdges.remove(edgeToRemove);
        // vis.js 删除节点后会自动删除相关联的边
        stateNodes.remove(current);
    }
}
