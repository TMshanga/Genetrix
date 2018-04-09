package dataStructures;

import java.util.ArrayList;

public class Tree<T> {
    private Node<T> root;

    public Tree(T rootData) {
        setRoot(rootData);
    }
    public Tree(Node<T> root) {
        setRoot(root);
    }
    public static class Node<T> {
    	public T data;
        private Node<T> parent;
        private ArrayList<Node<T>> children;
        
        public Node(){children = new ArrayList<Node<T>>();}
        public Node(T _data){children = new ArrayList<Node<T>>();this.data = _data;}
        public Node(T _data,Node<T> _parent){children = new ArrayList<Node<T>>();this.data = _data;_parent.add(this);}
    
        public Node<T> getParent(){return parent;}
        public ArrayList<Node<T>> getChildren(){return children;}
        
        public void add(Node<T> child) {
        	if (child.parent!=null) {child.parent.remove(child);}
        	this.children.add(child);
        	child.parent = this;
        }
        public void add(T data) {
        	add(new Node<T>(data));   	
        }  
        public void add(Node<T> child, int index) {
        	if (child.parent!=null)child.parent.remove(child);
        	this.children.ensureCapacity(index+1);
        	this.children.add(index, child);     	
        	child.parent = this;  	
        }
        public void add(T data, int index) {
        	add(new Node<T>(data),index);   	
        }     
        
        public void shiftAdd(Node<T> child, int index) {
        	if (child.parent!=null)child.parent.remove(child);
        	this.children.add(index, child);     	
        	child.parent = this;  	
        }
        public void shiftAdd(T data, int index) {
        	shiftAdd(new Node<T>(data),index);
        }
        
        public Node<T> branch(T data){
        	Node<T> child = new Node<T>(data);
        	add(child);
        	return child;
        }

        public void remove(Node<T> child) 
        {
        	if(this.children.remove(child)) child.parent = null;
        }
        public void remove(T data) 
        {
        	for(int i=0;i<this.children.size();i++)
        		if(this.children.get(i).data == data) {this.children.remove(this.children.get(i)); this.children.get(i).parent = null;}
        }
        public void remove(int childIndex) 
        {
        	children.get(childIndex).parent = null;
        	this.children.remove(childIndex);
        }
        public void removeSelf() {
        	if(parent != null)
        		parent.getChildren().remove(this);
        	parent = null;
        }
       
        public boolean hasChild(T nodeValue) {
        	for(Node<T> child: this.children) 
        		if(child.data == nodeValue) return true;
        	return false;
        }
        public Node<T> getChild(T data){
    		for(Node<T> chrNode: this.children)
    			if(data == chrNode.data) return chrNode;
    		return null;
        }
         
        public Node<T> getAncestor(){
        	Node<T> node = this;
        	while(node!=null) {
        		if (node==node.parent) break;
        		node = node.parent;
        	}
        	return node;
        }
        public boolean hasAncestor(Node<T> ancestor){
        	Node<T> node = this;
        	while(node!=null) {
        		node = node.parent;
        		if(ancestor==node) return true;
        		else if (node == null) break;
        		else if (node == node.parent) break;
        	}
        	return false;
        }
        
        public ArrayList<Integer> getAddress(){
        	ArrayList<Integer> address = new ArrayList<Integer>();
        	Node<T> node = this;
        	while(node!=null) {
        		if(node.parent==null) return null;
        		if (node==node.parent) {
            		address.add(0,0);
        			break;
        		}
        		address.add(0,node.parent.getChildren().indexOf(node));
        		node = node.parent;
        	}
        	return address;
        }
        
     	public void dfs(Node<T> node, ArrayList<Node<T>> visited)
     	{
     		if(visited ==null)visited = new ArrayList<Node<T>>();
            visited.add(node);
     		for (int i = 0; i < node.children.size(); i++) {
     			Node<T> n= node.children.get(i);
     			if(n!=null && !visited.contains(n))
     			{
     				dfs(n,visited);
     			}
     		}
     	}
    }    
    
    public void setRoot(T rootData) {
        setRoot(new Node<T>(rootData));
    }
    public void setRoot( Node<T> node) {
    	root = node;
    	node.parent = root;
    	node.children = new ArrayList<Node<T>>();
    }
    
    public Node<T> getRoot() {
    	return root;
    }    
    
    public Node<T> getNode(ArrayList<Integer> address){
    	Node<T> currentNode = this.root;
    	for(int i=1;i<address.size();i++)
    		currentNode = currentNode.children.get(address.get(i));
    	return currentNode;
    }
    public void setNode(ArrayList<Integer> address, Node<T> node) {
    	if(address.size()==1 && address.get(0)==0) {
    		setRoot(node);
    		return;
    	}
    	Node<T> currentNode = this.root;
    	for(int i=1;i<address.size()-1;i++)
    		currentNode = currentNode.children.get(address.get(i));
		currentNode.add(node, address.get(address.size()-1));
    }
}	
