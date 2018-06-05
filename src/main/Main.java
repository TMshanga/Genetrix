package main;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Modality;
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

import com.google.common.io.Resources;

import dataStructures.Trie;

public class Main extends Application
{	
	final static String title = "Genetrix ver-1.6.7";
	public static String styleFile = TopMenu.styles.Modena.toString();
	
	public static Stage mainStage;
	public static Project currentProject;
	public static ContentsPage contentsPage;
	public static PageViewer pageViewer;
	public static TopMenu topMenu;
	public static Trie languageTrie = Trie.parseWordList("EnglishLanguage.txt");
	public static Settings settings;
	
	static double mousePosX=0, mousePosY=0;
	public static double mouseDeltaX=0, mouseDeltaY=0;
	

	@Override public void start(Stage stage) throws Exception
	{	
		stage = mainStage = new Stage();
		settings = new Settings();
		initScene();
		if(settings.currentFile!=null)if(settings.currentFile.exists()){topMenu.load(settings.currentFile);}
		initIcon(mainStage,"external/mainStageIcon.png");
		mainStage.setTitle(title);
		mainStage.show();
		
		mainStage.setOnCloseRequest((event)->{ 
			try {
				requestConfirmation("Save changes","Save changes?",()->{if(!Main.topMenu.save(Main.settings.currentFile)) event.consume();},()->{},()->event.consume());				
				if(!event.isConsumed()){
					Main.pageViewer.subStageMap.values().forEach((s)->s.close());
					Main.currentProject.clearImageDir();
					savePreferences();
				}
			}catch (Exception e) {
				e.printStackTrace();
				mainStage.close();
			}
		});
	}
	
	public static void initScene() {		
		contentsPage = new ContentsPage();
		pageViewer = new PageViewer();
		topMenu= new TopMenu();
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
		
		mainStage.setScene(scene);
		loadPreferences();	
	}

	public static void restyle(String file) {		
		styleFile = file;
		mainStage.getScene().getStylesheets().setAll(file);
		pageViewer.refreshPages();
	}
	
	public static void savePreferences() {
		StringBuilder prefs = new StringBuilder();
		prefs.append("<style>" + styleFile + "<style>");
		if(settings.currentFileDir !=null) {
			if(settings.currentFile!=null) prefs.append("<saveFile>" + settings.currentFile + "<saveFile>");
			prefs.append("<saveDir>" + settings.currentFileDir + "<saveDir>");
			prefs.append("<imageDir>" + settings.currentImageFileDir + "<imageDir>");
			prefs.append("<devSpelling>" + settings.spellChecking + "<devSpelling>");
			prefs.append("<devHTML>" + settings.directHtmlEditing + "<devHTML>");
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
				if(prefs.contains("<saveDir>")) settings.currentFileDir = new File(StringUtils.substringBetween(prefs,"<saveDir>"));
				if(prefs.contains("<imageDir>")) settings.currentImageFileDir = new File(StringUtils.substringBetween(prefs,"<imageDir>"));
				if(prefs.contains("<devSpelling>")) settings.spellChecking = new Boolean(StringUtils.substringBetween(prefs,"<devSpelling>"));
				if(prefs.contains("<devHTML>")) settings.directHtmlEditing = new Boolean(StringUtils.substringBetween(prefs,"<devHTML>"));
				if(prefs.contains("<saveFile>")) settings.currentFile = new File(StringUtils.substringBetween(prefs,"<saveFile>"));
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
		    	if(settings.currentFile!=null && settings.currentFile.exists()) 
					topMenu.save(settings.currentFile);
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
	
	public static Stage createSubStage(Scene scene, String name, Modality modality) {
		scene.getStylesheets().add(Main.styleFile);
		initHotKeys(scene);
		Stage stage = new Stage();
		initIcon(stage,"external/subStageIcon.png");		
		stage.setScene(scene);
		stage.getScene().getStylesheets().add(Main.styleFile);
		stage.setTitle(name);
		stage.initOwner(Main.mainStage);
		stage.initModality(modality);
		stage.setAlwaysOnTop(false);
		stage.requestFocus();
		return stage;
	}
	
	public static void initIcon(Stage stage,String file) {
        Image applicationIcon = new Image(Resources.getResource(file).toString());
        stage.getIcons().add(applicationIcon);
	}
	
	public static <T> void requestConfirmation(String title, String content, Runnable confirmAction, Runnable denyAction, Runnable cancelAction) {	
		Alert alert = new Alert(AlertType.CONFIRMATION, content, ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
		initIcon((Stage)alert.getDialogPane().getScene().getWindow(),"external/subStageIcon.png");
		alert.getDialogPane().getStylesheets().setAll(Main.styleFile);
		alert.setGraphic(null);
		alert.setHeaderText(null);
		alert.showAndWait();
		try {
			if(alert.getResult() == ButtonType.YES)
				confirmAction.run();
			else if(alert.getResult() == ButtonType.NO)
				denyAction.run();
			else
				cancelAction.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args)
	{
		launch(args);
	}	
}