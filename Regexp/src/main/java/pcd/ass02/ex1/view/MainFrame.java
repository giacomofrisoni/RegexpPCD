package pcd.ass02.ex1.view;

import java.io.File;
import java.text.DecimalFormat;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;
import pcd.ass02.ex1.controller.RegexpController;

public class MainFrame extends VBox {
	
	private static final int WIDTH = 700;
	private static final int HEIGHT = 300;
	
	private static final String CHOOSE_FOLDER_TITLE = "Choose folder to scan";
	private static final String IDLE_MESSAGE = "Idle";
	private static final String SEARCHING_MESSAGE = "Searching...";
	private static final String PERCENTAGE_SYMBOL = "%";
	private static final String PATH_EMPTY_MESSAGE = "Path shouldn't be empty";
	private static final String REGEX_EMPTY_MESSAGE = "Regular Expression shouldn't be empty";
	private static final String DEPTH_EMPTY_MESSAGE = "Max depth should be default or non empty";
	private static final String DEPTH_ERROR_MESSAGE = "Depth is not a number";
	
	private static final String PATH_COLUMN_TITLE = "Path";
	private static final String VALUE_COLUMN_TITLE = "Value";
	private static final String PATH_PROPERTY_NAME = "path";
	private static final String VALUE_PROPERTY_NAME = "value";
	
	private static final String DECIMAL_FORMAT_PATTERN = "0.00";
	
	private final RegexpController controller;
	private final Window window;
	private final DecimalFormat decimalFormat;
	private int totalFilesToScan = 0;
	private final ObservableList<RowType> resultRows = FXCollections.observableArrayList();
	
	@FXML
	private TextField path, regularExpression, depth;
	
	@FXML
	private Button choosePath, start;
	
	@FXML
	private Label statusLabel, leastOneMatchPercentage, meanNumberOfMatches, currentScanned, totalToScan;
	
	@FXML
	private CheckBox maxDepth;
	
	@FXML
	private ProgressIndicator progress;
	
	@FXML
	private ProgressBar progressBar;
	
