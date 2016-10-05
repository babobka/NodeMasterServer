package ru.babobka.nodemasterserver.task;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Set;

import org.junit.Test;

public class TaskPoolTest {

	@Test
	public void testInstance() {
		TaskPool.getInstance();
	}

	@Test
	public void testEquality() throws IOException {
		TaskPool pool = TaskPool.getInstance();
		Set<String> keySet = pool.getTasksMap().keySet();
		assertFalse(keySet.isEmpty());
		String taskName = keySet.iterator().next();
		assertFalse(pool.get(taskName) == pool.get(taskName));
	}
}
