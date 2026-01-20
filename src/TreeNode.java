import java.util.LinkedList;

public class TreeNode<T> {

    T data;
    TreeNode<T> parent;
    LinkedList<TreeNode<T>> children;

    public TreeNode(T data) {
        this.data = data;
        this.children = new LinkedList<TreeNode<T>>();
    }

    public TreeNode<T> addChild(T child) {
        TreeNode<T> childNode = new TreeNode<T>(child);
        childNode.parent = this;
        this.children.add(childNode);
        return childNode;
    }

    public TreeNode<T> replaceChild(TreeNode<T> operator, T replacer) {
        TreeNode<T> replaceNode = new TreeNode<T>(replacer);
        replaceNode.parent = operator.parent;
        return replaceNode;
    }

}
