package ProjectSections;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

public class Folder implements PageInterface{
	public String content = "ABCDEFG";
	public String title ="";

	@Override
	public byte[] encode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void decode(byte[] data) {
		// TODO Auto-generated method stub
	}

	@Override
	public Pane BuildPane() {
		// TODO Auto-generated method stub
		return new BorderPane();
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

	@Override
	public boolean isDetached() {
		return false;
	}

	@Override
	public void isDetached(boolean value) {}
}
