package ru.babobka.nodemasterserver.task;

import org.json.JSONObject;

public class TaskConfig {

	private final String name;

	private final String className;

	private final String description;

	private final boolean raceStyle;

	public TaskConfig(String name, String className, String description, boolean raceStyle) {
		super();
		this.name = name;
		this.description = description;
		this.raceStyle = raceStyle;
		this.className = className;
	}

	public TaskConfig(JSONObject jsonObject) {
		this(jsonObject.getString("name"), jsonObject.getString("className"), jsonObject.getString("description"),
				jsonObject.getBoolean("raceStyle"));
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public boolean isRaceStyle() {
		return raceStyle;
	}

	public String getClassName() {
		return className;
	}

	public TaskConfig newInstance() {
		return new TaskConfig(name, className, description, raceStyle);
	}

}
