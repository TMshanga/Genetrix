package Apex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

import ProjectSections.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import DataStructures.Tree;


public class ContentsPage {
	TreeView<String> tree = new TreeView<String>();	
	
	public VBox getContentsPage()
	{
		VBox stackPane = new VBox();
		stackPane.setPadding(new Insets(5,5,5,5));
		//Title
			Text text = new Text();
		    text.setFont(Font.font("Tahoma",FontWeight.BOLD,20));	
			text.setText("ℂ𝕠𝕟𝕥𝕖𝕟𝕥𝕤 ℙ𝕒𝕘𝕖");
		//Options
			MenuBar  menuBar = new MenuBar();	
			Menu fileMenu = new Menu("File");	
			fileMenu.getItems().add(new MenuItem("flow"));
			menuBar.getMenus().addAll(fileMenu);
		//Tree
			tree = depthFirstAssembily(Main.currentProject);	
			tree.setShowRoot(false);
			tree.setContextMenu(getContextMenu());	
		//Final
			stackPane.setAlignment(Pos.TOP_CENTER);
			stackPane.getChildren().addAll(text,new Separator(),menuBar,tree);
		return stackPane;
	}
	
	class MyTreeItem<T1> extends TreeItem<T1> {
		ArrayList<Integer> internalAddress;
	    public MyTreeItem(){super(); internalAddress = new ArrayList<Integer>();}
	    public MyTreeItem(T1 value) {super(); setValue(value); internalAddress = new ArrayList<Integer>();} 
	
		public MyTreeItem<T1> branch(T1 value){
			MyTreeItem<T1>item = new MyTreeItem<T1>(value);
			item.setExpanded(true);
			getChildren().add(item);
			return item;
		}
	}
	
	class listener implements ChangeListener{
		@Override public void changed(ObservableValue observable,Object oldValue,Object newValue) {
			MyTreeItem item = (MyTreeItem)newValue;
			
			if(item.internalAddress.size()>1) { //if the clicked item is not the root node
				PageInterface page = (PageInterface)Main.currentProject.pageTree.getNode(item.internalAddress).data;
				Pane pane = page.BuildPane();
				Stage subStage = new Stage();
				subStage.setOpacity(0.8);
				subStage.setScene(new Scene(pane,500,500));
				subStage.setTitle(page.getTitle());
				subStage.setAlwaysOnTop(true);
				subStage.show();
			}
		}	
	}
	
	public ContextMenu getContextMenu(){
		final ContextMenu contextMenu = new ContextMenu();
		MenuItem cut = new MenuItem("Cut");
		MenuItem copy = new MenuItem("Copy");
		MenuItem paste = new MenuItem("Paste");
		contextMenu.getItems().addAll(cut, copy, paste);
		cut.setOnAction(new EventHandler<ActionEvent>() {
		    @Override
		    public void handle(ActionEvent event) {
		    	MyTreeItem item = (MyTreeItem)tree.getSelectionModel().getSelectedItem();
		    	System.out.println(item.toString());
				Tree.Node<PageInterface> node = (Tree.Node<PageInterface>)Main.currentProject.pageTree.getNode(item.internalAddress);
				node.getParent().remove(node);
				tree = depthFirstAssembily(Main.currentProject);
		}});
		return contextMenu;
	}
	
	public TreeView<String> depthFirstAssembily(Project project){
		MyTreeItem<String> root = new MyTreeItem<String>();
		tree.setRoot(root);
		depthFirstAssembily(project.pageTree.root,new ArrayList<Tree.Node<PageInterface>>(), root,0);
		return tree;
	}	
	private void depthFirstAssembily(Tree.Node<PageInterface> pageNode, ArrayList<Tree.Node<PageInterface>> visited, MyTreeItem<String> ParentGUINode, int childArrayIndex) {
		MyTreeItem<String> GUINode =  ParentGUINode.branch(pageNode.data.getIcon() + " " + pageNode.data.getTitle());
		GUINode.internalAddress.addAll(ParentGUINode.internalAddress);GUINode.internalAddress.add(childArrayIndex);
 		if(visited == null)visited = new ArrayList<Tree.Node<PageInterface>>();
        visited.add(pageNode);
 		for (int i = 0; i < pageNode.getChildren().size(); i++) {
 			Tree.Node<PageInterface> childPage = pageNode.getChildren().get(i);
 			if(childPage!=null && !visited.contains(childPage))
 			{
 				depthFirstAssembily(childPage,visited,GUINode,i);
 			}
 		}		
	}
}
