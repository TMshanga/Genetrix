package Apex;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.*;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import java.awt.Dimension;
import java.awt.Toolkit;

import DataStructures.Tree;
import ProjectSections.Folder;
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
		Tree.add(currentProject.pageTree.root, page1);
		Tree.Node<PageInterface> folder = new Tree.Node<PageInterface>(new Folder("Folder 1"));
		Tree.Node<PageInterface> folder2 = new Tree.Node<PageInterface>(new Folder("Folder 2"));
		Tree.add(currentProject.pageTree.root, folder);;
		Tree.add(folder, new Tree.Node<PageInterface>(new Page("Page 2")));
		Tree.add(folder, folder2);
		folder2.children.add(new Tree.Node<PageInterface>(new Folder("Folder 3")));
		Tree.move(page1,folder2);
		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	    
		BorderPane borderPane = new BorderPane();	
		borderPane.setTop(TopMenu.getMainMenu());
		borderPane.setLeft(new ContentsPage().getContentsPage());
		
		HBox sep = new HBox();
		Separator sepa = new Separator();
		sepa.setOrientation(Orientation.VERTICAL);
		sep.getChildren().add(sepa);
		borderPane.setCenter(sep);
		
		mainStage.setScene(new Scene(borderPane,screenSize.getWidth()-300,screenSize.getHeight()-200));
		mainStage.setTitle("Genetrix");
		mainStage.show();
	}
}