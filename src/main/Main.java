package main;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import projectSections.BasicPage;
import projectSections.Project;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Files;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import dataStructures.Trie;

public class Main extends Application
{	
	final String title = "Genetrix ver-1.6.0";
	public static String styleFile = TopMenu.styles.Modena.toString();
	
	public static Stage stage = new Stage();
	public static Project currentProject = new Project();
	public static ContentsPage contentsPage = new ContentsPage();
	public static PageViewer pageViewer = new PageViewer();
	public static TopMenu topMenu= new TopMenu();
	public static Trie languageTrie = new Trie();
	
	static double mousePosX=0, mousePosY=0;
	public static double mouseDeltaX=0, mouseDeltaY=0;
	

	@Override public void start(Stage mainStage) throws Exception
	{	
		Main.languageTrie.buildLanguageTrie(Main.languageTrie.readWordList("EnglishLanguage.txt"));
		mainStage = stage;
		currentProject = new Project();
		contentsPage.tree.getRoot().getChildren().add(new TreeItem<>(new BasicPage("New Page")));
		
		BorderPane borderPane = new BorderPane();	
		borderPane.setTop(topMenu.getMainMenu());
		borderPane.setLeft(contentsPage.getContentsPage());		
		borderPane.setCenter(pageViewer.viewerPane);
		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Scene scene = new Scene(borderPane,screenSize.getWidth()/1.2,screenSize.getHeight()/1.2);			
		initHotKeys(scene);
		initMouseDeltaPos(scene);

		mainStage.setOnCloseRequest((event)->{ 
			try {
			Main.currentProject.clearImageDir(); savePreferences();
			}catch (Exception e) {
				e.printStackTrace();
			}
		});
		mainStage.setScene(scene);
		mainStage.setTitle(title);
		mainStage.show();
		loadPreferences();
	}
	
	public static void restyle(String file) {		
		styleFile = file;
		stage.getScene().getStylesheets().setAll(file);
		pageViewer.restyle(file);
	}
	
	public static void savePreferences() {
		StringBuilder prefs = new StringBuilder();
		prefs.append("<style>" + styleFile + "<style>");
		if(topMenu.currentFileDir !=null) {
			prefs.append("<saveDir>" + topMenu.currentFileDir + "<saveDir>");
			prefs.append("<imageDir>" + topMenu.currentImageFileDir + "<imageDir>");
		}
		
		String jarDir = FilenameUtils.concat(getJarDir(),"data");
		if(!new File(jarDir).exists()) new File(jarDir).mkdir();
		
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(FilenameUtils.concat(jarDir,"preferences.ini")));		
			out.write(prefs.toString());
			out.close();
		} catch (IOException e) {e.printStackTrace();}
	}
	public static void loadPreferences() {
		String jarDir = FilenameUtils.concat(getJarDir(),"data/preferences.ini");
		if(new File(jarDir).exists()) {
			try {
				String prefs = new String(Files.readAllBytes(new File(jarDir).toPath()));
				if(prefs.contains("<style>")) restyle(StringUtils.substringBetween(prefs,"<style>"));
				if(prefs.contains("<saveDir>")) topMenu.currentFileDir = new File(StringUtils.substringBetween(prefs,"<saveDir>"));
				if(prefs.contains("<imageDir>")) topMenu.currentImageFileDir = new File(StringUtils.substringBetween(prefs,"<imageDir>"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static String getJarDir() {
		try {
			String jarDir = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
			jarDir = (new File(jarDir)).getParentFile().getPath();
			return URLDecoder.decode(jarDir, "UTF-8").replace("\\", "/");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return "..";
		}
	}
	
	public static void initHotKeys(Scene scene) {
		scene.addEventFilter(KeyEvent.KEY_PRESSED, (event)-> {
		    KeyCombination saveComb = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN);
		    if (saveComb.match(event)) {
		    	if(topMenu.currentFile!=null && topMenu.currentFile.exists()) 
					topMenu.save(topMenu.currentFile);
				else 
					topMenu.saveAs();
		           event.consume();
		    }
		});	
	}
	
	public static void initMouseDeltaPos(Scene scene) {
		scene.addEventFilter(MouseEvent.MOUSE_MOVED, (event) -> {
		    mouseDeltaX = mousePosX-event.getSceneX();
		 	mouseDeltaY = mousePosY-event.getSceneY();
		 	mousePosX = event.getSceneX();
		 	mousePosY = event.getSceneY();	
		});
	}
	
	public static void main(String[] args)
	{
		launch(args);
	}	
}