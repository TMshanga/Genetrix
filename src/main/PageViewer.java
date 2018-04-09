package main;

import java.awt.Toolkit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import dataStructures.Tree.Node;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import projectSections.Folder;
import projectSections.Page;

public class PageViewer {
    TabPane tabPane = new TabPane();
    BorderPane viewerPane = new BorderPane();
	public Map<Node<Page>,Stage> subStageMap = new HashMap<>();
	
	PageViewer(){
		viewerPane.setMinSize(100, 100);
        viewerPane.setTop(tabPane);
		tabPane.getSelectionModel().selectedItemProperty().addListener( (obsv,oldVal,newVal)->{
				if(newVal != null) { //in case of a closure
					if (Main.currentProject.pageMap.containsKey(((CustomTab)newVal).pageMapKey)) {
						if (Main.currentProject.pageMap.get(((CustomTab)newVal).pageMapKey).hasAncestor(Main.currentProject.pageTree.getRoot())) {
							newVal.setText(Main.currentProject.pageMap.get(((CustomTab)newVal).pageMapKey).data.getTitle());
							dockPage(Main.currentProject.pageMap.get(((CustomTab)newVal).pageMapKey));
						}
						else {
							tabPane.getTabs().remove(newVal);
							Main.currentProject.pageMap.remove(((CustomTab)newVal).pageMapKey);
						}
					}
					else tabPane.getTabs().remove(newVal);
				}
				else viewerPane.setCenter(Main.currentProject.pageTree.getRoot().data.BuildPane());
			});
		tabPane.setOnMouseClicked((event)->{
			if(tabPane.getSelectionModel().getSelectedItem() != null) {
				String key = ((CustomTab)tabPane.getSelectionModel().getSelectedItem()).pageMapKey;
				String title = Main.currentProject.pageMap.get(key).data.getTitle();
				tabPane.getSelectionModel().getSelectedItem().setText(title);	
			}
		});
		
		viewerPane.setCenter(Main.currentProject.pageTree.getRoot().data.BuildPane());
	}
	
	public void reset() {
		tabPane.getTabs().clear();
		viewerPane.setCenter(Main.currentProject.pageTree.getRoot().data.BuildPane());
		for (Stage stage: subStageMap.values()) {
			stage.close();
		}
		subStageMap = new HashMap<>();
	}
	
	public void dockPage(Node<Page> pageNode) {
		subStageMap.remove(pageNode);
		if(pageNode.data instanceof Folder)
			viewerPane.setCenter(((Folder)pageNode.data).BuildPane(pageNode));
		else
			viewerPane.setCenter(pageNode.data.BuildPane());
	}
	
