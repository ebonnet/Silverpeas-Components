/*--------------------------------------------------|
| This script can be used freely as long as all     |
| Updated: 28.06.2006 by Mahmoud Tahoon           
|--------------------------------------------------*/
// Node object
	this.id = id;
	this.pid = pid;
	this.name = name;
	this.url = url;
	this.title = title;
	this.target = target;
	this.icon = icon;
	this.iconOpen = iconOpen;
	this._io = open || false;
	this._is = false; // if this node is selected or not
	this._ls = false; // is this node is the last sibling
	this._hc = false; // if this node have children or not
	this._ai = 0;
	this._p;            // the parent node of this node
};

// Tree object
function dTree(objName) {
	this.config = {
		target			: null,
	this.icon = {
	this.obj = objName;
// Adds a new node to the node array
// Open/close all nodes
dTree.prototype.closeAll = function() {
// Outputs the tree to the page
	if (!this.selectedFound) this.selectedNode = null;
// Creates the tree structure
	if (this.config.inOrder) n = pNode._ai;
	var childsIndex =0;
	for (n; n<this.aNodes.length; n++) {
	}
	str += '</table>';
	return str;
};
// Creates the node icon, url and text
		node._isOnly=true;
	//alert("Node '"+node.name+"' is: ONLY:"+node._isOnly+" - Left: "+node._isLeft+" - Right: "+node._isRight+" PARENT: "+node._p._children);
	//alert("NODE NAME: "+node.name+" HAS CHILDREN: "+node._hc+" INDENT: "+this.indent(node, nodeId));
	var str = '<div class="dTreeNode" style="white-space:nowrap">';
	str += '<tr>';
	str += '	<td align="center" width="52%" ';
	str += '	<td valign="top" style="padding-top:0px;" align="center" width="1%" ';
	str += ' >';

	str += '	</td>';
	if (this.config.useIcons) {
	if (node.url) {
	str += node.name;
	if (node._hc) {
		str += '<tr><td height="9" align="center"><img src="'+this.icon.smallLine+'" alt="" border=0></td></tr>';
		str += '<tr><td>';
		str += this.addNode(node);
		str += '</div>';
	this.aIndent.pop();
// Adds the empty and line icons
// Checks if a node has any children and if it is the last sibling
	if (lastId==node.id) node._ls = true;
// Returns the selected node
// Highlights the selected node
// Toggle Open or close
// Open or close all nodes
// Opens the tree to a specific node
// Closes all nodes on the same level as certain node
// Closes all children of a node
// Change the status of a node(open or closed)
// [Cookie] Clears a cookie
// [Cookie] Sets value in a cookie
// [Cookie] Gets a value from a cookie
// [Cookie] Returns ids of open nodes as a string
// [Cookie] Checks if a node id is in a cookie
// If Push and pop is not implemented by the browser
if (!Array.prototype.pop) {