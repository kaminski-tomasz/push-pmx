/*
* Copyright 2012 Tomasz Kamiński
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.pushpmx.metric;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.pushpmx.Semantics;

public class TopMetricTest {

	TopMetric metric;
	Semantics sem1, sem2;
	
	@Before
	public void prepare() {
		metric = new TopMetric();
		sem1 = new Semantics();
		sem2 = new Semantics();
	}
	
	@Test
	public void test_get_stack_distance_hamming() {
		metric.type = TopMetric.C_HAMMING;
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
	public void test_get_stack_distance_cityblock() {
		metric.type = TopMetric.C_CITYBLOCK;
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
		assertEquals(2, metric.getStackDistance(new float[]{ 1, 1 }, new float[]{ 3, 3 }), 0);
		assertEquals(12/3, metric.getStackDistance(new float[]{ 1, 2, 3 }, new float[]{ 5, 6, 7 }), 0);
	}
	
}
