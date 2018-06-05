package main;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.common.io.ByteStreams;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;
import projectSections.BasicPage;
import projectSections.Folder;
import projectSections.Note;
import projectSections.Page;

public class TopMenu {
	final String exportStyle = "<style>*{font-family: calibri;}.pagebreak { page-break-before: always; }</style>";
	
	public static enum styles {
	    Modena("Styles/modena/modena.css"), 
	    ModenaDark("Styles/modena_dark/modena_dark.css") ,
	    Bootstrap("Styles/bootstrap/bootstrap3.css"), 
	    Oscar("Styles/simple/oscar.css"), 
	    Luke("Styles/simple/luke.css"), 
	    Vincent("Styles/simple/vincent.css");	

	    private String strVal;
	    styles(String strVal) {
	        this.strVal = strVal;
	    }
	    public String toString() {
	        return strVal;
	    }
	}
	
	public MenuBar getMainMenu(){
		MenuBar  menuBar = new MenuBar();	
		
		Menu fileMenu = new Menu("File");	
		MenuItem load = new MenuItem("Open");

		MenuItem newProject = new MenuItem("New Project");
		MenuItem saveOver = new MenuItem("Save");
		MenuItem saveAs = new MenuItem("Save As...");
		Menu save = new Menu("Save...");
		save.getItems().addAll(saveOver,saveAs);	
		MenuItem export = new MenuItem("Export HTML");
		
		Menu themesMenu = new Menu("Themes");	
		MenuItem modena = new MenuItem("Modena");
		MenuItem modenaDark = new MenuItem("Modena Dark");
		MenuItem bootstrap = new MenuItem("Bootstrap");
		MenuItem oscar  = new MenuItem("Oscar");
		MenuItem luke  = new MenuItem("Luke");
		MenuItem vincent  = new MenuItem("Vincent");
		
		Menu settingsMenu = new Menu("Settings");
		MenuItem devSettings  = new MenuItem("Developer Settings");

		
		fileMenu.getItems().addAll(newProject,load,save,new SeparatorMenuItem(),export);		
		themesMenu.getItems().addAll(modena,modenaDark,new SeparatorMenuItem(),bootstrap,new SeparatorMenuItem(),oscar,luke,vincent);
		settingsMenu.getItems().addAll(devSettings);
		
		menuBar.getMenus().addAll(fileMenu,themesMenu,settingsMenu);
		
		newProject.setOnAction(event -> {
			Main.requestConfirmation("Save changes","Save changes?",()->{if(!Main.topMenu.save(Main.settings.currentFile))event.consume();},()->{},()->event.consume());		
			if (!event.isConsumed()) {
				Main.pageViewer.subStageMap.values().forEach(s->s.close());
				Main.mainStage.setTitle(Main.title);
				Main.initScene();
				Main.settings.currentFile = Main.settings.currentFileDir = null;
			}		
		});
		save.setOnAction(event -> {
			if(Main.settings.currentFile!=null && Main.settings.currentFile.exists()) 
				save(Main.settings.currentFile);
			else saveAs();
		});
		saveOver.setOnAction(event -> {
			if(Main.settings.currentFile!=null && Main.settings.currentFile.exists()) 
				save(Main.settings.currentFile);
			else saveAs();
		});
		saveAs.setOnAction(event -> saveAs());
		load.setOnAction(event -> loadFrom());
		export.setOnAction(event -> exportHTML());
		devSettings.setOnAction(event -> developerSettings());
		modena.setOnAction(event -> Main.restyle(styles.Modena.toString()));
		modenaDark.setOnAction(event -> Main.restyle(styles.ModenaDark.toString()));
		bootstrap.setOnAction(event -> Main.restyle(styles.Bootstrap.toString()));
		oscar.setOnAction(event -> Main.restyle(styles.Oscar.toString()));
		luke.setOnAction(event -> Main.restyle(styles.Luke.toString()));
		vincent.setOnAction(event -> Main.restyle(styles.Vincent.toString()));
		return menuBar;
	}
	
	public boolean saveAs() {	
		FileChooser directoryChooser = new FileChooser();
		if(Main.settings.currentFileDir!=null && Main.settings.currentFileDir.exists()) directoryChooser.setInitialDirectory(Main.settings.currentFileDir);
        directoryChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Genetrix Project File(*.gpf)", "*.gpf"));
        File directory = directoryChooser.showSaveDialog(Main.mainStage);
        return save(directory);
	}
	public boolean save(File directory) {
		Main.currentProject.clearUnusedImages();
        if(directory == null){
            return false;
        }else{
        	try {
				DeflaterOutputStream stream = new DeflaterOutputStream(new FileOutputStream(directory));				
				stream.write(Main.currentProject.encode());
				stream.close();
		        Main.settings.currentFile = directory;
		        Main.settings.currentFileDir = directory.getParentFile();
		        Main.mainStage.setTitle(Main.title + " (last saved: "+ new SimpleDateFormat("HH:mm:ss").format(new Date())+")");
	            return true;
			} catch (IOException e) {
				System.out.println(e.getMessage() + "\n"+ e.getStackTrace());
	            return false;
			}
        }
       }
	
