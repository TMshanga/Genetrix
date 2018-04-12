package dataStructures;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.commons.text.WordUtils;

public class Trie extends Tree<Character>{

	public Trie(){
		super('\0');
	}
	
    public void buildLanguageTrie(ArrayList<String> wordList) {
    	wordList.forEach((w) ->addWord(w));
    }
    
    public void addWords(String... words) {
  		for(String word:words) addWord(word);
    }
    public void addWord(String word) {
  		Node<Character> currentNode = getRoot();
    	char[] charArr = word.toCharArray();
		for(int c=0;c<charArr.length;c++){
			if(!currentNode.hasChild(charArr[c]))
				currentNode.add(charArr[c]);
			currentNode = currentNode.getChild(charArr[c]);
		}
		currentNode.add(new Node<Character>('\0'));
    }

    public boolean hasExactWord(String word) {
    	Node<Character> currentNode = (Node<Character>)getRoot();
    	char[] charArr = word.toCharArray();
    	for(int i=0;i<charArr.length;i++)
    		if (currentNode.hasChild(charArr[i]))
    			currentNode = currentNode.getChild(charArr[i]);
    		else return false; 		
    	if (currentNode.hasChild('\0')) return true;
    	else return false;
    }
    
    public boolean hasWord(String word){
    	String[] wordForms = new String[3];   
    	word = word.replaceAll("\\p{Punct}+", "");
    	wordForms[0] = word;
    	wordForms[1] = WordUtils.uncapitalize(word);
    	wordForms[2] = word.toUpperCase();
    	    	
    	for (String form: wordForms)
    		if (hasExactWord(form)) return true;
    	return false;
    }

    public void removeWord(String word){
    	if(hasExactWord(word)) {
	  		Node<Character> currentNode = (Node<Character>)getRoot();
	    	char[] charArr = word.toCharArray();
			for(int c=0;c<charArr.length;c++){ //the last letter is reached before backtracking
				currentNode = currentNode.getChild(charArr[c]);
			}
			currentNode.remove((Character)'\0');
			for(int c=0;c<charArr.length;c++){
				if (currentNode.getChildren().size()>0) break; //the end of a different word has been encountered
				else currentNode.getParent().getChildren().remove(currentNode);currentNode = currentNode.getParent(); 
			}	
		}			
    }
 
    public ArrayList<String> getSuggestions(String word){
    	int mistakenCharNo = (word.length()<=6)?1:(word.length()<=8)?2:3;
    	boolean checkOtherLengths = (word.length()<=7)?false:true;
    	return includeOtherLengths(word, mistakenCharNo, checkOtherLengths);
    }
    
    public ArrayList<String> includeOtherLengths(String word, int mistakenCharNo, boolean checkOtherLengths){
    	ArrayList<String> suggestions = new ArrayList<String>();
		if(checkOtherLengths) {
	    	for(int i=0;i<word.length();i++)
	    		includeMistakeCombinations(suggestions,(new StringBuilder(word).deleteCharAt(i)).toString(), mistakenCharNo,-1);	
	    	for(int i=0;i<word.length();i++)
	    		includeMistakeCombinations(suggestions,(new StringBuilder(word).insert(i, '_')).toString(), mistakenCharNo,i);	
		}
		includeMistakeCombinations(suggestions,word, mistakenCharNo,-1);
    	return suggestions;
    }      
    private void includeMistakeCombinations(ArrayList<String> suggestions,String word, int mistakenCharNo, int certainMistakeIndex){    	
    	
    	ArrayList<ArrayList<Integer>> combinations = new ArrayList<ArrayList<Integer>>();
    	for(int i=1;i<=mistakenCharNo;i++)
    		combinations.addAll(combination(word.length(),i));
    	
	    for(int i=0;certainMistakeIndex>=0 && i<combinations.size();i++)
	    	combinations.get(i).add(certainMistakeIndex);
    	
		for(int i=0;i<combinations.size();i++)
	    	getSuggestions(getRoot(),word.toCharArray(),suggestions,0,combinations.get(i));
    }   
    private void getSuggestions(Node<Character> currentNode, char[] originalWord, ArrayList<String> suggestions, int currentIndex, ArrayList<Integer> mistakeIndexes){
    	for(;currentIndex<originalWord.length;currentIndex++) {
    		if (mistakeIndexes.contains(currentIndex)) {
    			for(Node<Character> child:currentNode.getChildren())
    				getSuggestions(child, originalWord, suggestions, currentIndex+1, mistakeIndexes);
				return;
    		}
    		else if (currentNode.hasChild(originalWord[currentIndex]))
    			currentNode = currentNode.getChild(originalWord[currentIndex]);
    		else return;
    	}
    	if (currentNode.hasChild('\0')) {
    		char[] suggestion = new char[originalWord.length];
    		for(int i=suggestion.length-1;i>=0;i--) {
    			suggestion[i] = currentNode.data;
    			currentNode = currentNode.getParent();
    		}
    		if (!suggestions.contains(new String(suggestion))) suggestions.add(new String(suggestion));
    	}
    }
      
 	public ArrayList<String> readWordList(String file) throws IOException { 		
 		BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/external/" + file)));
        ArrayList<String> list = new ArrayList<String>();
        String line;
        while ((line = reader.readLine()) != null)
           list.add(line);
        reader.close();
        return list;	
 	}

 	public ArrayList<ArrayList<Integer>> combination(int n, int k) {
	ArrayList<ArrayList<Integer>> result = new ArrayList<ArrayList<Integer>>();
	if (n <= 0 || n < k) return result;
	ArrayList<Integer> item = new ArrayList<Integer>();
	dfs(n, k, 0, item, result); // because it need to begin from 0 
	return result;
}
 	private void dfs(int n, int k, int start, ArrayList<Integer> item,
		ArrayList<ArrayList<Integer>> res) {
	if (item.size() == k) {
		res.add(new ArrayList<Integer>(item));
		return;
	}
	for (int i = start; i <= n; i++) {
		item.add(i);
		dfs(n, k, i + 1, item, res);
		item.remove(item.size() - 1);
	}
}
}

class Tree<T> {
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
        
        public String toString() {
        	return data.toString();
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
