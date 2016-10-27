package ru.babobka.nodemasterserver.model;

import static org.junit.Assert.*;

import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ru.babobka.nodemasterserver.thread.SlaveThread;



public class SlaveThreadsTest {

	private final int maxSize = 1000;
	private final int maxThreads = 10;
	private Slaves slaves;
	private final SlaveThread slaveThreadMock = new SlaveThread(new Socket());

	@Before
	public void setUp() {
		slaves = new Slaves(maxSize);
	}

	@After
	public void tearDown() {
		slaves.clear();
	}

	@Test
	public void testEmpty() {
		assertTrue(slaves.isEmpty());
	}

	@Test
	public void testMaxSize() {

		for (int i = 0; i < maxSize; i++) {
			assertTrue(slaves.add(slaveThreadMock));
		}
		assertFalse(slaves.add(slaveThreadMock));
	}

	@Test
	public void testAdd() {

		assertTrue(slaves.add(slaveThreadMock));
	}

	@Test
	public void testClear() {
		slaves.add(slaveThreadMock);
		slaves.clear();
		assertTrue(slaves.isEmpty());
	}

	@Test
	public void testAddNull() {
		assertFalse(slaves.add(null));
	}

	@Test
	public void testRemoveNull() {
		assertFalse(slaves.remove(null));
	}

	@Test
	public void testRemove() {
		slaves.add(slaveThreadMock);
		assertFalse(slaves.isEmpty());
		assertTrue(slaves.remove(slaveThreadMock));
		assertTrue(slaves.isEmpty());
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
						if (slaves.add(slaveThreadMock)) {
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
		assertEquals(slaves.getClusterSize(), maxSize);
		assertEquals(slaves.getFullList().size(), maxSize);
	}

	@Test
	public void testRemoveParallel() throws InterruptedException {
		for (int i = 0; i < maxSize; i++) {
			slaves.add(slaveThreadMock);
		}
		Thread[] removeThreads = new Thread[maxThreads];
		final AtomicInteger succededRemoves = new AtomicInteger();
		for (int i = 0; i < removeThreads.length; i++) {
			removeThreads[i] = new Thread(new Runnable() {

				@Override
				public void run() {
					for (int i = 0; i < maxSize; i++) {
						if (slaves.remove(slaveThreadMock)) {
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
		assertEquals(slaves.getClusterSize(), 0);
		assertTrue(slaves.isEmpty());
		assertTrue(slaves.getFullList().isEmpty());
	}

}