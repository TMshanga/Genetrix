package Apex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import DataStructures.Tree.Node;
import ProjectSections.PageInterface;
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

public class PageViewer {
    TabPane tabPane = new TabPane();
    BorderPane viewerPane = new BorderPane();
	public Map<Node<PageInterface>,Stage> subStageMap = new HashMap<>();

	PageViewer(){
		viewerPane.setMinSize(100, 100);
        viewerPane.setTop(tabPane);
		tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>(){
			@Override public void changed(ObservableValue<? extends Tab> observable, Tab oldVal, Tab newVal) {
				if(newVal != null) { //in case of a closure
					if (Main.currentProject.pageMap.containsKey(((CustomTab)newVal).pageMapKey)) {
						if (Main.currentProject.pageMap.get(((CustomTab)newVal).pageMapKey).hasAncestor(Main.currentProject.pageTree.root)) {
							newVal.setText(Main.currentProject.pageMap.get(((CustomTab)newVal).pageMapKey).data.getTitle());
							displayPage(Main.currentProject.pageMap.get(((CustomTab)newVal).pageMapKey));
						}
						else {
							tabPane.getTabs().remove(newVal);
							Main.currentProject.pageMap.remove(((CustomTab)newVal).pageMapKey);
						}
					}
					else tabPane.getTabs().remove(newVal);
				}
				else viewerPane.setCenter(null);
			}});
	}
	public void displayPage(Node<PageInterface> pageNode) {
		pageNode.data.isDetached(false);
		subStageMap.remove(pageNode);
		viewerPane.setCenter(pageNode.data.BuildPane());
	}
	
	public void projectPage(Node<PageInterface> pageNode){
		
		if(subStageMap.containsKey(pageNode)) {
			subStageMap.get(pageNode).requestFocus();
		}
		else {
			pageNode.data.isDetached(true);
			Long pageKey = Main.currentProject.pageMap.inverse().get(pageNode);
			for(Tab currentTab: tabPane.getTabs()) {
				if(((CustomTab)currentTab).pageMapKey == pageKey){
					tabPane.getTabs().remove(currentTab);
					break;
				}
			}
			
			Stage subStage = new Stage();
			subStage.setScene(new Scene(pageNode.data.BuildPane(), 500, 500));
			subStage.setTitle(pageNode.data.getTitle());
			subStage.show();
			subStage.setUserData(pageNode);	
			subStage.iconifiedProperty().addListener(new ChangeListener<Boolean>() {
				@Override public void changed(ObservableValue<? extends Boolean> observable, Boolean oldVal, Boolean minimised) {
					if (minimised) {
						PageViewer.this.addTab((Node<PageInterface>)subStage.getUserData());
						subStageMap.remove((Node<PageInterface>)subStage.getUserData());
						subStage.close();
					}
				}});
			subStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				@Override public void handle(WindowEvent event) {
					subStageMap.remove((Node<PageInterface>)subStage.getUserData());
				}});
			
			subStageMap.put(pageNode, subStage);
		}
	}
	
	class CustomTab extends Tab{
		public Long pageMapKey;
		CustomTab(String title, Node<PageInterface> pageNode ){
			super(title);
			if (Main.currentProject.pageMap.containsValue(pageNode))
				pageMapKey = Main.currentProject.pageMap.inverse().get(pageNode);
			else
				Main.currentProject.pageMap.put(pageMapKey = (++Main.currentProject.nextKey), pageNode);
			setTooltip(new Tooltip("Right click to detach page"));
			
			ContextMenu contextMenu = new ContextMenu();
			MenuItem detach = new MenuItem("Detatch 🗔");
			MenuItem moveRight = new MenuItem("Move ⇉");
			MenuItem moveLeft = new MenuItem("Move ⇇");

			detach.setOnAction(new EventHandler<ActionEvent>() {
				@Override public void handle(ActionEvent event) {
					if(Main.currentProject.pageMap.containsKey(pageMapKey))
						projectPage(Main.currentProject.pageMap.get(pageMapKey));
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
			contextMenu.getItems().addAll(detach,moveRight,moveLeft);	
			setContextMenu(contextMenu);
		}	
	}
	
	public void addTab(Node<PageInterface> pageNode) {
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
}
