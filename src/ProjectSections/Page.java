package ProjectSections;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Skin;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import Apex.Main;
import Apex.PageViewer;
import Apex.ContentsPage;
import Apex.ContentsPage.MyTreeItem;
import DataStructures.Tree;

import com.google.common.base.CharMatcher;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.sun.javafx.webkit.Accessor;
import com.sun.webkit.WebPage;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class Page implements PageInterface{
	public String title ="";
    String JSReplaceSel = "", JSReplaceSelWithHTML = "";
    String content = "";
    public boolean detached = false;
	@Override
	public byte[] encode() {
		byte[] titleData = title.getBytes();
		byte[] data = Bytes.concat(Ints.toByteArray(titleData.length),titleData);
		data = Bytes.concat(data,content.getBytes());
		return data;
	}

	@Override
	public void decode(byte[] data) {
		int titleLen = Ints.fromByteArray(Arrays.copyOfRange(data, 0, 4));
		title = new String(Arrays.copyOfRange(data,4,titleLen));
		content = new String(Arrays.copyOfRange(data,4+titleLen,data.length));
	}

	@Override public BorderPane BuildPane() {
		BorderPane borderPane = new BorderPane();

	    HTMLEditor htmlEditor = new HTMLEditor();
		initHTMLEditor(htmlEditor);
		borderPane.setCenter(htmlEditor);
    
		return borderPane;
	}

	public void initHTMLEditor(HTMLEditor htmlEditor) {
		htmlEditor.setHtmlText(content);
		
		htmlEditor.addEventFilter(InputEvent.ANY, new EventHandler<InputEvent>() {
            WebView webView = (WebView)htmlEditor.lookup("WebView"); 
			@Override public void handle(InputEvent arg0) {
				if ((boolean)webView.getEngine().executeScript("document.getElementById('pageScript') == null"))
					webView.getEngine().executeScript("var script = document.createElement('script');"
							+ "script.id = 'pageScript';"
							+ "script.innerHTML = \"function pushPage(id) {document.getElementById('pageFront').innerHTML = id;}\";"
							+ "document.head.appendChild(script);");
				if ((boolean)webView.getEngine().executeScript("document.getElementById('pageFront') == null"))
					webView.getEngine().executeScript("var div = document.createElement('div');"
							+ "div.id = 'pageFront';"
							+ "div.style = 'display:none;'; div.hidden = true;"
							+ "div.innerHTML = -1;"
							+ "document.head.appendChild(div);");
				if ((boolean)webView.getEngine().executeScript("document.getElementById('pageStyle') == null"))
					webView.getEngine().executeScript("var style = document.createElement('style');"
							+ "style.id = 'pageStyle';"
							+ "style.innerHTML = 'button { background:none!important; color:inherit!important; border:none!important; padding:0!important; font: inherit; cursor: pointer; -webkit-text-decoration:underline dotted;}"
							+ ".spelling {-webkit-text-decoration:underline red wavy;}"
							+ "';"
							+ "document.head.appendChild(style);");
				content = htmlEditor.getHtmlText();
			}
		});
		htmlEditor.addEventFilter(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
			@Override public void handle(MouseEvent event) {
				if (event.getClickCount()==2) {
		            WebView webView = (WebView)htmlEditor.lookup("WebView"); 
					String refIndex;
					if ((boolean)webView.getEngine().executeScript("document.getElementById('pageFront') != null"))
						refIndex = (String)webView.getEngine().executeScript("document.getElementById('pageFront').innerHTML;");
					else refIndex = "-1";
					try {
						if (refIndex!= "-1" && refIndex!="undefined") {
							if(Main.currentProject.pageMap.containsKey(Long.parseLong(refIndex)))
						//	if(Main.currentProject.pageMap.get(Long.parseLong(refIndex))!=null)
								if(Main.currentProject.pageMap.get(Long.parseLong(refIndex)).getAncestor()!=null) {
									System.out.print(refIndex);
									if(Main.pageViewer.subStageMap.containsKey(Main.currentProject.pageMap.get(Long.parseLong(refIndex))))
										Main.pageViewer.projectPage(Main.currentProject.pageMap.get(Long.parseLong(refIndex)));
									else
										Main.pageViewer.addTab(Main.currentProject.pageMap.get(Long.parseLong(refIndex)));
									webView.getEngine().executeScript("document.getElementById('pageFront').innerHTML = -1;");
								}
								else {
									delinkSelection(htmlEditor);
									Main.currentProject.pageMap.remove(Long.parseLong(refIndex));
								}
						}
					}
					catch(NumberFormatException e) {}
				}
			}});
		initHTMLToolBar(htmlEditor);
	}
	
	public void initHTMLToolBar(HTMLEditor htmlEditor) {
	    ToolBar bar = (ToolBar)htmlEditor.lookup(".top-toolbar");
	    MenuItem link = new MenuItem("Link ⛓");
	    MenuItem delink = new MenuItem("Delink ⤫");
	    
	    MenuItem clearLines = new MenuItem("Clear Lines ⌫");
	    MenuItem underline = new MenuItem("Underline Mistakes 〰");
	    
	    link.setOnAction(new EventHandler<ActionEvent>() {
	          @Override public void handle(ActionEvent event) {
	            WebView webView = (WebView)htmlEditor.lookup("WebView"); 
	            
	      		TreeView<String> miniTree = new TreeView<>();
	    		depthFirstAssembily(miniTree,Main.currentProject);
	    		miniTree.setShowRoot(false);
	    		Stage pageSelStage = new Stage();
	    		pageSelStage.setScene(new Scene(new StackPane(miniTree), 300, 400));
	    		pageSelStage.setTitle("Select Page");
	    		pageSelStage.initModality(Modality.APPLICATION_MODAL);
	    		pageSelStage.requestFocus();
	    		pageSelStage.show();
	    		
	    		miniTree.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<String>>(){
	    			@Override public void changed(ObservableValue<? extends TreeItem<String>> observable, TreeItem<String> oldValue, TreeItem<String> newValue) {
	    				MyTreeItem<String> item = (MyTreeItem<String>)miniTree.getSelectionModel().getSelectedItem();
	    				if (item != null) 
	    					if (item.internalAddress.size() > 1) {
	    						Tree.Node<PageInterface> pageNode = Main.currentProject.pageTree.getNode(item.internalAddress);
	    						if(pageNode.data!=Page.this) {
		    						if (!Main.currentProject.pageMap.containsValue(pageNode))
		    							Main.currentProject.pageMap.forcePut(new Long(++Main.currentProject.nextKey),pageNode);
		    			   			String selection =  (String)webView.getEngine().executeScript("window.getSelection().toString();");
		    						webView.getEngine().executeScript(String.format(JSReplaceSelWithHTML,String.format("<button type='button' id = '%d' onclick='pushPage(this.id);'><!--opEnding-->%s</button>",Main.currentProject.pageMap.inverse().get(pageNode),selection)));
		    			            pageSelStage.close();	
	    						}
	    					}		
	    			}}); 
	          }});
	    delink.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent event) {
				delinkSelection(htmlEditor);
			}});
	    
	    underline.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent event) {
				clearSpellingLines(htmlEditor);
				
		        WebView webView = (WebView)htmlEditor.lookup("WebView"); 
		        
		        String htmlText = htmlEditor.getHtmlText();;				
				String plainText = (String)webView.getEngine().executeScript("document.body.innerText;");
				
				LinkedHashSet<String> linkedHashSet=new LinkedHashSet<String>();

				linkedHashSet.addAll(Arrays.asList(CharMatcher.WHITESPACE.trimFrom(plainText).replace("-"," ").split("[\\s\\xA0]+")));
				
				for(String string: linkedHashSet) {
					if(!Main.englishTrie.hasWord(string)) {
						int start = 0;
						while((start = indexOfExTags(htmlText,string,start))!=-1) {
							int end = start + string.length();	
							String newStr = "<span class='spelling'>" + string + "</span><!--spellEnd-->";
							htmlText = htmlText.substring(0, start) + newStr + htmlText.substring(end);
							start += newStr.length()-1;
						}
					}
				}
				htmlEditor.setHtmlText(htmlText);
			}});
	    clearLines.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent event) {
				clearSpellingLines(htmlEditor);
			}});

	    Menu linkMenu = new Menu("Link...");
	    linkMenu.getItems().addAll(link,delink);
	    
	    Menu spellingMenu = new Menu("Spelling...");
	    spellingMenu.getItems().addAll(clearLines,underline);
	    
	    MenuBar menuBar = new MenuBar(linkMenu,spellingMenu);
	    bar.getItems().add(menuBar);
	}
	
	public int indexOfExTags(String htmlText,String text,int startIndex){
		StringBuilder currentWord = new StringBuilder();;
		StringBuilder tagWord = new StringBuilder();;
		boolean withinTag = false;	
		boolean betweenIllegalTag = false;
		for(int i=startIndex;i<htmlText.length();i++) {
			if(htmlText.charAt(i)=='<') {
				withinTag = true;
				currentWord = new StringBuilder();
			}
			else if(htmlText.charAt(i)=='>')
				withinTag = false;
			
			else if (withinTag)
				tagWord.append(htmlText.charAt(i));
			
			else if(!withinTag && !betweenIllegalTag) 
				currentWord.append(htmlText.charAt(i));
						
			if (tagWord.toString().contains("</script") || tagWord.toString().contains("</style"))
				betweenIllegalTag = false;
			else if(tagWord.toString().contains("<script")|| tagWord.toString().contains("<style"))
				betweenIllegalTag = true;

			if(currentWord.toString().contains(text))
				return i-text.length()+1;
		}
		return -1;
	}
	
	public void clearSpellingLines(HTMLEditor htmlEditor) {
        String htmlText = htmlEditor.getHtmlText();;
                
        htmlText = htmlText.replace("<span class=\"spelling\">", "");
        htmlText = htmlText.replace("</span><!--spellEnd-->", "");
       
        htmlEditor.setHtmlText(htmlText);
	}
	
	public void delinkSelection(HTMLEditor htmlEditor) {
        WebView webView = (WebView)htmlEditor.lookup("WebView"); 
		String selection =  (String)webView.getEngine().executeScript("window.getSelection().toString();");
		webView.getEngine().executeScript(String.format(JSReplaceSelWithHTML,String.format("<!--startSel-->%s<!--endSel-->",selection)));
		String htmlText = htmlEditor.getHtmlText();
		int startIndex, endIndex;
		{ /*startIndex*/
			StringBuilder startString = new StringBuilder(htmlText.substring(0,htmlText.indexOf("<span><!--startSel-->")));
			if(startString.lastIndexOf("<!--opEnding-->") > startString.lastIndexOf("</button>")) //if start tag is closer
				startIndex = startString.lastIndexOf("<button type=");
			else 
				startIndex = htmlText.indexOf("<span><!--startSel-->");
		}
		{ /*endIndex*/
			int from = htmlText.indexOf("<!--endSel--></span>");
			if(htmlText.indexOf("</button>",from) < htmlText.indexOf("<button type=",from) || (htmlText.indexOf("</button>",from)!=-1 && htmlText.indexOf("<button type=",from)==-1)) //if the end tag is closer
				endIndex = htmlText.indexOf("</button>",from)+"</button>".length();
			else
				endIndex = htmlText.indexOf("<!--endSel--></span>");
		}
		String subString = htmlText.substring(startIndex,endIndex);
		subString = subString.replaceAll("<button type="+".*"+"<!--opEnding-->", "");
		subString = subString.replace("</button>","");
						
		htmlText = htmlText.substring(0,startIndex) +subString+ htmlText.substring(endIndex);
		htmlText = htmlText.replace("<span><!--startSel-->","");
		htmlText = htmlText.replace("<!--endSel--></span>","");
		htmlEditor.setHtmlText(htmlText);		
		
	}
	
	public void depthFirstAssembily(TreeView<String> tree, Project project) {
		ContentsPage.MyTreeItem<String>  root = new ContentsPage.MyTreeItem<String>();
		tree.setRoot(root);
		depthFirstAssembily(project.pageTree.root, new ArrayList<Tree.Node<PageInterface>>(), root, 0);
	}

	private void depthFirstAssembily(Tree.Node<PageInterface> pageNode, ArrayList<Tree.Node<PageInterface>> visited, MyTreeItem<String> ParentGUINode, int childArrayIndex) {
		MyTreeItem<String> GUINode = ParentGUINode.branch(pageNode.data.getIcon() + " " + pageNode.data.getTitle());
		GUINode.internalAddress.addAll(ParentGUINode.internalAddress);
		GUINode.internalAddress.add(childArrayIndex);
		if (visited == null)
			visited = new ArrayList<Tree.Node<PageInterface>>();
		visited.add(pageNode);
		for (int i = 0; i < pageNode.getChildren().size(); i++) {
			Tree.Node<PageInterface> childPage = pageNode.getChildren().get(i);
			if (childPage != null && !visited.contains(childPage)) {
				depthFirstAssembily(childPage, visited, GUINode, i);
			}
		}
	}
	
	@Override
	public String getIcon() {
		// TODO Auto-generated method stub
		return "📝";
	}

	@Override
	public String getTitle() {
		return title;
	}
	
	public Page() {this.title = "Book";try {loadExternalCommands();} catch (IOException e) {e.printStackTrace();}}
	public Page(String title) {this.title = title;try {loadExternalCommands();} catch (IOException e) {e.printStackTrace();}}

	@Override
	public void setTitle(String title) {
		this.title = title;		
	}
	
	public void loadExternalCommands() throws IOException {
		JSReplaceSel = readExternalFile("scriptReplaceSelection.txt");
		JSReplaceSelWithHTML = readExternalFile("scriptReplaceSelectionWithHTML.txt");
	}
	
	public static String readExternalFile(String file) throws IOException{
		  BufferedReader bufferedReader = new BufferedReader(new FileReader(System.getProperty("user.dir") + "\\src\\External\\"+file));		  
		  StringBuffer stringBuffer = new StringBuffer();
		  String line = "";	
		  while((line =bufferedReader.readLine())!=null)	 
			  stringBuffer.append(line).append("\n");
		  bufferedReader.close();
		  return stringBuffer.toString();	
	}

	@Override
	public boolean isDetached() {
		return detached;
	}

	@Override
	public void isDetached(boolean value) {
		detached = value;	
	}
}
