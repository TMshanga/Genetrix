package ProjectSections;

import javafx.scene.layout.Pane;

public class Book implements PageInterface {
	
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
		return null;
	}

	@Override
	public String getIcon() {
		// TODO Auto-generated method stub
		return "🕮";
	}

	@Override
	public String getTitle() {
		return title;
	}
	
	public Book() {this.title = "Book";}
	public Book(String title) {this.title = title;}

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
