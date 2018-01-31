package Apex;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.*;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class CrystalChain extends Application
{	
	public static void main(String[] args)
	{
		launch(args);
	}	
	@Override public void start(Stage mainStage) throws Exception
	{	
		BorderPane S_borderPane = new BorderPane();	
		initHeader();
		S_borderPane.setTop(initHeader());
		mainStage.setScene(new Scene(S_borderPane,500,500));
		mainStage.setTitle("Genetrix - Alpha");
		mainStage.show();
	}
	
	public static HBox initHeader()
	{
		HBox S_header = new HBox();
		ComboBox<String> comboBox = new ComboBox<>();
		comboBox.setPromptText("Options");
		comboBox.getItems().addAll("Op1","Op2","Op3");	
		S_header.getChildren().add(comboBox);
		return S_header;
	}
}
