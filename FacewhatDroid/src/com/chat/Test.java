package com.chat;

import junit.framework.Assert;
import android.test.AndroidTestCase;
import android.util.Log;

public class Test  extends AndroidTestCase{
//	private Calculator calculator;
	/**
	*  This method is invoked before any of the test methods in the class.
	*  Use it to set up the environment for the test (the test fixture. 
	*  You can use setUp() to instantiate a new Intent with the action ACTION_MAIN. 
	*  You can then use this intent to start the Activity under test.
	*/
	@Override
	protected void setUp() throws Exception {
		Log.e("test", "setUp");
		super.setUp();
	}

	/**
	 * 测试Calculator的add(int x, int y)方法
	 * 把异常抛给测试框架
	 * @throws Exception
	 */
//	public void testAdd() throws Exception{
//		int result = calculator.add(2, 5);
//		Assert.assertEquals(8, result);
//	}

	/**
	 * This method is invoked after all the test methods in the class.
	 * Use it to do garbage collection and to reset the test fixture.
	 */
	@Override
	protected void tearDown() throws Exception {
		Log.e("test", "tearDown");
//		calculator = null;
		super.tearDown();
	}
}
