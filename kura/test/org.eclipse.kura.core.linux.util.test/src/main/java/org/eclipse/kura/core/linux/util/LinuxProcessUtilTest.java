/*******************************************************************************
 * Copyright (c) 2017 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.kura.core.linux.util;

import static org.junit.Assert.*;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.kura.KuraException;
import org.eclipse.kura.system.SystemService;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LinuxProcessUtilTest {
	private static final String TEMP_SCRIPT_FILE_PATH = "/tmp/kura_test_script_LinuxProcessUtilTest";
	private static final String TEMP_OUTPUT_FILE_PATH = "/tmp/kura_test_output_LinuxProcessUtilTest.txt";

	private static CountDownLatch dependencyLatch = new CountDownLatch(1);
	private static SystemService systemService;

	@BeforeClass
	public static void setUpClass() throws Exception {

		try {
			boolean ok = dependencyLatch.await(10, TimeUnit.SECONDS);

			assertTrue("Dependencies OK", ok);
		} catch (final InterruptedException e) {
			fail("OSGi dependencies unfulfilled");
		}
	}

	@Before
	public void setup() throws KuraException {
	}

	protected void bindSystemService(final SystemService sysService) {
		if (systemService == null) {
			systemService = sysService;
			dependencyLatch.countDown();
		}
	}

	protected void unbindSystemService(final SystemService sysService) {
		if (systemService == sysService) {
			systemService = null;
		}
	}

	@Test
	public void testStartStringBooleanBooleanWaitForeground() throws Exception {
		int[] values = new int[] { 0, 1, 2 };
		String command = "/bin/sh " + TEMP_SCRIPT_FILE_PATH;
		File file = new File(TEMP_SCRIPT_FILE_PATH);
		file.deleteOnExit();

		for (int value : values) {
			int errorCode = -1;

			try {
				try (PrintWriter out = new PrintWriter(file)) {
					out.println("exit " + value);
				}

				errorCode = LinuxProcessUtil.start(command, true, false);
			} catch (Exception e) {
				throw e;
			} finally {
				file.delete();
			}

			assertEquals(value, errorCode);
		}
	}

	@Test
	public void testStartStringBooleanBooleanNoWaitForeground() throws Exception {
		int[] values = new int[] { 0, 1, 2 };
		String command = "/bin/sh " + TEMP_SCRIPT_FILE_PATH;
		File scriptFile = new File(TEMP_SCRIPT_FILE_PATH);
		scriptFile.deleteOnExit();

		File outputFile = new File(TEMP_OUTPUT_FILE_PATH);
		outputFile.deleteOnExit();

		for (int value : values) {
			try (PrintWriter out = new PrintWriter(scriptFile)) {
				out.println("sleep 1");
				out.println("echo " + value + " > " + TEMP_OUTPUT_FILE_PATH);
			}

			int errorCode = -1;

			try {
				errorCode = LinuxProcessUtil.start(command, false, false);
			} catch (Exception e) {
				throw e;
			} finally {
				scriptFile.delete();
			}

			assertEquals(0, errorCode);

			// Get process ID
			int pid = LinuxProcessUtil.getPid(command);
			assertTrue(pid > 0);

			// Wait for process to finish
			int count = 0;
			
			while (LinuxProcessUtil.isProcessRunning(pid)) {
				Thread.sleep(100);
				count++;
				
				if (count > 15) {
					fail("Timeout");
					return;
				}
			}

			// Read output file content
			String readData = new String(Files.readAllBytes(Paths.get(outputFile.getPath())));
			outputFile.delete();

			String expected = Integer.toString(value) + "\n";
			assertEquals(expected, readData);
		}
	}

	@Test
	public void testStartStringBooleanBooleanWaitBackground() throws Exception {
		int[] values = new int[] { 0, 1, 2 };
		String command = "/bin/sh " + TEMP_SCRIPT_FILE_PATH + " &";
		File scriptFile = new File(TEMP_SCRIPT_FILE_PATH);
		scriptFile.deleteOnExit();

		File outputFile = new File(TEMP_OUTPUT_FILE_PATH);
		outputFile.deleteOnExit();

		for (int value : values) {
			try (PrintWriter out = new PrintWriter(scriptFile)) {
				out.println("echo " + value + " > " + TEMP_OUTPUT_FILE_PATH);
			}

			int errorCode = -1;

			try {
				errorCode = LinuxProcessUtil.start(command, true, true);
			} catch (Exception e) {
				throw e;
			} finally {
				scriptFile.delete();
			}

			assertEquals(0, errorCode);

			// Read output file content
			String readData = new String(Files.readAllBytes(Paths.get(outputFile.getPath())));
			outputFile.delete();

			String expected = Integer.toString(value) + "\n";
			assertEquals(expected, readData);
		}
	}

	@Test
	public void testStartStringBooleanBooleanNoWaitBackground() throws Exception {
		int[] values = new int[] { 0, 1, 2 };
		String command = "/bin/sh " + TEMP_SCRIPT_FILE_PATH + " &";
		File scriptFile = new File(TEMP_SCRIPT_FILE_PATH);
		scriptFile.deleteOnExit();

		File outputFile = new File(TEMP_OUTPUT_FILE_PATH);
		outputFile.deleteOnExit();

		for (int value : values) {
			try (PrintWriter out = new PrintWriter(scriptFile)) {
				out.println("sleep 1");
				out.println("echo " + value + " > " + TEMP_OUTPUT_FILE_PATH);
			}

			int errorCode = -1;

			try {
				errorCode = LinuxProcessUtil.start(command, false, true);
			} catch (Exception e) {
				throw e;
			} finally {
				scriptFile.delete();
			}

			assertEquals(0, errorCode);

			// Get process ID
			int pid = LinuxProcessUtil.getPid(command);
			assertTrue(pid > 0);

			// Wait for process to finish
			int count = 0;
			
			while (LinuxProcessUtil.isProcessRunning(pid)) {
				Thread.sleep(100);
				count++;
				
				if (count > 15) {
					fail("Timeout");
					return;
				}
			}

			// Read output file content
			String readData = new String(Files.readAllBytes(Paths.get(outputFile.getPath())));
			outputFile.delete();

			String expected = Integer.toString(value) + "\n";
			assertEquals(expected, readData);
		}
	}

	@Test
	public void testStartString() throws Exception {
		int[] values = new int[] { 0, 1, 2 };
		String command = "/bin/sh " + TEMP_SCRIPT_FILE_PATH;
		File file = new File(TEMP_SCRIPT_FILE_PATH);
		file.deleteOnExit();

		for (int value : values) {
			int errorCode = -1;

			try {
				try (PrintWriter out = new PrintWriter(file)) {
					out.println("exit " + value);
				}

				errorCode = LinuxProcessUtil.start(command);
			} catch (Exception e) {
				throw e;
			} finally {
				file.delete();
			}

			assertEquals(value, errorCode);
		}
	}

	@Test
	public void testStartStringBooleanWait() throws Exception {
		int[] values = new int[] { 0, 1, 2 };
		String command = "/bin/sh " + TEMP_SCRIPT_FILE_PATH;
		File file = new File(TEMP_SCRIPT_FILE_PATH);
		file.deleteOnExit();

		for (int value : values) {
			int errorCode = -1;

			try {
				try (PrintWriter out = new PrintWriter(file)) {
					out.println("exit " + value);
				}

				errorCode = LinuxProcessUtil.start(command, true);
			} catch (Exception e) {
				throw e;
			} finally {
				file.delete();
			}

			assertEquals(value, errorCode);
		}
	}

	@Test
	public void testStartStringBooleanNoWait() throws Exception {
		int[] values = new int[] { 0, 1, 2 };
		String command = "/bin/sh " + TEMP_SCRIPT_FILE_PATH;
		File scriptFile = new File(TEMP_SCRIPT_FILE_PATH);
		scriptFile.deleteOnExit();

		File outputFile = new File(TEMP_OUTPUT_FILE_PATH);
		outputFile.deleteOnExit();

		for (int value : values) {
			try (PrintWriter out = new PrintWriter(scriptFile)) {
				out.println("sleep 1");
				out.println("echo " + value + " > " + TEMP_OUTPUT_FILE_PATH);
			}

			int errorCode = -1;

			try {
				errorCode = LinuxProcessUtil.start(command, false);
			} catch (Exception e) {
				throw e;
			} finally {
				scriptFile.delete();
			}

			assertEquals(0, errorCode);

			// Get process ID
			int pid = LinuxProcessUtil.getPid(command);
			assertTrue(pid > 0);

			// Wait for process to finish
			int count = 0;
			
			while (LinuxProcessUtil.isProcessRunning(pid)) {
				Thread.sleep(100);
				count++;
				
				if (count > 15) {
					fail("Timeout");
					return;
				}
			}

			// Read output file content
			String readData = new String(Files.readAllBytes(Paths.get(outputFile.getPath())));
			outputFile.delete();

			String expected = Integer.toString(value) + "\n";
			assertEquals(expected, readData);
		}
	}

	@Test
	public void testStartStringArrayBooleanWait() throws Exception {
		int[] values = new int[] { 0, 1, 2 };
		String[] commandArray = { "/bin/sh", TEMP_SCRIPT_FILE_PATH };
		File file = new File(TEMP_SCRIPT_FILE_PATH);
		file.deleteOnExit();

		for (int value : values) {
			int errorCode = -1;

			try {
				try (PrintWriter out = new PrintWriter(file)) {
					out.println("exit " + value);
				}

				errorCode = LinuxProcessUtil.start(commandArray, true);
			} catch (Exception e) {
				throw e;
			} finally {
				file.delete();
			}

			assertEquals(value, errorCode);
		}
	}

	@Test
	public void testStartStringArrayBooleanNoWait() throws Exception {
		int[] values = new int[] { 0, 1, 2 };
		String[] commandArray = { "/bin/sh", TEMP_SCRIPT_FILE_PATH };
		File scriptFile = new File(TEMP_SCRIPT_FILE_PATH);
		scriptFile.deleteOnExit();

		File outputFile = new File(TEMP_OUTPUT_FILE_PATH);
		outputFile.deleteOnExit();

		for (int value : values) {
			try (PrintWriter out = new PrintWriter(scriptFile)) {
				out.println("sleep 1");
				out.println("echo " + value + " > " + TEMP_OUTPUT_FILE_PATH);
			}

			int errorCode = -1;

			try {
				errorCode = LinuxProcessUtil.start(commandArray, false);
			} catch (Exception e) {
				throw e;
			} finally {
				scriptFile.delete();
			}

			assertEquals(0, errorCode);

			// Get process ID
			int pid = LinuxProcessUtil.getPid(String.join(" ", commandArray));
			assertTrue(pid > 0);

			// Wait for process to finish
			int count = 0;
			
			while (LinuxProcessUtil.isProcessRunning(pid)) {
				Thread.sleep(100);
				count++;
				
				if (count > 15) {
					fail("Timeout");
					return;
				}
			}

			// Read output file content
			String readData = new String(Files.readAllBytes(Paths.get(outputFile.getPath())));
			outputFile.delete();

			String expected = Integer.toString(value) + "\n";
			assertEquals(expected, readData);
		}
	}

	@Test
	public void testStartBackgroundWait() throws Exception {
		int[] values = new int[] { 0, 1, 2 };
		String command = "/bin/sh " + TEMP_SCRIPT_FILE_PATH + " &";
		File scriptFile = new File(TEMP_SCRIPT_FILE_PATH);
		scriptFile.deleteOnExit();

		File outputFile = new File(TEMP_OUTPUT_FILE_PATH);
		outputFile.deleteOnExit();

		for (int value : values) {
			try (PrintWriter out = new PrintWriter(scriptFile)) {
				out.println("echo " + value + " > " + TEMP_OUTPUT_FILE_PATH);
			}

			int errorCode = -1;

			try {
				errorCode = LinuxProcessUtil.startBackground(command, true);
			} catch (Exception e) {
				throw e;
			} finally {
				scriptFile.delete();
			}

			assertEquals(0, errorCode);

			// Read output file content
			String readData = new String(Files.readAllBytes(Paths.get(outputFile.getPath())));
			outputFile.delete();

			String expected = Integer.toString(value) + "\n";
			assertEquals(expected, readData);
		}
	}

	@Test
	public void testStartBackgroundNoWait() throws Exception {
		int[] values = new int[] { 0, 1, 2 };
		String command = "/bin/sh " + TEMP_SCRIPT_FILE_PATH + " &";
		File scriptFile = new File(TEMP_SCRIPT_FILE_PATH);
		scriptFile.deleteOnExit();

		File outputFile = new File(TEMP_OUTPUT_FILE_PATH);
		outputFile.deleteOnExit();

		for (int value : values) {
			try (PrintWriter out = new PrintWriter(scriptFile)) {
				out.println("sleep 1");
				out.println("echo " + value + " > " + TEMP_OUTPUT_FILE_PATH);
			}

			int errorCode = -1;

			try {
				errorCode = LinuxProcessUtil.startBackground(command, false);
			} catch (Exception e) {
				throw e;
			} finally {
				scriptFile.delete();
			}

			assertEquals(0, errorCode);

			// Get process ID
			int pid = LinuxProcessUtil.getPid(command);
			assertTrue(pid > 0);

			// Wait for process to finish
			int count = 0;
			
			while (LinuxProcessUtil.isProcessRunning(pid)) {
				Thread.sleep(100);
				count++;
				
				if (count > 15) {
					fail("Timeout");
					return;
				}
			}

			// Read output file content
			String readData = new String(Files.readAllBytes(Paths.get(outputFile.getPath())));
			outputFile.delete();

			String expected = Integer.toString(value) + "\n";
			assertEquals(expected, readData);
		}
	}

	@Test
	public void testStartWithStatsString() throws Exception {
		int[] values = new int[] { 1, 0, 1, 2 };
		String command = "/bin/sh " + TEMP_SCRIPT_FILE_PATH;
		File file = new File(TEMP_SCRIPT_FILE_PATH);
		file.deleteOnExit();

		for (int value : values) {
			ProcessStats stats;

			try {
				try (PrintWriter out = new PrintWriter(file)) {
					out.println("echo stdout");
					out.println("echo stderr 1>&2");
					out.println("exit " + value);
				}

				stats = LinuxProcessUtil.startWithStats(command);
			} catch (Exception e) {
				throw e;
			} finally {
				file.delete();
			}

			String stdout = LinuxProcessUtil.getInputStreamAsString(stats.getInputStream());
			String stderr = LinuxProcessUtil.getInputStreamAsString(stats.getErrorStream());

			assertEquals(value, stats.getReturnValue());
			assertEquals("stdout\n", stdout);
			assertEquals("stderr\n", stderr);
		}
	}

	@Test
	public void testStartWithStatsStringArray() throws Exception {
		int[] values = new int[] { 1, 0, 1, 2 };
		String[] commandArray = { "/bin/sh", TEMP_SCRIPT_FILE_PATH };
		File file = new File(TEMP_SCRIPT_FILE_PATH);
		file.deleteOnExit();

		for (int value : values) {
			ProcessStats stats;

			try {
				try (PrintWriter out = new PrintWriter(file)) {
					out.println("echo stdout");
					out.println("echo stderr 1>&2");
					out.println("exit " + value);
				}

				stats = LinuxProcessUtil.startWithStats(commandArray);
			} catch (Exception e) {
				throw e;
			} finally {
				file.delete();
			}

			String stdout = LinuxProcessUtil.getInputStreamAsString(stats.getInputStream());
			String stderr = LinuxProcessUtil.getInputStreamAsString(stats.getErrorStream());

			assertEquals(value, stats.getReturnValue());
			assertEquals("stdout\n", stdout);
			assertEquals("stderr\n", stderr);
		}
	}

	@Test
	public void testGetPidString() throws Exception {
		String command = "sleep 1";

		int errorCode = LinuxProcessUtil.start(command, false);
		assertEquals(0, errorCode);

		// Get process ID (an exception is thrown if PID is not found)
		int pid = LinuxProcessUtil.getPid("sleep");
		assertTrue(pid > 0);

		// Kill the process
		LinuxProcessUtil.kill(pid);
	}

	@Test
	public void testGetPidStringStringArray() throws Exception {
		String commandName = "echo";
		String[] commandArgs = { "aaa", "bbb", "ccc", "&&", "sleep", "1" };

		String command = commandName + " " + String.join(" ", commandArgs);

		int errorCode = LinuxProcessUtil.start(command, false);
		assertEquals(0, errorCode);

		// Get process ID (an exception is thrown if PID is not found)
		int pid = LinuxProcessUtil.getPid(commandName, commandArgs);
		assertTrue(pid > 0);

		// Kill the process
		LinuxProcessUtil.kill(pid);
	}

	// @Test
	// public void testGetKuraPid() {
	// // TODO: a kura instance needs to be running?
	// fail("Not yet implemented");
	// }

	// @Test
	// public void testStop() {
	// // TODO: test will fail because OSGi framework needs to be running
	// fail("Not yet implemented");
	// }

	// @Test
	// public void testKill() {
	// // TODO: test will fail because OSGi framework needs to be running
	// fail("Not yet implemented");
	// }

	// @Test
	// public void testKillAll() {
	// // TODO: test will fail because OSGi framework needs to be running
	// fail("Not yet implemented");
	// }

	// @Test
	// public void testStopAndKill() {
	// // TODO: test will fail because OSGi framework needs to be running
	// fail("Not yet implemented");
	// }

}
