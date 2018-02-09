package DataStructures;

import java.util.ArrayList;

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
        public void add(Node<T> child,int index) {
        	if (child.parent!=null)child.parent.remove(child);
        	this.children.ensureCapacity(index+1);
        	this.children.set(index, child);     	
        	child.parent = this;  	
        }
        public void add(T data, int index) {
        	add(new Node<T>(data),index);   	
        }     
        
        public Node<T> branch(T data){
        	Node<T> child = new Node<T>(data);
        	add(child);
        	return child;
        }

        public void remove(Node<T> child) 
        {
        	this.children.remove(child);
        }
        public void remove(T data) 
        {
        	for(int i=0;i<this.children.size();i++)
        		if(this.children.get(i).data == data) {this.children.remove(this.children.get(i));}
        }
        public void remove(int childIndex) 
        {
        	this.children.remove(childIndex);
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
    
    public Node<T> getNode(ArrayList<Integer> address){
    	Node<T> currentNode = this.root;
    	for(int i=1;i<address.size();i++)
    		currentNode = currentNode.children.get(address.get(i));
    	return currentNode;
    }
}	
