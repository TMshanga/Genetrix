package ProjectSections;
import javafx.scene.layout.Pane;

public interface PageInterface
{
	public String getTitle();
	public String getIcon();
	public String encode();
	public void decode();
	public Pane BuildPane();
}
