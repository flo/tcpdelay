/*
 * Copyright 2010 Florian Köberle
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fkoeberle.tcpdelay;

import java.util.concurrent.TimeUnit;

class DelayedStreamActionsFactory {
	private final long delayInNS;
	private long lastTime = 0;
	private final String name;

	public DelayedStreamActionsFactory(long delay, TimeUnit delayUnit,
			String name) {
		this.delayInNS = TimeUnit.NANOSECONDS.convert(delay, delayUnit);
		this.name = name;
	}

	private long calculateExecutionTime() {
		long executionTime = System.nanoTime() + delayInNS;
		if (executionTime <= lastTime) {
			executionTime = lastTime + 1;
		}
		lastTime = executionTime;
		return executionTime;
	}

	public DelayedClose createCloseAction() {
		return new DelayedClose(calculateExecutionTime());
	}

	public DelayedWrite createWriteAction(byte[] data) {
		return new DelayedWrite(calculateExecutionTime(), data, name);
	}
}