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

import org.ecj.psh.PshDefaults;

import ec.util.Parameter;

public class HammingMetric extends SemanticsMetric {

	public static final String P_METRIC = "hamming-metric";

	@Override
	public Parameter defaultBase() {
		return PshDefaults.base().push(P_METRIC);
	}

	@Override
	protected double getStackDistance(float[] stack1, float[] stack2) {
		if (stack1 == null || stack2 == null)
			throw new InternalError();
		if (stack1.length == 0 && stack2.length == 0)
			return 0.0;
		if (stack1.length != stack2.length)
			return 1.0;
		for (int i = 0; i < stack1.length; i++) {
			if (Math.abs(stack1[i] - stack2[i]) > epsilon)
				return 1.0;
		}
		return 0.0;
	}

}
