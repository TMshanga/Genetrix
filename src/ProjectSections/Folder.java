package projectSections;
import java.io.UnsupportedEncodingException;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import main.ContentsPage;
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
	
	public Pane BuildPane(TreeItem<Page> folderNode) {
		ListView<pageString> listView = new ListView<>();
		Button refresh = new Button("⟳");
		VBox vbox = new VBox(listView,refresh);
		
		for (TreeItem<Page> child: folderNode.getChildren()) {
			listView.getItems().add(new pageString(child));
		}			
		listView.getSelectionModel().selectedItemProperty().addListener((obsv,oldV,newV)->{
			if (newV!=null) {
				if(ContentsPage.hasAncestor(newV.page,Main.contentsPage.tree.getRoot())) {
					if(listView.getScene() != Main.stage.getScene())
						Main.pageViewer.detachPage(newV.page);
					else
						Main.pageViewer.addTab(newV.page);
				}
				else {
					listView.getItems().clear();
					for (TreeItem<Page> child: folderNode.getChildren()) {
						listView.getItems().add(new pageString(child));
					}	
				}
			}
		});
		
		refresh.setOnAction((event)->{
			listView.getItems().clear();
			for (TreeItem<Page> child: folderNode.getChildren()) {
				listView.getItems().add(new pageString(child));
			}	
		});
		vbox.setAlignment(Pos.TOP_CENTER);
		return vbox;
	}
	
	class pageString{
		public TreeItem<Page> page;
		public String string;
		pageString(TreeItem<Page> page){
			this.string = page.getValue().getTitle();
			this.page = page;
		}
		@Override
		public String toString(){
			return string;
		}
	}

	@Override
	public String getTitle() {
		return title;
	}
	
	@Override
	public String toString() {
		return "📁 " +title;
	}
	
	public Folder() {this.title = "Folder";}
	public Folder(String title) {this.title = title;}

	@Override
	public void setTitle(String title) {
		this.title = title;
	}
}
