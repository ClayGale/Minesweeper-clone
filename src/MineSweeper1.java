

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MineSweeper1 extends Application {
	
	static int fieldWidth = 8; // specified field width - tied to difficulty 
	static int fieldHeight = 8; // specified field height - tied to difficulty 
	static int totalMines = 10; // specified mine count - tied to difficulty
	static boolean minesOut = false; // states whether mines have been placed yet. Set to true once the first mine has been placed
	static boolean gameOver = false;
	static boolean largeBoardNeeded; //Used for determining if scrollbars need to be used for the minePane
	
	static int currentDifficulty = 0; // used for determining what value to write the highscore to
	
	static int highscores[] = {100, 400, 999}; //Initialization values of highscore. These are replaced if a player scores better
	static String highscoreNames[] = {"","",""};
	
	static Text minesRemaining = new Text(String.format("%03d", totalMines));
	static Text timer = new Text("000"); //Header Components
	
	static Timeline timeline = new Timeline(new KeyFrame(Duration.millis(1000), ae -> 
	timer.setText(String.format("%03d", Integer.parseInt(timer.getText()) + 1)))); //timeline being declared globally to allows it to start after the first click	
	
	static Image blankTile = new Image("/cover.png"); // When I showed you(Ken) this in lab these image arrays were declared in the MineTile class
	static Image flag = new Image("/flag.png");       // Moving them to this class creates a massive performance and load time boost since they are no longer being loaded per tile.
	static Image[] mineType = {new Image("/mine-grey.png"),
			new Image("/mine-misflagged.png"),
			new Image("/mine-red.png"),};
	static Image[] numbers = {new Image("/0.png"),
			new Image("/1.png"),
			new Image("/2.png"),
			new Image("/3.png"),
			new Image("/4.png"),
			new Image("/5.png"),
			new Image("/6.png"),
			new Image("/7.png"),
			new Image("/8.png")};
	
	static MineTile[][] field = new MineTile[fieldWidth][fieldHeight];
	
	static Stage globalStageReference;
	
	static Face guy = new Face(); //Is the face guy at the top of the screen

	public void start(Stage theStage){
		globalStageReference = theStage; // Allows the stage to be reset from any valid method
		initializeStage(theStage);
	}

	private void initializeStage(Stage theStage) {
		BorderPane overPane = new BorderPane(); //OverPane is used to keep the menuBar separate from the rest of the components and their styling
		BorderPane mainPane = new BorderPane();
		GridPane minePane = new GridPane();
		BorderPane faceBox = populateFaceBox(); //FaceBox was replaced with a borderPane for tidier management of the text in it 
		minePane.setAlignment(Pos.CENTER);
		
		minePane.setPadding(new Insets(15, 12, 15, 12));
		
		populateField();

		populateMinePane(minePane, field);
		
		MenuBar menuBar = populateMenuBar(theStage);

		guy.setOnMouseClicked( e -> { //face click handler
    		if(e.getButton() == MouseButton.PRIMARY) 
    			resetField();
		});
		
		loadHighscores();

		String borderCSS = //tried to make a border. it went pretty ok
						"-fx-padding: 0;" + 
						"-fx-border-style: solid;" +
						"-fx-border-shadow: grey;" +
						"-fx-border-width: 5;" +
						"-fx-border-insets: 5;" + 
						"-fx-border-radius: 1;" + 
						"-fx-border-color: white darkgrey darkgrey white;";
		String innerBorderCSS = 
						"-fx-padding: 0;" + 
						"-fx-border-style: solid;" +
						"-fx-border-shadow: grey;" +
						"-fx-border-width: 5;" +
						"-fx-border-insets: 5;" + 
						"-fx-border-radius: 1;" + 
						"-fx-border-color: darkgrey white white darkgrey;";
		mainPane.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
		minePane.setBackground(new Background(new BackgroundFill(Color.LIGHTGREY, CornerRadii.EMPTY, Insets.EMPTY)));
		mainPane.setStyle(borderCSS);
		faceBox.setStyle(innerBorderCSS);
		
		overPane.setTop(menuBar); // The menuBar being in the overPane means it falls outside of the borders on the mainPane
		overPane.setCenter(mainPane);
		mainPane.setTop(faceBox);

		timeline.setCycleCount(Animation.INDEFINITE);

		Scene scene = new Scene(overPane, (32 * fieldWidth) + 56, (32 * fieldHeight) + 176);
		
		if(largeBoardNeeded) { //If a large board has been created using custom difficulties then this statement contains the minePane in a ScrollPane so the window doesnt become too large
			minePane.setPadding(new Insets(0, 0, 0, 0));
			ScrollPane scrollPane = new ScrollPane();
			scrollPane.setContent(minePane);
			scrollPane.setPrefSize(640, 562);
			scrollPane.setStyle(innerBorderCSS);
			scrollPane.setBackground(new Background(new BackgroundFill(Color.LIGHTGREY, CornerRadii.EMPTY, Insets.EMPTY)));
			scrollPane.setPannable(true);
			
			mainPane.setCenter(scrollPane);
		}
		else {
		minePane.setStyle(innerBorderCSS);
		
		mainPane.setCenter(minePane);
		}
		theStage.getIcons().add(mineType[0]);
		theStage.setScene(scene);
		
		if(largeBoardNeeded) { //resizes the board to fit the scrollPane nicely
			theStage.setWidth(680);
			theStage.setHeight(700);
		}
		else if(!largeBoardNeeded) {
			theStage.setWidth((32 * fieldWidth) + 56);
			theStage.setHeight((32 * fieldHeight) + 176);
		}
		
		theStage.show();
		
	}

	public void resetField() { //the whole board is cleared, restarted, and prepared to be remined
		timeline.pause();
		
		field = null;
		
		minesRemaining.setText(String.format("%03d", totalMines));
		timer.setText("000");

		minesOut = false;
		gameOver = false;

		guy.reset();
		
		start(globalStageReference);
	}

	private BorderPane populateFaceBox() {
		BorderPane faceBox = new BorderPane(); //BorderPane used for tidy management of the objects within
		StackPane mineBox = new StackPane();
		StackPane timerBox = new StackPane(); //stackPanes were used to give a background colour for each text piece. Labels had some behaviors that were undesirable, such as weird alignment of text

		faceBox.setPadding(new Insets(15, 12, 15, 12));
		
		minesRemaining.setFont((Font.loadFont (this.getClass().getClassLoader().getResourceAsStream("digital-dream.regular.ttf"), 36)));
		minesRemaining.setFill(Color.RED);
		mineBox.getChildren().add(minesRemaining);
		timer.setFont((Font.loadFont (this.getClass().getClassLoader().getResourceAsStream("digital-dream.regular.ttf"), 36)));
		timer.setFill(Color.RED);
		timerBox.getChildren().add(timer);
		
		faceBox.setBackground(new Background(new BackgroundFill(Color.LIGHTGREY, CornerRadii.EMPTY, Insets.EMPTY)));
		mineBox.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
		timerBox.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
		
		faceBox.setLeft(mineBox);
		faceBox.setCenter(guy);
		faceBox.setRight(timerBox);

		return faceBox;
	}

	private MenuBar populateMenuBar(Stage theStage) {
		MenuBar menuBar = new MenuBar();
		Menu menuDifficulty = new Menu("Difficulty"); 
		Menu highscoreMenu = new Menu("Highscores"); 
		MenuItem difficulty1 = new MenuItem("Beginner");
		MenuItem difficulty2 = new MenuItem("Intermediate");
		MenuItem difficulty3 = new MenuItem("Expert");
		MenuItem difficulty4 = new MenuItem("Custom");
		MenuItem viewHighscores = new MenuItem("View Highscores");
		menuDifficulty.getItems().add(difficulty1);
		menuDifficulty.getItems().add(difficulty2);
		menuDifficulty.getItems().add(difficulty3);
		menuDifficulty.getItems().add(difficulty4);
		highscoreMenu.getItems().add(viewHighscores);
		menuBar.getMenus().add(menuDifficulty);
		menuBar.getMenus().add(highscoreMenu);

		difficulty1.setOnAction(e -> {
			fieldWidth = 8;
			fieldHeight = 8;
			totalMines = 10;
			currentDifficulty = 0;
			largeBoardNeeded = false;
			resetField();
		});
		difficulty2.setOnAction(e ->{
			fieldWidth = 16;
			fieldHeight = 16;
			totalMines = 40;
			currentDifficulty = 1;
			largeBoardNeeded = false;
			resetField();
		});
		difficulty3.setOnAction(e ->{
			fieldWidth = 32;
			fieldHeight = 16;
			totalMines = 99;
			currentDifficulty = 2;
			largeBoardNeeded = false;
			resetField();
		});	
		difficulty4.setOnAction(e ->{
			largeBoardNeeded = false;
			customDifficultyBuilder();
			currentDifficulty = 3;
			resetField();
		});	

		viewHighscores.setOnAction(e ->{
			Alert highscorePopup = new Alert(AlertType.INFORMATION); //Highscore pop up screen. Scores are viewed here
			highscorePopup.setTitle("Minesweeper Champions!"); //the messy spacing below results in even lines once the dialog box appears
			highscorePopup.setHeaderText("Score better than these guys and you could end up on this list!\nScores are based on shortest times");
			highscorePopup.setContentText("Beginner:         " + highscores[0] + " seconds " + highscoreNames[0] + "\n" +
					"Intermediate:   " + highscores[1] + " seconds " + highscoreNames[1] + "\n" +
					"Expert:              " + highscores[2] + " seconds " + highscoreNames[2] + "\n");
			
			highscorePopup.setGraphic(new ImageView(mineType[0]));
			highscorePopup.showAndWait();
		});
		return menuBar;
	}

	private static void populateField() {
		field = new MineTile[fieldWidth][fieldHeight];
		for(int i = 0; i < field.length; i++) {
			for(int j = 0; j < field[i].length; j++) {
				field[i][j] = new MineTile(i,j,false);
			}
		}
		// Click handler for tiles
		for(int i = 0; i < field.length; i++) {
			for(int j = 0; j < field[i].length; j++) {
				MineTile tempMine = field[i][j];
				tempMine.setOnMouseClicked( e -> {
					if(!gameOver) {
						if(e.getButton() == MouseButton.PRIMARY) {
							if(!tempMine.isFlagged()) {
								if (!minesOut) {
									tempMine.circleOriginTile(true);//Origin Tile is determined before the mines are placed. none are placed next to it
									populateMines();
									calculateNeighborMines(field);
									timeline.play();
								}

								if(tempMine.isMinePresent()) 
									lose();
								
								if(tempMine.isExposed())
									tempMine.uncoveredNumberClick();
								
								if(checkWin(field)) {
									guy.win();
									minesRemaining.setText("WIN");
									gameOver = true;
									timeline.stop();
									writeHighscores(Integer.parseInt(timer.getText()));
								}
								tempMine.spreadBlankSpace();//blanks are spread once mines are in place.
							}
						}

						if(e.getButton() == MouseButton.SECONDARY) {
							if(!tempMine.isExposed()) {
								if(!tempMine.isFlagged()) {
									tempMine.setFlagged(true);
									minesRemaining.setText(String.format("%03d", Integer.parseInt(minesRemaining.getText()) - 1));
									if(minesRemaining.getText().equals("000"))
										minesRemaining.setFill(Color.TURQUOISE); // This code block allows the mines remaining counter to be blue if it is 000 like in original minesweeper
									else if(minesRemaining.getText().equals("001"))
										minesRemaining.setFill(Color.RED);
									else if(minesRemaining.getText().equals("-01"))
										minesRemaining.setFill(Color.RED);
								}
								else if(tempMine.isFlagged()) {
									tempMine.setFlagged(false);
									minesRemaining.setText(String.format("%03d", Integer.parseInt(minesRemaining.getText()) + 1));
									if(minesRemaining.getText().equals("000"))
										minesRemaining.setFill(Color.TURQUOISE); //Ditto as above
									else if(minesRemaining.getText().equals("001"))
										minesRemaining.setFill(Color.RED);
									else if(minesRemaining.getText().equals("-01"))
										minesRemaining.setFill(Color.RED);
								}
							}
						}
					}
				});

				tempMine.setOnMousePressed(e -> { //o-Face Handler
					if(!gameOver)
						guy.oFace();
				});
				tempMine.setOnMouseReleased(e -> { //In regular minesweeper both mouse buttons cause the o-face, so in the case it is not checked which button is pressed.
					if(!gameOver)
						guy.reset();
				});
			}
		}
	}

	private static void populateMines() {
		int mineCounter = totalMines;

		while(mineCounter > 0) { // A while loop is used since the loop may have to be continued out of in the event of a bad placement
			int randomColumn = (int) (Math.random() * fieldWidth);
			int randomRow = (int) (Math.random() * fieldHeight);
			
			if(field[randomColumn][randomRow].isMinePresent() || field[randomColumn][randomRow].isOriginTile()) {
				continue; // tries again if the target tile contains a mine or is part of the first clicked tile area
			}
			
			field[randomColumn][randomRow].setMinePresent(true);
			mineCounter--;
		}
		minesOut = true;
	}

	private void populateMinePane(GridPane minePane, MineTile[][] field) { // assigns all the created tiles to the center gridPane
        for(int i = 0; i < field.length; i++) {
            for(int j = 0; j < field[i].length; j++) {
            	minePane.add(field[i][j], i, j);
            }
        }
	}

	private void loadHighscores() {
		try {
			FileReader fileReader = new FileReader("Highscores.txt");

			Scanner scores = new Scanner(fileReader);

			highscores[0] = scores.nextInt();
			highscoreNames[0] = scores.next();
			highscores[1] = scores.nextInt();
			highscoreNames[1] = scores.next();
			highscores[2] = scores.nextInt();
			highscoreNames[2] = scores.next();
			scores.close();
		}
		catch(FileNotFoundException e) {
			System.out.println("Highscores file not found, Writing new Highscores.txt");
			
			List<String> scores = Arrays.asList("100 Bob", "400 Ianto", "999 Ethan"); //Default values and names for the highscore list
			Path file = Paths.get("Highscores.txt");
			try {
			Files.write(file, scores, Charset.forName("UTF-8"));
			}
			catch(IOException i) {
				System.out.println("Highscore write failed.");
			}
		}
		catch(Exception e) {
			System.err.println("something is wrong with the highscores file. please delete it and allow the program to regenerate it");
		}

	}
	
	private static void writeHighscores(int score) {
		String name;

		if(!(currentDifficulty == 0 || currentDifficulty == 1 || currentDifficulty == 2))
			return; //Prevents this high score list from being interacted with when using custom difficulties
		
		if(score < highscores[currentDifficulty]) {
			name = getPlayerName();
			highscores[currentDifficulty] = score;
			highscoreNames[currentDifficulty] = name;
		}

		try{
			PrintWriter pw = new PrintWriter("Highscores.txt");
			
			for(int i = 0; i < 3; i++){//output to highscores file
				pw.println(highscores[i] + " " + highscoreNames[i]);
			}
			
			pw.close();
		}
		catch(FileNotFoundException e){
			System.out.println("Highscores.txt is missing. score write cancelled"); //should never happen because the program checks for the file when launched
		}
	}
	private static String getPlayerName() {
		TextInputDialog newHighscorePopup = new TextInputDialog(); //Requests the players name since they made a new highscore
		newHighscorePopup.setTitle("New Champion!");
		newHighscorePopup.setHeaderText("Please Enter Your Name!");
		newHighscorePopup.setContentText("Please enter your name:");
		newHighscorePopup.setGraphic(new ImageView(flag));

		Optional<String> result = newHighscorePopup.showAndWait(); //waits for player to enter their name
		if (result.isPresent()){
		    return result.get();
		}
		else
			return ""; // returns a blank if the player does not enter their name.
	}

	double mineRatio = 0.156; //mineRatio is declared outside the method here so the click handlers within can set its value
	
	private void customDifficultyBuilder() {
		Alert difficultyBuilder = new Alert(AlertType.CONFIRMATION);
		difficultyBuilder.setTitle("Custom Difficulty");
		difficultyBuilder.setHeaderText("Enter number values for Width and Height. \nThe custom count value is a ratio between 0.0 and 1.0. \nRatio's higher than 1 will be ignored"); 
		difficultyBuilder.setGraphic(new ImageView(numbers[8]));

		mineRatio = 0.156;
		
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 20, 10, 10));

		// I did a fair bit of research on how to mask the input on the text fields and concluded that it's not something JavaFX can do without a custom made class
		// As it is the fields will accept any text but if it is not in the correct format then it will throw a NumberFormatException in customDifficultyInitializer
		
		TextField widthInput = new TextField();
		widthInput.setPromptText("Field Width");
		TextField heightInput = new TextField();
		heightInput.setPromptText("Field Height");
		
		MenuItem mines1 = new MenuItem("Intermediate");
		MenuItem mines2 = new MenuItem("Expert");
		MenuItem mines3 = new MenuItem("Brutal");
		MenuItem mines4 = new MenuItem("Custom Count");
		
		Menu mineDropDown = new Menu("Select mine presense");
		
		TextField mineInput = new TextField();
		mineInput.setDisable(true); //is disabled until custom count is selected
		
		mineDropDown.getItems().add(mines1);
		mineDropDown.getItems().add(mines2);
		mineDropDown.getItems().add(mines3);
		mineDropDown.getItems().add(mines4);

		MenuBar mineDropDownHolder = new MenuBar();
		mineDropDownHolder.getMenus().add(mineDropDown);
		mineDropDownHolder.setMinWidth(145.00);
		
		grid.add(new Text("Enter Width:"), 0, 0);
		grid.add(widthInput, 1, 0);
		grid.add(new Text("WARNING"), 3, 0);
		grid.add(new Text("Enter Height:"), 0, 1);
		grid.add(heightInput, 1, 1);
		grid.add(new Text("Large sizes will cause crashes."), 3, 1);
		grid.add(mineDropDownHolder, 0, 2);
		grid.add(mineInput, 1, 2);

		mines1.setOnAction(e -> {
			mineDropDown.setText("Intermediate"); //sets the text displayed at the top of the drop down. simulating a combo box, but more useful
			mineRatio = 0.156;
			mineInput.setDisable(true);
		});
		mines2.setOnAction(e -> {
			mineDropDown.setText("Expert");
			mineRatio = 0.19;
			mineInput.setDisable(true);
		});
		mines3.setOnAction(e -> {
			mineDropDown.setText("Brutal");
			mineRatio = 0.25;
			mineInput.setDisable(true);
		});
		mines4.setOnAction(e -> {
			mineDropDown.setText("Custom Count");
			mineInput.setDisable(false); // Opens the custom ratio input box
		});

		difficultyBuilder.getDialogPane().setContent(grid);

		Optional<ButtonType> result = difficultyBuilder.showAndWait();
		if (result.get() == ButtonType.OK){
			if(!mineInput.isDisabled())
				customDifficultyInitializer(widthInput.getText(), heightInput.getText(), mineInput.getText());
			else
				customDifficultyInitializer(widthInput.getText(), heightInput.getText(), mineRatio);
		} else {
			//Cancel button.
		}
	}
	private void customDifficultyInitializer(String width, String height, double tempMineRatio) {
		try {
			int tempWidth = Integer.parseInt(width);
			int tempHeight = Integer.parseInt(height); // all handling of parsing being done here allows it to catch bad input
			
			if(tempWidth < 7) // User interface breaks if width is below 7
				tempWidth = 7;
			if(tempHeight < 4) // 4 is just the minimum at which the game functions well in my opinion
				tempHeight = 4;
			
			if(tempWidth > 1000) //tentative upper bounds for the custom difficulty. May still cause crashes at max size
				tempWidth = 1000;
			if(tempHeight > 1000)
				tempHeight = 1000;
			
			if(tempWidth > 40 || tempHeight > 25)
				largeBoardNeeded = true; // Enables the scrollPane so the large board is useable
			
			fieldWidth = tempWidth;
			fieldHeight = tempHeight;
			if(tempMineRatio > 1.0)
				tempMineRatio = 0.156;
			
			int tempTotalMines = ((int) (((double)(tempWidth * tempHeight)) * (tempMineRatio)));
			
			if (tempTotalMines > ((tempWidth * tempHeight) -9))
				tempTotalMines = ((tempWidth * tempHeight) -9); // makes sure there are never too many mines to place on the board
			
			totalMines = tempTotalMines;
		}
		catch(NumberFormatException e) {
			Alert inputFailure = new Alert(AlertType.ERROR);
			inputFailure.setTitle("Input Error");
			inputFailure.setHeaderText("Could not parse width or height values");
			inputFailure.setContentText("Ensure you enter only numbers next time");
			inputFailure.setGraphic(new ImageView(mineType[2]));
			
			inputFailure.showAndWait();
		}
	}
	private void customDifficultyInitializer(String width, String height, String tempStringMineRatio) { //Overloading
		try {
			int tempWidth = Integer.parseInt(width);
			int tempHeight = Integer.parseInt(height);
			double tempMineRatio = Double.parseDouble(tempStringMineRatio); // all handling of parsing being done here allows it to catch bad input
			
			if(tempWidth < 7) // User interface breaks if width is below 7
				tempWidth = 7;
			if(tempHeight < 4) // 4 is just the minimum at which the game functions well in my opinion
				tempHeight = 4;
			
			if(tempWidth > 1000) //tentative upper bounds for the custom difficulty. May still cause crashes at max size
				tempWidth = 1000;
			if(tempHeight > 1000)
				tempHeight = 1000;
			
			if(tempWidth > 40 || tempHeight > 25)
				largeBoardNeeded = true; // Enables the scrollPane so the large board is useable
			
			fieldWidth = tempWidth;
			fieldHeight = tempHeight;
			if(tempMineRatio > 1.0)
				tempMineRatio = 0.156;
			
			int tempTotalMines = ((int) (((double)(tempWidth * tempHeight)) * (tempMineRatio)));
			
			if (tempTotalMines > ((tempWidth * tempHeight) -9))
				tempTotalMines = ((tempWidth * tempHeight) -9); // makes sure there are never too many mines to place on the board
			
			totalMines = tempTotalMines;
		}
		catch(NumberFormatException e) {
			Alert inputFailure = new Alert(AlertType.ERROR);
			inputFailure.setTitle("Input Error");
			inputFailure.setHeaderText("Could not parse width or height values");
			inputFailure.setContentText("Ensure you enter only numbers next time");
			inputFailure.setGraphic(new ImageView(mineType[2]));
			
			inputFailure.showAndWait();
		}
	}

	private static void explosion() {
		for(int i = 0; i < field.length; i++) {
            for(int j = 0; j < field[i].length; j++) {
            	if(field[i][j].isMinePresent() && !field[i][j].isFlagged())
            		field[i][j].explode();
            	else if(!field[i][j].isMinePresent() && field[i][j].isFlagged())
            		field[i][j].misFlagged(); 
            }
        }
	}

	public static void lose() {
		guy.die();
		timeline.stop();
		gameOver = true;
		explosion();
	}
	
	private static void calculateNeighborMines(MineTile[][] field) {
		for(int i = 0; i < field.length; i++) {
            for(int j = 0; j < field[i].length; j++) {
            	if(field[i][j].isMinePresent()) {
            		for(int k = i-1; k <= i+1; k++) {
            			for(int l = j-1; l <= j+1; l++) {
            				if(k >= 0 && k < fieldWidth && l >= 0 && l < fieldHeight) { //neighborMineCount is incremented on tiles containing mines to prevent problems the recursion method
            					field[k][l].setNeighborMineCount(field[k][l].neighborMineCount + 1);
            				}
            			}
            		}
            	}
            }
		}
	}

	private static boolean checkWin(MineTile[][] field) {
		for(int i = 0; i < field.length; i++) {
			for(int j = 0; j < field[i].length; j++) {
				if(field[i][j].isExposed() && field[i][j].isMinePresent()) {
					return false;
				}
            	if(!field[i][j].isExposed() && !field[i][j].isMinePresent()) {
            		return false;
            	}
            }
		}
		return true;
	}
	public static void main(String[] args) {launch(args);}
}