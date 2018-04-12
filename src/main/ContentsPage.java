package main;

import java.awt.Toolkit;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import com.google.common.io.Resources;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
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
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import main.PageViewer.CustomTab;
import projectSections.*;

public class ContentsPage {
	public Label title = new Label("ℂ𝕠𝕟𝕥𝕖𝕟𝕥𝕤 ℙ𝕒𝕘𝕖");

	public TreeView<Page> tree = new TreeView<Page>();
	
	TreeItem<Page> copiedPage = null;
	
	ContentsPage(){
		tree.setRoot(new TreeItem<Page>(new Book("New Project")));
	}

	public VBox getContentsPage() {
		VBox stackPane = new VBox();
		stackPane.setPadding(new Insets(5, 5, 0, 5));
		// Title
		title.setFont(Font.font("Tahoma", FontWeight.BOLD, 20));
		// Options
		Menu pageMenu = new Menu("+🗍");
		MenuItem newPage = new MenuItem("new Page +📄");
		MenuItem newFolder = new MenuItem("new Folder +📁");
		MenuItem newNote = new MenuItem("new Note +🗒");
		newPage.setOnAction((event) ->{
			tree.getRoot().getChildren().add(new TreeItem<>(new BasicPage("New Page")));
		});		
		newFolder.setOnAction((event) ->{
			tree.getRoot().getChildren().add(new TreeItem<>(new Folder("New Folder")));
		});
		newNote.setOnAction((event) ->{
			tree.getRoot().getChildren().add(new TreeItem<>(new Note("New Note")));
		});
		
		Menu templateMenu = new Menu("+🗐");
		MenuItem characterPage = new MenuItem("Character +📃");
		MenuItem locationPage = new MenuItem("Location +📃");
		MenuItem eventPage = new MenuItem("Event +📃");
		MenuItem motifPage = new MenuItem("Motif/Symbol +📃");
		MenuItem threeActPage = new MenuItem("Three Act Structure +📃");
		MenuItem eightArcPage = new MenuItem("Eight Arc Structure +📃");
		MenuItem episodicPage = new MenuItem("Episodic Structure +📃");
		
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

		pageMenu.getItems().addAll(newPage, newFolder, newNote);
		templateMenu.getItems().addAll(characterPage,locationPage,eventPage,motifPage,threeActPage,eightArcPage,episodicPage);
		// Tree
		tree.setContextMenu(getContextMenu());
		tree.getRoot().setExpanded(true);
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
		tree.setCellFactory(new Callback<TreeView<Page>, TreeCell<Page>>() {
			@Override
			public TreeCell<Page> call(TreeView<Page> stringTreeView) {
				TreeCell<Page> treeCell = new TreeCell<Page>() {
					protected void updateItem(Page item, boolean empty) {
						super.updateItem(item, empty);
						if (item != null) {
							setText(item.toString());
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
						TreeItem<Page> targetNode = treeCell.getTreeItem();
						TreeItem<Page> priorNode = tree.getSelectionModel().getSelectedItem();

						if (targetNode !=null && priorNode != targetNode && tree.getRoot() != targetNode && !hasAncestor(targetNode,priorNode)) {
							priorNode.getParent().getChildren().remove(priorNode);
							if (Main.mouseDeltaX > 0) {
								targetNode.getChildren().add(priorNode);
								targetNode.setExpanded(true);
							}
							else {
								targetNode.getParent().getChildren().add(targetNode.getParent().getChildren().indexOf(targetNode) + 1,priorNode);
							}
							tree.getSelectionModel().select(treeCell.getTreeItem());
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
					TreeItem<Page> item = tree.getSelectionModel().getSelectedItem();
					if (item != null) 
						if (item.getParent()!=item) { // if the clicked item is not the root node
							Main.pageViewer.addTab(item);
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
				TreeItem<Page> item = tree.getSelectionModel().getSelectedItem();
				if(item != tree.getRoot()) {
					copiedPage = new TreeItem<Page>();
					deepCopy(item,copiedPage);
				}
			}
		});
		paste.setOnAction((event) ->{
			if(tree.getSelectionModel().getSelectedItem() !=null) {
				if(copiedPage != null) {
					TreeItem<Page> item = tree.getSelectionModel().getSelectedItem();
					item.getChildren().add(copiedPage);
					TreeItem<Page> oldCopy = copiedPage;
					deepCopy(oldCopy,copiedPage = new TreeItem<Page>());
				}
			}
		});
		detach.setOnAction((event)->{
			TreeItem<Page> item = tree.getSelectionModel().getSelectedItem();
	 		if(item != tree.getRoot())
	 			Main.pageViewer.detachPage(item);
		});
		
		return contextMenu;
	}
	
	class cutAction implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent event) {
			if(tree.getSelectionModel().getSelectedItem() !=null) {
				TreeItem<Page> item = tree.getSelectionModel().getSelectedItem();
				
				if (item != tree.getRoot()) {
					deepCopy(item,copiedPage = new TreeItem<Page>());
					deepDelete(item);
					if(Main.pageViewer.tabPane.getTabs().size()==0) {
						Main.pageViewer.viewerPane.setCenter(tree.getRoot().getValue().BuildPane());
					}
				}
			}
		}
	}

	class renameAction implements EventHandler<ActionEvent> {
		@SuppressWarnings("unchecked")
		@Override
		public void handle(ActionEvent event) {
			if(tree.getSelectionModel().getSelectedItem() !=null) {
				TreeItem<Page> node = tree.getSelectionModel().getSelectedItem();
	
				
				TextField field = new TextField(node.getValue().getTitle());
				StackPane pane = new StackPane(field);
				Stage renameStage = Main.createSubStage(new Scene(pane, 300, field.getMinHeight()), "Rename", Modality.APPLICATION_MODAL);
				pane.addEventHandler(KeyEvent.KEY_PRESSED, (keyEvent) ->{
					if (keyEvent.getCode() == KeyCode.ENTER) {
						Page page = node.getValue();
						page.setTitle(field.getText());
						node.setValue(null);
						node.setValue(page); //refreshing the displayed text
						Main.pageViewer.tabPane.getTabs().stream().map(t -> (CustomTab)t)
						.filter(t->Main.currentProject.pageMap.containsKey(t.pageMapKey))
						.forEach(t -> t.setText(Main.currentProject.pageMap.get(t.pageMapKey).getValue().getTitle()));
						renameStage.close();
					}
				});
				renameStage.show();
			}
		}
	}

 	public void deepDelete(TreeItem<Page> node)
 	{		
 		node.getParent().getChildren().remove(node);
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
 		node.getChildren().forEach(n ->deepDelete(n));
 	}
 	
 	public void deepCopy(TreeItem<Page> node, TreeItem<Page> clone)
 	{		
		if(node.getValue() instanceof BasicPage) 
 			clone.setValue(new BasicPage(node.getValue().getTitle(),((BasicPage)node.getValue()).htmlEditor.getHtmlText()));
 		else if (node.getValue() instanceof Note) 
 			clone.setValue(new Note(node.getValue().getTitle(),((Note)node.getValue()).textArea.getText()));
		else if (node.getValue() instanceof Book) 
 			clone.setValue(new Book(node.getValue().getTitle()));
 		else if (node.getValue() instanceof Folder) 
 			clone.setValue(new Folder(node.getValue().getTitle()));
 		for (int i = 0;i<node.getChildren().size();i++) {
	 		TreeItem<Page> childClone = new TreeItem<>();
	 		clone.getChildren().add(childClone);
	 		deepCopy(node.getChildren().get(i),childClone);
 		}
 	}
	
	public void addTemplate(String path, String title) {
		URL url = Resources.getResource(path);
		String text ="";
		try {text = Resources.toString(url, StandardCharsets.UTF_8);
		} catch (IOException e) {e.printStackTrace();}
		
		BasicPage page = new BasicPage(title,text);
		tree.getRoot().getChildren().add(new TreeItem<>(page));
	}
	
    public static <T> boolean hasAncestor(TreeItem<T> node,TreeItem<T> ancestor){
    	while(node!=null) {
    		node = node.getParent();
    		if(ancestor==node) return true;
    		else if (node == null) break;
    	}
    	return false;
    }
    
    public <T> ArrayList<Integer> getAddress(TreeItem<T> node){
    	ArrayList<Integer> address = new ArrayList<Integer>();
    	while(node!=null) {
    		if (node==node.getParent()) {
        		address.add(0,0);
    			break;
    		}
    		address.add(0,node.getParent().getChildren().indexOf(node));
    		node = node.getParent();
    	}
    	return address;
    }
    
    public static void setNode(ArrayList<Integer> address, TreeView<Page> tree, TreeItem<Page> node) {
    	if(address.size()==1 && address.get(0)==0) {
    		tree.setRoot(node);
    		return;
    	}
    	TreeItem<Page> currentNode = tree.getRoot() ;
    	for(int i=1;i<address.size()-1;i++)
    		currentNode = currentNode.getChildren().get(address.get(i));
		currentNode.getChildren().add(address.get(address.size()-1),node);
    }	
}