	public void detachPage(Node<Page> pageNode){		
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
			
			Stage subStage = new Stage();
			if(pageNode.data instanceof Folder)
				subStage.setScene(new Scene(((Folder)pageNode.data).BuildPane(pageNode), Toolkit.getDefaultToolkit().getScreenSize().getWidth()/2, Toolkit.getDefaultToolkit().getScreenSize().getHeight()/1.2));
			else
				subStage.setScene(new Scene(pageNode.data.BuildPane(), Toolkit.getDefaultToolkit().getScreenSize().getWidth()/2, Toolkit.getDefaultToolkit().getScreenSize().getHeight()/1.2));
			subStage.setTitle(pageNode.data.getTitle());
			subStage.getScene().getStylesheets().add(Main.styleFile);
			subStage.show();
			subStage.setUserData(pageNode);	
			subStage.iconifiedProperty().addListener(new ChangeListener<Boolean>() {
				@SuppressWarnings("unchecked")
				@Override public void changed(ObservableValue<? extends Boolean> observable, Boolean oldVal, Boolean minimised) {
					if (minimised) {
						PageViewer.this.addTab((Node<Page>)subStage.getUserData());
						subStageMap.remove((Node<Page>)subStage.getUserData());
						subStage.close();
					}
				}});
			subStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				@SuppressWarnings("unchecked")
				@Override public void handle(WindowEvent event) {
					subStageMap.remove((Node<Page>)subStage.getUserData());
				}});
			
			subStageMap.put(pageNode, subStage);
			if(tabPane.getTabs().isEmpty())
				viewerPane.setCenter(Main.currentProject.pageTree.getRoot().data.BuildPane());
		}
	}
	
	class CustomTab extends Tab{
		public String pageMapKey;
		CustomTab(String title, Node<Page> pageNode ){
			super(title);
			if (Main.currentProject.pageMap.containsValue(pageNode))
				pageMapKey = Main.currentProject.pageMap.inverse().get(pageNode);
			else
				Main.currentProject.pageMap.put(pageMapKey = UUID.randomUUID().toString(), pageNode);
			setTooltip(new Tooltip("Right click to detach page"));
			
			ContextMenu contextMenu = new ContextMenu();
			MenuItem detach = new MenuItem("Detatch 🗔");
			MenuItem moveRight = new MenuItem("Move ⇉");
			MenuItem moveLeft = new MenuItem("Move ⇇");

			detach.setOnAction(new EventHandler<ActionEvent>() {
				@Override public void handle(ActionEvent event) {
					if(Main.currentProject.pageMap.containsKey(pageMapKey))
						detachPage(Main.currentProject.pageMap.get(pageMapKey));
					tabPane.getTabs().remove(CustomTab.this);
				}});
			moveRight.setOnAction(new EventHandler<ActionEvent>() {
				@Override public void handle(ActionEvent event) {
					int index = tabPane.getTabs().indexOf(CustomTab.this);
					int size = tabPane.getTabs().size();
					tabPane.getTabs().remove(CustomTab.this);
					tabPane.getTabs().add((index+1)%(size), CustomTab.this);
				}});
			moveLeft.setOnAction(new EventHandler<ActionEvent>() {
				@Override public void handle(ActionEvent event) {	
					int index = tabPane.getTabs().indexOf(CustomTab.this);
					int size = tabPane.getTabs().size();
					tabPane.getTabs().remove(CustomTab.this);
					tabPane.getTabs().add(((index-1)%(size)+size)%size, CustomTab.this);
				}});
			this.setOnClosed((event) -> {		
				if(tabPane.getTabs().isEmpty())
					viewerPane.setCenter(Main.currentProject.pageTree.getRoot().data.BuildPane());
			});
			
			contextMenu.getItems().addAll(detach,moveRight,moveLeft);	
			setContextMenu(contextMenu);
		}	
	}
	
	public void addTab(Node<Page> pageNode) {
		boolean addTab = true;
		Tab tab = new CustomTab(pageNode.data.getTitle(),pageNode);
		for(Tab currentTab: tabPane.getTabs()) {
			if(Main.currentProject.pageMap.containsKey(((CustomTab)currentTab).pageMapKey))
				if(Main.currentProject.pageMap.get(((CustomTab)currentTab).pageMapKey)==pageNode) {
					addTab = false; 
					tab = currentTab;
					break;
				}
		}
		if(addTab) tabPane.getTabs().add(tab);
		tabPane.getSelectionModel().select(tab);
		Main.stage.requestFocus();
	}
	@SuppressWarnings("unchecked")
	public void restyle(String file) {
		for (Stage stage: subStageMap.values()) {
			stage.getScene().getStylesheets().setAll(file);
			if(((Node<Folder>)stage.getUserData()).data instanceof Folder)
				stage.setScene(new Scene((((Node<Folder>)stage.getUserData()).data).BuildPane((Node<Page>)stage.getUserData())));
			else
				stage.setScene(new Scene(((Node<Page>)stage.getUserData()).data.BuildPane()));
		}
		if(tabPane.getTabs().isEmpty()) {
			viewerPane.setCenter(Main.currentProject.pageTree.getRoot().data.BuildPane());
		}
	}
}
