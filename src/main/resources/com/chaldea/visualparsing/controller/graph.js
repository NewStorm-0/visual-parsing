const treeNodes = new vis.DataSet([]);

const treeEdges = new vis.DataSet([]);

// provide the data in the vis format
const treeData = {
    nodes: treeNodes,
    edges: treeEdges
};
let options = {
    layout: {
        improvedLayout: true,
        hierarchical: {
            enabled: true,
            levelSeparation: 80,
            nodeSpacing: 50,
            treeSpacing: 80,
            direction: 'UD',
            sortMethod: 'hubsize',
            shakeTowards: "roots"
        }
    }
};

const symbolNumberMap = new Map();
const treeNodeIdStack = [];

/**
 * 向语法分析树中加一个节点
 * @param symbolValue 节点代表的文法符号的值
 * @returns {string} 节点的id
 */
function addNodeToTree(symbolValue) {
    let value;
    if (symbolNumberMap.has(symbolValue)) {
        value = symbolValue + symbolNumberMap.get(symbolValue);
        symbolNumberMap.set(symbolValue, symbolNumberMap.get(symbolValue) + 1);
    } else {
        symbolNumberMap.set(symbolValue, 2);
        value = symbolValue + '1';
    }
    treeNodes.add({id: value, label: symbolValue});
    treeNodeIdStack.push(value);
    return value;
}

/**
 * 向语法分析树中的children节点加同一个父节点parent，并且连接相应的边
 * @param parent 父节点对应的文法符号的值
 * @param children 子节点们对应的文法符号的值
 */
function addParentNodeToTree(parent, ...children) {
    const childrenNodes = [];
    for (const childrenNode of children) {
        childrenNodes.push(treeNodeIdStack.pop());
    }
    let parentNodeId = addNodeToTree(parent);
    for (const childrenNode of childrenNodes) {
        treeEdges.add({from: childrenNode, to: parentNodeId});
    }
}

const parseTreeContainer = document.getElementById('parse-tree');
const treeNetwork = new vis.Network(parseTreeContainer, treeData, options);
treeNetwork.setOptions(options);