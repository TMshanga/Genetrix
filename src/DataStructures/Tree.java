package DataStructures;

import java.util.ArrayList;
import java.util.List;

import DataStructures.Tree.Node;

public class Tree<T> {
    public Node<T> root;

    public Tree(T rootData) {
        root = new Node<T>();
        root.data = rootData;
        root.parent = root;
        root.children = new ArrayList<Node<T>>();
    }
    public static class Node<T> {
    	public T data;
        private Node<T> parent;
        public ArrayList<Node<T>> children;
        
        public Node(){children = new ArrayList<Node<T>>();}
        public Node(T _data){children = new ArrayList<Node<T>>();this.data = _data;}
        public Node(T _data,Node<T> _parent){children = new ArrayList<Node<T>>();this.data = _data;this.parent = _parent;}
    
        public Node<T> getParent(){return parent;}
    }    
        
    public static <T> void add(Node<T> parent, Node<T> node) {
    	parent.children.add(node);
    	node.parent = parent;
    }
    public static <T> void add(Node<T> parent, T data) {
    	add(parent,new Node<T>(data));   	
    }
    
    public static <T> void remove(Node<T> parent, Node<T> child) 
    {
    	parent.children.remove(child);
    }
    public static <T> void remove(Node<T> parent, T data) 
    {
    	for(Node<T> child: parent.children)
    		if(child.data == data) {parent.children.remove(data);}
    }
    public static <T> void remove(Node<T> parent, int index) 
    {
    	parent.children.remove(index);
    }
 
    public static <T> void move(Node<T> parent, Node<T> node){
    	if(node.parent!=null){node.parent.children.remove(node); add(parent,node);}
    }
    public static <T> void move(Node<T> parent, Node<T> node, int newIndex){
    	if(node.parent!=null)if(node.parent.children.remove(node)) parent.children.add(newIndex, node);
    }

    public static <T> Node<T> getChild(Node<T> parent, T data){
		for(Node<T> chrNode: parent.children)
			if(data == chrNode.data)
    			return chrNode;
		return null;
    }
 
    public static <T> boolean hasChild(Node<T> parent, T nodeValue) {
    	for(Node<T> child: parent.children) 
    		if(child.data == nodeValue)return true;
    	return false;
    }
    
    public static <T> Node<T> getNode(Node<T> root, ArrayList<Integer> address){
    	System.out.print(address.toString());
    	Node<T> currentNode = root;
    	for(int i=1;i<address.size();i++)
    		currentNode = currentNode.children.get(address.get(i));
    	return currentNode;
    }

    public List<ArrayList<T>> DFS(){
    	ArrayList<ArrayList<T>> depthOrderedList = new ArrayList<ArrayList<T>>();
    	int depth=0;
    	depthOrderedList.add(new ArrayList<T>());
    	depthOrderedList.get(depth).add((T)root.data);
    	for(Object child: root.children)
    		depthOrderedList = DFS((Node<T>)child,depthOrderedList,depth+1);
		return depthOrderedList; 	
    }   
  
    private ArrayList<ArrayList<T>> DFS(Node<T> node, ArrayList<ArrayList<T>> depthOrderedList,int depth){ 	
    	if(depthOrderedList.size() == depth) depthOrderedList.add(new ArrayList<T>());
    	depthOrderedList.get(depth).add((T)node.data);
    	for(Object child: node.children)
    		depthOrderedList = DFS((Node<T>)child,depthOrderedList,depth+1);
    	return depthOrderedList;
    } 
}	
