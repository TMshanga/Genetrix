package Apex;

import java.awt.MouseInfo;
import java.awt.Point;
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
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import DataStructures.Tree;


public class ContentsPage {
	TreeView<String> tree = new TreeView<String>();	
	
	MyTreeItem<String> root = new MyTreeItem<String>();
	MyTreeItem<String> newPosItem= new MyTreeItem<String>("New Position");

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
			Menu pageMenu = new Menu("+🗍");	
			MenuItem  newPage  = new MenuItem("new Page +📝");
			newPage.setOnAction(new EventHandler<ActionEvent>() {        
				 @Override public void handle(ActionEvent event) {
					Main.currentProject.pageTree.root.add(new Page("New Page"));
				    depthFirstAssembily(Main.currentProject); }});
			MenuItem  newFolder  = new MenuItem("new Folder +📁");
			newFolder.setOnAction(new EventHandler<ActionEvent>() {        
				 @Override public void handle(ActionEvent event) {
					Main.currentProject.pageTree.root.add(new Folder("New Folder"));
				    depthFirstAssembily(Main.currentProject); }});
			
			pageMenu.getItems().addAll(newPage,newFolder);
			menuBar.getMenus().addAll(pageMenu);
		//Tree
			depthFirstAssembily(Main.currentProject);	
			tree.setShowRoot(false);
			tree.setContextMenu(getContextMenu());	
		//RuPaul's Drag Race
			tree.setOnDragDetected(new EventHandler <MouseEvent>() {
				@Override public void handle(MouseEvent event) {
	                //System.out.println("onDragDetected");
	                Dragboard db = tree.startDragAndDrop(TransferMode.ANY);
	                ClipboardContent content = new ClipboardContent();
	                content.putString("DRAG");
	                db.setContent(content);
	                event.consume();
				}});
			//tree.setOnMouseDragOver(new EventHandler<MouseDragEvent>(){});
			tree.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
			    @Override
			    public void handle(MouseEvent mouseEvent) {
			    	
			    //	VisualTreeHelper v = new VisualTreeHelper();
			       // System.out.println("mouse click detected! " + mouseEvent.getTarget());
			    }
			});
			
			   tree.setCellFactory(new Callback<TreeView<String>, TreeCell<String>>() {
			        @Override
			        public TreeCell<String> call(TreeView<String> stringTreeView) {
			            TreeCell<String> treeCell = new TreeCell<String>(){
			                protected void updateItem(String item, boolean empty) {
			                    super.updateItem(item, empty);
			                    if (item != null) {
			                        setText(item);
			                    }
			                    if(empty){
			                    	setText(null);
			                    	setGraphic(null);
			                    }
			                    }};
			            treeCell.setOnDragDropped(new EventHandler<DragEvent>() {
							@Override
							public void handle(DragEvent arg0) {	
									MyTreeItem item = (MyTreeItem)treeCell.getTreeItem();
									
									Tree.Node<PageInterface> priorNode = Main.currentProject.pageTree.getNode(((MyTreeItem)tree.getSelectionModel().getSelectedItem()).internalAddress);
									Tree.Node<PageInterface> targetNode = Main.currentProject.pageTree.getNode(item.internalAddress);
									
									if(priorNode!=targetNode && root != item) {
										priorNode.getParent().remove(priorNode);
										if (Main.mousePosChangeX>0)
											targetNode.add(priorNode);
										else targetNode.getParent().shiftAdd(priorNode,targetNode.getParent().getChildren().indexOf(targetNode)+1);
										
										tree.getSelectionModel().select(treeCell.getTreeItem());
										depthFirstAssembily(Main.currentProject);
									}}});		            
			            return treeCell;
			        }
			    });
			
			tree.setOnDragOver(new EventHandler <DragEvent>() {
				@Override public void handle(DragEvent event) {
	              //  System.out.println("onDragOver_");
	                //System.out.println(event.getTarget());
	                //System.out.println(;  + "_");
	                MyTreeItem t = new MyTreeItem();	
	                if (event.getGestureSource() != tree.getSelectionModel().getSelectedItem() && event.getDragboard().hasString()) {
	                    event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
	                }	            
	                event.consume();
				}});
			tree.setOnDragEntered(new EventHandler <DragEvent>() {
				@Override public void handle(DragEvent event) {
	                /*System.out.println("onDragEntered");
	                if (event.getGestureSource() != tree.getSelectionModel().getSelectedItem() && event.getDragboard().hasString()) {
	                	//tree.getSelectionModel().getSelectedItem().setValue("GREEN");
	                }
	                event.consume();*/
				}});
			tree.setOnDragExited(new EventHandler <DragEvent>() {
				@Override public void handle(DragEvent event) {
					//tree.getSelectionModel().getSelectedItem().setValue("BLACK");
	               /* event.consume();	*/
				}});
			tree.setOnDragDropped(new EventHandler <DragEvent>() {
				@Override public void handle(DragEvent event) {
	               /* System.out.println("onDragDropped");
	                Dragboard db = event.getDragboard();
	                boolean success = false;
	                if (db.hasString()) {
	                	//tree.getSelectionModel().getSelectedItem().setValue(db.getString());
	                    success = true;
	                }
	                event.setDropCompleted(success);    
	                event.consume();*/
				}});
			tree.setOnDragDone(new EventHandler <DragEvent>() {
				@Override public void handle(DragEvent event) {
	               /* System.out.println("onDragDone");
	                event.consume();*/
				}});
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
	
	class itemClickListener implements ChangeListener{
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
	
	public void depthFirstAssembily(Project project){
		root = new MyTreeItem<String>();
		tree.setRoot(root);
		depthFirstAssembily(project.pageTree.root,new ArrayList<Tree.Node<PageInterface>>(), root,0);
	}	
	private void depthFirstAssembily(Tree.Node<PageInterface> pageNode, ArrayList<Tree.Node<PageInterface>> visited, MyTreeItem<String> ParentGUINode, int childArrayIndex) {
		MyTreeItem<String> GUINode =  ParentGUINode.branch(pageNode.data.getIcon() + " " + pageNode.data.getTitle() + "                                 \t");
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
	
	public ContextMenu getContextMenu(){
		final ContextMenu contextMenu = new ContextMenu();
		MenuItem cut = new MenuItem("Cut");
		MenuItem copy = new MenuItem("Copy");
		MenuItem paste = new MenuItem("Paste");
		MenuItem rename = new MenuItem("Rename");
		contextMenu.getItems().addAll(cut, copy, paste,new SeparatorMenuItem(),rename);
		cut.setOnAction(new cutAction());
		rename.setOnAction(new renameAction());
		return contextMenu;
	}
	
	class cutAction implements EventHandler<ActionEvent>{
	    @Override
	    public void handle(ActionEvent event) {
	    	MyTreeItem item = (MyTreeItem)tree.getSelectionModel().getSelectedItem();
	    	System.out.println(item.toString());
			Tree.Node<PageInterface> node = (Tree.Node<PageInterface>)Main.currentProject.pageTree.getNode(item.internalAddress);
			node.getParent().remove(node);
			depthFirstAssembily(Main.currentProject);
	    }
	}	
	class renameAction implements EventHandler<ActionEvent>{
	    @Override public void handle(ActionEvent event) {
	    	MyTreeItem item = (MyTreeItem)tree.getSelectionModel().getSelectedItem();
	    	System.out.println(item.toString());
			Tree.Node<PageInterface> node = (Tree.Node<PageInterface>)Main.currentProject.pageTree.getNode(item.internalAddress);			

			Stage renameStage = new Stage();
			renameStage.setTitle("Rename");
			renameStage.initOwner(Main.stage);
			TextField  field = new TextField();
			StackPane pane = new StackPane(field);
			pane.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>(){
			        @Override public void handle(KeyEvent t) {
			          if(t.getCode()==KeyCode.ENTER)
			          {
			  			node.data.setTitle(field.getText());;
						depthFirstAssembily(Main.currentProject);	  
			           renameStage.close();
			          }}});
			renameStage.setScene(new Scene(pane,300,field.getMinHeight()));
			renameStage.initModality(Modality.APPLICATION_MODAL);
			renameStage.requestFocus();
			renameStage.show();			
	    }
	}
}
