package pcclient;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/**
 * class PCClientEx
 * @version 1.0 01/12/99
 * @author dinglinhui
 */
public class PCClientEx extends JFrame implements ActionListener, Runnable {

	private static final long serialVersionUID = 1L;

	//Request Command
	private final static int TRANSMIT_CHNL_A = 0X31;//Transmit all channel
	private final static int TRANSMIT_CHNL_S = 0X32;//Transmit single channel
	private final static int TRANSMIT_AI = 0X33;//Transmit Ai
	private final static int TRANSMIT_COS = 0X34;//Transmit Cos
	private final static int TRANSMIT_DO = 0X35;//Transmit Do
	//Response Command
	private final static int TRANSMIT_CHNL_A_RSP = 0XB1;
	private final static int TRANSMIT_CHNL_S_RSP = 0XB2;
	private final static int TRANSMIT_AI_RSP = 0XB3;
	private final static int TRANSMIT_COS_RSP = 0XB4;
	private final static int TRANSMIT_DO_RSP = 0XB5;
	private final static int TRANSMIT_END_RSP = 0XBF;//Transmit End Packet
	//
	private final static int CHANNEL_MAX = 10; // max channel number
	private final static int HEADNUMBER = 26; // packet head number
	//
	private final static String RESOURCE_IMAGES_PATH = "resource" + File.separator + "images" + File.separator + "64" + File.separator; // 报文头的长度
	//
	private IconNode root;//tree root node
	private JTree tree;//tree view
	private JTable table;//table view
	private JButton cfg, reload, export, info, close;//button action
	private JFileChooser fc;//file choose dialog
	private int chnlIndex;//current select channel index
	private ProgressBar processbar;//process bar
	private boolean process;//process bar flag
	public Transthread transthread;//transmit communicate thread
	private Thread runner;//pcclient thread
	private boolean bRunable, bFinish;//pcclient thread flag, end packet flag 
	
	//model each for ai\cos\do
	private TransTableModel[] aitableModel = new TransTableModel[CHANNEL_MAX];
	private TransTableModel[] costableModel = new TransTableModel[CHANNEL_MAX];
	private TransTableModel[] dotableModel = new TransTableModel[CHANNEL_MAX];
	private String[] aiheadings = { "序号", "信息地址", "装置地址", "描述", "门槛类型", "门槛值", "偏移量B", "乘积系数K" };
	private String[] cosheadings = { "序号", "信息地址", "装置地址", "描述", "状态修正(1-Normal,2-Inverted)" };
	private String[] doheadings = { "序号", "信息地址", "装置地址", "描述" };

