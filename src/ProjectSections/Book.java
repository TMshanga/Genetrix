package projectSections;

import java.io.UnsupportedEncodingException;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;

import javafx.scene.control.ColorPicker;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.HTMLEditor;
import main.Main;

public class Book implements Page {
	
	public String title ="";

	@Override
	public byte[] encode() {
		byte[] data = Ints.toByteArray(Page.pageTypes.Book.toInt());
		try {
			data = Bytes.concat(data,title.getBytes("UTF-16"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return data;
	}

	@Override
	public void decode(byte[] data, int offset, int length) {
		try {
			title = new String(data, offset+4, length-4,"UTF-16");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	@Override public BorderPane BuildPane() {
		BorderPane borderPane = new BorderPane();

	    HTMLEditor htmlEditor = new HTMLEditor();
	    ToolBar bar = (ToolBar)htmlEditor.lookup(".top-toolbar");
	    MenuBar menuBar = new MenuBar(new Menu("Link..."),new Menu("Spelling..."),new Menu("Image..."));
	    bar.getItems().addAll(menuBar,new ColorPicker());
	    
		if (Main.styleFile.contains("modena_dark.css")) {
			htmlEditor.setHtmlText("<style>body {background-color: rgb(20, 20, 20);</style>");
		}	
		else if (Main.styleFile.contains("vincent.css")) {
			htmlEditor.setHtmlText("<style>body {background-color: rgb(0, 0, 0);</style>");
		}	
		htmlEditor.setDisable(true);
		borderPane.setCenter(htmlEditor); 
		return borderPane;
	}

	@Override
	public String getTitle() {
		return title;
	}
	
	@Override
	public String toString() {
		return "🕮 " +title;
	}
	
	public Book() {this.title = "Book";}
	public Book(String title) {this.title = title;}

	@Override
	public void setTitle(String title) {
		this.title = title;		
	}
}
