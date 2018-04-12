package projectSections;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;

import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import main.Main;

public class Note implements Page{
	String title ="";	
	public TextArea textArea = new TextArea();

	@Override
	public byte[] encode() {
		byte[] data = new byte[0];
		try {
			data = title.getBytes("UTF-8");
			data = Bytes.concat(Ints.toByteArray(data.length),data);
			data = Bytes.concat(data,textArea.getText().getBytes("UTF-8"));
			data = Bytes.concat(Ints.toByteArray(Page.pageTypes.Note.toInt()),data);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return data;
	}

	@Override
	public void decode(byte[] data, int offset, int length) {
		try {
			int titleLen = ByteBuffer.wrap(data,offset+4,4).getInt();
			title = new String(data,offset+8,titleLen,"UTF-8");
			textArea.setText(new String(data,offset+titleLen+8,length-(titleLen+8),"UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	@Override public BorderPane BuildPane() {
		textArea.getStylesheets().add(Main.styleFile);
		BorderPane borderPane = new BorderPane(textArea);
		return borderPane;
	}

	@Override
	public String getTitle() {
		return title;
	}
	
	@Override
	public String toString() {
		return "🗒 " +title;
	}
	
	public Note() {
		this("Note");
	}
	public Note(String title,String content) {
		this(title);
		textArea.setText(content);
	}
	public Note(String title) {
		this.title = title;
	}

	@Override
	public void setTitle(String title) {
		this.title = title;		
	}
}
