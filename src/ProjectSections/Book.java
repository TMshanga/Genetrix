package ProjectSections;

import javafx.scene.layout.Pane;

public class Book implements PageInterface {
	
	public String title ="";

	@Override
	public String encode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void decode() {
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
}
