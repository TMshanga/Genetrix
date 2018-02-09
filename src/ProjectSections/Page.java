package ProjectSections;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

public class Page implements PageInterface{
	public String content = "ABCDEFG";
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
		return new BorderPane();
	}

	@Override
	public String getIcon() {
		// TODO Auto-generated method stub
		return "📝";
	}

	@Override
	public String getTitle() {
		return title;
	}
	
	public Page() {this.title = "Book";}
	public Page(String title) {this.title = title;}

	@Override
	public void setTitle(String title) {
		this.title = title;		
	}
}
