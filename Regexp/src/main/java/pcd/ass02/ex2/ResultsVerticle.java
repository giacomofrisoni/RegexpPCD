package pcd.ass02.ex2;

import java.util.Optional;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import pcd.ass02.ex1.model.SearchFileErrorResult;
import pcd.ass02.ex1.model.SearchFileSuccessfulResult;
import pcd.ass02.ex1.view.RegexpView;

public class ResultsVerticle extends AbstractVerticle {
	
	private final RegexpView view;
	private int nLeastOneMatch;
	private double meanNumberOfMatches;
	private int nComputedFiles;
	
	private Optional<Integer> nTotalFiles;

	
	public ResultsVerticle(final RegexpView view) {
		this.view = view;
		this.nLeastOneMatch = 0;
		this.meanNumberOfMatches = 0;
		this.nComputedFiles = 0;
		this.nTotalFiles = Optional.empty();
	}

	@Override
	public void start(final Future<Void> done) throws Exception {
		vertx.eventBus().consumer("result", message -> {
			if (message.body() instanceof SearchFileSuccessfulResult) {
				final SearchFileSuccessfulResult result = (SearchFileSuccessfulResult)message.body();
				final int nMatches = result.getNumberOfMatches();
				if (nMatches > 0) {
					// Updates the number of files with least one match
					this.nLeastOneMatch++;
					// Updates the mean number of matches among files with matches
					final double oldMean = this.meanNumberOfMatches;
					this.meanNumberOfMatches += (nMatches - oldMean) / this.nLeastOneMatch;
				}
				// Shows file result on view
				this.view.showResult(result.getPath().toString(), nMatches, result.getElapsedTime());
			} else if (message.body() instanceof SearchFileErrorResult) {
				final SearchFileErrorResult errorRes = (SearchFileErrorResult)message.body();
				// Shows file result on view
				this.view.showResult(errorRes.getPath().toString(), errorRes.getErrorMessage());
			}
			// Shows statistics on view
			this.view.showMeanNumberOfMatches(this.meanNumberOfMatches);
			this.view.showLeastOneMatchPercentage((double)this.nLeastOneMatch / (double)nComputedFiles);
			// Shows analysis progress on view
			this.view.setNumberOfScannedFiles(++this.nComputedFiles);
			
			this.nTotalFiles.ifPresent(total -> {
				if (this.nComputedFiles == total) {
					this.view.setFinish();
					vertx.undeploy(this.deploymentID());
					vertx.eventBus().send("end", null);
				}
			});
		});
		vertx.eventBus().consumer("totalFiles", message -> {
			this.nTotalFiles = Optional.of((int)message.body());
			if (this.nComputedFiles == this.nTotalFiles.get()) {
				this.view.setFinish();
				vertx.undeploy(this.deploymentID());
				vertx.eventBus().send("end", null);
			}
		});
	}
	
}
