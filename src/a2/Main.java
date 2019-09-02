package a2;

import java.io.File;
import java.util.Optional;

import javafx.application.Application;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * This application is a VARpedia which gives the user the ability to
 * search a term on wikipedia and have it repeated to them through
 * a text-to-speak. This is paired with a visual of the word. The user
 * is given the choice to create creations, play and delete the creations.
 * @author cpat430
 *
 */
public class Main extends Application {

	// Create the buttons for clicking between screens
	private Button return1 = new Button("Return to Main Menu");
	private Button return2 = new Button("Return to Main Menu");
	private Button list = new Button("List Creations");
	private Button create = new Button("Create a Creation");
	private Button play = new Button("Play");
	private Button delete = new Button("Delete");

	// String to display the text in the text area
	private String[] _display;

	// a count to keep track of the number of creations
	private int _creationCount;

	// Creates the menu scene
	VBox menuBox = new VBox(new Label("Main menu"), list, new Label("Create a creation"), create);
	Scene menuScene = new Scene(menuBox, 600, 600);

	/**
	 * Start method which will initialise all the scenes and
	 * show the menu scene
	 */
	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("VARpedia");

		initialise(primaryStage);

		primaryStage.setScene(menuScene);
		primaryStage.show();
	}

	/**
	 * This initialises the application and can be used to also refresh
	 * it if need be through self-calls.
	 * @param primaryStage
	 */
	public void initialise(Stage primaryStage) {

		// create the directories if they don't already exist
		Terminal.command("mkdir -p ./Production 2> /dev/null");
		Terminal.command("mkdir -p ./creations 2> /dev/null");
		Terminal.command("touch ./Production/output_text.txt");
		Terminal.command("touch ./Production/temp_audio.wav");
		Terminal.command("touch ./Production/temp_video.mp4");

		// list of creations
		TableView<Creation> listOfCreations = new TableView<Creation>();
		listOfCreations.setPlaceholder(new Label("No exisitng creations to display"));

		TableColumn<Creation, Creation> creationNameColumn = new TableColumn<Creation, Creation>("Creation name");
		creationNameColumn.setCellValueFactory(new PropertyValueFactory<>("creationName"));

		// list of the creations
		String[] creations = new File("./creations").list();
		for (String string : creations) {
			listOfCreations.getItems().add(new Creation(string));
		}

		listOfCreations.getColumns().add(creationNameColumn);

		VBox listBox = new VBox(new Label("Select a creation to play"),
				listOfCreations, play, delete, return1);
		Scene listScene = new Scene(listBox, 600, 600);
		//----------------------------------------------------------------
		// create scene
		TextField wikiSearch = new TextField();
		Button searchButton = new Button("Search");
		TextArea wikiTextArea = new TextArea();
		TextField numberOfSentences = new TextField();
		Button sentenceButton = new Button("Okay");
		TextField creationName = new TextField();
		Button createButton = new Button("Okay");

		// disable use of buttons until the step before has been finished
		wikiTextArea.setEditable(false);
		numberOfSentences.setEditable(false);
		sentenceButton.setDisable(true);
		creationName.setEditable(false);
		createButton.setDisable(true);

		VBox createBox = new VBox(new Label("Enter a term you would like to search."), wikiSearch,
				searchButton, wikiTextArea, new Label("How many sentences would you like to hear?"),
				numberOfSentences, sentenceButton, new Label("What would you like to name your creation? "),
				creationName, createButton, new Label(), return2);
		Scene createScene = new Scene(createBox, 600,600);

		//----------------------------------------------------------------

		// returns back to main menu
		return1.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				initialise(primaryStage);
				primaryStage.setScene(menuScene);
				primaryStage.show();

			}

		});

		// returns back to main menu
		return2.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				initialise(primaryStage);
				primaryStage.setScene(menuScene);
				primaryStage.show();

			}

		});

		// Changes to the list screen
		list.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				primaryStage.setScene(listScene);
				primaryStage.show();
			}

		});

		// plays the mp4 file
		play.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				// gets the input from the selected part of the TableView
				Creation creation = listOfCreations.getSelectionModel().getSelectedItem();
				if (creation == null) {
					addAlert("You need to select a creation to play");
				} else {
					String name = creation.getCreationName();
					Terminal.command("ffplay -autoexit ./creations/" + name + " &> /dev/null");
				}
			}

		});

		// changes to the create scene
		create.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				primaryStage.setScene(createScene);
				primaryStage.show();
			}

		});

		// deletes a selected creation
		delete.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				Creation creation = listOfCreations.getSelectionModel().getSelectedItem();

				// checks if the creation is null and alert user
				if (creation == null) {
					addAlert("You need to select a creation to delete");
				} else {
					String name = creation.getCreationName();

					File file = new File("./creations/" + name);

					// ask if they are sure they want to delete
					Alert a = new Alert(Alert.AlertType.CONFIRMATION);
					String message = "Are you sure you want to delete " + name + "?";
					a.setContentText(message);

					// if they want to overwrite, then they will be prompted
					Optional<ButtonType> result = a.showAndWait();
					if (result.get() == ButtonType.OK) {

						if (file.delete()) {
							message = name + " deleted successfully";
							addAlert(message);
							listOfCreations.getItems().remove(creation);

						} else {
							message = name + " not deleted";
							addAlert(message);
						}
					}
				}
			}
		});

		// collects the wikit command search from the TextField
		searchButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				WikitWorker wikitWorker = new WikitWorker(wikiSearch.getText());

				wikitWorker.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
					@Override
					public void handle(WorkerStateEvent event) {
						wikiTextArea.clear();
						String output = " " + wikitWorker.getValue().trim();
						_display = output.split("\\.");
						String displayString = "";
						int count = 1;

						// makes the display for the numbered lines
						for (String string : _display) {
							displayString = count + "." + string + "\n";
							count++;
							wikiTextArea.appendText(displayString);
						}

						// set the sentence buttons to become editable
						// only if the sentence found is a valid search
						if (!displayString.contains(":^(")) {
							numberOfSentences.setEditable(true);
							sentenceButton.setDisable(false);

							// set the name to be set
							wikiSearch.setEditable(false);
							searchButton.setDisable(true);
						} else {
							String message = wikiSearch.getText() + " was not found, please try again";
							addAlert(message);
							wikiTextArea.clear();
						}

						_creationCount = count - 1;
					}
				});

				Thread th = new Thread(wikitWorker);
				th.start();
			}
		});

		// Collects the number from the sentence TextField
		sentenceButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				String number = numberOfSentences.getText();

				if (number == null) {
					throw new NullPointerException("Please enter a number ");
				}

				// try / catch statement to deal with non-integers and ArrayOutOfBoundsExceptions
				try {
					int integer = Integer.parseInt(number);

					if (integer > _creationCount) {
						String message = "Please enter a valid number between 1 and " + _creationCount;
						throw new ArrayIndexOutOfBoundsException(message);
					}

					String cutSentence = "";

					for (int i = 0; i < integer; i++) {
						cutSentence = cutSentence + _display[i] + ".";
					}

					String command = "echo \"" + cutSentence.trim() + "\" | tee ./Production/output_text.txt";
					Terminal.command(command);

					command = "espeak -f ./Production/output_text.txt -w ./Production/temp_audio.wav -s 130";
					Terminal.command(command);

					// enable use of the other buttons
					creationName.setEditable(true);
					createButton.setDisable(false);

				} catch (NumberFormatException e) {
					addAlert(e.getMessage());
				} catch (ArrayIndexOutOfBoundsException e) {
					addAlert(e.getMessage());
				} catch (NullPointerException e) {
					addAlert(e.getMessage());
				}
			}
		});

		// creates the creation with the given name if it exists
		// the user is given the choice to overwrite or change the name
		createButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {

				boolean create = true;
				// first we will check that the creation name doesn't exist
				String nameOfCreation = creationName.getText();

				// checks for null, empty or only spaces.
				if (nameOfCreation == null) {
					String message = "Please enter a name, cannot have an empty name";
					addAlert(message);
					throw new NullPointerException(message);
				} else if (nameOfCreation.trim() == "") {
					String message = "Cannot only be spaces";
					addAlert(message);
				}

				File tempFile = new File("./creations/" + nameOfCreation + ".mp4");

				// check if the given filename exists
				if (tempFile.exists()) {
					String message = "File name already exists, would you like to overwrite?";

					Alert a = new Alert(Alert.AlertType.CONFIRMATION);
					a.setContentText(message);

					// if they want to overwrite, then they will be prompted
					Optional<ButtonType> result = a.showAndWait();
					if (result.get() == ButtonType.OK) {
						create = true;

					} else {
						create = false;
					}
				}

				// if we will be creating or overwriting
				if (create) {

					String command1 = "ffmpeg -y -f lavfi -i color=c=blue:s=320x240:d=5 -vf \"drawtext=fontfile:fontsize=30:fontcolor=white:x=(w-text_w)/2:y=(h-text_h)/2:text=" + wikiSearch.getText() + "\" ./Production/temp_video.mp4 2> /dev/null";
					String command2 = "ffmpeg -y -i ./Production/temp_video.mp4 -i ./Production/temp_audio.wav -c:v copy -c:a aac -strict experimental ./creations/" + nameOfCreation + ".mp4 &> /dev/null";
					// create a creation worker
					creationWorker creationWorker = new creationWorker(command1,command2);

					creationWorker.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
						@Override
						public void handle(WorkerStateEvent event) {
							String message = nameOfCreation + " has been created";
							addAlert(message);
						}
					});
					// start the concurrent thread
					Thread th = new Thread(creationWorker);
					th.start();

					initialise(primaryStage);
					primaryStage.setScene(menuScene);
				}
			}
		});
	}

	/**
	 * An alert class which will alert the user with the input message
	 * @param message
	 */
	public void addAlert(String message) {
		Alert a = new Alert(Alert.AlertType.INFORMATION);
		a.setContentText(message);
		a.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}