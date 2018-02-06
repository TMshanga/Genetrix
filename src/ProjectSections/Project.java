package ProjectSections;

import java.lang.instrument.Instrumentation;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import DataStructures.Tree;
import DataStructures.Tree.Node;
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
        FileReader in = new FileReader("C:\\Users\\Vio\\Desktop\\test.txt");
        BufferedReader br = new BufferedReader(in);
        String newString ="";

        ArrayList<String> list = new ArrayList<String>();
        String line;
        while ((line = br.readLine()) != null) {
           list.add(line);
        }
        in.close();
    	
    	Trie<Character> trie = new Trie<Character>('\0');
    	    	
    	trie = Trie.buildDictionaryTrie(list, '\n');
    		
    	System.out.println(trie.containsWord("anime"));
    	
    	//trie.dfsS(trie.root, new ArrayList<Tree.Node<Character>>(), new Stack<Character>());
    	
    	List<ArrayList<Character>> depthOrderedList = trie.DFS();
    	int no=-1;
    }
	
}
