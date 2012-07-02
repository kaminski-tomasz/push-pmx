package org.pushpmx.metric;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.pushpmx.Semantics;
import org.spiderland.Psh.floatStack;

public class SemanticsMetricTest {
	
	// we will use HammingMetric to inspect SemanticsMetric behaviour 
	HammingMetric metric;
	Semantics sem1, sem2;
	
	@Before
	public void prepare() {
		metric = new HammingMetric();
		sem1 = new Semantics();
		sem2 = new Semantics();
	}
			
	@Test
	public void test_get_distance_equal_semantics() {
		sem1.stackVector.clear();
		sem2.stackVector.clear();
		
		assertEquals(0, metric.getDistance(sem1, sem2), 0);
		
		floatStack stack = new floatStack();
		stack.push(1);
		stack.push(2);
		
		sem1.addFloatStack(stack);
		sem1.addFloatStack(stack);
				
		sem2.addFloatStack(stack);
		sem2.addFloatStack(stack);
		
		assertEquals(0, metric.getDistance(sem1, sem2), 0);
	}
	
	@Test
	public void test_get_distance_different_stack_sizes() {
		sem1.stackVector.clear();
		sem2.stackVector.clear();
		
		assertEquals(0, metric.getDistance(sem1, sem2), 0);
		
		floatStack stack = new floatStack();
		stack.push(1);
		stack.push(2);
		
		sem1.addFloatStack(stack);
		
		assertEquals(Float.POSITIVE_INFINITY, metric.getDistance(sem1, sem2), 0);
		
		sem1.addFloatStack(stack);
				
		sem2.addFloatStack(stack);
		sem2.addFloatStack(stack);
		sem2.addFloatStack(stack);
		
		assertEquals(Float.POSITIVE_INFINITY, metric.getDistance(sem1, sem2), 0);
	}
	
	@Test
	public void test_get_distance_different_stack_contents() {
		sem1.stackVector.clear();
		sem2.stackVector.clear();
		
		assertEquals(0, metric.getDistance(sem1, sem2), 0);
		
		floatStack stack1 = new floatStack();
		floatStack stack2 = new floatStack();
		
		stack1.push(1);
		stack1.push(2);
		stack2.push(1);
		sem1.addFloatStack(stack1);
		sem2.addFloatStack(stack2);
		assertEquals(1, metric.getDistance(sem1, sem2), 0);
		
		stack2.push(2);
		sem1.addFloatStack(stack1);
		sem2.addFloatStack(stack2);
		assertEquals(0.5, metric.getDistance(sem1, sem2), 0);
	}
}
