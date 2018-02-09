package ProjectSections;
import java.io.IOException;
import java.util.ArrayList;

import DataStructures.Tree;
import DataStructures.Trie;

public class Project{
	public Page page = new Page();	
	
	public Project() {
		pageTree = new Tree<PageInterface>(new Book());
	}
	public Project(String title) {
		pageTree = new Tree<PageInterface>(new Book(title));
	}
	
	public Tree<PageInterface> pageTree = new Tree<PageInterface>(new Book());
    
    public static void oldDictionaryStuff() throws IOException {    	
    	Trie trie = new Trie();
    	trie.buildLanguageTrie(Trie.readWordList(System.getProperty("user.dir") + "\\src\\External\\EnglishLanguage.txt"));
    	
    	ArrayList<String> suggestions = trie.getSuggestions("fire", 1,false);
    	for(String sug: suggestions) {
    		System.out.println(sug);
    	}  	
    	System.out.println(trie.containsWord("anime")); 
    }
    
    public static void main(String[] args) throws IOException{
    	oldDictionaryStuff();
    }
}
