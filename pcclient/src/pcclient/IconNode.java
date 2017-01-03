package pcclient;

import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.Hashtable;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.plaf.metal.MetalIconFactory;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

class IconNodeRenderer extends DefaultTreeCellRenderer {

	private static final long serialVersionUID = 1L;

	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf,
			int row, boolean hasFocus) {

		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

		Icon icon = ((IconNode) value).getIcon();

		if (icon == null) {
			Hashtable<?, ?> icons = (Hashtable<?, ?>) tree.getClientProperty("JTree.icons");
			String name = ((IconNode) value).getIconName();
			if ((icons != null) && (name != null)) {
				icon = (Icon) icons.get(name);
				if (icon != null) {
					setIcon(icon);
				}
			}
		} else {
			setIcon(icon);
		}

		return this;
	}
}

class IconNode extends DefaultMutableTreeNode {

	private static final long serialVersionUID = 1L;

	protected Icon icon;

	protected String iconName;

	public IconNode() {
		this(null);
	}

	public IconNode(Object userObject) {
		this(userObject, true, null);
	}

	public IconNode(Object userObject, boolean allowsChildren, Icon icon) {
		super(userObject, allowsChildren);
		this.icon = icon;
	}

	public void setIcon(Icon icon) {
		this.icon = icon;
	}

	public Icon getIcon() {
		return icon;
	}

	public String getIconName() {
		if (iconName != null) {
			return iconName;
		} else {
			String str = userObject.toString();
			int index = str.lastIndexOf(".");
			if (index != -1) {
				return str.substring(++index);
			} else {
				return null;
			}
		}
	}

	public void setIconName(String name) {
		iconName = name;
	}

}

class TextIcons extends MetalIconFactory.TreeLeafIcon {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected String label;

	private static Hashtable labels;

	protected TextIcons() {
	}

	public void paintIcon(Component c, Graphics g, int x, int y) {
		super.paintIcon(c, g, x, y);
		if (label != null) {
			FontMetrics fm = g.getFontMetrics();

			int offsetX = (getIconWidth() - fm.stringWidth(label)) / 2;
			int offsetY = (getIconHeight() - fm.getHeight()) / 2 - 2;

			g.drawString(label, x + offsetX, y + offsetY + fm.getHeight());
		}
	}

	public static Icon getIcon(String str) {
		if (labels == null) {
			labels = new Hashtable();
			setDefaultSet();
		}
		TextIcons icon = new TextIcons();
		icon.label = (String) labels.get(str);
		return icon;
	}

	public static void setLabelSet(String ext, String label) {
		if (labels == null) {
			labels = new Hashtable();
			setDefaultSet();
		}
		labels.put(ext, label);
	}

	private static void setDefaultSet() {
		labels.put("c", "C");
		labels.put("java", "J");
		labels.put("html", "H");
		labels.put("htm", "H");
	}
}