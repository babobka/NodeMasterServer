package ru.babobka.nodemasterserver.task;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Set;

import org.junit.Test;

import ru.babobka.container.Container;
import ru.babobka.nodemasterserver.server.MasterServer;
import ru.babobka.nodemasterserver.server.MasterServerContainerStrategy;
import ru.babobka.nodemasterserver.util.StreamUtil;


public class TaskPoolTest {

	static {
		new MasterServerContainerStrategy(StreamUtil.getLocalResource(
				MasterServer.class, MasterServer.MASTER_SERVER_TEST_CONFIG))
						.contain(Container.getInstance());
		
	}

	@Test
	public void testEquality() throws IOException {
		TaskPool pool = Container.getInstance().get(TaskPool.class);
		Set<String> keySet = pool.getTasksMap().keySet();
		assertFalse(keySet.isEmpty());
		String taskName = keySet.iterator().next();
		assertFalse(pool.get(taskName) == pool.get(taskName));
	}
}
