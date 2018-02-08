package DataStructures;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;

public class Trie extends Tree<Character>{

	public Trie(){
		super('\0');
	}
	
    public void buildLanguageTrie(ArrayList<String> wordList) {
    	this.root = new Node<Character>('\0');
    	for(int i=0;i<wordList.size();i++)
    		addWord(wordList.get(i));
    }
    
    public void addWords(String... words) {
  		for(String word:words) addWord(word);
    }
    public void addWord(String word) {
  		Node<Character> currentNode = root;
    	char[] charArr = word.toCharArray();
		for(int c=0;c<charArr.length;c++){
			if(!currentNode.hasChild(charArr[c]))
				currentNode.add(charArr[c]);
			currentNode = currentNode.getChild(charArr[c]);
		}
		currentNode.add(new Node<Character>('\0'));
    }

    public boolean containsWord(String word) {
    	Node<Character> currentNode = (Node<Character>)this.root;
    	char[] charArr = word.toCharArray();
    	for(int i=0;i<charArr.length;i++) {
    		if (currentNode.hasChild(charArr[i])) {
    			currentNode = currentNode.getChild(charArr[i]);
    		}
    		else return false; 		
    	}
    	if (currentNode.hasChild('\0')) return true;
    	else return false;
    }

    public void removeWord(String word){
    	if(containsWord(word)) {
	  		Node<Character> currentNode = (Node<Character>)root;
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
 	
    public  void depthFirstPrintTrie(){
    	depthFirstPrintTrie(root,new ArrayList<Node<Character>>(),new Stack<Character>());
    }
    public  void depthFirstPrintTrie(Node<Character> node, ArrayList<Node<Character>> visited,Stack<Character> stack){
 		if(stack == null)stack= new Stack<Character>();
 		if(visited == null)visited = new ArrayList<Node<Character>>();
 		
 		if((Character)node.data != null){
	 		if((Character)node.data == '\0') {
	 			String arr = stack.toString().replace(",", "").replace("[", "").replace("]", "").replace(" ", "");
	 	 		System.out.println(arr);
	 		}
	 		else stack.push(node.data);
 		}	 		
        visited.add(node);
 		for (int i = 0; i < node.getChildren().size(); i++) {
 			Node<Character> n= node.getChildren().get(i);
 			if(n!=null && !visited.contains(n))
 			{
 				depthFirstPrintTrie(n,visited,stack);
 			}
 		}		
 		if((Character)node.data != null){
	 		if((Character)node.data != '\0') 
	 	 		if(!stack.isEmpty()) stack.pop();
 		}
 	}

 	public static ArrayList<String> readWordList(String directory) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(directory));
        ArrayList<String> list = new ArrayList<String>();
        String line;
        while ((line = br.readLine()) != null)
           list.add(line);
        br.close();
        return list;	
 	}
}
