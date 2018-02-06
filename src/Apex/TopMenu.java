package Apex;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

public class TopMenu {
	
	public static MenuBar getMainMenu(){
		MenuBar  menuBar = new MenuBar();	
		
		Menu fileMenu = new Menu("File");	
		fileMenu.getItems().add(new MenuItem("New Project"));
		fileMenu.getItems().add(new SeparatorMenuItem());
		fileMenu.getItems().add(new MenuItem("Open"));
		fileMenu.getItems().add(new MenuItem("Save..."));
		fileMenu.getItems().add(new SeparatorMenuItem());
		fileMenu.getItems().add(new MenuItem("Exit"));
		
		Menu viewMenu = new Menu("View");	
		viewMenu.getItems().add(new MenuItem("Contents page"));
		viewMenu.getItems().add(new MenuItem("Recent pages  ▶"));

		menuBar.getMenus().addAll(fileMenu,viewMenu);
		
		return menuBar;
	}
	

}
