package ru.babobka.nodemasterserver.model;

public class Timer {

	private final String title;

	private final long startTime;

	public Timer() {
		this(null);
	}

	public Timer(String title) {
		this.startTime = System.currentTimeMillis();
		this.title = title;
	}

	public long getTimePassed() {
		return System.currentTimeMillis() - startTime;
	}

	public String getTitle() {
		return title;
	}

	public long getStartTime() {
		return startTime;
	}

	@Override
	public String toString() {
		return "Timer [title=" + title + ",timePassed=" + (System.currentTimeMillis() - startTime) + "]";
	}

}
