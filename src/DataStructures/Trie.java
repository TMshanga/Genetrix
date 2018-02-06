package DataStructures;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import DataStructures.Tree.Node;

public class Trie<T> extends Tree<T>{

	public Trie(T data) {
		super(data);
	}
	
    public static Trie<Character> buildDictionaryTrie(ArrayList<String> wordList, char separator) {
    	Trie<Character> trie = new Trie<Character>(null);
    	
    	for(int i=0;i<wordList.size();i++) {
    		Node<Character> currentNode = trie.root;
        	char[] charArr = wordList.get(i).toCharArray();
    		for(int c=0;c<charArr.length;c++){
    			if(!Tree.hasChild(currentNode, charArr[c])){
    				Tree.add(currentNode, charArr[c]);
    			}
    			currentNode = getChild(currentNode,charArr[c]);
    		}
			Trie.add(currentNode, new Node<Character>('\0'));
    	} 	
    	return trie;
    }

    public boolean containsWord(String word) {
    	Node<Character> currentNode = (Node<Character>)this.root;
    	char[] charArr = word.toCharArray();
    	for(int i=0;i<charArr.length;i++) {
    		if (Tree.hasChild(currentNode, charArr[i])) {
    			for(Node<Character> chrNode: currentNode.children) {
    				if(charArr[i] == chrNode.data) {
    	    			currentNode = chrNode;
    				}
    			}
    		}
    		else return false; 		
    	}
    	if (Tree.hasChild(currentNode,'\0')) return true;
    	else return false;
    }

 // Recursive DFS
 	public  void dfs(Node<T> node, ArrayList<Node<T>> visited)
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
 	
 	public  void dfsS(Node<T> node, ArrayList<Node<T>> visited,Stack<T> stack)
 	{
 		if(stack == null)stack= new Stack<T>();
 		if(visited == null)visited = new ArrayList<Node<T>>();
 		
 		if((Character)node.data != null){
	 		if((Character)node.data == '\0') {
	 			String arr = stack.toString().replace(",", "").replace("[", "").replace("]", "").replace(" ", "");
	 	 		System.out.println(arr);
	 		}
	 		else stack.push(node.data);
 		}	 		
        visited.add(node);
 		for (int i = 0; i < node.children.size(); i++) {
 			Node<T> n= node.children.get(i);
 			if(n!=null && !visited.contains(n))
 			{
 				dfsS(n,visited,stack);
 			}
 		}
 		
 		if((Character)node.data != null){
	 		if((Character)node.data != '\0') 
	 	 		if(!stack.isEmpty()) stack.pop();
 		}
 	}
}
