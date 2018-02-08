package Apex;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.geometry.Orientation;
import javafx.stage.Stage;
import javafx.scene.control.*;

import java.awt.Dimension;
import java.awt.Toolkit;

import DataStructures.Tree;
import ProjectSections.Page;
import ProjectSections.PageInterface;
import ProjectSections.Project;

public class Main extends Application
{	
	static Project currentProject = new Project();
	
	public static void main(String[] args)
	{
		launch(args);
	}	
	@Override public void start(Stage mainStage) throws Exception
	{	
		currentProject = new Project("Project Name");
		Tree.Node<PageInterface> page1 = new Tree.Node<PageInterface>(new Page("Page 1"));
		currentProject.pageTree.root.add(page1);
		
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
		mainStage.setScene(scene);
		mainStage.setTitle("Genetrix");
		mainStage.show();
	}
	
	
}