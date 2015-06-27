package org.tangence.java;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.lang.ClassNotFoundException;
import java.lang.IllegalAccessException;
import java.lang.InstantiationException;
import java.lang.NoSuchMethodException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.net.InetSocketAddress;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.tangence.java.Constants.*;

/**
 * Client handling.
 * This does the client bit.
 */
public class Client extends TangenceBase implements Connection {
	private static final Logger log = LoggerFactory.getLogger(Client.class.getName());
	private boolean writing = false;

	/**
	 * Represents an outgoing message.
	 * Used as a temporary holder for the ByteBuffer containing the message,
	 * and the {@link Future} status for tracking when we start sending the
	 * message and when we've completed message delivery.
	 */

	private String hostname;
	private int port;

	private long sentBytes = 0;
	private long recvBytes = 0;
	private long sentMessages = 0;
	private long recvMessages = 0;

	/** List of messages that we're waiting to send */
	private List<OutgoingMessage> pendingOutput = new ArrayList<OutgoingMessage>();
	/** List of messages that have been sent and are now awaiting responses */
	private List<OutgoingMessage> awaitingResponse = new ArrayList<OutgoingMessage>();

	/** True when we're currently connecting */
	private boolean connecting = false;
	/** True when we have established a connection */
	private boolean connected = false;

	private Selector selector;

	/**
	 * Instantiates a new client.
	 * @param hostname the hostname or IP address to connect to
	 * @param port the port to connect to
	 */
	public Client(final String hostname, final int port) {
		this.hostname = hostname;
		this.port = port;
		try {
			selector = Selector.open();
		} catch(final IOException e) {
			log.error("Exception when trying to set up a selector" + e.getMessage());
			selector = null;
		}
		registry().connection(this);
	}

	public class ConnectionThread extends Thread {
		public ConnectionThread() {
		}

		@Override
		public void run() {
			try {
				final SocketChannel client = connect(30 * 1000);
				poll(client, 0);
				log.info("Client thread about to finish");
			} catch(IOException e) {
				log.error("Had an IO exception in client: " + e.getMessage());
			}
		}
	}

	private ConnectionThread thread = null;
	private Future<TangenceObjectProxy> connectionFuture = new Future<>();

	public Future<TangenceObjectProxy> run() {
		thread = new ConnectionThread();
		thread.start();
		return connectionFuture;
	}

	/**
	 * Method connect.
	 * @param timeout long
	 * @return SocketChannel
	 * @throws IOException
	 */
	private SocketChannel connect(final long timeout) throws IOException {
		/** Clientside */
		final SocketChannel client = SocketChannel.open();
		client.configureBlocking(false);
		client.connect(new InetSocketAddress(hostname, port));
		connecting = true;
		connected = false;
		return client;
	}

	/**
	 * Method onConnected.
	 * @param chan SocketChannel
	 * @throws IOException
	 * @throws TangenceException
	 */
	void onConnected(final SocketChannel chan) throws IOException, TangenceException {
		startup("tom@roku/GTK2");
	}

	/**
	 * Send startup sequence.
	 * @param name String
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 * @throws TangenceException
	 */
	public void startup(final String name) throws IOException, UnsupportedEncodingException, TangenceException {
		log.debug("Queuing startup sequence");

		Future.needsAll(
			initMessage().completion(),
			getRegistry().completion(),
			getRoot(name).completion().add(
				new Future.DefaultTask<Object>() {
					@Override
					public void onDone(final Object param) throws TangenceException {
						connectionFuture.done(registry().getObject(new Long(1)));
					}
				}
			)
		).add(new Future.Task<Object>() {
			public void onReady(final Future f) {
				log.debug("Ready after startup");
			}
			public void onCancel(final Future f) {
				log.debug("Cancelled after startup");
			}
			public void onDone(final Object param) throws TangenceException {
				log.debug("Done after startup");
			}
			public void onFail(final Throwable e) throws TangenceException {
				log.debug("Fail after startup: %s%n", e.getMessage());
			}
		});
	}

	/**
	 * Method initMessage.
	 * @return OutgoingMessage
	 * @throws TangenceException
	 */
	public OutgoingMessage initMessage() throws TangenceException {
		final OutgoingMessage om = writeMessage((byte)MSG_INIT, new byte[] {
			DATANUM_UINT8, (byte)MAJOR_VERSION,
			DATANUM_UINT8, (byte)MINOR_VERSION,
			DATANUM_UINT8, (byte)MINOR_VERSION_MIN
		});
		return om;
	}

	/**
	 * Method getRegistry.
	 * @return OutgoingMessage
	 * @throws TangenceException
	 */
	public OutgoingMessage getRegistry() throws TangenceException {
		return writeMessage((byte)MSG_GETREGISTRY, new byte[] {
		});
	}

