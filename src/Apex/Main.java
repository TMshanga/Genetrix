package Apex;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.geometry.Orientation;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.ArrayList;

import DataStructures.Tree;
import DataStructures.Trie;
import ProjectSections.Page;
import ProjectSections.PageInterface;
import ProjectSections.Project;

public class Main extends Application
{	
	static Stage stage = new Stage();
	public static Project currentProject = new Project();
	public static PageViewer pageViewer = new PageViewer();
	
	static double mousePosX=0, mousePosY=0;
	public static double mousePosChangeX=0, mousePosChangeY=0;
	
	public static Trie englishTrie = new Trie();
	

	@Override public void start(Stage mainStage) throws Exception
	{	
    	englishTrie.buildLanguageTrie(Trie.readWordList(System.getProperty("user.dir") + "\\src\\External\\EnglishLanguage.txt"));
		
		mainStage = stage;
		currentProject = new Project("Project Name");
		currentProject.pageTree.root.add(new Page("New Page"));
		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	    
		BorderPane borderPane = new BorderPane();	
		borderPane.setTop(TopMenu.getMainMenu());
		ContentsPage contentsPage = new ContentsPage();
		borderPane.setLeft(contentsPage.getContentsPage());
		
		HBox sep = new HBox();
		Separator sepa = new Separator();
		sepa.setOrientation(Orientation.VERTICAL);
		sep.getChildren().addAll(sepa);
				
		borderPane.setCenter(pageViewer.viewerPane);
		
		Scene scene = new Scene(borderPane,screenSize.getWidth()-500,screenSize.getHeight()-300);	
		scene.addEventFilter(MouseEvent.MOUSE_MOVED, new EventHandler<MouseEvent>() {
		    @Override
		    public void handle(MouseEvent event) {
		    	//System.out.println("X:" + mousePosChangeX +" Y:" + mousePosChangeY);
		    	mousePosChangeX = mousePosX-event.getSceneX();
		 		mousePosChangeY = mousePosY-event.getSceneY();
		 		mousePosX = event.getSceneX();
		 		mousePosY = event.getSceneY();	
		    }
		});
		mainStage.setScene(scene);
		mainStage.setTitle("Genetrix");
		mainStage.show();
	}
	public static void main(String[] args)
	{
		launch(args);
	}	
}