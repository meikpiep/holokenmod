package com.holokenmod.backtrack.hybrid;

import com.holokenmod.Grid;
import com.holokenmod.creation.GridSingleCageCreator;

import java.util.List;

public class BackTrackThread extends Thread {
	public final List<GridSingleCageCreator> cageCreators;
	public final Grid grid;
	public final BackTrackSolutionListener solutionListener;
	public final boolean isPreSolved;
	
	public BackTrackThread(Runnable r, Grid grid, List<GridSingleCageCreator> cageCreators, boolean isPreSolved, BackTrackSolutionListener solutionListener) {
		super(r);
		this.grid = grid;
		this.cageCreators = cageCreators;
		this.isPreSolved = isPreSolved;
		this.solutionListener = solutionListener;
	}
}