	/**
	 * Method getRoot.
	 * @param clientName String
	 * @return OutgoingMessage
	 * @throws TangenceException
	 * @throws IOException
	 */
	public OutgoingMessage getRoot(final String clientName) throws TangenceException, IOException {
		return writeMessage(
			(byte)MSG_GETROOT,
			Types.bytesForStr(clientName)
		);
	}

	/**
	 * Method writeMessage.
	 * @param type byte
	 * @param payload byte[]
	 * @return OutgoingMessage
	 * @throws TangenceException
	 */
	public synchronized OutgoingMessage writeMessage(final byte type, final byte[] payload) throws TangenceException {
		log.debug("Writing data");
		final ByteBuffer buffer = ByteBuffer.allocate(1 + 4 + payload.length);
		buffer.put(type); // single byte, write as-is
		buffer.putInt(payload.length); // uint32, default is always big-endian, so again write as-is
		buffer.put(payload); // 0..n byte buffer, write as-is
		buffer.flip();
		final OutgoingMessage m = new OutgoingMessage(buffer);
		m.request(messageFromBuffer(buffer));
		m.response().add(new Future.DefaultTask<TangenceMessage>() {
			public void onDone(final TangenceMessage resp) throws TangenceException {
				log.debug("Had response to some message: {}", resp);
				if(resp.type() == MSG_ERROR) {
					final TangenceMessageError err = (TangenceMessageError) resp;
					log.error(String.format("Received error %s", err.text()));
				}
				m.request().onResponse(resp);
			}
		});
		buffer.rewind();
		pendingOutput.add(m);
		selector.wakeup();
		return m;
	}

	public synchronized OutgoingMessage writeMessage(final TangenceMessage request) throws TangenceException {
		final OutgoingMessage m = new OutgoingMessage(request);
		m.response().add(new Future.DefaultTask<TangenceMessage>() {
			public void onDone(final TangenceMessage resp) throws TangenceException {
				log.debug("Had response to some message: {}", resp);
				m.request().onResponse(resp);
			}
		});
		final ByteBuffer buffer = m.buffer();
		buffer.rewind();
		pendingOutput.add(m);
		selector.wakeup();
		return m;
	}

	/**
	 * Method handleIncomingMessage.
	 * 
	 * @param m TangenceMessage
	 */
	private void handleIncomingMessage(final TangenceMessage m) throws TangenceException {
		log.debug(String.format("Incoming message: %02x [%s], length = %d", m.type(), messageName(m.type()), m.length()));
		switch(m.type()) {
		case MSG_UPDATE: {
			final TangenceMessageUpdate request = (TangenceMessageUpdate) m;
			log.debug(String.format("We have an update for %d on %s", (int)request.id(), request.name()));
			final TangenceMessageOK reply = new TangenceMessageOK(
				Constants.MSG_OK,
				0,
				registry()
			);
			writeMessage(reply);
			break;
		}
		case MSG_DESTROY: {
			final TangenceMessageDestroy request = (TangenceMessageDestroy) m;
			log.debug(String.format("Object destruction requested for %d", (int)request.id()));
			final TangenceMessageOK reply = new TangenceMessageOK(
				Constants.MSG_OK,
				0,
				registry()
			);
			writeMessage(reply);
			break;
		}
		case MSG_EVENT: {
			final TangenceMessageEvent request = (TangenceMessageEvent) m;
			log.debug(String.format("Event %s for %d", request.name(), (int)request.id()));
			final TangenceMessageOK reply = new TangenceMessageOK(
				Constants.MSG_OK,
				0,
				registry()
			);
			writeMessage(reply);
			break;
		}
		default: {
			log.debug(String.format("Unexpected unknown type %d", m.type()));
			break;
		}
		}
	}

	private synchronized void checkPending(final SocketChannel chan, final SelectionKey clientKey, final SelectionKey key) throws IOException {
		if(pendingOutput.isEmpty() || !awaitingResponse.isEmpty()) {
			log.debug("Outgoing buffer empty, deregister for write");
			clientKey.interestOps(clientKey.interestOps() & ~SelectionKey.OP_WRITE);
		} else {
			if(key != null && key.isValid() && key.isWritable()) {
				log.debug("We can now write data");
				writing = true;
				final OutgoingMessage m = pendingOutput.get(0);
				final ByteBuffer b = m.buffer();
				sentBytes += (long) chan.write(b);
				try {
					if(!m.started()) m.onStart();
					if(!b.hasRemaining()) {
						log.debug("Message complete");
						++sentMessages;
						pendingOutput.remove(0);
						if(!m.isReply())
							awaitingResponse.add(m);
					}
				} catch(TangenceException e) {
					log.debug("Exception occurred: {}", e.getMessage());
				}
			} else {
				log.debug("Have data, but not yet able to write");
				clientKey.interestOps(clientKey.interestOps() | SelectionKey.OP_WRITE);
			}
		}
	}

