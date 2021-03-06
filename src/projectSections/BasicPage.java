package projectSections;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.input.InputEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.ContentsPage;
import main.Main;
import main.TopMenu;

import com.google.common.base.CharMatcher;
import com.google.common.io.Resources;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.codec.Charsets;
import org.apache.commons.lang3.StringUtils;

public class BasicPage implements Page {
	public String title ="";
    String JSReplaceSel = "",JSReplaceSelWithHTML = "",JSGetSel ="", JSAddColumn="";
    public HTMLEditor htmlEditor;
	TextArea directHtmlEditor = new TextArea();
    String imageFilters ="";
    
	@Override
	public byte[] encode() {
		try {
		byte[] titleData = title.getBytes("UTF-16");
		byte[] data = Bytes.concat(Ints.toByteArray(titleData.length),titleData);
		data = Bytes.concat(data,htmlEditor.getHtmlText().getBytes("UTF-16"));
		data = Bytes.concat(Ints.toByteArray(Page.pageTypes.BasicPage.toInt()),data);
		return data;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void decode(byte[] data, int offset, int length) {
		try {
			int titleLen = ByteBuffer.wrap(data, offset+4, 4).getInt();
			title = new String(data,offset+8,titleLen,"UTF-16");
			String htmlText = new String(data, offset+8 + titleLen,length-(8 + titleLen),"UTF-16");
			htmlText = htmlText.replaceAll("<img src=\"file://"+".*?"+"/data/IMG","<img src=\"file://"+ Main.getJarDir() +"/data/IMG");
			htmlEditor.setHtmlText(htmlText);
		}catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	@Override public Pane BuildPane() {
		Main.currentProject.readyAllImages();
		BorderPane bp = new BorderPane(htmlEditor);
		if(Main.settings.directHtmlEditing == true) {
			bp.setBottom(directHtmlEditor);
			directHtmlEditor.setVisible(false);
		}
		return bp;
	}	

	public void initHTMLEditor() {
	    htmlEditor = new HTMLEditor();
		initHtmlText();
		
		directHtmlEditor.textProperty().addListener((obs,oldv,newv)->{
			String htmlText = htmlEditor.getHtmlText();
			if (htmlText.contains("<body contenteditable=\"true\">") && htmlText.contains("</body>")) {
				String first = htmlText.substring(0, htmlText.indexOf("<body contenteditable=\"true\">") + "<body contenteditable=\"true\">".length());
				String last = htmlText.substring(htmlText.indexOf("</body>"));
				htmlEditor.setHtmlText(first + newv + last);
			}
		});
		
		htmlEditor.addEventFilter(InputEvent.ANY, new EventHandler<InputEvent>() {
			@Override public void handle(InputEvent arg0) {
				initHtmlText();
				if (directHtmlEditor.isVisible()) {
		    		String htmlText = htmlEditor.getHtmlText();
					if (htmlText.contains("<body contenteditable=\"true\">") && htmlText.contains("</body>")) {
			    		htmlText = htmlText.substring(htmlText.indexOf("<body contenteditable=\"true\">")+"<body contenteditable=\"true\">".length(),htmlText.indexOf("</body>"));
			    		directHtmlEditor.setText(htmlText);
					}
				}
			}
		});
		htmlEditor.addEventFilter(MouseEvent.MOUSE_RELEASED, (event)->{
				if (event.getClickCount()==2) {
		            WebView webView = (WebView)htmlEditor.lookup("WebView"); 
					String refId;
					if ((boolean)webView.getEngine().executeScript("document.getElementById('pageFront') != null"))
						refId = (String)webView.getEngine().executeScript("document.getElementById('pageFront').innerHTML;");
					else refId = "-1";
					if (!refId.equals("-1") && !refId.equals("\"-1\"") && !refId.equals("undefined")) {
						if(Main.currentProject.pageMap.containsKey(refId))
							if(ContentsPage.hasAncestor(Main.currentProject.pageMap.get(refId),Main.contentsPage.tree.getRoot())) {
								if(htmlEditor.getScene() != Main.mainStage.getScene())
									Main.pageViewer.detachPage(Main.currentProject.pageMap.get(refId));
								else
									Main.pageViewer.addTab(Main.currentProject.pageMap.get(refId));
								webView.getEngine().executeScript("document.getElementById('pageFront').innerHTML = -1;");
							}
							else {
								delinkSelection();
								Main.currentProject.pageMap.remove(refId);
							}
						else {
							delinkSelection();
						}
					}
				}
			});
		initTopToolBar();
		initBottomToolBar();
	}
	
	public void initHtmlText() {
        WebView webView = (WebView)htmlEditor.lookup("WebView"); ;
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
		if ((boolean)webView.getEngine().executeScript("document.getElementById('pageStyle') == null")) {	
			String backgroundSetting = "/*Background*//*/Background*/";	
			if (Main.styleFile.equals(TopMenu.styles.ModenaDark.toString())) {
				backgroundSetting = "/*Background*/body {background-color: rgb(20, 20, 20); color: white;} table, th, td {outline: 0.5px solid white;}/*/Background*/";
			}	
			else if (Main.styleFile.equals(TopMenu.styles.Vincent.toString())) {
				backgroundSetting = "/*Background*/body {background-color: rgb(0, 0, 0); color: white;} table, th, td {outline: 0.5px solid white;}/*/Background*/";
			}	
			webView.getEngine().executeScript("var style = document.createElement('style');"
					+ "style.id = 'pageStyle';"
					+ "style.innerHTML = 'button { background:none!important; color:inherit!important; border:none!important; padding:0!important; font: inherit; cursor: pointer; -webkit-text-decoration:underline dotted;}"
					+ ".spelling {-webkit-text-decoration:underline red wavy;}"
					+ "a { color: inherit; -webkit-text-decoration:underline dotted;}" //for exporting
					+ "" + backgroundSetting
					+ "';"
					+ "document.head.appendChild(style);");
		}
	}
	
	public void initBottomToolBar() {
	    ToolBar bar = (ToolBar)htmlEditor.lookup(".bottom-toolbar");
	    
	    MenuItem extendRow = new MenuItem("Extend Row ⍗");	    
	    Button subscript = new Button("ₛ");	  
	    Button regularscript = new Button("s");	  
	    Button superscript = new Button("ˢ");	
	    Button editHtml = new Button("🔧");
	    
	    editHtml.setDisable(!Main.settings.directHtmlEditing);
	    editHtml.setOnAction((event)->{
	    	if(directHtmlEditor.isVisible()) {
				directHtmlEditor.setVisible(false);
	    	}
	    	else {
	    		directHtmlEditor.setVisible(true);
	    		String htmlText = htmlEditor.getHtmlText();
				if (htmlText.contains("<body contenteditable=\"true\">") && htmlText.contains("</body>")) {
		    		htmlText = htmlText.substring(htmlText.indexOf("<body contenteditable=\"true\">")+"<body contenteditable=\"true\">".length(),htmlText.indexOf("</body>"));
		    		directHtmlEditor.setText(htmlText);
				}
	    	}
	    });
		htmlEditor.addEventFilter(InputEvent.ANY, (event)-> {
			editHtml.setDisable(!Main.settings.directHtmlEditing);
			if(Main.settings.directHtmlEditing == false) {
				directHtmlEditor.setVisible(false);
			}
		});
	    
	    extendRow.setOnAction((event)->{
	        WebEngine webEngine = ((WebView)htmlEditor.lookup("WebView")).getEngine();
	        if((boolean)webEngine.executeScript("window.getSelection().rangeCount > 0")) {
		    	String selection =  (String)webEngine.executeScript("window.getSelection().toString();");
		    	webEngine.executeScript(String.format(JSReplaceSelWithHTML,String.format("<!--startSel-->%s<!--endSel-->",selection)));
				String htmlText = htmlEditor.getHtmlText();

				int endIndex = htmlText.indexOf("<!--endSel-->");
				if(htmlText.indexOf("</table>",endIndex) > htmlText.indexOf("</tr>",endIndex) && htmlText.indexOf("</tr>",endIndex) !=-1) { //if sel's both within a table and before the current row ending
					int startIndex = htmlText.substring(0,endIndex).lastIndexOf("<tr");
					String rowCopy = htmlText.substring(startIndex,htmlText.indexOf("</tr>",endIndex)+"</tr>".length());					
					int appendIndex = htmlText.indexOf("</tr>",endIndex) + "</tr>".length();
					htmlText = htmlText.substring(0,appendIndex) + rowCopy + htmlText.substring(appendIndex);
				}
				
				htmlText = htmlText.replace("<span><!--startSel-->","");
				htmlText = htmlText.replace("<!--endSel--></span>","");
				htmlEditor.setHtmlText(htmlText);
	        }
	    });
	    
	    subscript.setOnAction((event)->{
	        WebEngine webEngine = ((WebView)htmlEditor.lookup("WebView")).getEngine();
	        if((boolean)webEngine.executeScript("window.getSelection().rangeCount > 0")) {
		    	String selection =  (String)webEngine.executeScript("window.getSelection().toString();");
		    	webEngine.executeScript(String.format(JSReplaceSelWithHTML,String.format("<!--startSel-->%s<!--endSel-->",selection)));
				String htmlText = htmlEditor.getHtmlText();
		    	String htmlSelection = htmlText.substring(htmlText.indexOf("<!--startSel-->"),htmlText.indexOf("<!--endSel-->"));
				htmlText = htmlText.replace("<span><!--startSel-->","<sub>");
				htmlText = htmlText.replace("<!--endSel--></span>","</sub>");
				
				htmlText = htmlText.replace("<span><!--startSel-->","");
				htmlText = htmlText.replace("<!--endSel--></span>","");
				htmlEditor.setHtmlText(htmlText);
	        }
	    });
	    
	    superscript.setOnAction((event)->{
	        WebEngine webEngine = ((WebView)htmlEditor.lookup("WebView")).getEngine();
	        if((boolean)webEngine.executeScript("window.getSelection().rangeCount > 0")) {
		    	String selection =  (String)webEngine.executeScript("window.getSelection().toString();");
		    	webEngine.executeScript(String.format(JSReplaceSelWithHTML,String.format("<!--startSel-->%s<!--endSel-->",selection)));
				String htmlText = htmlEditor.getHtmlText();
		    	String htmlSelection = htmlText.substring(htmlText.indexOf("<!--startSel-->"),htmlText.indexOf("<!--endSel-->"));
		    	if(htmlSelection.contains("<sup>") && htmlSelection.contains("</sup>")) {
		    		htmlSelection = StringUtils.replaceEach(htmlSelection, new String[]{"<sup>","</sup>","<sub>","</sub>"}, new String[]{"","","",""});
		    		htmlText = htmlText.substring(0,htmlText.indexOf("<!--startSel-->")) + htmlSelection + htmlText.substring(htmlText.indexOf("<!--endSel-->"));
		    	}
		    	else{
					htmlText = htmlText.replace("<span><!--startSel-->","<sup>");
					htmlText = htmlText.replace("<!--endSel--></span>","</sup>");
		    	}	    	
				htmlText = htmlText.replace("<span><!--startSel-->","");
				htmlText = htmlText.replace("<!--endSel--></span>","");
				htmlEditor.setHtmlText(htmlText);
	        }
	    });
	    
	    regularscript.setOnAction((event)->{
	        WebEngine webEngine = ((WebView)htmlEditor.lookup("WebView")).getEngine();
	        if((boolean)webEngine.executeScript("window.getSelection().rangeCount > 0")) {
		    	String selection =  (String)webEngine.executeScript("window.getSelection().toString();");
		    	webEngine.executeScript(String.format(JSReplaceSelWithHTML,String.format("<!--startSel-->%s<!--endSel-->",selection)));
				String htmlText = htmlEditor.getHtmlText();
		    		
		    	htmlText = htmlText.substring(0,htmlText.indexOf("<!--startSel-->")) + selection + htmlText.substring(htmlText.indexOf("<!--endSel-->")); 	
				
		    	htmlText = htmlText.replace("<span><!--startSel-->","");
				htmlText = htmlText.replace("<!--endSel--></span>","");
				htmlEditor.setHtmlText(htmlText);
	        }
	    });
	    
	    Menu tableMenu = new Menu("Table ▤");
	    tableMenu.getItems().addAll(extendRow);  
	    
	    MenuBar menuBar = new MenuBar(tableMenu);
	    bar.getItems().addAll(menuBar,subscript,regularscript,superscript,editHtml);
	}
	
	int stringSimilarity(String str1, String str2) {
	    if(str1.length() == 0 && str2.length() == 0) return 1;
	    int similarityNo = 0;
	    for(int i = 0; i < str1.length() && i < str2.length(); ++i) {
	        if(str1.charAt(i) == str2.charAt(i)) {
	            similarityNo++;
	        }
	    }
	    return (int)(100*((float)similarityNo / ((str1.length()+str1.length())/2f)));
	}

	public void initTopToolBar() {
    	WebEngine webEngine = ((WebView)htmlEditor.lookup("WebView")).getEngine(); 
		
	    ToolBar bar = (ToolBar)htmlEditor.lookup(".top-toolbar");
	    MenuItem link = new MenuItem("Link ⛓");
	    MenuItem delink = new MenuItem("Delink ⤫");
	    
	    MenuItem clearLines = new MenuItem("Clear Lines ⌫");
	    MenuItem underline = new MenuItem("Underline Mistakes 〰");
	    
	    MenuItem english = new MenuItem("English");
	    MenuItem french = new MenuItem("Français");
	    MenuItem italian = new MenuItem("Italiano");
	    MenuItem spanish = new MenuItem("Español");
	    
	    MenuItem insertImage = new MenuItem("Insert Image 🖻");
	    MenuItem editImage = new MenuItem("Edit Image 🎨");

        ColorPicker colorPicker = new ColorPicker(Color.BLACK);
        
        MenuItem suggestions = new MenuItem("Suggestions ✓");
        
		underline.setDisable(!Main.settings.spellChecking);
		clearLines.setDisable(!Main.settings.spellChecking); 
        
        suggestions.setOnAction((event)->{
        	ContextMenu contextMenu = new ContextMenu();
        	        	
            if((boolean)webEngine.executeScript("window.getSelection().rangeCount > 0")) {
	        	String selection = ((String)webEngine.executeScript("document.getSelection().toString()")).trim().split("\\s+")[0];

	        	if(!selection.equals("")) {
		        	if(Main.languageTrie.hasWord(selection)) {
		        		contextMenu.getItems().add(new MenuItem(selection));
		        	}
		        	else {
			        	HashSet<String> hs = new HashSet<>();
			        	hs.addAll(Main.languageTrie.getSuggestions(selection));
			        	hs.addAll(Main.languageTrie.getSuggestions(selection.toLowerCase()));
			        	
			        	ArrayList<String> list = new ArrayList<String>(hs);
			        	list.sort((w1,w2)->(stringSimilarity(selection,w2)-stringSimilarity(selection,w1)));
			        	
			        	for(String word: list) {
			        		MenuItem item = new MenuItem(word);
			        		contextMenu.getItems().add(item);
			        		item.setOnAction((actionEvent)->{
			        			webEngine.executeScript(String.format(JSReplaceSel,word));
			        		});
			        	}
			        	if(contextMenu.getItems().isEmpty()){
			        		contextMenu.getItems().add(new MenuItem("(no suggestions found)"));
			        	}
		        	}
	        	}
	        	else contextMenu.getItems().add(new MenuItem("(highlight the required text)"));
            }
            else contextMenu.getItems().add(new MenuItem("(highlight the required text)"));
            contextMenu.show(htmlEditor.getScene().getWindow());
        });
 
        colorPicker.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent event) {
				String htmlText = htmlEditor.getHtmlText();
				String textCol = colorPicker.getValue().getBrightness()<0.6?"color: white; ":"";
				String tableCol = colorPicker.getValue().getBrightness()<0.6?"table, th, td {outline: 0.5px solid white;}":"";

				htmlText = htmlText.substring(0, htmlText.indexOf("/*Background*/")+ "/*Background*/".length()) + String.format("body {background-color: rgba(%s,%s,%s,%s);", (int)(colorPicker.getValue().getRed()*255f),(int)(colorPicker.getValue().getGreen()*255f),(int)(colorPicker.getValue().getBlue()*255f),colorPicker.getValue().getOpacity()) + textCol + "}" + tableCol + htmlText.substring(htmlText.indexOf("/*/Background*/"));						
				htmlEditor.setHtmlText(htmlText);
			}});
	    
	    insertImage.setOnAction((event) -> {
				insertImage();
			});   
	    
	    editImage.setOnAction((event) -> {
            	WebView webView = (WebView)htmlEditor.lookup("WebView"); 
            	String selection = (String)webView.getEngine().executeScript(JSGetSel);
            	String imageID = StringUtils.substringBetween(selection, "IMG",".png");
            	if(imageID!=null && imageID!="") {
                	webView.getEngine().executeScript("document.getSelection().removeAllRanges()");
            		styleImage(imageID);
            	}
			}); 
	    
	    english.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent arg0) {
				try { Main.languageTrie.buildLanguageTrie(Main.languageTrie.readWordList("EnglishLanguage.txt"));
				}catch (IOException e) {}
			}});
	    french.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent arg0) {
				try { Main.languageTrie.buildLanguageTrie(Main.languageTrie.readWordList("FrenchLanguage.txt"));
				}catch (IOException e) {}
			}});
	    italian.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent arg0) {
				try { Main.languageTrie.buildLanguageTrie(Main.languageTrie.readWordList("ItalianLanguage.txt"));
				}catch (IOException e) {}
			}});
	    spanish.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent arg0) {
				try { Main.languageTrie.buildLanguageTrie(Main.languageTrie.readWordList("SpanishLanguage.txt"));
				}catch (IOException e) {}
			}});
	    
	    link.setOnAction(new EventHandler<ActionEvent>() {
	          @Override public void handle(ActionEvent event) {	            
	      		TreeView<Page> miniTree = new TreeView<>();
	    		miniTree.setRoot(Main.contentsPage.tree.getRoot());
	    		miniTree.setShowRoot(true);
	    		Stage pageSelStage = Main.createSubStage(new Scene(new StackPane(miniTree), 300, 400), "Select Page", Modality.APPLICATION_MODAL);
	    		pageSelStage.show();
	    		
	    		miniTree.getSelectionModel().selectedItemProperty().addListener((obsv,oldV,item) ->{
	    				if (item != null) 
	    					if (item != miniTree.getRoot()) {
	    						if(item.getValue()!=BasicPage.this) {
		    						if (!Main.currentProject.pageMap.containsValue(item))
		    							Main.currentProject.pageMap.put(UUID.randomUUID().toString(),item);
		    			   			String selection =  (String)webEngine.executeScript("window.getSelection().toString();");
		    			   			webEngine.executeScript(String.format(JSReplaceSelWithHTML,String.format("<button onclick='pushPage(this.name)' name='%s' type='button' id='pageLink'>%s</button>",Main.currentProject.pageMap.inverse().get(item),selection.replace("\n","<br>"))));
		    						pageSelStage.close();	
	    						}
	    					}		
	    			}); 
	          }});
	    delink.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent event) {
				delinkSelection();
			}});
	    
	    underline.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent event) {
				clearSpellingLines();
				
		        WebEngine webEngine = ((WebView)htmlEditor.lookup("WebView")).getEngine(); 
		        
		        String htmlText = htmlEditor.getHtmlText();				
				String plainText = (String)webEngine.executeScript("document.body.innerText;");
				LinkedHashSet<String> linkedHashSet=new LinkedHashSet<String>();
				
				List<String> allWords = new LinkedList<>(Arrays.asList(CharMatcher.whitespace().trimFrom(plainText).replace("-"," ").replaceAll("[^a-zA-Z ]", " ").split("[\\s\\xA0]+")));
				while(allWords.removeIf(u -> u.length() ==0 || u.contains("&") ));	
				linkedHashSet.addAll(allWords);
				for(String string: linkedHashSet) {
					if(!Main.languageTrie.hasWord(string)) {
						int start = 0;
						
						while((start = indexOfInnerText(string,start))!=-1) {
							int end = start + string.length();	
							String newStr = "<span class='spelling'>" + string + "</span>";
							htmlText = htmlText.substring(0, start) + newStr + htmlText.substring(end);
							start += newStr.length();
						}
					}
				}
				htmlEditor.setHtmlText(htmlText);
			}});
	    clearLines.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent event) {
				clearSpellingLines();
			}});

	    Menu linkMenu = new Menu("Link...");
	    linkMenu.getItems().addAll(link,delink);
	    
	    Menu spellingMenu = new Menu("Spelling...");
	    Menu language = new Menu("Change Langauge 📕...");
	    language.getItems().addAll(english,spanish,french,italian);
	    spellingMenu.getItems().addAll(clearLines,underline,suggestions,language);
	    
	    Menu imageMenu = new Menu("Image...");
	    imageMenu.getItems().addAll(insertImage,editImage);  
	    
	    MenuBar menuBar = new MenuBar(linkMenu,spellingMenu,imageMenu); 
	    
	    bar.getItems().addAll(menuBar,colorPicker);
	}
	
	public void insertImage() {
		FileChooser directoryChooser = new FileChooser();
		if(Main.settings.currentImageFileDir!=null && Main.settings.currentImageFileDir.exists()) directoryChooser.setInitialDirectory(Main.settings.currentImageFileDir);
        directoryChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("image files", "*.jpg","*.jpeg", "*.png"));
        File directory = directoryChooser.showOpenDialog(Main.mainStage);
        
        if(directory == null){
            System.out.println("No file selected");
        }
        else {
        	Image image = new Image(directory.toURI().toString());
		    WebEngine webEngine = ((WebView)htmlEditor.lookup("WebView")).getEngine(); 		    	    		
		    String imagePath = Main.currentProject.addImage(image);
		    String imageID = StringUtils.substringBetween(imagePath, "IMG",".png");
			webEngine.executeScript(""
					+ "var image = document.createElement('img');"
					+ "image.src=\"file://" + imagePath +"\";"
					+ "image.id = '"+imageID+"';"
					+ "if (window.getSelection().rangeCount > 0){"
					+ "	range = window.getSelection().getRangeAt(0);"
					+ "	range.insertNode(image);}"
					+ "else document.body.appendChild(image);"
					);
	    	styleImage(imageID);
			Main.settings.currentImageFileDir = directory.getParentFile();
        }
	}
	
	public void styleImage(String imageId) {
        WebEngine webEngine = ((WebView)htmlEditor.lookup("WebView")).getEngine(); 		    	    		
		imageFilters = "";
		
		Slider sizeSlider = new Slider(0,200,100);
	    Label sizeLabel = new Label("Size %100");
	    sizeSlider.setShowTickLabels(true);	sizeSlider.setShowTickMarks(true);    
	    
	    ComboBox<String> alignBox = new ComboBox<>();
	    alignBox.getItems().addAll("left","right","middle","top","bottom");
	    alignBox.setPromptText("Text Alignment 🖺");
	    
	    ComboBox<String> borderTypeBox = new ComboBox<>();
	    borderTypeBox.getItems().addAll("none","solid","dotted","dashed","double","groove","ridge","inset","outset","hidden");
	    borderTypeBox.setPromptText("Border Type ▯");
	    
	    ComboBox<String> borderWidthBox = new ComboBox<>();
	    borderWidthBox.getItems().addAll("medium","thin","thick");
	    borderWidthBox.setPromptText("Border Width ⇔");
	    
	    TextField borderColourField = new TextField();
	    borderColourField.setPromptText("Border Colour 🖌");
	    
	    Slider borderSlider = new Slider(0,100,0);
	    borderSlider.setShowTickLabels(true);	borderSlider.setShowTickMarks(true);
	    Label borderRadiusLabel = new Label("Border Radius (%): ");

	    CheckBox sizeBox = new CheckBox(); sizeBox.setSelected(true);
    	sizeBox.selectedProperty().addListener((obsv,oldV,newV)->{ 
    		sizeSlider.setDisable(!newV); });
    	CheckBox borderBox = new CheckBox(); borderBox.setSelected(true);
     	borderBox.selectedProperty().addListener((obsv,oldV,newV)->{ 
     		borderSlider.setDisable(!newV); });

		sizeSlider.valueProperty().addListener((observable,oldVal,newVal) ->{
			sizeLabel.setText("Size %"+newVal.intValue());
			webEngine.executeScript("var image = document.getElementById('"+imageId+"');"
					+ "image.style.width = '" + (((int)sizeSlider.getValue()==0)?100:(int)sizeSlider.getValue()) + "%';");
			});
	    
	    alignBox.valueProperty().addListener((observable,oldVal,newVal) ->{
	    	webEngine.executeScript("var image = document.getElementById('"+imageId+"');"
						+ "image.align = '" + newVal +"';");
				});
	    
	    borderTypeBox.valueProperty().addListener((observable,oldVal,newVal) ->{
	    	webEngine.executeScript("var image = document.getElementById('"+imageId+"');"
						+ "image.style.borderStyle = '" + newVal +"';");
				});
	    
	    borderWidthBox.valueProperty().addListener((observable,oldVal,newVal) ->{
	    	webEngine.executeScript("var image = document.getElementById('"+imageId+"');"
						+ "image.style.borderWidth = '" + newVal +"';");
				});
	    
	    borderColourField.textProperty().addListener((observable,oldVal,newVal) ->{
	    	webEngine.executeScript("var image = document.getElementById('"+imageId+"');"
					+ "image.style.borderColor = '" + newVal +"';");
	    	});
	    
	    borderSlider.valueProperty().addListener((observable,oldVal,newVal) ->{
	    	webEngine.executeScript("var image = document.getElementById('"+imageId+"');"
					+ "image.style.borderRadius = '"+newVal+"%';");
			});
		
	    GridPane gridPane = new GridPane();
	    gridPane.add(sizeBox, 0, 0);	 	gridPane.add(sizeLabel, 1, 0); 					gridPane.add(sizeSlider, 2, 0);		gridPane.add(alignBox, 3, 0);
	    gridPane.add(borderBox, 0, 1);		gridPane.add(borderRadiusLabel, 1, 1); 		 	gridPane.add(borderSlider, 2, 1);	gridPane.add(borderTypeBox, 3, 1);	    
	    gridPane.add(borderWidthBox, 3, 2);
	    gridPane.add(borderColourField, 3, 3);
	    
	    addFilterSlider(gridPane,imageId,"Blur (px):","blur","px",25,0,2);
	    addFilterSlider(gridPane,imageId,"Brightness (%):","brightness","%",500,100,3);
	    addFilterSlider(gridPane,imageId,"Contrast (%):","contrast","%",500,100,4);
	    addFilterSlider(gridPane,imageId,"Saturation (%):","saturate","%",500,100,5);
	    addFilterSlider(gridPane,imageId,"Invert (%):","invert","%",100,0,6);
	    addFilterSlider(gridPane,imageId,"Greyscale (%):","grayscale","%",100,0,7);
	    addFilterSlider(gridPane,imageId,"Sepia (%):","sepia","%",100,0,8);
	    addFilterSlider(gridPane,imageId,"Hue (degrees):","hue-rotate","deg",360,0,9);
	    addFilterSlider(gridPane,imageId,"Opacity (%):","opacity","%",100,100,10);
	    
	    Main.createSubStage(new Scene(gridPane),"Configure Image", Modality.APPLICATION_MODAL).show();
	}
	
	public void addFilterSlider(GridPane gridPane, String imageID, String labelText, String filterAttr, String unit, int sliderLen, int currentVal, int row) {
	    Slider slider = new Slider(0,sliderLen,currentVal);
	    slider.setShowTickLabels(true);	slider.setShowTickMarks(true);
	    slider.setDisable(true);
	    
	    CheckBox tickBox = new CheckBox(labelText); 
	    tickBox.selectedProperty().addListener((obsv,oldV,newV)->{ 
	    	slider.setDisable(!newV); 
	    	if(newV == false){
	    		imageFilters = imageFilters.replaceAll(filterAttr+".*?\\)", ""); 
	    		updateImageFilters(imageID);
	    		} 
	    	});
	    
	    slider.valueProperty().addListener((observable,oldVal,newVal) ->{
				if(!imageFilters.contains(filterAttr)) {
					imageFilters += " " + filterAttr +"(0"+unit+")";
				}
	    		imageFilters = imageFilters.replaceAll("("+filterAttr+"\\()(.*?)("+unit+"\\))", filterAttr+"\\("+newVal.intValue()+unit+"\\)");
				updateImageFilters(imageID);
	    	});
	    gridPane.add(tickBox, 0, row);
	    gridPane.add(slider, 1, row);
	}	
	
	public void updateImageFilters(String imageID) {
        WebEngine webEngine= ((WebView)htmlEditor.lookup("WebView")).getEngine(); 
        webEngine.executeScript("var image = document.getElementById('"+imageID+"');"
				+ "image.style.filter = 'none';"
				+ "image.style.filter = \""+imageFilters+"\";");
	}
	
	public int indexOfInnerText(String text,int startIndex){
		String htmlText = htmlEditor.getHtmlText();
		StringBuilder currentWord = new StringBuilder();
		StringBuilder tagWord = new StringBuilder();
		boolean withinTag = false;	
		String[][] skipTag = new String[][] 
				{{"<script","<style","<span","<div","<img","<meta"},
					{"</script>","</style>",">",">",">",">"}};
		for(int i=startIndex;i<htmlText.length();i++) {
			
			for(int c=0;c<skipTag[0].length;c++) {
				String open = skipTag[0][c], close = skipTag[1][c];
				if(tagWord.toString().contains(open)) {
					i = htmlText.indexOf(close,i)+close.length()-1;						

					break;
				}
			}		
			if(htmlText.charAt(i)=='<') {
				withinTag = true;
				currentWord = new StringBuilder();
			}
			else if(htmlText.charAt(i)=='>') {
				withinTag = false;
				tagWord = new StringBuilder();
			}		
			if (withinTag)
				tagWord.append(htmlText.charAt(i));			
			else
				currentWord.append(htmlText.charAt(i));
									
			if(currentWord.toString().contains(text)) {
				String startString= htmlText.substring(0, i);
				if(startString.lastIndexOf(">")>startString.lastIndexOf("<"))
					if(htmlText.indexOf("<",i)<htmlText.indexOf(">",i) || (htmlText.indexOf("<",i) !=-1 && htmlText.indexOf(">",i) ==-1))
						return i-text.length()+1;
			}
		}
		return -1;
	}
	
	public void clearSpellingLines() {
        String htmlText = htmlEditor.getHtmlText().replaceAll("(<span class=\"spelling\">)(.*?)(<\\/span>)","$2");
        htmlEditor.setHtmlText(htmlText);
	}
	
	public void delinkSelection() {
        WebEngine webEngine = ((WebView)htmlEditor.lookup("WebView")).getEngine();
        if((boolean)webEngine.executeScript("window.getSelection().rangeCount > 0")) {
			String selection =  (String)webEngine.executeScript("window.getSelection().toString();");
			webEngine.executeScript(String.format(JSReplaceSelWithHTML,String.format("<!--startSel-->%s<!--endSel-->",selection)));
			String htmlText = htmlEditor.getHtmlText();
			int startIndex, endIndex;
			{ /*startIndex*/
				StringBuilder startString = new StringBuilder(htmlText.substring(0,htmlText.indexOf("<span><!--startSel-->")+"<span><!--startSel-->".length()));
				if(startString.lastIndexOf("type=\"button\" id=\"pageLink\">") > startString.lastIndexOf("</button>")) //if start tag is closer
					startIndex = startString.lastIndexOf("<button");
				else 
					startIndex = htmlText.indexOf("<span><!--startSel-->");
			}
			{ /*endIndex*/
				int from = htmlText.indexOf("<!--endSel--></span>");
				if(htmlText.indexOf("</button>",from) < htmlText.indexOf("<button",from) || (htmlText.indexOf("</button>",from)!=-1 && htmlText.indexOf("<button",from)==-1)) //if the end tag is closer
					endIndex = htmlText.indexOf("</button>",from)+"</button>".length();
				else
					endIndex = from;
			}
			String subString = htmlText.substring(startIndex,endIndex);
			subString = subString.replaceAll("<button"+".*"+"type=\"button\" id=\"pageLink\">", "");
			subString = subString.replace("</button>","");
							
			htmlText = htmlText.substring(0,startIndex) +subString+ htmlText.substring(endIndex);
			htmlText = htmlText.replace("<span><!--startSel-->","");
			htmlText = htmlText.replace("<!--endSel--></span>","");
			htmlEditor.setHtmlText(htmlText);		
        }
	}

	@Override
	public String getTitle() {
		return title;
	}
	
	public BasicPage() {
		this("Page");
	}
	
	public BasicPage(String title,String htmlText) {
		this(title);
		htmlEditor.setHtmlText(htmlText);
	}
	
	public BasicPage(String title) {
		this.title = title;
		try {loadexternalCommands();
		} catch (IOException e) {
			e.printStackTrace();
		}
		initHTMLEditor();
	}

	@Override
	public void setTitle(String title) {
		this.title = title;		
	}
	
	public void loadexternalCommands() throws IOException {
		JSAddColumn = readexternalFile("external/scriptAddColumn.txt");
		JSReplaceSel = readexternalFile("external/scriptReplaceSelection.txt");
		JSReplaceSelWithHTML = readexternalFile("external/scriptReplaceSelectionWithHTML.txt");
		JSGetSel = readexternalFile("external/scriptGetSelectedHTML.txt");
	}
	
	public String readexternalFile(String file) throws IOException{
		URL url = Resources.getResource(file);
		return Resources.toString(url, Charsets.UTF_8);
	}
	
	@Override
	public String toString() {
		return "📄 " +title;
	}
}