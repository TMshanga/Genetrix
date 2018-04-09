package main;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.common.io.ByteStreams;

import dataStructures.Tree.Node;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Pair;
import projectSections.BasicPage;
import projectSections.Folder;
import projectSections.Page;

public class TopMenu {
	File currentFile;
	File currentFileDir;
	public File currentImageFileDir;
	
	public MenuBar getMainMenu(){
		MenuBar  menuBar = new MenuBar();	
		
		Menu fileMenu = new Menu("File");	
		MenuItem load = new MenuItem("Open");
		MenuItem save = new MenuItem("Save");
		MenuItem saveAs = new MenuItem("Save As...");
		MenuItem export = new MenuItem("Export HTML");
		
		MenuItem modena = new MenuItem("Modena");
		MenuItem modenaDark = new MenuItem("Modena Dark");
		MenuItem bootstrap = new MenuItem("Bootstrap");
		MenuItem oscar  = new MenuItem("Oscar");
		MenuItem luke  = new MenuItem("Luke");
		MenuItem vincent  = new MenuItem("Vincent");

		fileMenu.getItems().addAll(load,save,saveAs,new SeparatorMenuItem(),export);
		
		Menu themesMenu = new Menu("Themes");	
		themesMenu.getItems().addAll(modena,modenaDark,new SeparatorMenuItem(),bootstrap,new SeparatorMenuItem(),oscar,luke,vincent);

		menuBar.getMenus().addAll(fileMenu,themesMenu);
		
		save.setOnAction((actionEvent) -> {
			if(currentFile!=null && currentFile.exists()) 
				save(currentFile);
			else
				saveAs();
		});
		saveAs.setOnAction((actionEvent) -> {saveAs();});
		load.setOnAction((actionEvent) -> {load();});
		export.setOnAction((actionEvent) -> {export();});
		modena.setOnAction((actionEvent) -> {Main.restyle("Styles/modena/modena.css");});
		modenaDark.setOnAction((actionEvent) -> {Main.restyle("Styles/modena_dark/modena_dark.css");});
		bootstrap.setOnAction((actionEvent) -> {Main.restyle("Styles/bootstrap/bootstrap3.css");});
		oscar.setOnAction((actionEvent) -> {Main.restyle("Styles/simple/oscar.css");});
		luke.setOnAction((actionEvent) -> {Main.restyle("Styles/simple/luke.css");});
		vincent.setOnAction((actionEvent) -> {Main.restyle("Styles/simple/vincent.css");});	
		return menuBar;
	}
	
	public void saveAs() {	
		FileChooser directoryChooser = new FileChooser();
		if(currentFileDir!=null && currentFileDir.exists()) directoryChooser.setInitialDirectory(currentFileDir);
        directoryChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Genetrix Project File(*.gpf)", "*.gpf"));
        File directory = directoryChooser.showSaveDialog(Main.stage);
        save(directory);

	}
	public void save(File directory) {
		Main.currentProject.clearUnusedImages();
        if(directory == null){
            System.out.println("No Directory selected");
        }else{
        	try {
				FileOutputStream stream = new FileOutputStream(directory);
				stream.write(Main.currentProject.encode());
				stream.close();
		        currentFile = directory;
		        currentFileDir = directory.getParentFile();
			} catch (IOException e) {
				System.out.println(e.getMessage() + "\n"+ e.getStackTrace());
			}
        }
	}
	
	public void load() {		
		FileChooser directoryChooser = new FileChooser();
		if(currentFileDir!=null && currentFileDir.exists()) directoryChooser.setInitialDirectory(currentFileDir);
        directoryChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Genetrix Project File(*.gpf)", "*.gpf"));
        File directory = directoryChooser.showOpenDialog(Main.stage);
        
        if(directory == null){
            System.out.println("No Directory selected");
        }
        else {
			try {
				Main.pageViewer.reset();
				byte[] bytes = ByteStreams.toByteArray(new FileInputStream(directory));
				Main.currentProject.decode(bytes);		
				Main.currentProject.readyAllImages();
				Main.contentsPage.reassembleTreeView(Main.currentProject);
		        currentFile = directory;
		        currentFileDir = directory.getParentFile();
			} catch (IOException e) {
				System.out.println(e.getMessage() + "\n"+ e.getStackTrace());
			} 
        }
	}

