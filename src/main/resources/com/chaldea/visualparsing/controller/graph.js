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

const symbolNumberMap = new Map();
const treeNodeStack = [];

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
        // // 先删除原先的边
        // const edgeIdToDelete = edges.get({
        //     filter: function (item) {
        //         return item.from === root.id && item.to === childNode.id;
        //     }
        // });
        // if (edgeIdToDelete.length > 0) {
        //     edges.remove(edgeIdToDelete[0].id);
        // }
        // // 再添加新的边
        // treeEdges.add({from: root.id, to: childNode.id});
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
        if(edge.from === nodeId) {
            childrenId.push(edge.to);
        }
    });
    return childrenId;
}

const parseTreeContainer = document.getElementById('parse-tree');
const treeNetwork = new vis.Network(parseTreeContainer, treeData, treeOptions);
treeNetwork.setOptions(treeOptions);