package projectSections;
import java.io.UnsupportedEncodingException;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;

import dataStructures.Tree;
import dataStructures.Tree.Node;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import main.Main;

public class Folder implements Page{
	public String title ="";

	@Override
	public byte[] encode() {
		byte[] data = Ints.toByteArray(Page.pageTypes.Folder.toInt());
		try {
			data = Bytes.concat(data,title.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return data;
	}

	@Override
	public void decode(byte[] data, int offset, int length) {
		try {
			title = new String(data, offset+4, length-4,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Pane BuildPane() {
		return new BorderPane();
	}
	
	public Pane BuildPane(Node<Page> folderNode) {
		ListView<FauxString> listView = new ListView<>();
		Button refresh = new Button("⟳");
		VBox vbox = new VBox(listView,refresh);
		
		for (Node<Page> child: folderNode.getChildren()) {
			listView.getItems().add(new FauxString(child.data.getTitle(),child));
		}			
		listView.getSelectionModel().selectedItemProperty().addListener((obsv,oldV,newV)->{
			if (newV!=null) {
				if(newV.page.hasAncestor(Main.currentProject.pageTree.getRoot())) {
					if(listView.getScene() != Main.stage.getScene())
						Main.pageViewer.detachPage(newV.page);
					else
						Main.pageViewer.addTab(newV.page);
				}
				else {
					listView.getItems().clear();
					for (Node<Page> child: folderNode.getChildren()) {
						listView.getItems().add(new FauxString(child.data.getTitle(),child));
					}	
				}
			}
		});
		
		refresh.setOnAction((event)->{
			listView.getItems().clear();
			for (Node<Page> child: folderNode.getChildren()) {
				listView.getItems().add(new FauxString(child.data.getTitle(),child));
			}	
		});
		vbox.setAlignment(Pos.TOP_CENTER);
		return vbox;
	}
	
	class FauxString{
		public Tree.Node<Page> page;
		public String string;
		FauxString(String string, Tree.Node<Page> page){
			this.string = string;
			this.page = page;
		}
		@Override
		public String toString(){
			return string;
		}
	}

	@Override
	public String getIcon() {
		// TODO Auto-generated method stub
		return "📁";
	}

	@Override
	public String getTitle() {
		return title;
	}
	
	public Folder() {this.title = "Folder";}
	public Folder(String title) {this.title = title;}

	@Override
	public void setTitle(String title) {
		this.title = title;
	}
}
