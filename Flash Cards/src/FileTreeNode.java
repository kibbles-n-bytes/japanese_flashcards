import java.io.File;

import javax.swing.tree.DefaultMutableTreeNode;

// An expanded upon tree node class that contains a reference to a File object.
class FileTreeNode extends DefaultMutableTreeNode {
	private File file;
		
	public FileTreeNode(String name, File file) { 
		super(name); 
		this.file = file;
	}
		
	public File getFile() { return file; }
}
