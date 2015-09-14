package pcclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * class Transthread
 * 
 * @version 1.0
 * @author dinglinhui
 */
public class Transthread extends Thread {

	private Socket socket;
	private String ip;
	private int port;
	private boolean bStop;
	private BufferedReader reader;// 输入流
	private PrintWriter writer;
	private InputStream is;
	private OutputStream os;
	public Lock lock = new ReentrantLock();
	public Queue<ByteBuffer> sendqueue;
	public Queue<ByteBuffer> recvqueue;

	final static int bufferSize = 1024;// 接收数据缓冲字节数组长度
	byte[] buffer = new byte[bufferSize];

	public Transthread() {
		bStop = false;
		sendqueue = new LinkedList<ByteBuffer>();
		recvqueue = new LinkedList<ByteBuffer>();
	}

	public void run() {

		System.out.println("transthread run!");
		while (true) {
			try {
				if (bStop) {
					bStop = false;
					break;
				}

				if (socket == null) {

					socket = new Socket();
					socket.connect(new InetSocketAddress(ip, port));
					// 读写流
					os = socket.getOutputStream();
					writer = new PrintWriter(os);
					// 输入流
					is = socket.getInputStream();
					reader = new BufferedReader(new InputStreamReader(is));
				} else {
					//
					lock.lock();
					if (!sendqueue.isEmpty()) {
						os.write(sendqueue.poll().array());
					}
					//
					if (reader.ready()) {
						int bytesRead = is.read(buffer);
						// System.out.println(CHexConver.byte2HexStr(buffer,
						// bytesRead));
						if (bytesRead > 0) {
							recvqueue.offer(ByteBuffer.wrap(buffer, 0, bytesRead));
						}
					}
					lock.unlock();
				}

			} catch (SocketException e) {

				try {
					if (socket != null && socket.isConnected() && !socket.isClosed())
						socket.close();
					socket = null;
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			} catch (IOException e) {
				try {
					socket.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}

			try {
				if (sendqueue.isEmpty() && recvqueue.isEmpty())
					Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		try {
			if (socket != null) {
				
				reader.close();
				writer.close();
				is.close();
				os.close();

				if (socket.isConnected() && !socket.isClosed()) {
					socket.close();
				}
			}
			socket = null;
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		System.out.println("thread stop");
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void stopThread() {
		bStop = true;
	}
}
