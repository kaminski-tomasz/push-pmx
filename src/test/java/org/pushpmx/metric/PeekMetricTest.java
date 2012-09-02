package org.pushpmx.metric;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.pushpmx.Semantics;

public class PeekMetricTest {

	PeekMetric metric;
	Semantics sem1, sem2;
	
	@Before
	public void prepare() {
		metric = new PeekMetric();
		sem1 = new Semantics();
		sem2 = new Semantics();
	}
	
	@Test
	public void test_get_stack_distance_hamming() {
		metric.type = PeekMetric.V_HAMMING;
		assertEquals(0, metric.getStackDistance(new float[0], new float[0]), 0);
		assertEquals(1, metric.getStackDistance(new float[0], new float[]{  1, 2 }), 0);
		assertEquals(1, metric.getStackDistance(new float[]{ 1, 2 }, new float[0]), 0);
		
		assertEquals(1, metric.getStackDistance(new float[]{ 1, 2 }, new float[]{ 1, 3 }), 0);
		assertEquals(0, metric.getStackDistance(new float[]{ 1, 2 }, new float[]{ 1, 2 }), 0);
		assertEquals(0, metric.getStackDistance(new float[]{ 1, 2 }, new float[]{ 5, 2 }), 0);
		assertEquals(0, metric.getStackDistance(new float[]{ 1, 2 }, new float[]{ 6, 5, 2 }), 0);
		assertEquals(0, metric.getStackDistance(new float[]{ 2 }, new float[]{ 5, 2 }), 0);
		assertEquals(0, metric.getStackDistance(new float[]{ 2 }, new float[]{ 2 }), 0);
		assertEquals(1, metric.getStackDistance(new float[]{ 3 }, new float[]{ 2 }), 0);
	}

	@Test
	public void test_get_stack_distance_euclidean() {
		metric.type = PeekMetric.V_EUCLIDEAN;
		assertEquals(0, metric.getStackDistance(new float[0], new float[0]), 0);
		assertEquals(Double.POSITIVE_INFINITY, metric.getStackDistance(new float[0], new float[]{  1, 2 }), 0);
		assertEquals(Double.POSITIVE_INFINITY, metric.getStackDistance(new float[]{ 1, 2 }, new float[0]), 0);
		
		assertEquals(1, metric.getStackDistance(new float[]{ 1, 2 }, new float[]{ 1, 3 }), 0);
		assertEquals(0, metric.getStackDistance(new float[]{ 1, 2 }, new float[]{ 1, 2 }), 0);
		assertEquals(0, metric.getStackDistance(new float[]{ 1, 2 }, new float[]{ 5, 2 }), 0);
		assertEquals(0, metric.getStackDistance(new float[]{ 1, 2 }, new float[]{ 6, 5, 2 }), 0);
		assertEquals(0, metric.getStackDistance(new float[]{ 2 }, new float[]{ 5, 2 }), 0);
		assertEquals(0, metric.getStackDistance(new float[]{ 2 }, new float[]{ 2 }), 0);
		assertEquals(1, metric.getStackDistance(new float[]{ 3 }, new float[]{ 2 }), 0);
		assertEquals(2, metric.getStackDistance(new float[]{ 1 }, new float[]{ 3 }), 0);
	}
	
}
