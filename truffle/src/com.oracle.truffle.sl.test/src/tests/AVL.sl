function main() {
    tree = make_avl_tree();
    i = 0;
    while (i < 1000) {
	insert_item(tree, i);
	i = i + 1;
    }
    //println(height(tree.root));
}

function make_avl_tree() {
    ret = new();
    ret.root = null();
    return ret;
}

function insert_item(tree, item) {
    tree.root = insert_inner(tree.root, item);
}

function height(node) {
    if (isNull(node)) {
	return 0;
    } else {
	return node.height;
    }
}

function update_height(node) {
    node.height = max(height(node.left), height(node.right)) + 1;
}

function right_rotate(node) {
    l = node.left;
    lr = l.right;
    l.right = node;
    node.left = lr;
    update_height(node);
    update_height(l);
    return l;
}

function left_rotate(node) {
    r = node.right;
    rl = r.left;
    r.left = node;
    node.right = rl;
    update_height(node);
    update_height(r);
    return r;
}

function balance_factor(node) {
    if (isNull(node)) {
	return 0;
    } else {
	return height(node.left) - height(node.right);
    }
}

function make_node(item) {
    ret = new();
    ret.item = item;
    ret.height = 1;
    ret.left = null();
    ret.right = null();
    return ret;
}

function insert_inner(node, item) {
    if (isNull(node)) {
	return make_node(item);
    }

    if (item == node.item) {
	return node;
    }

    if (item < node.item) {
	node.left = insert_inner(node.left, item);
    } else {
	node.right = insert_inner(node.right, item);
    }

    update_height(node);
    
    bf = balance_factor(node);
    if (bf > 1) {
	if (height(node.left.right) >= height(node.left.left)) {
	    node.left = left_rotate(node.left);
	}
	return right_rotate(node);
    }

    if (bf < 0 - 1) {
	if (height(node.right.left) >= height(node.right.right)) {
	    node.right = right_rotate(node.right);
	}
	return left_rotate(node);
    }

    return node;
}

function max(e1, e2) {
    if (e1 > e2) {
	return e1;
    } else {
	return e2;
    }
}

function null() {}
