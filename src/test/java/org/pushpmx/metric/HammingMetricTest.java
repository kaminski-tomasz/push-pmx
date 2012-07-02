package org.pushpmx.metric;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.pushpmx.Semantics;

public class HammingMetricTest {

	HammingMetric metric;
	Semantics sem1, sem2;
	
	@Before
	public void prepare() {
		metric = new HammingMetric();
		sem1 = new Semantics();
		sem2 = new Semantics();
	}
	
	@Test
	public void test_get_stack_distance() {
		assertEquals(0, metric.getStackDistance(new float[0], new float[0]), 0);
		assertEquals(1, metric.getStackDistance(new float[0], new float[]{  1, 2 }), 0);
		assertEquals(1, metric.getStackDistance(new float[]{ 1, 2 }, new float[0]), 0);
		assertEquals(1, metric.getStackDistance(new float[]{ 1, 2 }, new float[]{ 1, 3 }), 0);
		assertEquals(0, metric.getStackDistance(new float[]{ 1, 2 }, new float[]{ 1, 2 }), 0);
	}
	
}
