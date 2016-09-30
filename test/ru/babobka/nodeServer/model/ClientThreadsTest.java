package ru.babobka.nodeServer.model;

import static org.junit.Assert.*;

import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;

import ru.babobka.nodeServer.thread.ClientThread;

public class ClientThreadsTest {

	private final int maxSize = 1000;
	private final int maxThreads = 10;
	private ClientThreads clientThreads;
	private final ClientThread clientThreadMock = new ClientThread(new Socket());

	@Before
	public void init() {
		clientThreads = new ClientThreads(maxSize);
	}

	@Test
	public void testEmpty() {
		assertTrue(clientThreads.isEmpty());
	}

	@Test
	public void testMaxSize() {

		for (int i = 0; i < maxSize; i++) {
			assertTrue(clientThreads.add(clientThreadMock));
		}
		assertFalse(clientThreads.add(clientThreadMock));
	}

	@Test
	public void testAdd() {

		assertTrue(clientThreads.add(clientThreadMock));
	}

	@Test
	public void testClear() {
		clientThreads.add(clientThreadMock);
		clientThreads.clear();
		assertTrue(clientThreads.isEmpty());
	}

	@Test
	public void testAddNull() {
		assertFalse(clientThreads.add(null));
	}

	@Test
	public void testRemoveNull() {
		assertFalse(clientThreads.remove(null));
	}

	@Test
	public void testRemove() {
		clientThreads.add(clientThreadMock);
		assertFalse(clientThreads.isEmpty());
		assertTrue(clientThreads.remove(clientThreadMock));
		assertTrue(clientThreads.isEmpty());
	}

	@Test
	public void testAddParallel() throws InterruptedException {

		Thread[] addThreads = new Thread[maxThreads];
		final AtomicInteger succededAdds = new AtomicInteger();
		for (int i = 0; i < addThreads.length; i++) {
			addThreads[i] = new Thread(new Runnable() {

				@Override
				public void run() {
					for (int i = 0; i < maxSize; i++) {
						if (clientThreads.add(clientThreadMock)) {
							succededAdds.incrementAndGet();
						}
					}
				}
			});
		}
		for (Thread addThread : addThreads) {
			addThread.start();
		}

		for (Thread addThread : addThreads) {
			addThread.join();
		}
		assertEquals(succededAdds.intValue(), maxSize);
		assertEquals(clientThreads.getClusterSize(), maxSize);
		assertEquals(clientThreads.getFullList().size(), maxSize);
	}

	@Test
	public void testRemoveParallel() throws InterruptedException {
		for (int i = 0; i < maxSize; i++) {
			clientThreads.add(clientThreadMock);
		}
		Thread[] removeThreads = new Thread[maxThreads];
		final AtomicInteger succededRemoves = new AtomicInteger();
		for (int i = 0; i < removeThreads.length; i++) {
			removeThreads[i] = new Thread(new Runnable() {

				@Override
				public void run() {
					for (int i = 0; i < maxSize; i++) {
						if (clientThreads.remove(clientThreadMock)) {
							succededRemoves.incrementAndGet();
						}
					}
				}
			});
		}
		for (Thread removeThread : removeThreads) {
			removeThread.start();
		}

		for (Thread removeThread : removeThreads) {
			removeThread.join();
		}
		assertEquals(succededRemoves.intValue(), maxSize);
		assertEquals(clientThreads.getClusterSize(), 0);
		assertTrue(clientThreads.isEmpty());
		assertTrue(clientThreads.getFullList().isEmpty());
	}

}