	public void loadFrom() {		
		FileChooser directoryChooser = new FileChooser();
		if(Main.settings.currentFileDir!=null && Main.settings.currentFileDir.exists()) directoryChooser.setInitialDirectory(Main.settings.currentFileDir);
        directoryChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Genetrix Project File(*.gpf)", "*.gpf"));
        File directory = directoryChooser.showOpenDialog(Main.mainStage);
        
        if(directory == null){
            System.out.println("No Directory selected");
        }
        else {
			try {
				load(directory);
			} catch (IOException e) {
				System.out.println(e.getMessage() + "\n"+ e.getStackTrace());
			} 
        }
	}

	public void load(File directory) throws IOException {
        if(directory == null || !directory.exists()){
        	throw new IOException("File not found");
        }
		Main.pageViewer.reset();
		InflaterInputStream inflator = new InflaterInputStream(new FileInputStream(directory));	
		Main.currentProject.decode(ByteStreams.toByteArray(inflator));		
		Main.currentProject.readyAllImages();
		Main.contentsPage.tree.getRoot().setExpanded(true);
        Main.settings.currentFile = directory;
        Main.settings.currentFileDir = directory.getParentFile();
	}
	
	public void developerSettings() {
		GridPane gridPane = new GridPane();
		
		CheckBox spellingBox = new CheckBox("Enable spell checking options");
		CheckBox htmlBox = new CheckBox("Enable direct HTML editing");
		
		spellingBox.setSelected(Main.settings.spellChecking);
		htmlBox.setSelected(Main.settings.directHtmlEditing);
		
		spellingBox.selectedProperty().addListener((obsv,oldV,newV)->{
			Main.settings.spellChecking = newV;
		});
		htmlBox.selectedProperty().addListener((obsv,oldV,newV)->{
			Main.settings.directHtmlEditing = newV;
		});
		
		gridPane.add(spellingBox, 0, 0);
		gridPane.add(htmlBox, 0, 1);
			
		Stage subStage = Main.createSubStage(new Scene(gridPane), "Developer Settings", Modality.APPLICATION_MODAL);
		subStage.setOnCloseRequest((event)->{ Main.pageViewer.refreshPages(); });
		subStage.show();
	}

	public void exportHTML() {
		Main.currentProject.clearUnusedImages();
		DirectoryChooser directoryChooser = new DirectoryChooser();
		if(Main.settings.currentFileDir!=null && Main.settings.currentFileDir.exists()) directoryChooser.setInitialDirectory(Main.settings.currentFileDir);
        File directory = directoryChooser.showDialog(Main.mainStage);
         
        if(directory == null){ System.out.println("No Directory selected");}
        else{
        	HashMap<TreeItem<Page>,Pair<String,String>> exportMap = getExportMap();
        	File mainDir = new File(FilenameUtils.concat(directory.toString(), Main.contentsPage.tree.getRoot().getValue().getTitle()));
        	File pageDir = new File(FilenameUtils.concat(mainDir.toString(), "pages"));
        	File dataDir = new File(FilenameUtils.concat(mainDir.toString(), "data"));
    		if(!mainDir.exists()) mainDir.mkdir();
    		if(!pageDir.exists()) pageDir.mkdir();
    		if(!dataDir.exists()) dataDir.mkdir();
    		
				try {
					BufferedWriter out = new BufferedWriter(new FileWriter(FilenameUtils.concat(mainDir.toString(), "Root.html")));
					StringBuilder contentsPage = new StringBuilder();
					contentsPageToHtml(exportMap,Main.contentsPage.tree.getRoot(),contentsPage);
					out.write("<!DOCTYPE html><html>"+ exportStyle +"<body>" + contentsPage.toString() + "</body></html>");
					out.close();
				} catch (IOException e) {e.printStackTrace();}
				for(TreeItem<Page> page: exportMap.keySet()) {
	        		if(page != Main.contentsPage.tree.getRoot()) {
		        		try {    			
		        			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(FilenameUtils.concat(pageDir.toString(), exportMap.get(page).getKey())), "UTF-16"));    			
		        			out.write(exportMap.get(page).getValue());
							out.close();
						} catch (IOException e) {e.printStackTrace();}
	        		}
	        	}
	        	for(String key: Main.currentProject.imageMap.keySet()) {
	        		File imageDir = new File(FilenameUtils.concat(dataDir.toString(), "IMG" + key +".png"));
	    		    BufferedImage image = SwingFXUtils.fromFXImage(Main.currentProject.imageMap.get(key), null);
	    		    try {ImageIO.write(image, "png", imageDir);} catch (IOException e) {e.printStackTrace();}
	        	} 
	        	
