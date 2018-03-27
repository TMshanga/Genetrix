package ProjectSections;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import DataStructures.Tree;
import DataStructures.Tree.Node;
import DataStructures.Trie;

public class Project{
	
	public Project() {
		pageTree = new Tree<PageInterface>(new Book());
	}
	public Project(String title) {
		pageTree = new Tree<PageInterface>(new Book(title));
	}

	public long nextKey = 0;
	public BiMap<Long,Node<PageInterface>> pageMap = HashBiMap.create();
	public Tree<PageInterface> pageTree = new Tree<>(new Book());
    
    public static void oldDictionaryStuff() throws IOException {    	
    	Trie trie = new Trie();
    	trie.buildLanguageTrie(Trie.readWordList(System.getProperty("user.dir") + "\\src\\External\\EnglishLanguage.txt"));
    	
    	ArrayList<String> suggestions = trie.getSuggestions("tunice", 1,false);
    	for(String sug: suggestions) {
    		System.out.println(sug);
    	}  	
    	System.out.println(trie.strictlyHasWord("anime")); 
    }
    
    public static void main(String[] args) throws IOException{
    	oldDictionaryStuff();
    }
}