	public void export() {
		Main.currentProject.clearUnusedImages();
		DirectoryChooser directoryChooser = new DirectoryChooser();
		if(currentFileDir!=null && currentFileDir.exists()) directoryChooser.setInitialDirectory(currentFileDir);
        File directory = directoryChooser.showDialog(Main.stage);
         
        if(directory == null){ System.out.println("No Directory selected");}
        else{
        	HashMap<Node<Page>,Pair<String,String>> exportMap = new HashMap<>();
        	initExportMap(Main.currentProject.pageTree.root,exportMap);
        	for(Node<Page> page: exportMap.keySet()) {
        		String content ="";
        		if(page.data instanceof BasicPage) {
        			content = (String)page.data.getContent();
        			content = content.replace("contenteditable=\"true\"","");	            		
        			String[] allKeys = StringUtils.substringsBetween(content, "<button onclick=\"pushPage(this.name)\" name=\"", "\" type=\"button\" id=\"pageLink\">");
        			if(allKeys !=null)
	        			for(String key: allKeys) {
	        				Node<Page> reference = Main.currentProject.pageMap.get(key);
	        				if(reference != null)
	        					content = content.replaceAll("(<button onclick=\"pushPage\\(this.name\\)\" name=\"" + key + "\" type=\"button\" id=\"pageLink\">)(.*?)(<\\/button>)","<a href=\"" + exportMap.get(reference).getKey().replace(" ", "%20") +"\">$2<\\/a>");     
	        				else
	        					content = content.replaceAll("(<button onclick=\"pushPage\\(this.name\\)\" name=\"" + key + "\" type=\"button\" id=\"pageLink\">)(.*?)(<\\/button>)","$2");     
	        			}
        			content = content.replaceAll("<img src=\"file:\\/\\/"+".*"+"\\/data\\/IMG","<img src=\"../data/IMG");	            		
        		}
        		else if (page.data instanceof Folder) {
        			content = "<!DOCTYPE html><html><head></head><body><br><b>"+page.data.getTitle()+"</b><br><ul>";
        			for (Node<Page> child : page.getChildren()) {
        				content += "<li><a href=\"" + exportMap.get(child).getKey().replace(" ", "%20") + "\">"+child.data.getTitle()+"</a></li>";
        			}
        			content += "</ul></body></html>";
        		}
        		
    			if(page.getParent() == Main.currentProject.pageTree.root) {
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
    			exportMap.put(page, new Pair<>(exportMap.get(page).getKey(),content));
        	}
        	File mainDir = new File(FilenameUtils.concat(directory.toString(), Main.currentProject.pageTree.root.data.getTitle()));
        	File pageDir = new File(FilenameUtils.concat(mainDir.toString(), "pages"));
        	File dataDir = new File(FilenameUtils.concat(mainDir.toString(), "data"));
    		if(!mainDir.exists()) mainDir.mkdir();
    		if(!pageDir.exists()) pageDir.mkdir();
    		if(!dataDir.exists()) dataDir.mkdir();

        	for(Node<Page> page: exportMap.keySet()) {
        		try {    			
        			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(FilenameUtils.concat(pageDir.toString(), exportMap.get(page).getKey())), "UTF-16"));    			
        			out.write(exportMap.get(page).getValue());
					out.close();
				} catch (IOException e) {e.printStackTrace();}
        	}
        	for(String key: Main.currentProject.imageMap.keySet()) {
        		File imageDir = new File(FilenameUtils.concat(dataDir.toString(), "IMG" + key +".png"));
    		    BufferedImage image = SwingFXUtils.fromFXImage(Main.currentProject.imageMap.get(key), null);
    		    try {ImageIO.write(image, "png", imageDir);} catch (IOException e) {e.printStackTrace();}
        	}   				
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(FilenameUtils.concat(mainDir.toString(), "Root.html")));
				StringBuilder contentsPage = new StringBuilder();
				contentsPageToHtml(exportMap,Main.currentProject.pageTree.root,contentsPage);
				out.write("<!DOCTYPE html><html><body>" + contentsPage.toString() + "</html></body>");
				out.close();
			} catch (IOException e) {e.printStackTrace();}
        }
	}	
	
	public void initExportMap(Node<Page> page, HashMap<Node<Page>,Pair<String,String>> exportMap) {
		if(!exportMap.containsKey(page))
			exportMap.put(page,new Pair<>(page.data.getTitle()+" "+Arrays.toString(page.getAddress().toArray()).replaceAll("\\s+|\\[|\\]|,", "")+".html",""));
		for(int i =0;i<page.getChildren().size();i++) {
			initExportMap(page.getChildren().get(i),exportMap);
		}
	}
	
	public void contentsPageToHtml(HashMap<Node<Page>,Pair<String,String>> exportMap ,Node<Page> page, StringBuilder content) {
		content.append( "<li><a href=\"pages/" + exportMap.get(page).getKey().replace(" ", "%20") + "\">"+page.data.getTitle()+"</a></li>");
		for(int i =0;i<page.getChildren().size();i++) {
			content.append( "<ul>");
			contentsPageToHtml(exportMap,page.getChildren().get(i),content);
			content.append("</ul>");
		}
	}
}
