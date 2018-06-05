package projectSections;

import javafx.scene.layout.Pane;

public class Chapter implements Page{
	
	public String title ="";

	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setTitle(String title) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public byte[] encode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void decode(byte[] data, int offset, int length) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Pane BuildPane() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String toString() {
		return "🗟"+title;
	}

}
