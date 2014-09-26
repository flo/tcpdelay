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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;

public class Server {
	private static int BUFFER_SIZE = 32 * 1024;

	public static void main(String[] args) throws IOException {
		if (args.length != 4) {
			System.err
					.println("Require 4 args: targetAddress targetPort port delay");
			System.exit(1);
			return;
		}
		String targetAddress = args[0];
		int targetPort = Integer.parseInt(args[1]);
		int port = Integer.parseInt(args[2]);
		int delay = Integer.parseInt(args[3]);
		final TimeUnit delayUnit = TimeUnit.MILLISECONDS;
		ServerSocket serverSocket = new ServerSocket(port);
		while (true) {
			Socket socket = serverSocket.accept();
			Socket secondSocket = null;
			try {
				secondSocket = new Socket(targetAddress, targetPort);
			} finally {
				if (secondSocket == null) {
					socket.close();
					System.err.printf("%s does not listen at %d!%n",
							targetAddress, targetPort);
					continue;
				}
			}
			delayedTransfer(socket, secondSocket, delay, delayUnit);
		}
	}

	public static void delayedTransfer(Socket s0, Socket s1, long delay,
			TimeUnit delayUnit) throws IOException {
		pipeDelayed(s0.getInputStream(), s1.getOutputStream(), delay,
				delayUnit, "->");
		pipeDelayed(s1.getInputStream(), s0.getOutputStream(), delay,
				delayUnit, "<-");
	}

	public static void pipeDelayed(final InputStream inputStream,
			final OutputStream outputStream, final long delay,
			final TimeUnit delayUnit, final String name) {
		final DelayQueue<DelayedStreamAction> queue = new DelayQueue<DelayedStreamAction>();
		Thread reader = new Thread() {
			final byte[] buffer = new byte[BUFFER_SIZE];

			@Override
			public void run() {
				final DelayedStreamActionsFactory factory = new DelayedStreamActionsFactory(
						delay, delayUnit, name);
				try {
					try {
						int readedBytes;
						do {
							readedBytes = inputStream.read(buffer);
							if (readedBytes != -1) {
								byte[] data = Arrays
										.copyOf(buffer, readedBytes);
								DelayedWrite delayed = factory
										.createWriteAction(data);
								queue.put(delayed);
							}
						} while (readedBytes != -1);
					} finally {
						queue.put(factory.createCloseAction());
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		reader.start();

		Thread writer = new Thread() {
			@Override
			public void run() {
				try {
					try {
						try {
							boolean closed = false;
							while (!closed) {
								DelayedStreamAction delayed = queue.take();
								delayed.apply(outputStream);
								if (delayed instanceof DelayedClose) {
									closed = true;
								}
							}
						} finally {
							inputStream.close();
						}
					} finally {
						outputStream.close();
					}
				} catch (InterruptedException e) {
					// do nothing: thread will terminate anyway
				} catch (IOException e) {
					/*
					 * Everything reasonable to do gets already done in the
					 * finally blocks.
					 */
					e.printStackTrace();
				}
			}
		};
		writer.start();
	}

}
