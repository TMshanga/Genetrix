package OldStuff;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.*;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class AquaChain extends Application
{	
	int crrnt=0;
	Scene[] scenes = new Scene[2];

	public static void main(String[] args)
	{
		launch(args);
	}
	@Override public void start(Stage mainStage) throws Exception
	{			
		GridPane grid = new GridPane();
		grid.setPadding(new Insets(10,10,10,10));
		grid.setVgap(8);
		grid.setHgap(8);
		
		ChoiceBox<String> menu = new ChoiceBox<String>();
		menu.getItems().addAll("Option1","Option2","Option3");
		menu.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>(){
			@Override public void changed(ObservableValue<? extends String> v, String old, String New) {System.out.println(New);}});
		GridPane.setConstraints(menu,1,0);
		
		ComboBox<String> comboBox = new ComboBox<>();
		comboBox.getItems().addAll("Op1","Op2","Op3");	
		GridPane.setConstraints(comboBox,1,1);
		
		ListView<String> listView = new ListView<String>();
		listView.getItems().addAll("Op1","Op2","Op3");	
		listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		GridPane.setConstraints(listView,1,2);
		
		TreeView<String> tree = new TreeView<String>(new TreeItem<String>());
		//tree.getChildrenUnmodifiable().add(new TreeItem<String>(""));
		tree.setShowRoot(false);
		GridPane.setConstraints(tree,1,3);
		
		Label label1 = new Label("Labelle 1");
		GridPane.setConstraints(label1,0,0);
		Label label2 = new Label("Labelle 2");
		GridPane.setConstraints(label1,0,1);
		
		grid.getChildren().addAll(label1,label2,menu,comboBox,listView,tree);
		
		mainStage.setScene(new Scene(grid,500,500));
		mainStage.setTitle("La Títle");
		mainStage.show();
	}
}