				try {
		    		File[] allfiles = pageDir.listFiles();		
		    		Arrays.sort(allfiles,(a,b)->a.getName().compareTo(b.getName()));
		    		String combined="";
	    		for(File file: allfiles) {
	    			System.out.println(file.getName());
					combined += "<hr>" + FileUtils.readFileToString(file, "UTF-16") + "</hr>";
	    		}
					BufferedWriter out = new BufferedWriter(new FileWriter(FilenameUtils.concat(pageDir.toString(), "ALL.html")));
					out.write("<!DOCTYPE html><html>" + combined + "</html>");
					out.close();  		
				} catch (IOException e) {
					e.printStackTrace();
				}
        }
	}	
	
	public HashMap<TreeItem<Page>,Pair<String,String>> getExportMap() {
    	HashMap<TreeItem<Page>,Pair<String,String>> exportMap = new HashMap<>();
    	initExportMap(Main.contentsPage.tree.getRoot(),exportMap);
    	
    	for(TreeItem<Page> page: exportMap.keySet()) {
    		String content ="";
    		if(page != Main.contentsPage.tree.getRoot()) {
    			
        		if(page.getValue() instanceof BasicPage) {
        			content = ((BasicPage)page.getValue()).htmlEditor.getHtmlText();
        			content = content.replace("contenteditable=\"true\"","");	            		
        			String[] allKeys = StringUtils.substringsBetween(content, "<button onclick=\"pushPage(this.name)\" name=\"", "\" type=\"button\" id=\"pageLink\">");
        			if(allKeys !=null)
	        			for(String key: allKeys) {
	        				TreeItem<Page> reference = Main.currentProject.pageMap.get(key);
	        				if(reference != null)
	        					content = content.replaceAll("(<button onclick=\"pushPage\\(this.name\\)\" name=\"" + key + "\" type=\"button\" id=\"pageLink\">)(.*?)(<\\/button>)","<a href=\"" + exportMap.get(reference).getKey().replace(" ", "%20") +"\">$2<\\/a>");     
	        				else
	        					content = content.replaceAll("(<button onclick=\"pushPage\\(this.name\\)\" name=\"" + key + "\" type=\"button\" id=\"pageLink\">)(.*?)(<\\/button>)","$2");     
	        			}
        			content = content.replaceAll("<img src=\"file:\\/\\/"+".*?"+"\\/data\\/IMG","<img src=\"../data/IMG");	            		
        		}
        		
        		else if (page.getValue() instanceof Folder) {
        			content = "<!DOCTYPE html><html><head>"+ exportStyle +"</head><body><br><b>"+"Folder: "+page.getValue().getTitle()+"</b><br><ul>";
        			for (TreeItem<Page> child : page.getChildren()) {
        				content += "<li><a href=\"" + exportMap.get(child).getKey().replace(" ", "%20") + "\">"+child.getValue().getTitle()+"</a></li>";
        			}
        			content += "</ul></body></html>";
        		}
        		
        		else if (page.getValue() instanceof Note) {
        			content = "<!DOCTYPE html><html><head>"+ exportStyle +"</head><body><br><b>"+"Note: "+page.getValue().getTitle()+"</b><br><blockquote><p>";
        			content += ((Note)page.getValue()).textArea.getText().replaceAll("\n", "<br>");
        			content += "</p></blockquote></body></html>";
        		}
        		
    			if(page.getParent() == Main.contentsPage.tree.getRoot()) {
    				if(content.contains("</head>"))
    					content = content.substring(0, content.indexOf("</head>"))+"<div><a href=../Root.html>"+"\n<< Parent Page"+"</a></div>" + content.substring(content.indexOf("</head>"));
    				else
    					content += "<div><a href=../Root.html>"+"\n<< Parent Page"+"</a></div>";
    			}
    			else {
    				if(content.contains("</head>"))
    					content = content.substring(0, content.indexOf("</head>"))+"<div><a href=\"" + exportMap.get(page.getParent()).getKey().replace(" ", "%20") +"\">"+"\n<< Parent Page"+"</a></div>"+ content.substring(content.indexOf("</head>"));
    				else
    					content += "<div><a href=\"" + exportMap.get(page.getParent()).getKey().replace(" ", "%20") +"\">"+"\n<< Parent Page"+"</a></div>";
    			}   
        	}
			exportMap.put(page, new Pair<>(exportMap.get(page).getKey(),content));
    	}
    	return exportMap;
	}
	
	public void initExportMap(TreeItem<Page> page, HashMap<TreeItem<Page>,Pair<String,String>> exportMap) {
		if(!exportMap.containsKey(page))
			exportMap.put(page,new Pair<>(Arrays.toString(Main.contentsPage.getAddress(page).toArray()).replaceAll("\\[|]|\\s", "").replace(",", ".") + " " + page.getValue().getTitle()+".html",""));
		
		page.getChildren().forEach(p -> initExportMap(p,exportMap));
	}
	
	public void contentsPageToHtml(HashMap<TreeItem<Page>,Pair<String,String>> exportMap ,TreeItem<Page> page, StringBuilder content) {
		if(page != Main.contentsPage.tree.getRoot())
			content.append( "<li><a href=\"pages/" + exportMap.get(page).getKey().replace(" ", "%20") + "\">"+page.getValue().getTitle() + "</a></li>");
		else
			content.append("<li><a href=\"\">" + page.getValue().getTitle() + "</a></li>");
		
		page.getChildren().forEach(p -> {
			content.append( "<ul>");
			contentsPageToHtml(exportMap,p,content);
			content.append( "</ul>");
		});
	}
}