	/**
	 * PCClientEx constructor
	 * @version 1.0
	 * @author dinglinhui
	 */
	public PCClientEx() {
		super("pcclient ex");
		Image image = Toolkit.getDefaultToolkit().getImage("resource/images/256/virus.png");
		this.setIconImage(image);

		this.addMenuBar();
		this.addToolBar();

		runner = new Thread(this);
		transthread = new Transthread();
		root = new IconNode("转发表");
		root.setIcon(getImageIcon(RESOURCE_IMAGES_PATH + "aim.png", 16, 16));
		tree = new JTree(root);
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent evt) {

				DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
				if (selectedNode != null) {

					if (selectedNode.getParent() != null) {

						if (selectedNode.getParent().getParent() != null) {
							// 召唤具体通道下的某个类型数据
							if (selectedNode.toString().equals("遥测")) {
								chnlIndex = root.getIndex(selectedNode.getParent());
								table.setModel(aitableModel[chnlIndex]);
							}

							if (selectedNode.toString().equals("遥信")) {
								chnlIndex = root.getIndex(selectedNode.getParent());
								table.setModel(costableModel[chnlIndex]);
							}

							if (selectedNode.toString().equals("遥控")) {
								chnlIndex = root.getIndex(selectedNode.getParent());
								table.setModel(dotableModel[chnlIndex]);
							}
						} else {
							// 召唤具体通道
							chnlIndex = root.getIndex(selectedNode);
							table.setModel(aitableModel[chnlIndex]);
						}

						FitTableColumns(table);
					} else {
						chnlIndex = -1;
					}
				} else {
					chnlIndex = -1;
				}
			}
		});

		tree.setCellRenderer(new IconNodeRenderer());
		JScrollPane treeSp = new JScrollPane(tree);

		for (int i = 0; i < CHANNEL_MAX; i++) {
			aitableModel[i] = new TransTableModel(aiheadings);
			costableModel[i] = new TransTableModel(cosheadings);
			dotableModel[i] = new TransTableModel(doheadings);
		}

		table = new JTable(aitableModel[0]);
		table.setColumnSelectionAllowed(true);
		table.setRowSelectionAllowed(true);
		JScrollPane tableSp = new JScrollPane(table);

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeSp, tableSp);
		splitPane.setDividerLocation(200);

		this.getContentPane().add(splitPane, BorderLayout.CENTER);
		runner.start();
		bRunable = true;
		bFinish = false;
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				super.windowClosing(e);
				closeWindow();
			}
		});
	}

	/**
	 * request packet function
	 * @version 1.0
	 * @author dinglinhui
	 */
	public void switchRequest(int nFlag, int nChannel) {

		switch (nFlag) {
		case TRANSMIT_CHNL_A:
		case TRANSMIT_CHNL_S:
		case TRANSMIT_AI:
		case TRANSMIT_COS:
		case TRANSMIT_DO: {

			byte[] buffer = new byte[HEADNUMBER];

			int nPos = 0;
			buffer[nPos++] = 0X68;
			buffer[nPos++] = 0X00;
			buffer[nPos++] = 0X00;
			buffer[nPos++] = 0X00;
			buffer[nPos++] = 0X00;
			buffer[nPos++] = (byte) nFlag;
			buffer[nPos++] = 0X01;
			buffer[23] = 0X00;
			buffer[24] = 0X00;
			buffer[25] = (byte) nChannel;

			transthread.sendqueue.offer(ByteBuffer.wrap(buffer));

			if (nFlag == TRANSMIT_CHNL_S) {
				aitableModel[nChannel].clearModel();
				costableModel[nChannel].clearModel();
				dotableModel[nChannel].clearModel();
			}

			if (nFlag == TRANSMIT_AI) {
				aitableModel[nChannel].clearModel();
			}

			if (nFlag == TRANSMIT_COS) {
				costableModel[nChannel].clearModel();
			}

			if (nFlag == TRANSMIT_DO) {
				dotableModel[nChannel].clearModel();
			}

			break;
		}
		}
	}

	/**
	 * response packet function
	 * @version 1.0
	 * @author dinglinhui
	 */
	public void switchResponse(ByteBuffer buf) {

		try {
			buf.order(ByteOrder.LITTLE_ENDIAN);
			byte[] buffer = buf.array();
			int length = buf.limit();
			int nPos = 0;

			while (nPos < length) {

				int flag = 0;
				int nRecordNum = 0;
				int nChannelId = 0;

				if (buffer[nPos] == 0x68) {
					flag = buffer[nPos + 5] & 0xFF;
					nRecordNum = buffer[nPos + 24] & 0xFF;
					nChannelId = buffer[nPos + 25] & 0xFF;

					// 报文头
					buf.get(new byte[HEADNUMBER]);
					nPos += HEADNUMBER;

					// 报文数据
					switch (flag) {
					case TRANSMIT_CHNL_A_RSP: {
						System.out.println("TRANSMIT_CHNL_A_RSP");
						for (int i = 0; i < nRecordNum; i++) {
							byte[] chnl = new byte[16];
							buf.get(chnl);
							// buf.slice();
							System.out.println(new String(chnl));
							this.addTreeNode(new String(chnl));
							nPos += 16;
						}
						expandAll();
						break;
					}
					case TRANSMIT_CHNL_S_RSP:
					case TRANSMIT_AI_RSP:
					case TRANSMIT_COS_RSP:
					case TRANSMIT_DO_RSP: {
						for (int i = 0; i < nRecordNum; i++) {

							Vector<String> list = new Vector<String>();
							list.clear();

							int pointType = buf.get() & 0xFF;
							nPos++;

							int index = buf.getShort() & 0xFFFF;
							nPos += 2;
							list.add(String.valueOf(index));

							int infoaddr = buf.getInt();
							nPos += 4;
							list.add(String.valueOf(infoaddr));

							int iedaddr = buf.getInt();
							nPos += 4;
							list.add(String.valueOf(iedaddr));

							int desclen = buf.get() & 0xFF;
							nPos++;

							byte[] desc = new byte[desclen];
							buf.get(desc);
							nPos += desclen;

							try {
								list.add(new String(desc, "UTF-8"));
							} catch (UnsupportedEncodingException e) {
								e.printStackTrace();
							}

							if (pointType == TRANSMIT_AI_RSP) {

								int thresholdType = buf.get() & 0xFF;
								nPos++;
								list.add(String.valueOf(thresholdType));

								float threshold = buf.getFloat();
								nPos += 4;
								list.add(String.valueOf(threshold));

								float offset = buf.getFloat();
								nPos += 4;
								list.add(String.valueOf(offset));

								float multifactor = buf.getFloat();
								nPos += 4;
								list.add(String.valueOf(multifactor));

								aitableModel[nChannelId].addElement(list);

							} else if (pointType == TRANSMIT_COS_RSP) {
								int pointMode = buf.getInt();
								nPos += 4;
								list.add(String.valueOf(pointMode));
								costableModel[nChannelId].addElement(list);

							} else if (pointType == TRANSMIT_DO_RSP) {

								dotableModel[nChannelId].addElement(list);
							} else {
								System.out.println("error pointType:" + pointType);
							}

						}
						break;
					}
					case TRANSMIT_END_RSP: {
						System.out.println("TRANSMIT_END_RSP");
						bFinish = true;
						break;
					}
					}
					FitTableColumns(table);
				} else {
					nPos++;

					if (nPos < length)
						buf.get();
				}
			}
		} catch (java.nio.BufferUnderflowException e) {
			System.out.println("BufferUnderflowException");
		} catch (java.lang.ArrayIndexOutOfBoundsException e1) {
			System.out.println("ArrayIndexOutOfBoundsException");
		}
	}

	
	public static String byteBufferToString(ByteBuffer buffer) {
		CharBuffer charBuffer = null;
		try {
			Charset charset = Charset.forName("UTF-8");
			CharsetDecoder decoder = charset.newDecoder();
			charBuffer = decoder.decode(buffer);
			buffer.flip();
			return charBuffer.toString();
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	/**
	 * auto fit table columns width
	 * @version 1.0
	 * @author dinglinhui
	 */
	public void FitTableColumns(JTable myTable) {
		JTableHeader header = myTable.getTableHeader();
		int rowCount = myTable.getRowCount();
		Enumeration columns = myTable.getColumnModel().getColumns();
		while (columns.hasMoreElements()) {
			TableColumn column = (TableColumn) columns.nextElement();
			int col = header.getColumnModel().getColumnIndex(column.getIdentifier());
			int width = (int) myTable.getTableHeader().getDefaultRenderer().getTableCellRendererComponent(myTable, column.getIdentifier(), false, false, -1, col).getPreferredSize().getWidth();
			for (int row = 0; row < rowCount; row++) {
				int preferedWidth = (int) myTable.getCellRenderer(row, col).getTableCellRendererComponent(myTable, myTable.getValueAt(row, col), false, false, row, col).getPreferredSize().getWidth();
				width = Math.max(width, preferedWidth);
			}
			header.setResizingColumn(column); // 此行很重要
			column.setWidth(width + myTable.getIntercellSpacing().width);
		}
	}

	/**
	 * window close function
	 * @version 1.0
	 * @author dinglinhui
	 */
	public void closeWindow() {
		if (JOptionPane.showConfirmDialog(null, "确定退出pcclient ex?", "退出pcclient ex", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			transthread.stopThread();
			bRunable = false;
			try {
				transthread.join();
				runner.join();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			System.exit(0);
		}
	}

	/**
	 * add menubar function
	 * @version 1.0
	 * @author dinglinhui
	 */
	private void addMenuBar() {

		JMenuBar menuBar = new JMenuBar();
		this.setJMenuBar(menuBar);

		JMenu helpmenu = new JMenu("帮助(H)");
		helpmenu.setMnemonic('H');

		JMenuItem helpMenuBanben = new JMenuItem("版本(B)");
		helpmenu.add(helpMenuBanben);
		menuBar.add(helpmenu);
	}

	/**
	 * add toolbar function
	 * @version 1.0
	 * @author dinglinhui
	 */
	private void addToolBar() {

		JToolBar toolBar = new JToolBar();
		toolBar.setRollover(true);
		toolBar.setBounds(0, 0, 700, 30);
		toolBar.setFloatable(true);
		this.getContentPane().add(toolBar, BorderLayout.NORTH);

		cfg = new JButton(getImageIcon(RESOURCE_IMAGES_PATH + "antenna2.png", 24, 24));
		cfg.setText("配置");
		cfg.addActionListener(this);
		toolBar.add(cfg);
		toolBar.addSeparator();

		reload = new JButton(getImageIcon(RESOURCE_IMAGES_PATH + "reload.png", 24, 24));
		reload.setText("加载");
		reload.addActionListener(this);
		reload.setEnabled(false);
		toolBar.add(reload);

		export = new JButton(getImageIcon(RESOURCE_IMAGES_PATH + "diskette.png", 24, 24));
		export.setText("导出");
		export.addActionListener(this);
		export.setEnabled(false);
		toolBar.add(export);

		toolBar.addSeparator();
		info = new JButton(getImageIcon(RESOURCE_IMAGES_PATH + "info.png", 24, 24));
		info.setText("信息");
		info.addActionListener(this);
		toolBar.add(info);

		close = new JButton(getImageIcon(RESOURCE_IMAGES_PATH + "close.png", 24, 24));
		close.setText("退出");
		close.addActionListener(this);
		toolBar.add(close);
	}

	/**
	 * add tree node function
	 * @version 1.0
	 * @author dinglinhui
	 */
	private boolean addTreeNode(String name) {

		for (int i = 0; i < root.getChildCount(); i++) {
			TreeNode node = root.getChildAt(i);
			if (name.equals(node.toString())) {
				return false;
			}
		}

		IconNode nodesChnl = new IconNode(name);
		IconNode[] nodesType = new IconNode[3];
		nodesChnl.setIcon(getImageIcon(RESOURCE_IMAGES_PATH + "ligthbulb_on.png", 16, 16));

		nodesType[0] = new IconNode("遥测");
		nodesType[0].setIcon(getImageIcon(RESOURCE_IMAGES_PATH + "Orange Ball.png", 16, 16));
		nodesChnl.add(nodesType[0]);

		nodesType[1] = new IconNode("遥信");
		nodesType[1].setIcon(getImageIcon(RESOURCE_IMAGES_PATH + "Green Ball.png", 16, 16));
		nodesChnl.add(nodesType[1]);

		nodesType[2] = new IconNode("遥控");
		nodesType[2].setIcon(getImageIcon(RESOURCE_IMAGES_PATH + "Yellow Ball.png", 16, 16));
		nodesChnl.add(nodesType[2]);

		root.add(nodesChnl);

		return true;
	}

	/**
	 * expand tree node function
	 * @version 1.0
	 * @author dinglinhui
	 */
	public void expandAll() {
		for (int i = 0; i < tree.getRowCount(); i++) {
			tree.expandRow(i);
		}
	}

	/**
	 * scale image function
	 * @version 1.0
	 * @author dinglinhui
	 */
	public static ImageIcon getImageIcon(String path, int width, int height) {

		if (width == 0 || height == 0) {
			return new ImageIcon(path);
		}
		ImageIcon icon = new ImageIcon(path);
		icon.setImage(icon.getImage().getScaledInstance(width, height, Image.SCALE_DEFAULT));
		return icon;
	}

	@Override
	public void run() {
		System.out.println("runner run!");
		while (true) {
			if (!bRunable) {
				bRunable = true;
				break;
			}

			if (process) {
				process = false;
				processbar.setVisible(false);
				processbar.dispose();
			}

			transthread.lock.lock();
			if (!transthread.recvqueue.isEmpty()) {
				//improve to buffer, and change the buffer to packet
				switchResponse(transthread.recvqueue.poll());
			}
			transthread.lock.unlock();

			try {
				if (transthread.recvqueue.isEmpty())
					Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("runner stop!");
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == cfg) {
			new CommCfgDlg(this);
		} else if (e.getSource() == reload) {

			DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
			if (selectedNode != null) {

				if (selectedNode.getParent() != null) {

					if (selectedNode.getParent().getParent() != null) {
						// 召唤具体通道下的某个类型数据
						if (selectedNode.toString().equals("遥测")) {
							this.switchRequest(TRANSMIT_AI, root.getIndex(selectedNode.getParent()));
						}

						if (selectedNode.toString().equals("遥信")) {
							this.switchRequest(TRANSMIT_COS, root.getIndex(selectedNode.getParent()));
						}

						if (selectedNode.toString().equals("遥控")) {
							this.switchRequest(TRANSMIT_DO, root.getIndex(selectedNode.getParent()));
						}
					} else {
						// 召唤具体通道
						// System.out.println(root.getIndex(selectedNode));
						this.switchRequest(TRANSMIT_CHNL_S, root.getIndex(selectedNode));
					}
				} else {
					// 召唤所有通道
					this.switchRequest(TRANSMIT_CHNL_A, 0);
				}
			} else {
				// 召唤所有通道
				this.switchRequest(TRANSMIT_CHNL_A, 0);
			}

			processbar = new ProgressBar(this);
			processbar.setVisible(true);

		} else if (e.getSource() == export) {

			if (null != getNodeName()) {
				fc = new JFileChooser();
				// 设置打开文件对话框的标题
				fc.setDialogTitle("导出Excel文件");
				fc.setSelectedFile(new File(getNodeName())); // 设置默认文件名
				fc.setCurrentDirectory(new File("./"));// 设置当前目录
				fc.setDialogType(JFileChooser.SAVE_DIALOG);//
				fc.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {
					public boolean accept(File f) {
						return true;
					}

					public String getDescription() {
						return "Microsoft Office 2003 (*.xls)";
					}

				});

				int flag = 0;
				// 这里显示打开文件的对话框
				flag = fc.showSaveDialog(this);
				if (flag == JFileChooser.APPROVE_OPTION) {

					File f = fc.getSelectedFile();
					String fileName = fc.getName(f) + ".xls";
					String writePath = fc.getCurrentDirectory().getAbsolutePath() + File.separator + fileName;
					// System.out.println(writePath);
					writeExcel(writePath);
					// 程序执行完毕后，出现一个对话框来提示
					JOptionPane.showMessageDialog(null, "保存成功！");
				}
			}

		} else if (e.getSource() == info) {

			JOptionPane.showMessageDialog(null, "pcclient ex 为 pcclient远动配套软件的java版本，\n主要实现转发表的召唤和Excel导出功能！ \n\tdinglinhui@hotmail.com");
		} else if (e.getSource() == close) {
			closeWindow();
		}
	}

	/**
	 * get tree node text function
	 * @version 1.0
	 * @author dinglinhui
	 */
	public String getNodeName() {

		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");// 设置日期格式

		DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
		if (selectedNode != null) {

			if (selectedNode.getParent() != null) {

				if (selectedNode.getParent().getParent() != null) {
					// 召唤具体通道下的某个类型数据
					if (selectedNode.toString().equals("遥测")) {
						return selectedNode.getParent().toString().trim() + "_遥测" + df.format(new Date());
					}

					if (selectedNode.toString().equals("遥信")) {
						return selectedNode.getParent().toString().trim() + "_遥信" + df.format(new Date());
					}

					if (selectedNode.toString().equals("遥控")) {
						return selectedNode.getParent().toString().trim() + "_遥控" + df.format(new Date());
					}
				} else {
					// 召唤具体通道
					return selectedNode.toString().trim() + "_" + df.format(new Date());
				}
			}
		}
		return null;
	}

	/**
	 * get tree node type function
	 * @version 1.0
	 * @author dinglinhui
	 */
	public int getNodeType() {

		DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
		if (selectedNode != null) {

			if (selectedNode.getParent() != null) {

				if (selectedNode.getParent().getParent() != null) {
					// 召唤具体通道下的某个类型数据
					if (selectedNode.toString().equals("遥测")) {
						return TRANSMIT_AI;
					}

					if (selectedNode.toString().equals("遥信")) {
						return TRANSMIT_COS;
					}

					if (selectedNode.toString().equals("遥控")) {
						return TRANSMIT_DO;
					}
				} else {
					// 召唤具体通道
					return TRANSMIT_CHNL_S;
				}
			}
		}

		return 0;
	}

	/**
	 * table model write to excel function
	 * @version 1.0
	 * @author dinglinhui
	 */
	private void writeExcel(String writePath) {

		if (chnlIndex == -1) {
			return;
		}

		// 创建Excel的工作书册 Workbook,对应到一个excel文档
		HSSFWorkbook wb = new HSSFWorkbook();
		int rows = 0;
		int columns = 0;
		TransTableModel model = null;
		switch (getNodeType()) {
		case TRANSMIT_AI: {
			model = aitableModel[chnlIndex];
			rows = model.getRowCount();
			columns = model.getColumnCount();

			HSSFSheet sheet = wb.createSheet("遥测");
			HSSFRow row = sheet.createRow(0);
			for (int i = 0; i < aiheadings.length; i++) {
				row.createCell(i).setCellValue(aiheadings[i]);
			}

			for (int r = 1; r <= rows; r++) {
				row = sheet.createRow(r);
				for (int c = 0; c < columns; c++) {
					row.createCell(c).setCellValue(model.getValueAt(r - 1, c).toString());
				}
			}

			break;
		}
		case TRANSMIT_COS: {
			model = costableModel[chnlIndex];
			rows = model.getRowCount();
			columns = model.getColumnCount();

			HSSFSheet sheet = wb.createSheet("遥信");
			HSSFRow row = sheet.createRow(0);
			for (int i = 0; i < cosheadings.length; i++) {
				row.createCell(i).setCellValue(cosheadings[i]);
			}

			for (int r = 1; r <= rows; r++) {
				row = sheet.createRow(r);
				for (int c = 0; c < columns; c++) {
					row.createCell(c).setCellValue(model.getValueAt(r - 1, c).toString());
				}
			}
			break;
		}
		case TRANSMIT_DO: {
			model = dotableModel[chnlIndex];
			rows = model.getRowCount();
			columns = model.getColumnCount();

			HSSFSheet sheet = wb.createSheet("遥控");
			HSSFRow row = sheet.createRow(0);
			for (int i = 0; i < doheadings.length; i++) {
				row.createCell(i).setCellValue(doheadings[i]);
			}

			for (int r = 1; r <= rows; r++) {
				row = sheet.createRow(r);
				for (int c = 0; c < columns; c++) {
					row.createCell(c).setCellValue(model.getValueAt(r - 1, c).toString());
				}
			}
			break;
		}
		case TRANSMIT_CHNL_S://
		{
			// 遥测
			model = aitableModel[chnlIndex];
			rows = model.getRowCount();
			columns = model.getColumnCount();
			HSSFSheet sheetAi = wb.createSheet("遥测");
			HSSFRow rowAi = sheetAi.createRow(0);
			for (int i = 0; i < aiheadings.length; i++) {
				rowAi.createCell(i).setCellValue(aiheadings[i]);
			}

			for (int r = 1; r <= rows; r++) {
				rowAi = sheetAi.createRow(r);
				for (int c = 0; c < columns; c++) {
					rowAi.createCell(c).setCellValue(model.getValueAt(r - 1, c).toString());
				}
			}

			// 遥信
			model = costableModel[chnlIndex];
			rows = model.getRowCount();
			columns = model.getColumnCount();
			HSSFSheet sheetCos = wb.createSheet("遥信");
			HSSFRow rowCos = sheetCos.createRow(0);
			for (int i = 0; i < cosheadings.length; i++) {
				rowCos.createCell(i).setCellValue(cosheadings[i]);
			}

			for (int r = 1; r <= rows; r++) {
				rowCos = sheetCos.createRow(r);
				for (int c = 0; c < columns; c++) {
					rowCos.createCell(c).setCellValue(model.getValueAt(r - 1, c).toString());
				}
			}

			// 遥控
			model = dotableModel[chnlIndex];
			rows = model.getRowCount();
			columns = model.getColumnCount();
			HSSFSheet sheetDo = wb.createSheet("遥控");
			HSSFRow rowDo = sheetDo.createRow(0);
			for (int i = 0; i < doheadings.length; i++) {
				rowDo.createCell(i).setCellValue(doheadings[i]);
			}

			for (int r = 1; r <= rows; r++) {
				rowDo = sheetDo.createRow(r);
				for (int c = 0; c < columns; c++) {
					rowDo.createCell(c).setCellValue(model.getValueAt(r - 1, c).toString());
				}
			}
			break;
		}
		default:
			break;
		}

		try {
			FileOutputStream os = new FileOutputStream(writePath);
			wb.write(os);
			os.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String args[]) {
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (Exception evt) {
		}

		PCClientEx frame = new PCClientEx();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setSize(screenSize.width * 3 / 4, screenSize.height * 3 / 4);
		frame.setLocation(screenSize.width / 2 - frame.getWidth() / 2, screenSize.height / 2 - frame.getHeight() / 2);// 设置窗口居中显示
		frame.setVisible(true);
	}

	/**
	 * class CommCfgDlg
	 * @version 1.0
	 * @author dinglinhui
	 */
	public class CommCfgDlg extends JDialog implements ActionListener {

		private static final long serialVersionUID = 1L;
		private JLabel ipLabel, portLabel;
		private JTextField ipField, portField;
		private JButton comfirmbuttom, cancelbuttom;

		public CommCfgDlg(JFrame parent) {

			super(parent, true);

			setSize(240, 180);
			// setResizable(false);
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			setLocation(screenSize.width / 2 - getWidth() / 2, screenSize.height / 2 - getHeight() / 2);// 设置窗口居中显示
			ipLabel = new JLabel("IP地址：");
			portLabel = new JLabel("TCP端口：");

			ipField = new JTextField(20);
			ipField.setText("127.0.0.1");
			portField = new JTextField(20);
			portField.setText("6010");

			comfirmbuttom = new JButton("确认");
			comfirmbuttom.addActionListener(this);
			cancelbuttom = new JButton("取消");
			cancelbuttom.addActionListener(this);

			getContentPane().setLayout(new FlowLayout());

			getContentPane().add(ipLabel);
			getContentPane().add(ipField);

			getContentPane().add(portLabel);
			getContentPane().add(portField);

			getContentPane().add(comfirmbuttom);
			getContentPane().add(cancelbuttom);

			// this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			this.setVisible(true);
			this.setTitle("通信配置");
		}

		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == comfirmbuttom) {

				transthread.setIp(ipField.getText().trim());
				transthread.setPort(Integer.valueOf(portField.getText().trim()));
				reload.setEnabled(true);
				export.setEnabled(true);
				if (!transthread.isAlive())
					transthread.start();

				this.setVisible(false);
				this.dispose();
			} else if (e.getSource() == cancelbuttom) {
				this.setVisible(false);
				this.dispose();
			}
		}
	}

	/**
	 * class ProgressBar
	 * @version 1.0
	 * @author dinglinhui
	 */
	public class ProgressBar extends JDialog implements Runnable {
		// 创建进度条，水平，最小值0，最大值100，即默认值
		private JProgressBar jpb;
		private Thread thread;

		public void run() {
			jpb.setStringPainted(true);// 进度条呈现进度字符串
			jpb.setBorderPainted(false);// 进度条不绘制其边框
			jpb.setForeground(new Color(0, 210, 40));// 设置进度条的前景色
			jpb.setBackground(new Color(188, 190, 194));// 设置进度条的背景色
			for (int t = 0; t <= 100; t++) {

				jpb.setValue(t);// 设置进度条的值

				if (bFinish) {
					break;
				}

				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			bFinish = false;
			process = true;
		}

		public ProgressBar(JFrame parent) {

			super(parent, true);

			thread = new Thread(this);
			setSize(400, 80);
			int WIDTH1 = Toolkit.getDefaultToolkit().getScreenSize().width;
			int HEIGHT1 = Toolkit.getDefaultToolkit().getScreenSize().height;
			int WIDTH2 = getSize().width;
			int HEIGHT2 = getSize().height;
			setLocation((WIDTH1 - WIDTH2) / 2, (HEIGHT1 - HEIGHT2) / 2);

			jpb = new JProgressBar(JProgressBar.HORIZONTAL, 0, 100);
			// 用GridLayout布局管理器进行布局
			GridLayout gl = new GridLayout(1, 1);// 3行1列
			getContentPane().setLayout(gl);
			getContentPane().add(jpb);

			thread.start();
		}
	}
}