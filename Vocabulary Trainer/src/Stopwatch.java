// Simple stopwatch class implementation (No resume allowed).

public class Stopwatch {
	private long startTime;
	private long endTime;
	private boolean isRunning;
	
	public Stopwatch() {
		startTime = 0;
		endTime = 0;
		isRunning = false;
	}
	
	// Starts the stopwatch.
	public void start() {
		startTime = System.currentTimeMillis();
		isRunning = true;
	}
	
	// Stops the stopwatch, throwing an IllegalStateException if the stopwatch wasn't
	// already running.
	public void stop() {
		if(!isRunning)
			throw new IllegalStateException("Can't stop a stopwatch that isn't running.");
		
		endTime = System.currentTimeMillis();
		isRunning = false;
	}
	
	// Returns how long (in milliseconds) elapsed.
	public long time() {
		if(isRunning)
			return System.currentTimeMillis() - startTime;
		else
			return endTime - startTime;
	}
	
	// Returns whether the stopwatch is currently running
	public boolean isRunning() { return isRunning; }
}
