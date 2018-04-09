package main;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.commons.io.Charsets;

import com.google.common.io.Resources;

import dataStructures.Tree;
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
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
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
import javafx.scene.input.MouseDragEvent;
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
import main.PageViewer.CustomTab;
import projectSections.*;

public class ContentsPage {
	public Label title = new Label("ℂ𝕠𝕟𝕥𝕖𝕟𝕥𝕤 ℙ𝕒𝕘𝕖");

	TreeView<String> tree = new TreeView<String>();

	MyTreeItem<String> root = new MyTreeItem<String>();
	MyTreeItem<String> newPosItem = new MyTreeItem<String>("New Position");
	
	Tree.Node<Page> copiedPage = null;

	public VBox getContentsPage() {
		VBox stackPane = new VBox();
		stackPane.setPadding(new Insets(5, 5, 0, 5));
		// Title
		title.setFont(Font.font("Tahoma", FontWeight.BOLD, 20));
		// Options
		Menu pageMenu = new Menu("+🗍");
		MenuItem newPage = new MenuItem("new Page +📝");
		newPage.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				Main.currentProject.pageTree.root.add(new BasicPage("New Page"));
				reassembleTreeView(Main.currentProject);
			}
		});
		Menu templateMenu = new Menu("+🗐");
		MenuItem characterPage = new MenuItem("Character +📄");
		MenuItem locationPage = new MenuItem("Location +📄");
		MenuItem eventPage = new MenuItem("Event +📄");
		MenuItem motifPage = new MenuItem("Motif/Symbol +📄");
		MenuItem threeActPage = new MenuItem("Three Act Structure +📄");
		MenuItem eightArcPage = new MenuItem("Eight Arc Structure +📄");
		MenuItem episodicPage = new MenuItem("Episodic Structure +📄");
		
		characterPage.setOnAction( (event) ->{
			addTemplate("templates/Character.htm","New Character");
		});
		locationPage.setOnAction( (event) ->{
			addTemplate("templates/Location.htm","New Location");
		});
		eventPage.setOnAction( (event) ->{
			addTemplate("templates/Event.htm","New Event");
		});
		motifPage.setOnAction( (event) ->{
			addTemplate("templates/SymbolMotif.htm","New Symbol/Motif");
		});
		threeActPage.setOnAction( (event) ->{
			addTemplate("templates/ThreeActStructure.htm","New Three Act Structure");
		});
		eightArcPage.setOnAction( (event) ->{
			addTemplate("templates/EightArcStructure.htm","New Eight Arc Structure");
		});
		episodicPage.setOnAction( (event) ->{
			addTemplate("templates/EpisodicStructure.htm","New Episodic Structure");
		});

		MenuItem newFolder = new MenuItem("new Folder +📁");
		newFolder.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				Main.currentProject.pageTree.root.add(new Folder("New Folder"));
				reassembleTreeView(Main.currentProject);
			}
		});

		pageMenu.getItems().addAll(newPage, newFolder);
		templateMenu.getItems().addAll(characterPage,locationPage,eventPage,motifPage,threeActPage,eightArcPage,episodicPage);
		// Tree
		reassembleTreeView(Main.currentProject);
		tree.setShowRoot(false);
		tree.setContextMenu(getContextMenu());
		// Dragging
		tree.setOnDragDetected(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				Dragboard db = tree.startDragAndDrop(TransferMode.ANY);
				ClipboardContent content = new ClipboardContent();
				content.putString("DRAG");
				
				db.setContent(content);
				event.consume();
			}
		});
		tree.setOnDragOver(new EventHandler <DragEvent>() {
			@Override public void handle(DragEvent event) {
                if (event.getGestureSource() != tree.getSelectionModel().getSelectedItem() && event.getDragboard().hasString()) {
                    event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                }	            
                event.consume();
			}});
		tree.setCellFactory(new Callback<TreeView<String>, TreeCell<String>>() {
			@Override
			public TreeCell<String> call(TreeView<String> stringTreeView) {
				TreeCell<String> treeCell = new TreeCell<String>() {
					protected void updateItem(String item, boolean empty) {
						super.updateItem(item, empty);
						if (item != null) {
							setText(item);
						}
						if (empty) {
							setText(null);
							setGraphic(null);
						}
					}
				};
				treeCell.setOnDragDropped(new EventHandler<DragEvent>() {
					@Override
					public void handle(DragEvent arg0) {
						MyTreeItem item = (MyTreeItem) treeCell.getTreeItem();

						Tree.Node<Page> priorNode = Main.currentProject.pageTree.getNode(((MyTreeItem) tree.getSelectionModel().getSelectedItem()).internalAddress);
						Tree.Node<Page> targetNode = Main.currentProject.pageTree.getNode(item.internalAddress);

						if (priorNode != targetNode && root != item && !targetNode.hasAncestor(priorNode)) {
							priorNode.getParent().remove(priorNode);
							if (Main.mouseDeltaX > 0)
								targetNode.add(priorNode);
							else
								targetNode.getParent().shiftAdd(priorNode,
										targetNode.getParent().getChildren().indexOf(targetNode) + 1);

							tree.getSelectionModel().select(treeCell.getTreeItem());
							reassembleTreeView(Main.currentProject);
						}
					}
				});
				return treeCell;
			}
		});	
		// Click Operations
		tree.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				if (mouseEvent.getClickCount() == 2) {
					MyTreeItem<String> item = (MyTreeItem<String>) tree.getSelectionModel().getSelectedItem();
					if (item != null) 
						if (item.internalAddress.size() > 1) { // if the clicked item is not the root node
							Tree.Node<Page> pageNode = Main.currentProject.pageTree.getNode(item.internalAddress);
							if(Main.pageViewer.subStageMap.containsKey(pageNode))
								Main.pageViewer.projectPage(pageNode);
							else
								Main.pageViewer.addTab(pageNode);
						}
				}
			}
		});
		// Final
		stackPane.setAlignment(Pos.TOP_CENTER);
		tree.setMinSize(0, Toolkit.getDefaultToolkit().getScreenSize().getHeight()/1.2);
		stackPane.getChildren().addAll(title, new Separator(), new MenuBar(pageMenu,templateMenu), tree);
		return stackPane;
	}

	public static class MyTreeItem<T> extends TreeItem<T> {
		public ArrayList<Integer> internalAddress;

		public MyTreeItem() {
			super();
			internalAddress = new ArrayList<Integer>();
		}

		public MyTreeItem(T value) {
			super();
			setValue(value);
			internalAddress = new ArrayList<Integer>();
		}

		public MyTreeItem<T> branch(T value) {
			MyTreeItem<T> item = new MyTreeItem<T>(value);
			item.setExpanded(true);
			getChildren().add(item);
			return item;
		}
	}

	public void reassembleTreeView(Project project) {
		root = new MyTreeItem<String>();
		tree.setRoot(root);
		depthFirstAssembily(project.pageTree.root, new ArrayList<Tree.Node<Page>>(), root, 0);
	}

	private void depthFirstAssembily(Tree.Node<Page> pageNode, ArrayList<Tree.Node<Page>> visited,
			MyTreeItem<String> ParentGUINode, int childArrayIndex) {
		MyTreeItem<String> GUINode = ParentGUINode.branch(pageNode.data.getIcon() + " " + pageNode.data.getTitle());
		GUINode.internalAddress.addAll(ParentGUINode.internalAddress);
		GUINode.internalAddress.add(childArrayIndex);
		if (visited == null)
			visited = new ArrayList<Tree.Node<Page>>();
		visited.add(pageNode);
		for (int i = 0; i < pageNode.getChildren().size(); i++) {
			Tree.Node<Page> childPage = pageNode.getChildren().get(i);
			if (childPage != null && !visited.contains(childPage)) {
				depthFirstAssembily(childPage, visited, GUINode, i);
			}
		}
	}

	public ContextMenu getContextMenu() {

		ContextMenu contextMenu = new ContextMenu();
		MenuItem cut = new MenuItem("Cut");
		MenuItem copy = new MenuItem("Copy");
		MenuItem paste = new MenuItem("Paste");
		MenuItem rename = new MenuItem("Rename");
		MenuItem detach = new MenuItem("Detach 🗔");
		contextMenu.getItems().addAll(cut, copy, paste, new SeparatorMenuItem(), rename,new SeparatorMenuItem(),detach);
		cut.setOnAction(new cutAction());
		rename.setOnAction(new renameAction());
		copy.setOnAction((event) ->{
			if(tree.getSelectionModel().getSelectedItem() !=null) {
				MyTreeItem item = (MyTreeItem) tree.getSelectionModel().getSelectedItem();
				Tree.Node<Page> node = (Tree.Node<Page>) Main.currentProject.pageTree.getNode(item.internalAddress);
				if(node != Main.currentProject.pageTree.root) {
					copiedPage = new Tree.Node<Page>();
					deepCopy(node,copiedPage);
				}
			}
		});
		paste.setOnAction((event) ->{
			if(tree.getSelectionModel().getSelectedItem() !=null) {
				if(copiedPage != null) {
					MyTreeItem item = (MyTreeItem) tree.getSelectionModel().getSelectedItem();
					Tree.Node<Page> node = (Tree.Node<Page>) Main.currentProject.pageTree.getNode(item.internalAddress);
					node.add(copiedPage);
					Tree.Node<Page> oldCopy = copiedPage;
					deepCopy(oldCopy,copiedPage = new Tree.Node<Page>());
					reassembleTreeView(Main.currentProject);
				}
			}
		});
		detach.setOnAction((event)->{
			MyTreeItem item = (MyTreeItem) tree.getSelectionModel().getSelectedItem();
			Tree.Node<Page> node = (Tree.Node<Page>) Main.currentProject.pageTree.getNode(item.internalAddress);
	 		Main.pageViewer.projectPage(node);
		});
		
		return contextMenu;
	}
	
	class cutAction implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent event) {
			if(tree.getSelectionModel().getSelectedItem() !=null) {
				MyTreeItem item = (MyTreeItem) tree.getSelectionModel().getSelectedItem();
				System.out.println(item.toString());
				Tree.Node<Page> node = (Tree.Node<Page>) Main.currentProject.pageTree.getNode(item.internalAddress);
				if (node != Main.currentProject.pageTree.root) {
					deepCopy(node,copiedPage = new Tree.Node<Page>());
					deepDelete(node);
					if(Main.pageViewer.tabPane.getTabs().size()==0) {
						Main.pageViewer.viewerPane.setCenter(Main.currentProject.pageTree.root.data.BuildPane());
					}
					reassembleTreeView(Main.currentProject);
				}
			}
		}
	}

	class renameAction implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent event) {
			if(tree.getSelectionModel().getSelectedItem() !=null) {
				MyTreeItem item = (MyTreeItem) tree.getSelectionModel().getSelectedItem();
				System.out.println(item.toString());
				Tree.Node<Page> node = (Tree.Node<Page>) Main.currentProject.pageTree.getNode(item.internalAddress);
	
				Stage renameStage = new Stage();
				renameStage.setTitle("Rename");
				renameStage.initOwner(Main.stage);
				TextField field = new TextField();
				StackPane pane = new StackPane(field);
				pane.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
					@Override
					public void handle(KeyEvent t) {
						if (t.getCode() == KeyCode.ENTER) {
							node.data.setTitle(field.getText());
							;
							reassembleTreeView(Main.currentProject);
							renameStage.close();
						}
					}
				});
				renameStage.setScene(new Scene(pane, 300, field.getMinHeight()));
				renameStage.initModality(Modality.APPLICATION_MODAL);
				renameStage.requestFocus();
				renameStage.show();
			}
		}
	}

 	public void deepDelete(Tree.Node<Page> node)
 	{		
 		node.removeSelf();
 		if(Main.pageViewer.subStageMap.containsKey(node)) {
 			Main.pageViewer.subStageMap.get(node).close();
 		}
 		for(Tab tab : Main.pageViewer.tabPane.getTabs()) {
 			if(Main.currentProject.pageMap.containsKey(((CustomTab)tab).pageMapKey)){
	 			if (Main.currentProject.pageMap.get(((CustomTab)tab).pageMapKey) == node) {
	 					Main.pageViewer.tabPane.getTabs().remove(tab);
	 					break;
	 			}
 			}
 		}
 		Main.currentProject.pageMap.inverse().remove(node);
 		for (int i = 0;i<node.getChildren().size();i++) {
	 		deepDelete(node.getChildren().get(i));
 		}
 	}
 	
 	public void deepCopy(Tree.Node<Page> node, Tree.Node<Page> clone)
 	{		
		if(node.data instanceof BasicPage) 
 			clone.data = new BasicPage(node.data.getTitle(),((BasicPage)node.data).content);
 		else if (clone.data instanceof Book) 
 			clone.data = new Book(node.data.getTitle());
 		else if (clone.data instanceof Folder) 
 			clone.data = new Folder(node.data.getTitle());
 		for (int i = 0;i<node.getChildren().size();i++) {
	 		Tree.Node<Page> childClone = new Tree.Node<>();
	 		clone.add(childClone);
	 		deepCopy(node.getChildren().get(i),childClone);
 		}
 	}
	
	public void addTemplate(String path, String title) {
		URL url = Resources.getResource(path);
		String text ="";
		try {text = Resources.toString(url, StandardCharsets.UTF_8);
		} catch (IOException e) {e.printStackTrace();}
		
		BasicPage page = new BasicPage(title,text);
		Main.currentProject.pageTree.root.add(page);
		reassembleTreeView(Main.currentProject);
	}
}
