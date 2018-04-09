package projectSections;
import javafx.scene.layout.Pane;

public interface Page
{
	public static enum pageTypes {
	    BasicPage(0), Folder(1), Book(2);    
	    private int intVal;
	    pageTypes(int intVal) {
	        this.intVal = intVal;
	    }
	    public int toInt() {
	        return intVal;
	    }
	}
	
	public String getTitle();
	public void setTitle(String title);
	public String getIcon();
	public byte[] encode();
	public void decode(byte[] data, int offset, int length);
	public Pane BuildPane();
}
