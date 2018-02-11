package Apex;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.geometry.Orientation;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.awt.Dimension;
import java.awt.Toolkit;

import DataStructures.Tree;
import ProjectSections.Page;
import ProjectSections.PageInterface;
import ProjectSections.Project;

public class Main extends Application
{	
	static Stage stage = new Stage();
	static Project currentProject = new Project();
	static double mousePosX=0, mousePosY=0;
	static double mousePosChangeX=0, mousePosChangeY=0;
	
	public static void main(String[] args)
	{
		launch(args);
	}	
	@Override public void start(Stage mainStage) throws Exception
	{	
		mainStage = stage;
		currentProject = new Project("Project Name");
		Tree.Node<PageInterface> page1 = new Tree.Node<PageInterface>(new Page("Page 1"));
		currentProject.pageTree.root.add(page1);
		currentProject.pageTree.root.add(new Tree.Node<PageInterface>(new Page("Page 2")));
		currentProject.pageTree.root.add(new Tree.Node<PageInterface>(new Page("Page 3")));
		currentProject.pageTree.root.add(new Tree.Node<PageInterface>(new Page("Page 4")));


		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	    
		BorderPane borderPane = new BorderPane();	
		borderPane.setTop(TopMenu.getMainMenu());
		ContentsPage contentsPage = new ContentsPage();
		borderPane.setLeft(contentsPage.getContentsPage());
		
		HBox sep = new HBox();
		Separator sepa = new Separator();
		sepa.setOrientation(Orientation.VERTICAL);
		sep.getChildren().add(sepa);
		borderPane.setCenter(sep);
		
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
	
	
}