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
import static org.mockito.Mockito.*;

import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.kura.core.util.SafeProcess;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ProcessStatsTest {

	@Test
	public void testGetProcess() {
		SafeProcess mockSafeProcess = mock(SafeProcess.class);
		
		ProcessStats stats = new ProcessStats(mockSafeProcess);
		assertEquals(mockSafeProcess, stats.getProcess());
	}

	@Test
	public void testGetOutputStream() {
		OutputStream mockStream = mock(OutputStream.class);
		SafeProcess mockSafeProcess = mock(SafeProcess.class);
		when(mockSafeProcess.getOutputStream()).thenReturn(mockStream);
		
		ProcessStats stats = new ProcessStats(mockSafeProcess);
		assertEquals(mockStream, stats.getOutputStream());
	}

	@Test
	public void testGetInputStream() {
		InputStream mockStream = mock(InputStream.class);
		SafeProcess mockSafeProcess = mock(SafeProcess.class);
		when(mockSafeProcess.getInputStream()).thenReturn(mockStream);
		
		ProcessStats stats = new ProcessStats(mockSafeProcess);
		assertEquals(mockStream, stats.getInputStream());
	}

	@Test
	public void testGetErrorStream() {
		InputStream mockStream = mock(InputStream.class);
		SafeProcess mockSafeProcess = mock(SafeProcess.class);
		when(mockSafeProcess.getErrorStream()).thenReturn(mockStream);
		
		ProcessStats stats = new ProcessStats(mockSafeProcess);
		assertEquals(mockStream, stats.getErrorStream());
	}

	@Test
	public void testGetReturnValue() {
		int[] values = new int[] {-1, 0, 1};
		
		for (int value : values) {
			SafeProcess mockSafeProcess = mock(SafeProcess.class);
			when(mockSafeProcess.exitValue()).thenReturn(value);
			
			ProcessStats stats = new ProcessStats(mockSafeProcess);
			assertEquals(value, stats.getReturnValue());
		}
	}

}
