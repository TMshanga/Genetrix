package dataStructures;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Stack;
import java.util.stream.IntStream;

import org.apache.commons.text.WordUtils;

import dataStructures.Tree.Node;

public class Trie extends Tree<Character>{

	public Trie(){
		super('\0');
	}
	
    public void buildLanguageTrie(ArrayList<String> wordList) {
    	this.setRoot(new Node<Character>('\0'));
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

    public boolean strictlyHasWord(String word) {
    	Node<Character> currentNode = (Node<Character>)this.root;
    	char[] charArr = word.toCharArray();
    	for(int i=0;i<charArr.length;i++)
    		if (currentNode.hasChild(charArr[i]))
    			currentNode = currentNode.getChild(charArr[i]);
    		else return false; 		
    	if (currentNode.hasChild('\0')) return true;
    	else return false;
    }
    
    public boolean hasWord(String word){
    	String[] wordForms = new String[5];   
    	word = word.replaceAll("\\p{Punct}+", "");
    	wordForms[0] = word;
    	wordForms[1] = WordUtils.capitalize(word);
    	wordForms[2] = WordUtils.capitalizeFully(word);
    	wordForms[3] = WordUtils.uncapitalize(word);
    	wordForms[4] = word.toUpperCase();
    	
    	for (String form: wordForms)
    		if (strictlyHasWord(form)) return true;

    	return false;
    }

    public void removeWord(String word){
    	if(strictlyHasWord(word)) {
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
 	
    public  void depthFirstPrint(){
    	depthFirstPrint(root,new ArrayList<Node<Character>>(),new Stack<Character>());
    }
    private  void depthFirstPrint(Node<Character> node, ArrayList<Node<Character>> visited,Stack<Character> stack){
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
 				depthFirstPrint(n,visited,stack);
 		}		
 		if((Character)node.data != null)
	 		if((Character)node.data != '\0') 
	 	 		if(!stack.isEmpty()) stack.pop();
 	}
    public ArrayList<String> getSuggestions(String word){
    	int mistakenCharNo = (word.length()<=6)?1:(word.length()<=8)?2:3;
    	boolean checkOtherLengths = (word.length()<=7)?false:true;
    	return getSuggestions(word, mistakenCharNo, checkOtherLengths);
    }
    
    public ArrayList<String> getSuggestions(String word, int mistakenCharNo, boolean checkOtherLengths){
    	ArrayList<String> suggestions = new ArrayList<String>();
		if(checkOtherLengths) {
	    	for(int i=0;i<word.length();i++)
	    		getSuggestionsStage2(suggestions,(new StringBuilder(word).deleteCharAt(i)).toString(), mistakenCharNo,-1);	
	    	for(int i=0;i<word.length();i++)
	    		getSuggestionsStage2(suggestions,(new StringBuilder(word).insert(i, '_')).toString(), mistakenCharNo,i);	
		}
		getSuggestionsStage2(suggestions,word, mistakenCharNo,-1);
    	return suggestions;
    }      
    private void getSuggestionsStage2(ArrayList<String> suggestions,String word, int mistakenCharNo, int certainMistakeIndex){    	
    	
    	ArrayList<ArrayList<Integer>> combinations = new ArrayList<ArrayList<Integer>>();
    	for(int i=1;i<=mistakenCharNo;i++)
    		combinations.addAll(combine(word.length(),i));
    	
	    for(int i=0;certainMistakeIndex>=0 && i<combinations.size();i++)
	    	combinations.get(i).add(certainMistakeIndex);
    	
		for(int i=0;i<combinations.size();i++)
	    	getSuggestionsStage3(this.root,word.toCharArray(),suggestions,0,combinations.get(i));
    }   
    private void getSuggestionsStage3(Node<Character> currentNode, char[] originalWord, ArrayList<String> suggestions, int currentIndex, ArrayList<Integer> mistakeIndexes){
    	for(;currentIndex<originalWord.length;currentIndex++) {
    		if (mistakeIndexes.contains(currentIndex)) {
    			for(Node<Character> child:currentNode.getChildren())
    				getSuggestionsStage3(child, originalWord, suggestions, currentIndex+1, mistakeIndexes);
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
 		InputStream in = getClass().getResourceAsStream("/external/" + file); 
 		BufferedReader br = new BufferedReader(new InputStreamReader(in));
        ArrayList<String> list = new ArrayList<String>();
        String line;
        while ((line = br.readLine()) != null)
           list.add(line);
        br.close();
        return list;	
 	}

 	public ArrayList<ArrayList<Integer>> combine(int n, int k) {
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