	@FXML
	private TableView<RowType> tableView;
	
	
	/**
	 * Constructs a new MainFrame.
	 * 
	 * @param controller
	 * 		the controller of the application
	 * @param window
	 * 		the window in which display the frame
	 */
	public MainFrame(final RegexpController controller, final Window window) {
		//Set some references
		this.controller = controller;
		this.window = window;
		this.decimalFormat = new DecimalFormat(DECIMAL_FORMAT_PATTERN);
		
    	//Load the FXML definition
		final FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("MainFrame.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);
		
        try {
            fxmlLoader.load();
            this.setDimensions();
            this.setEventHandlers();
            this.setTableColumns();
            this.setIdle();
        } catch (Exception exception) {
        	MessageUtils.showFXMLException(this.toString(), exception);
        }
	}
	
	private void setDimensions() {
		this.setWidth(WIDTH);
    	this.setHeight(HEIGHT);
    	this.setMinWidth(WIDTH);
    	this.setMinHeight(HEIGHT);
	}
	
	private void setEventHandlers() {
		//Choosing the right path
        this.choosePath.setOnMouseClicked(e -> {
			final DirectoryChooser chooser = new DirectoryChooser();
			chooser.setTitle(CHOOSE_FOLDER_TITLE);
			final File pathFile = chooser.showDialog(this.window);
			if (pathFile != null) {
				this.path.setText(pathFile.getAbsolutePath());
			}		
		});
        
        //Starting computation
        this.start.setOnMouseClicked(e -> {
        	//Check if inputs are not empty / correct
        	if (checkInputs()) {
        		//Disable some views
        		Platform.runLater(() -> {
        			this.choosePath.setDisable(true);
        			this.path.setDisable(true);
        			this.regularExpression.setDisable(true);
        			this.depth.setDisable(true);
        			this.start.setDisable(true);
        		});
	
        		//Tell controller to start
        		this.controller.setStartingPath(this.path.getText());
        		this.controller.setPattern(this.regularExpression.getText());
        		if (!this.maxDepth.isSelected()) {
        			this.controller.setMaxDepthNavigation(Integer.parseInt(this.depth.getText()));
        		}
        		this.controller.search();
        		
        		//Set to searching
        		this.setSearching();
        	}
		});
        
        //Checkbox for max depth
        this.maxDepth.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				depth.setDisable(newValue);
			}
		});
    }
	
	/**
	 * Sets the view to the idle state.
	 */
	public void setIdle() {
		this.changeStatus(IDLE_MESSAGE, false, false);
	}
	
	/**
	 * Sets the view to the searching.
	 */
	public void setSearching() {
		this.changeStatus(SEARCHING_MESSAGE, true, false);
	}
	
	/**
	 * Sets the view to show an error message.
	 * 
	 * @param message
	 * 		the message to display
	 */
	public void setError(final String message) {
		this.changeStatus(message, false, true);
	}
	
	/**
	 * Sets the label to show percentage of files with least one match.
	 * 
	 * @param percentage
	 * 		the percentage of file with least one match
	 */
	public void showLeastOneMatchPercentage(final double percentage) {
		Platform.runLater(() -> {
			this.leastOneMatchPercentage.setText(this.decimalFormat.format(percentage) + PERCENTAGE_SYMBOL);
		});
	}
	
	/**
	 * Sets the label to show the mean number of matches.
	 * 
	 * @param mean
	 * 		the mean number of matches
	 */
	public void showMeanNumberOfMatches(final double mean) {
		Platform.runLater(() -> {
			this.meanNumberOfMatches.setText(this.decimalFormat.format(mean));
		});
	}
	
	/**
	 * Sets the total numbers of files to scan.
	 * 
	 * @param total
	 * 		the total numbers of files
	 */
	public void setTotalFilesToScan(final int total) {
		this.totalFilesToScan = total;
		Platform.runLater(() -> {
			this.totalToScan.setText("" + this.totalFilesToScan);
			// Resets the progress bar
			this.progressBar.setProgress(0);
		});
	}
	
	/**
	 * Sets the number of scanned files.
	 * 
	 * @param nScanned
	 * 		the number of analyzed files
	 */
	public void setNumberOfScannedFiles(final int nScanned) {
		Platform.runLater(() -> {
			this.currentScanned.setText("" + nScanned);
			// Updates the progress bar
			this.progressBar.setProgress(nScanned/this.totalFilesToScan);
		});
	}
	
	/**
	 * Adds a row into the result table.
	 * 
	 * @param path
	 * 		the path of the scanned file
	 * @param nMatches
	 * 		the number of matches in the file
	 */
	public void addResult(final String path, final int nMatches) {
		resultRows.add(new RowType(path, "" + nMatches));
		// Scrolls to the bottom
		Platform.runLater(() -> this.tableView.scrollTo(this.resultRows.size() - 1));
	}
	
	/**
	 * Adds a row into the result table.
	 * 
	 * @param path
	 * 		the path of the scanned file
	 * @param message
	 * 		the message to display
	 */
	public void addResult(final String path, final String message) {
		resultRows.add(new RowType(path, message));
		// Scrolls to the bottom
		Platform.runLater(() -> this.tableView.scrollTo(this.resultRows.size() - 1));
	}

	private void changeStatus(final String message, final boolean isSearching, final boolean isError) {
		if (isError) {
			this.statusLabel.getStyleClass().add("errorLabel");
		} else {
			this.statusLabel.getStyleClass().remove("errorLabel");
		}
		
		this.progress.setVisible(isSearching);
		this.progress.setManaged(isSearching);
		this.progressBar.setVisible(isSearching);
		this.progressBar.setManaged(isSearching);
		
		this.statusLabel.setText(message);
	}
	
	private boolean checkInputs() {
		this.setIdle();
		
		//Path is empty
		if (this.path.getText().isEmpty()) {
			this.setError(PATH_EMPTY_MESSAGE);
			return false;
		}
		
		//Regular Expression is empty
		if (this.regularExpression.getText().isEmpty()) {
			this.setError(REGEX_EMPTY_MESSAGE);
			return false;
		}
		
		//I have not selected the default max depth
		if (!this.maxDepth.isSelected()) {
			//Depth is empty
			if (this.depth.getText().isEmpty()) {
				this.setError(DEPTH_EMPTY_MESSAGE);
				return false;
			} else {
				//Depth is not a number
				try {
					Integer.parseInt(this.depth.getText());
				} catch (Exception e) {
					this.setError(DEPTH_ERROR_MESSAGE);
					return false;
				}
			}
		}
		
		return true;
	}
	
	private void setTableColumns() {
		//Create two columns
		final TableColumn<RowType, String> tcPath = new TableColumn<>(PATH_COLUMN_TITLE);
		final TableColumn<RowType, String> tcValue = new TableColumn<>(VALUE_COLUMN_TITLE);
		
		//First is not resizable
		tcValue.setResizable(false);
		
		//Preferred width
		tcPath.setPrefWidth(300.0d);  	
		tcValue.setPrefWidth(150.0d);
		
		//What they'll contain
		tcPath.setCellValueFactory(new PropertyValueFactory<RowType, String>(PATH_PROPERTY_NAME));
		tcValue.setCellValueFactory(new PropertyValueFactory<RowType, String>(VALUE_PROPERTY_NAME));
		
		//Stretch the first column
		tcPath.prefWidthProperty().bind(
                this.tableView.widthProperty()
                .subtract(tcValue.widthProperty())
             );
		
		//Add all columns
		this.tableView.getColumns().add(tcPath);
		this.tableView.getColumns().add(tcValue);
		
		//Bind to observable collection
		this.tableView.itemsProperty().bind(new SimpleObjectProperty<>(resultRows));
	}
	
}
