/*******************************************************************************
 * Copyright (c) 2017 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.kura.core.linux.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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
		int[] values = new int[] {0, 1, 2};
		String command = "/bin/sh " + TEMP_SCRIPT_FILE_PATH;
		File file = new File(TEMP_SCRIPT_FILE_PATH);
		file.deleteOnExit();
		
		for (int value : values) {
			int errorCode = -1;
			
			try {
				try(PrintWriter out = new PrintWriter(file)) {
				    out.println("exit " + value);
				}
				
				errorCode = LinuxProcessUtil.start(command, true, false);
			}
			catch (Exception e) {
				throw e;
			}
			finally {
				file.delete();
			}
			
			assertEquals(value, errorCode);
		}
	}

//	@Test
//	public void testStartStringBooleanBooleanNoWaitForeground() {
//      // TODO: test will fail because OSGi framework needs to be running
//		fail("Not yet implemented");
//	}

//	@Test
//	public void testStartStringBooleanBooleanWaitBackground() {
//      // TODO: test will fail because OSGi framework needs to be running
//		fail("Not yet implemented");
//	}

//	@Test
//	public void testStartStringBooleanBooleanNoWaitBackground() {
//      // TODO: test will fail because OSGi framework needs to be running
//		fail("Not yet implemented");
//	}

	@Test
	public void testStartString() throws Exception {
		int[] values = new int[] {0, 1, 2};
		String command = "/bin/sh " + TEMP_SCRIPT_FILE_PATH;
		File file = new File(TEMP_SCRIPT_FILE_PATH);
		file.deleteOnExit();
		
		for (int value : values) {
			int errorCode = -1;
			
			try {
				try(PrintWriter out = new PrintWriter(file)){
				    out.println("exit " + value);
				}
				
				errorCode = LinuxProcessUtil.start(command);
			}
			catch (Exception e) {
				throw e;
			}
			finally {
				file.delete();
			}
			
			assertEquals(value, errorCode);
		}
	}

	@Test
	public void testStartStringBooleanWait() throws Exception {
		int[] values = new int[] {0, 1, 2};
		String command = "/bin/sh " + TEMP_SCRIPT_FILE_PATH;
		File file = new File(TEMP_SCRIPT_FILE_PATH);
		file.deleteOnExit();
		
		for (int value : values) {
			int errorCode = -1;
			
			try {
				try(PrintWriter out = new PrintWriter(file)){
				    out.println("exit " + value);
				}
				
				errorCode = LinuxProcessUtil.start(command, true);
			}
			catch (Exception e) {
				throw e;
			}
			finally {
				file.delete();
			}
			
			assertEquals(value, errorCode);
		}
	}

	@Test
	public void testStartStringBooleanNoWait() throws Exception {
		// TODO: test fails because OSGi framework needs to be running
		int[] values = new int[] {0, 1, 2};
		
		for (int value : values) {
			int errorCode = -1;
			// TODO: make a script and execute it
			String command = "echo " + value + " > " + TEMP_OUTPUT_FILE_PATH;
			
			try {
				errorCode = LinuxProcessUtil.start(command, false);
			}
			catch (Exception e) {
				throw e;
			}
			
			assertEquals(0, errorCode);
			
			// Wait for process to finish
			int pid;
			
			do {
				pid = LinuxProcessUtil.getPid(command);
			} while (pid != -1);
			
			// Read output file content
			String readData = new String(Files.readAllBytes(Paths.get(TEMP_OUTPUT_FILE_PATH)));
			
			assertEquals(Integer.toString(value), readData);
		}
	}

	@Test
	public void testStartStringArrayBooleanWait() throws Exception {
		int[] values = new int[] {0, 1, 2};
		String[] commandArray = { "/bin/sh", TEMP_SCRIPT_FILE_PATH };
		File file = new File(TEMP_SCRIPT_FILE_PATH);
		file.deleteOnExit();
		
		for (int value : values) {
			int errorCode = -1;
			
			try {
				try(PrintWriter out = new PrintWriter(file)){
				    out.println("exit " + value);
				}
				
				errorCode = LinuxProcessUtil.start(commandArray, true);
			}
			catch (Exception e) {
				throw e;
			}
			finally {
				file.delete();
			}
			
			assertEquals(value, errorCode);
		}
	}

//	@Test
//	public void testStartStringArrayBooleanNoWait() {
//  	// TODO: test will fail because OSGi framework needs to be running
//		fail("Not yet implemented");
//	}

//	@Test
//	public void testStartBackground() {
//  	// TODO: test will fail because OSGi framework needs to be running
//		fail("Not yet implemented");
//	}

	@Test
	public void testStartWithStatsString() throws Exception {
		int[] values = new int[] {1, 0, 1, 2};
		String command = "/bin/sh " + TEMP_SCRIPT_FILE_PATH;
		File file = new File(TEMP_SCRIPT_FILE_PATH);
		file.deleteOnExit();
		
		for (int value : values) {
			ProcessStats stats;
			
			try {
				try(PrintWriter out = new PrintWriter(file)){
				    out.println("echo stdout");
				    out.println("echo stderr 1>&2");
				    out.println("exit " + value);
				}
				
				stats = LinuxProcessUtil.startWithStats(command);
			}
			catch (Exception e) {
				throw e;
			}
			finally {
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
		int[] values = new int[] {1, 0, 1, 2};
		String[] commandArray = { "/bin/sh", TEMP_SCRIPT_FILE_PATH };
		File file = new File(TEMP_SCRIPT_FILE_PATH);
		file.deleteOnExit();
		
		for (int value : values) {
			ProcessStats stats;
			
			try {
				try(PrintWriter out = new PrintWriter(file)){
				    out.println("echo stdout");
				    out.println("echo stderr 1>&2");
				    out.println("exit " + value);
				}
				
				stats = LinuxProcessUtil.startWithStats(commandArray);
			}
			catch (Exception e) {
				throw e;
			}
			finally {
				file.delete();
			}

			String stdout = LinuxProcessUtil.getInputStreamAsString(stats.getInputStream());
			String stderr = LinuxProcessUtil.getInputStreamAsString(stats.getErrorStream());

			assertEquals(value, stats.getReturnValue());
			assertEquals("stdout\n", stdout);
			assertEquals("stderr\n", stderr);
		}
	}

//	@Test
//	public void testGetPidString() {
//      // TODO: test will fail because OSGi framework needs to be running
//		fail("Not yet implemented");
//	}

//	@Test
//	public void testGetPidStringStringArray() {
//  	// TODO: test will fail because OSGi framework needs to be running
//		fail("Not yet implemented");
//	}

//	@Test
//	public void testGetKuraPid() {
//  	// TODO: a kura instance needs to be running?
//		fail("Not yet implemented");
//	}

//	@Test
//	public void testStop() {
//  	// TODO: test will fail because OSGi framework needs to be running
//		fail("Not yet implemented");
//	}

//	@Test
//	public void testKill() {
//  	// TODO: test will fail because OSGi framework needs to be running
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testKillAll() {
//  	// TODO: test will fail because OSGi framework needs to be running
//		fail("Not yet implemented");
//	}

//	@Test
//	public void testIsProcessRunning() {
//  	// TODO: test will fail because OSGi framework needs to be running
//		fail("Not yet implemented");
//	}

//	@Test
//	public void testStopAndKill() {
//  	// TODO: test will fail because OSGi framework needs to be running
//		fail("Not yet implemented");
//	}

}
