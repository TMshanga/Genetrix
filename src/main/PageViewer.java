package main;

import java.awt.Toolkit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import projectSections.Folder;
import projectSections.Page;

public class PageViewer {
    TabPane tabPane = new TabPane();
    BorderPane viewerPane = new BorderPane();
	public Map<TreeItem<Page>,Stage> subStageMap = new HashMap<>();
	
	PageViewer(){
		viewerPane.setMinSize(100, 100);
        viewerPane.setTop(tabPane);
		tabPane.getSelectionModel().selectedItemProperty().addListener( (obsv,oldVal,newVal)->{
				if(newVal != null) { //in case of a closure
					TreeItem<Page> item = Main.currentProject.pageMap.get(((CustomTab)newVal).pageMapKey);
					if (Main.currentProject.pageMap.containsKey(((CustomTab)newVal).pageMapKey)) {
						if (ContentsPage.hasAncestor(item,Main.contentsPage.tree.getRoot())){
							newVal.setText(item.getValue().getTitle());
							dockPage(item);
						}
						else {
							tabPane.getTabs().remove(newVal);
							Main.currentProject.pageMap.remove(((CustomTab)newVal).pageMapKey);
						}
					}
					else tabPane.getTabs().remove(newVal);
				}
				else viewerPane.setCenter(Main.contentsPage.tree.getRoot().getValue().BuildPane());
			});
		tabPane.setOnMouseClicked((event)->{
			if(tabPane.getSelectionModel().getSelectedItem() != null) {
				String key = ((CustomTab)tabPane.getSelectionModel().getSelectedItem()).pageMapKey;
				String title = Main.currentProject.pageMap.get(key).getValue().getTitle();
				tabPane.getSelectionModel().getSelectedItem().setText(title);	
			}
		});
		
		viewerPane.setCenter(Main.contentsPage.tree.getRoot().getValue().BuildPane());
	}
	
	public void reset() {
		tabPane.getTabs().clear();
		viewerPane.setCenter(Main.contentsPage.tree.getRoot().getValue().BuildPane());
		subStageMap.values().forEach((s)->s.close());
		subStageMap.values().clear();
	}
	
	public void dockPage(TreeItem<Page> pageNode) {
		subStageMap.remove(pageNode);
		if(pageNode.getValue() instanceof Folder)
			viewerPane.setCenter(((Folder)pageNode.getValue()).BuildPane(pageNode));
		else
			viewerPane.setCenter(pageNode.getValue().BuildPane());
	}
	
	@SuppressWarnings("unchecked")
	public void detachPage(TreeItem<Page> pageNode){		
		if(subStageMap.containsKey(pageNode)) {
			subStageMap.get(pageNode).requestFocus();
		}
		else {
			String pageKey = Main.currentProject.pageMap.inverse().get(pageNode);
			for(Tab currentTab: tabPane.getTabs()) {
				if(((CustomTab)currentTab).pageMapKey == pageKey){
					tabPane.getTabs().remove(currentTab);
					break;
				}
			}
			
			Scene scene;
			if(pageNode.getValue() instanceof Folder)
				scene =new Scene(((Folder)pageNode.getValue()).BuildPane(pageNode), Toolkit.getDefaultToolkit().getScreenSize().getWidth()/2, Toolkit.getDefaultToolkit().getScreenSize().getHeight()/1.2);
			else
				scene =new Scene(pageNode.getValue().BuildPane(), Toolkit.getDefaultToolkit().getScreenSize().getWidth()/2, Toolkit.getDefaultToolkit().getScreenSize().getHeight()/1.2);
			
			Stage subStage = Main.createSubStage(scene, pageNode.getValue().getTitle(),Modality.NONE);
			subStage.initOwner(null); //to stop it from always being on top
			subStage.show();
			subStage.setUserData(pageNode);	
			subStage.iconifiedProperty().addListener( (obsv,oldV,newV) -> {
					if (newV) {
						PageViewer.this.addTab((TreeItem<Page>)subStage.getUserData());
						subStageMap.remove((TreeItem<Page>)subStage.getUserData());
						subStage.close();
					}
				});
			subStage.setOnCloseRequest( (event) -> {
					subStageMap.remove((TreeItem<Page>)subStage.getUserData());
				});
			
			subStageMap.put(pageNode, subStage);
			if(tabPane.getTabs().isEmpty())
				viewerPane.setCenter(Main.contentsPage.tree.getRoot().getValue().BuildPane());
		}
	}
	
	public class CustomTab extends Tab{
		public String pageMapKey;
		CustomTab(String title, TreeItem<Page> pageNode ){
			super(title);
			if (Main.currentProject.pageMap.containsValue(pageNode))
				pageMapKey = Main.currentProject.pageMap.inverse().get(pageNode);
			else
				Main.currentProject.pageMap.put(pageMapKey = UUID.randomUUID().toString(), pageNode);
			
			ContextMenu contextMenu = new ContextMenu();
			MenuItem detach = new MenuItem("Detatch 🗔");
			MenuItem moveRight = new MenuItem("Move ⇉");
			MenuItem moveLeft = new MenuItem("Move ⇇");

			detach.setOnAction((event) ->{
				if(Main.currentProject.pageMap.containsKey(pageMapKey))
					detachPage(Main.currentProject.pageMap.get(pageMapKey));
				tabPane.getTabs().remove(CustomTab.this);
			});
			moveRight.setOnAction((event) -> {
				moveTab(1);
			});
			moveLeft.setOnAction((event) ->{
				moveTab(-1);
			});
			this.setOnClosed((event) -> {		
				if(tabPane.getTabs().isEmpty())
					viewerPane.setCenter(Main.contentsPage.tree.getRoot().getValue().BuildPane());
			});
			
			setTooltip(new Tooltip("Right click to detach page"));
			contextMenu.getItems().addAll(detach,moveRight,moveLeft);	
			setContextMenu(contextMenu);
		}
		public void moveTab(int distance) {
			int index = tabPane.getTabs().indexOf(CustomTab.this);
			int size = tabPane.getTabs().size();
			tabPane.getTabs().remove(CustomTab.this);
			tabPane.getTabs().add(((index+distance)%(size)+size)%size, CustomTab.this);
		}
	}
	
	public void addTab(TreeItem<Page> pageNode) {
		boolean addTab = true;
		CustomTab tab = new CustomTab(pageNode.getValue().getTitle(),pageNode);
		for(Tab currentTab: tabPane.getTabs()) {
			if(Main.currentProject.pageMap.containsKey(((CustomTab)currentTab).pageMapKey))
				if(Main.currentProject.pageMap.get(((CustomTab)currentTab).pageMapKey)==pageNode) {
					addTab = false; 
					tab = (CustomTab)currentTab;
					break;
				}
		}
		if(addTab) {tabPane.getTabs().add(tab);}
		tabPane.getSelectionModel().select(tab);
		Main.mainStage.requestFocus();
	}

	@SuppressWarnings("unchecked")
	public void restyle(String file) {
		for (Stage stage: subStageMap.values()) {
			stage.getScene().getStylesheets().setAll(file);
			if(((TreeItem<Page>)stage.getUserData()).getValue() instanceof Folder)
				stage.setScene(new Scene((((TreeItem<Folder>)stage.getUserData()).getValue()).BuildPane((TreeItem<Page>)stage.getUserData())));
			else
				stage.setScene(new Scene(((TreeItem<Page>)stage.getUserData()).getValue().BuildPane()));
		}
		if(tabPane.getTabs().isEmpty()) {
			viewerPane.setCenter(Main.contentsPage.tree.getRoot().getValue().BuildPane());
		}
	}
}
