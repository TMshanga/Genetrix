package AquaticRift;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class AquaChain extends Application
{	
	Button button = new Button();

	public static void main(String[] args)
	{
		launch(args);	
	}
	@Override public void start(Stage primaScéna) throws Exception
	{
		primaScéna.setTitle("The Goddess' Castle");	
		button.setText("Please Click");
		
		//button.setOnAction(new EventHandler<ActionEvent>(){@Override public void handle(ActionEvent event) {button.setText("Thank you");}});

		StackPane layout = new StackPane();
		layout.getChildren().add(button);
		
		primaScéna.setScene(new Scene(layout, 300, 250));
		primaScéna.show();
	}
}
