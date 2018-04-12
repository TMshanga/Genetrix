package main;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.common.io.ByteStreams;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TreeItem;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Pair;
import projectSections.BasicPage;
import projectSections.Folder;
import projectSections.Note;
import projectSections.Page;

public class TopMenu {
	File currentFile;
	File currentFileDir;
	public File currentImageFileDir;
	
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
		modena.setOnAction((actionEvent) -> {Main.restyle(styles.Modena.toString());});
		modenaDark.setOnAction((actionEvent) -> {Main.restyle(styles.ModenaDark.toString());});
		bootstrap.setOnAction((actionEvent) -> {Main.restyle(styles.Bootstrap.toString());});
		oscar.setOnAction((actionEvent) -> {Main.restyle(styles.Oscar.toString());});
		luke.setOnAction((actionEvent) -> {Main.restyle(styles.Luke.toString());});
		vincent.setOnAction((actionEvent) -> {Main.restyle(styles.Vincent.toString());});	
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
				Main.contentsPage.tree.getRoot().setExpanded(true);
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
	        			content = content.replaceAll("<img src=\"file:\\/\\/"+".*"+"\\/data\\/IMG","<img src=\"../data/IMG");	            		
	        		}
	        		else if (page.getValue() instanceof Folder) {
	        			content = "<!DOCTYPE html><html><head></head><body><br><b>"+"Folder: "+page.getValue().getTitle()+"</b><br><ul>";
	        			for (TreeItem<Page> child : page.getChildren()) {
	        				content += "<li><a href=\"" + exportMap.get(child).getKey().replace(" ", "%20") + "\">"+child.getValue().getTitle()+"</a></li>";
	        			}
	        			content += "</ul></body></html>";
	        		}
	        		else if (page.getValue() instanceof Note) {
	        			content = "<!DOCTYPE html><html><head></head><body><br><b>"+"Note: "+page.getValue().getTitle()+"</b><br><ul><p>";
	        			content += ((Note)page.getValue()).textArea.getText();
	        			content += "</p></ul></body></html>";
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
        	File mainDir = new File(FilenameUtils.concat(directory.toString(), Main.contentsPage.tree.getRoot().getValue().getTitle()));
        	File pageDir = new File(FilenameUtils.concat(mainDir.toString(), "pages"));
        	File dataDir = new File(FilenameUtils.concat(mainDir.toString(), "data"));
    		if(!mainDir.exists()) mainDir.mkdir();
    		if(!pageDir.exists()) pageDir.mkdir();
    		if(!dataDir.exists()) dataDir.mkdir();

        	for(TreeItem<Page> page: exportMap.keySet()) {
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
				contentsPageToHtml(exportMap,Main.contentsPage.tree.getRoot(),contentsPage);
				out.write("<!DOCTYPE html><html><body>" + contentsPage.toString() + "</html></body>");
				out.close();
			} catch (IOException e) {e.printStackTrace();}
        }
	}	
	
	public void initExportMap(TreeItem<Page> page, HashMap<TreeItem<Page>,Pair<String,String>> exportMap) {
		if(!exportMap.containsKey(page))
			exportMap.put(page,new Pair<>(page.getValue().getTitle()+" "+page.hashCode()+".html",""));
		for(int i =0;i<page.getChildren().size();i++) {
			initExportMap(page.getChildren().get(i),exportMap);
		}
	}
	
	public void contentsPageToHtml(HashMap<TreeItem<Page>,Pair<String,String>> exportMap ,TreeItem<Page> page, StringBuilder content) {
		content.append( "<li><a href=\"pages/" + exportMap.get(page).getKey().replace(" ", "%20") + "\">"+page.getValue().getTitle()+"</a></li>");
		for(int i =0;i<page.getChildren().size();i++) {
			content.append( "<ul>");
			contentsPageToHtml(exportMap,page.getChildren().get(i),content);
			content.append("</ul>");
		}
	}
}
