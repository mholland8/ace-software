package com.useful.socket;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

public class UsefulPubSocketTest
{

	@Test
	public void testUsefulPubSocket()
	{
		UsefulPubSocket s;
		try
		{
			s = new UsefulPubSocket();
			assertNotNull(s);
		}
		catch (IOException e)
		{
			fail("Constructor failed");
		}

	}

	@Test
	public void testBind()
	{
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testClose()
	{
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testSend()
	{
		fail("Not yet implemented"); // TODO
	}

}
