package ProjectSections;
import javafx.scene.layout.Pane;

public interface PageInterface
{
	public String getTitle();
	public void setTitle(String title);
	public String getIcon();
	public byte[] encode();
	public void decode(byte[] data);
	public Pane BuildPane();
	public boolean isDetached();
	public void isDetached(boolean value);
}
