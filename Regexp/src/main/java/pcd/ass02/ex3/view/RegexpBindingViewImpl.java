package pcd.ass02.ex3.view;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import pcd.ass02.ex1.controller.RegexpController;
import pcd.ass02.ex1.view.MessageUtils;
import pcd.ass02.ex1.view.MessageUtils.ExceptionType;

public class RegexpBindingViewImpl implements RegexpBindingView {
	
	private final String windowTitle;
	private final Stage stage;
	private RegexpController controller;
	private MainBindingFrame mainFrame;

	/**
	 * A JavaFX implementation of the view for RegeXP. Require the stage passed
	 * from the entry point of the application and the window title.
	 * @param stage
	 * 		stage passed from the entry point of JavaFX application (usually primaryStage)
	 */
	public RegexpBindingViewImpl(final Stage stage, final String windowTitle) {
		this.stage = stage;
		this.windowTitle = windowTitle;
	}

	
	@Override
	public void setController(RegexpController controller) {
		this.controller = controller;
	}
	
	@Override
	public void show() {
		mainFrame = new MainBindingFrame(this.controller, this.stage);
		final Scene scene = new Scene(mainFrame);
		
		this.stage.setOnCloseRequest(e -> {
			this.stage.close();
	        Platform.exit();
	        System.exit(0);
		});

		this.stage.setTitle(this.windowTitle);
		this.stage.setScene(scene);
		this.stage.getIcons().addAll(
				new Image(("file:res/icon16x16.png")),
				new Image(("file:res/icon32x32.png")),
				new Image(("file:res/icon64x64.png")));
		this.stage.show();	
	}
	
	@Override
	public void setFinish() {
		this.mainFrame.setFinish();
	}
	
	@Override
	public void showInputError(final String message) {
		this.mainFrame.setError(message);
	}

	@Override
	public void showException(final ExceptionType exceptionType, final String message, final Exception e) {
		MessageUtils.showExcpetion(exceptionType, message, e);
	}



	
}