	/**
	 * Attempt to deliver any pending messages and retrieve
	 * incoming data.
	 *
	 * This may involve blocking operations so would typically
	 * be called in your main networking thread. Wake this one
	 * up if there's data to write and you're not already in
	 * write mode.
	 * @param chan SocketChannel
	 * @param timeout long
	 * @throws IOException
	 * */
	public void poll(final SocketChannel chan, long timeout) throws IOException {
		writing = !pendingOutput.isEmpty();
		final SelectionKey clientKey = chan.register(
			selector,
			SelectionKey.OP_READ | (
				 writing ? SelectionKey.OP_WRITE : 0
			) | (
				 connecting ? SelectionKey.OP_CONNECT : 0
			)
		);

		log.debug("Waiting for events");
		final ByteBuffer buffer = ByteBuffer.allocate(5);
		ByteBuffer current = null;
		timeout = 0;
		boolean active = true;
		while(selector.select(timeout) >= 0) {
			final Set keys = selector.selectedKeys();
			final Iterator i = keys.iterator();

			while (i.hasNext()) {
				log.debug("Check next");

				/* Extract this event */
				final SelectionKey key = (SelectionKey)i.next();
				i.remove();
				log.debug(
					String.format(
						"Ready: %02x, interested: %02x",
						key.readyOps(),
						key.interestOps()
					)
				);

				final SocketChannel channel = (SocketChannel)key.channel();
				/* We have something to read - pass this off for immediate
				 * processing, since we may have an incoming event which
				 * would affect a pending output step
				 */
				if (connecting && key.isValid() && key.isConnectable()) {
					log.debug("Server Found");

					connected = true;
					connecting = false;
					if (channel.isConnectionPending())
						channel.finishConnect();

					try {
						onConnected(channel);
					} catch(TangenceException e) {
						log.error("Exception: {}", e.getMessage());
					}
				}
				if (key.isValid() && key.isReadable()) {
					log.debug("We have some data to read");
					if(current != null) {
						int c = chan.read(current);
						if(c < 0) {
							log.debug("Negative read, connection probably died");
							chan.close();
							active = false;
						} else {
							recvBytes += (long) c;
							if(!current.hasRemaining()) {
								log.debug("We have read a complete message");
								++recvMessages;
								current.rewind();
								try {
									final ByteBuffer b = current.asReadOnlyBuffer();
									final TangenceMessage m = messageFromBuffer(b);
									m.parse(b);
									if(m.isResponse()) {
										log.debug("This is a response, we have {} in the queue", awaitingResponse.size());
										final OutgoingMessage original = awaitingResponse.get(0);
										log.debug("Our original is {}", original.request().name());
										awaitingResponse.remove(0);
										original.response().done(m);
										original.completion().done(m);
									} else {
										handleIncomingMessage(m);
									}
								} catch(TangenceException e) {
									log.debug(e.getMessage());
								} finally {
									current = null;
								}
							}
						}
						log.debug(bytesToHex(buffer.array()));
					} else {
						int c = chan.read(buffer);
						if(c < 0) {
							log.debug("Negative read, connection probably died");
							chan.close();
							active = false;
						} else {
							recvBytes += (long) c;
							if(c >= 5) {
								buffer.rewind();
								log.debug("We have enough data to know how much data we want");
								int type = buffer.get();
								int len = buffer.getInt();
								current = ByteBuffer.allocate(1 + 4 + len);
								current.put((byte)type);
								current.putInt(len);
								buffer.clear();
							}
						}
					}
				}

				/* If we have something to write and we are write-ready
				 * then go ahead and send as much as we can
				 */
				checkPending(chan, clientKey, key);
			}

			if((clientKey.interestOps() & SelectionKey.OP_CONNECT) != 0 && !connecting) {
				log.debug("Already connected, deregister for connect");
				clientKey.interestOps(clientKey.interestOps() & ~SelectionKey.OP_CONNECT);
			} else if((clientKey.interestOps() & SelectionKey.OP_CONNECT) == 0 && connecting) {
				log.debug("Connecting, register for connect");
				clientKey.interestOps(clientKey.interestOps() | SelectionKey.OP_CONNECT);
			}
			checkPending(chan, clientKey, null);
			log.debug(String.format("Bytes read so far: %d (%d messages), written: %d (%d messages)", recvBytes, recvMessages, sentBytes, sentMessages));
		}
	}

	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	/**
	 * Method bytesToHex.
	 * @param bytes byte[]
	 * @return String
	 */
	public static String bytesToHex(final byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		int v;
		for ( int j = 0; j < bytes.length; j++ ) {
			v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}
}

