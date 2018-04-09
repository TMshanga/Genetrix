package projectSections;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.control.TreeItem;
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
import main.ContentsPage.MyTreeItem;

import com.google.common.base.CharMatcher;
import com.google.common.io.Resources;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;

import dataStructures.Tree;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.ByteBuffer;
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
    String imageFilters ="";
    
	@Override
	public byte[] encode() {
		try {
		byte[] titleData = title.getBytes("UTF-8");
		byte[] data = Bytes.concat(Ints.toByteArray(titleData.length),titleData);
		data = Bytes.concat(data,htmlEditor.getHtmlText().getBytes("UTF-8"));
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
			title = new String(data,offset+8,titleLen,"UTF-8");
			String htmlText = new String(data, offset+8 + titleLen,length-(8 + titleLen),"UTF-8");
			htmlText = htmlText.replaceAll("<img src=\"file:\\/\\/"+".*"+"\\/data\\/IMG","<img src=\"file://"+ Main.getJarDir() +"/data/IMG");
			htmlEditor.setHtmlText(htmlText);
		}catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	@Override public Pane BuildPane() {
		Main.currentProject.readyAllImages();
		return new BorderPane(htmlEditor);
	}	

	public void initHTMLEditor() {
	    htmlEditor = new HTMLEditor();
		initHtmlText();
		
		htmlEditor.addEventFilter(InputEvent.ANY, new EventHandler<InputEvent>() {
			@Override public void handle(InputEvent arg0) {
				initHtmlText();
			}
		});
		htmlEditor.addEventFilter(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
			@Override public void handle(MouseEvent event) {
				if (event.getClickCount()==2) {
		            WebView webView = (WebView)htmlEditor.lookup("WebView"); 
					String refId;
					if ((boolean)webView.getEngine().executeScript("document.getElementById('pageFront') != null"))
						refId = (String)webView.getEngine().executeScript("document.getElementById('pageFront').innerHTML;");
					else refId = "-1";
					if (!refId.equals("-1") && !refId.equals("\"-1\"") && !refId.equals("undefined")) {
						if(Main.currentProject.pageMap.containsKey(refId))
							if(Main.currentProject.pageMap.get(refId).getAncestor()!=null) {
								if(htmlEditor.getScene() != Main.stage.getScene())
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
			}});
		initTopToolBar();
		initBottomToolBar();
	}
	
	public void initHtmlText() {
        WebView webView = (WebView)htmlEditor.lookup("WebView"); 
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
					+ "" + backgroundSetting
					+ "';"
					+ "document.head.appendChild(style);");
		}
	}
	
	public void initBottomToolBar() {
	    ToolBar bar = (ToolBar)htmlEditor.lookup(".bottom-toolbar");
	    
	    MenuItem extendRow = new MenuItem("Extend Row ⍗");	    
	    
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
	    
	    Menu tableMenu = new Menu("Table ▤");
	    tableMenu.getItems().addAll(extendRow);  
	    
	    MenuBar menuBar = new MenuBar(tableMenu);
	    bar.getItems().add(menuBar);
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
        
        suggestions.setOnAction((event)->{
        	ContextMenu contextMenu = new ContextMenu();
        	        	
            if((boolean)webEngine.executeScript("window.getSelection().rangeCount > 0")) {
	        	String selection = (String)webEngine.executeScript("document.getSelection().toString()");
	        	if(!selection.equals("")) {
		        	if(Main.languageTrie.hasWord(selection)) {
		        		contextMenu.getItems().add(new MenuItem(selection));
		        	}
		        	else {
			        	HashSet<String> hs = new HashSet<>();
			        	hs.addAll(Main.languageTrie.getSuggestions(selection));
			        	hs.addAll(Main.languageTrie.getSuggestions(selection.toLowerCase()));
			        	
			        	for(String word: hs) {
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
            contextMenu.show(Main.stage);
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
	      		TreeView<String> miniTree = new TreeView<>();
	    		assembleTreeView(miniTree,Main.currentProject);
	    		miniTree.setShowRoot(false);
	    		Stage pageSelStage = new Stage();
	    		pageSelStage.setScene(new Scene(new StackPane(miniTree), 300, 400));
	    		pageSelStage.setTitle("Select Page");
	    		pageSelStage.initModality(Modality.APPLICATION_MODAL);
	    		pageSelStage.requestFocus();
	    		pageSelStage.getScene().getStylesheets().add(Main.styleFile);
	    		pageSelStage.show();
	    		
	    		miniTree.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<String>>(){
	    			@Override public void changed(ObservableValue<? extends TreeItem<String>> observable, TreeItem<String> oldValue, TreeItem<String> newValue) {
	    				MyTreeItem<String> item = (MyTreeItem<String>)miniTree.getSelectionModel().getSelectedItem();
	    				if (item != null) 
	    					if (item.internalAddress.size() > 1) {
	    						Tree.Node<Page> pageNode = Main.currentProject.pageTree.getNode(item.internalAddress);
	    						if(pageNode.data!=BasicPage.this) {
		    						if (!Main.currentProject.pageMap.containsValue(pageNode))
		    							Main.currentProject.pageMap.put(UUID.randomUUID().toString(),pageNode);
		    			   			String selection =  (String)webEngine.executeScript("window.getSelection().toString();");
		    			   			webEngine.executeScript(String.format(JSReplaceSelWithHTML,String.format("<button onclick='pushPage(this.name)' name='%s' type='button' id='pageLink'>%s</button>",Main.currentProject.pageMap.inverse().get(pageNode),selection.replace("\n","<br>"))));
		    						pageSelStage.close();	
	    						}
	    					}		
	    			}}); 
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
		if(Main.topMenu.currentImageFileDir!=null && Main.topMenu.currentImageFileDir.exists()) directoryChooser.setInitialDirectory(Main.topMenu.currentImageFileDir);
        directoryChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("image files", "*.jpg","*.jpeg", "*.png"));
        File directory = directoryChooser.showOpenDialog(Main.stage);
        
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
			Main.topMenu.currentImageFileDir = directory.getParentFile();
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
		
		Stage imageStage = new Stage();
	    GridPane gridPane = new GridPane();
	    gridPane.add(sizeLabel, 0, 0); 			gridPane.add(sizeBox, 1, 0);		gridPane.add(sizeSlider, 2, 0);		gridPane.add(alignBox, 3, 0);
	    gridPane.add(borderRadiusLabel, 0, 1); 	gridPane.add(borderBox, 1, 1);	 	gridPane.add(borderSlider, 2, 1);	gridPane.add(borderTypeBox, 3, 1);	    
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
	    
	    Scene scene = new Scene(gridPane);
		imageStage.setScene(scene);
		imageStage.setTitle("Configure Image");
		imageStage.initModality(Modality.APPLICATION_MODAL);
		imageStage.requestFocus();
		imageStage.getScene().getStylesheets().add(Main.styleFile);
		imageStage.show();
	}
	
	public void addFilterSlider(GridPane gridPane, String imageID, String labelText, String filterAttr, String unit, int length, int current, int row) {
	    Slider slider = new Slider(0,length,current);
	    slider.setShowTickLabels(true);	slider.setShowTickMarks(true);
	    Label label = new Label(labelText);
	    slider.setDisable(true);
	    
	    CheckBox tickBox = new CheckBox(); 
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
	    gridPane.add(label, 0, row);
	    gridPane.add(tickBox, 1, row);
	    gridPane.add(slider, 2, row);
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
		for(int i=startIndex;i<htmlText.length();i++) {
			if(tagWord.toString().contains("<img")) {
				i = htmlText.indexOf("%;\">",i)+"%;\">".length();
				tagWord = new StringBuilder();
				withinTag = false;
			}
			if(tagWord.toString().contains("<script")) {
				i = htmlText.indexOf("</script>",i)+"</script>".length();
				tagWord = new StringBuilder();
				withinTag = false;
			}
			if(tagWord.toString().contains("<style")) {
				i = htmlText.indexOf("</style>",i)+"</style>".length();
				tagWord = new StringBuilder();
				withinTag = false;
			}
			
			if(htmlText.charAt(i)=='<') {
				withinTag = true;
				currentWord = new StringBuilder();
			}
			else if(htmlText.charAt(i)=='>') {
				withinTag = false;
				tagWord = new StringBuilder();
			}		
			else if (withinTag)
				tagWord.append(htmlText.charAt(i));
			
			else if(!withinTag) 
				currentWord.append(htmlText.charAt(i));
									
			if(currentWord.toString().contains(text))
				return i-text.length()+1;
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
				StringBuilder startString = new StringBuilder(htmlText.substring(0,htmlText.indexOf("<span><!--startSel-->")));
				if(startString.lastIndexOf("type=\"button\" id=\"pageLink\">") > startString.lastIndexOf("</button>")) //if start tag is closer
					startIndex = startString.lastIndexOf("<button onclick=");
				else 
					startIndex = htmlText.indexOf("<span><!--startSel-->");
			}
			{ /*endIndex*/
				int from = htmlText.indexOf("<!--endSel--></span>");
				if(htmlText.indexOf("</button>",from) < htmlText.indexOf("<button onclick=",from) || (htmlText.indexOf("</button>",from)!=-1 && htmlText.indexOf("<button onclick=",from)==-1)) //if the end tag is closer
					endIndex = htmlText.indexOf("</button>",from)+"</button>".length();
				else
					endIndex = htmlText.indexOf("<!--endSel--></span>");
			}
			String subString = htmlText.substring(startIndex,endIndex);
			subString = subString.replaceAll("<button onclick="+".*"+"type=\"button\" id=\"pageLink\">", "");
			subString = subString.replace("</button>","");
							
			htmlText = htmlText.substring(0,startIndex) +subString+ htmlText.substring(endIndex);
			htmlText = htmlText.replace("<span><!--startSel-->","");
			htmlText = htmlText.replace("<!--endSel--></span>","");
			htmlEditor.setHtmlText(htmlText);		
        }
	}
	
	public void assembleTreeView(TreeView<String> tree, Project project) {
		ContentsPage.MyTreeItem<String>  root = new ContentsPage.MyTreeItem<String>();
		tree.setRoot(root);
		assembleTreeView(project.pageTree.getRoot(), root, 0);
	}

	private void assembleTreeView(Tree.Node<Page> pageNode, MyTreeItem<String> ParentGUINode, int childArrayIndex) {
		MyTreeItem<String> GUINode = ParentGUINode.branch(pageNode.data.getIcon() + " " + pageNode.data.getTitle());
		GUINode.internalAddress.addAll(ParentGUINode.internalAddress);
		GUINode.internalAddress.add(childArrayIndex);
		for (int i = 0; i < pageNode.getChildren().size(); i++) {
			assembleTreeView(pageNode.getChildren().get(i), GUINode, i);
		}
	}
	
	@Override
	public String getIcon() {
		return "📝";
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
}