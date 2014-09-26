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

import java.io.IOException;
import java.io.OutputStream;

class DelayedWrite extends DelayedStreamAction {
	private final byte[] data;
	private final String name;

	public DelayedWrite(long time, byte[] data, String name) {
		super(time);
		this.data = data;
		this.name = name;
	}

	@Override
	public void apply(OutputStream out) throws IOException {
		System.out.println(name + " " + data.length + " bytes");
		out.write(data);
	}

}