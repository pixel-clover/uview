package com.uview.gui;

import com.formdev.flatlaf.FlatClientProperties;
import com.uview.core.PackageManager;
import com.uview.core.SettingsManager;
import com.uview.gui.tree.TreeEntry;
import com.uview.io.PackageIO;
import com.uview.model.UnityAsset;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public class PackageViewPanel extends JPanel {

  private final PackageManager packageManager;
  private final SettingsManager settingsManager;
  private final JTree tree;
  private final DefaultTreeModel treeModel;
  private final JFrame owner;
  private final Timer searchDebounceTimer;
  private File packageFile;
  private final JTextField searchField;

  public PackageViewPanel(JFrame owner, File packageFile, SettingsManager settingsManager) {
    super(new BorderLayout());
    this.owner = owner;
    this.packageFile = packageFile;
    this.settingsManager = settingsManager;
    this.packageManager = new PackageManager(new PackageIO());

    // --- Search Bar ---
    searchDebounceTimer = new Timer(300, e -> filterTree()); // 300ms delay
    searchDebounceTimer.setRepeats(false);

    searchField = new JTextField();
    searchField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Type to search...");
    searchField
        .getDocument()
        .addDocumentListener(
            new DocumentListener() {
              @Override
              public void insertUpdate(DocumentEvent e) {
                searchDebounceTimer.restart();
              }

              @Override
              public void removeUpdate(DocumentEvent e) {
                searchDebounceTimer.restart();
              }

              @Override
              public void changedUpdate(DocumentEvent e) {
                searchDebounceTimer.restart();
              }
            });
    JPanel searchPanel = new JPanel(new BorderLayout());
    searchPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    searchPanel.add(searchField, BorderLayout.CENTER);
    add(searchPanel, BorderLayout.NORTH);

    // --- Tree View ---
    DefaultMutableTreeNode root = new DefaultMutableTreeNode("Loading...");
    this.treeModel = new DefaultTreeModel(root);
    this.tree = new JTree(treeModel);
    tree.setRootVisible(true); // Set to true initially to show "Loading..."
    tree.setCellRenderer(new FileTypeTreeCellRenderer());
    tree.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mousePressed(MouseEvent e) {
            if (SwingUtilities.isRightMouseButton(e)) {
              // Only show menu if we are not on an info message node
              TreePath path = tree.getClosestPathForLocation(e.getX(), e.getY());
              if (path != null && tree.getModel().getChildCount(path.getLastPathComponent()) > 0) {
                tree.setSelectionPath(path);
              }
              if (tree.getSelectionPath() != null) {
                createPopupMenu().show(e.getComponent(), e.getX(), e.getY());
              }
            }
            if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
              handleDoubleClick();
            }
          }
        });

    add(new JScrollPane(tree), BorderLayout.CENTER);
  }

  private void filterTree() {
    String query = searchField.getText();
    Collection<UnityAsset> filteredAssets = packageManager.getFilteredAssets(query);
    DefaultMutableTreeNode root = TreeModelBuilder.build(filteredAssets);

    if (root.getChildCount() == 0) {
      String message;
      if (query.trim().isEmpty()) {
        message = "Package is empty or its format could not be read.";
      } else {
        message = "No assets found matching '" + query + "'";
      }
      DefaultMutableTreeNode messageNode = new DefaultMutableTreeNode(message);
      treeModel.setRoot(messageNode);
      tree.setRootVisible(true); // Show the message node
    } else {
      treeModel.setRoot(root);
      tree.setRootVisible(false); // Hide the master root for normal view
      // Expand all nodes in the filtered view for better visibility
      for (int i = 0; i < tree.getRowCount(); i++) {
        tree.expandRow(i);
      }
    }
  }

  private JPopupMenu createPopupMenu() {
    JPopupMenu popup = new JPopupMenu();
    TreePath selectionPath = tree.getSelectionPath();
    boolean isNodeSelected = selectionPath != null;

    JMenuItem viewMenuItem = new JMenuItem("View");
    viewMenuItem.addActionListener(e -> handleDoubleClick());
    popup.add(viewMenuItem);

    popup.add(new JSeparator());

    JMenuItem addMenuItem = new JMenuItem("Add File...");
    addMenuItem.addActionListener(e -> addFile());
    popup.add(addMenuItem);

    JMenuItem removeMenuItem = new JMenuItem("Remove");
    removeMenuItem.setEnabled(isNodeSelected);
    removeMenuItem.addActionListener(e -> removeSelectedAsset());
    popup.add(removeMenuItem);

    popup.add(new JSeparator());

    JMenuItem extractSelectedMenuItem = new JMenuItem("Extract Selected...");
    extractSelectedMenuItem.setEnabled(isNodeSelected);
    extractSelectedMenuItem.addActionListener(e -> extractSelected());
    popup.add(extractSelectedMenuItem);

    if (isNodeSelected) {
      DefaultMutableTreeNode selectedNode =
          (DefaultMutableTreeNode) selectionPath.getLastPathComponent();
      viewMenuItem.setEnabled(selectedNode.getUserObject() instanceof TreeEntry.AssetEntry);
    } else {
      viewMenuItem.setEnabled(false);
    }

    return popup;
  }

  public void loadPackage(Runnable onDone) {
    if (packageFile == null) { // New package
      packageManager.createNew();
      refreshTree();
      onDone.run();
      return;
    }

    SwingWorker<Void, Void> worker =
        new SwingWorker<>() {
          @Override
          protected Void doInBackground() throws Exception {
            packageManager.loadPackage(packageFile);
            return null;
          }

          @Override
          protected void done() {
            try {
              get();
              refreshTree();
            } catch (Exception ex) {
              treeModel.setRoot(
                  new DefaultMutableTreeNode("Error loading file: " + ex.getMessage()));
              tree.setRootVisible(true);
            } finally {
              onDone.run();
            }
          }
        };
    worker.execute();
  }

  private JFileChooser createFileChooser(String title) {
    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle(title);
    chooser.setCurrentDirectory(settingsManager.getLastDirectory());
    chooser.setPreferredSize(new Dimension(1024, 768));
    return chooser;
  }

  private void addFile() {
    JFileChooser fileChooser = createFileChooser("Add File to Package");
    if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
      Path sourceFile = fileChooser.getSelectedFile().toPath();
      settingsManager.setLastDirectory(sourceFile.toFile().getParentFile());
      String assetPath =
          JOptionPane.showInputDialog(this, "Enter the asset path (e.g., Assets/MyFile.txt):");
      if (assetPath != null && !assetPath.trim().isEmpty()) {
        try {
          packageManager.addAsset(sourceFile, assetPath);
          refreshTree();
        } catch (Exception ex) {
          JOptionPane.showMessageDialog(
              this, "Error adding file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
      }
    }
  }

  private void removeSelectedAsset() {
    TreePath selectionPath = tree.getSelectionPath();
    if (selectionPath == null) {
      return;
    }
    DefaultMutableTreeNode selectedNode =
        (DefaultMutableTreeNode) selectionPath.getLastPathComponent();
    Object userObject = selectedNode.getUserObject();

    if (userObject instanceof TreeEntry.DirectoryEntry entry) {
      int result =
          JOptionPane.showConfirmDialog(
              this,
              "Are you sure you want to remove this directory and all its contents?",
              "Confirm Removal",
              JOptionPane.YES_NO_OPTION,
              JOptionPane.WARNING_MESSAGE);
      if (result == JOptionPane.YES_OPTION) {
        packageManager.removeDirectory(entry.getFullPath());
        refreshTree();
      }
    } else if (userObject instanceof TreeEntry.AssetEntry entry) {
      packageManager.removeAsset(entry.getFullPath());
      refreshTree();
    }
  }

  private void extractSelected() {
    TreePath selectionPath = tree.getSelectionPath();
    if (selectionPath == null) {
      return;
    }
    DefaultMutableTreeNode selectedNode =
        (DefaultMutableTreeNode) selectionPath.getLastPathComponent();
    if (!(selectedNode.getUserObject() instanceof TreeEntry entry)) {
      return;
    }

    JFileChooser chooser = createFileChooser("Select Directory for Extraction");
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
      Path outputDir = chooser.getSelectedFile().toPath();
      settingsManager.setLastDirectory(outputDir.toFile());
      extractInBackground(entry, outputDir);
    }
  }

  private void extractInBackground(TreeEntry entry, Path outputDir) {
    owner.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    SwingWorker<Void, Void> worker =
        new SwingWorker<>() {
          @Override
          protected Void doInBackground() throws Exception {
            Collection<com.uview.model.UnityAsset> assetsToExtract;
            String pathPrefixToStrip;
            if (entry instanceof TreeEntry.AssetEntry assetEntry) {
              assetsToExtract = List.of(assetEntry.asset());
              String parentPath = new File(assetEntry.asset().assetPath()).getParent();
              pathPrefixToStrip = (parentPath == null) ? "" : parentPath + "/";
            } else {
              pathPrefixToStrip = entry.getFullPath();
              assetsToExtract = packageManager.getAssetsUnderPath(pathPrefixToStrip);
            }
            packageManager.extractAssets(assetsToExtract, outputDir, pathPrefixToStrip);
            return null;
          }

          @Override
          protected void done() {
            try {
              get();
              Desktop.getDesktop().open(outputDir.toFile());
            } catch (Exception ex) {
              JOptionPane.showMessageDialog(
                  owner,
                  "Extraction failed: " + ex.getCause().getMessage(),
                  "Error",
                  JOptionPane.ERROR_MESSAGE);
            } finally {
              owner.setCursor(Cursor.getDefaultCursor());
            }
          }
        };
    worker.execute();
  }

  private void handleDoubleClick() {
    TreePath selectionPath = tree.getSelectionPath();
    if (selectionPath == null) {
      return;
    }
    DefaultMutableTreeNode selectedNode =
        (DefaultMutableTreeNode) selectionPath.getLastPathComponent();
    Object userObject = selectedNode.getUserObject();
    if (userObject instanceof TreeEntry.AssetEntry entry) {
      AssetViewerFrame viewer = new AssetViewerFrame(owner, entry.asset());
      viewer.setVisible(true);
    }
  }

  public void refreshTree() {
    // When refreshing, apply the current filter text
    filterTree();
  }

  public PackageManager getPackageManager() {
    return packageManager;
  }

  public File getPackageFile() {
    return packageFile;
  }

  public void setPackageFile(File packageFile) {
    this.packageFile = packageFile;
  }

  public String getTabTitle() {
    String title = (packageFile != null) ? packageFile.getName() : "New Package";
    if (packageManager.isModified()) {
      title += "*";
    }
    return title;
  }

  public JTree getTree() {
    return tree;
  }
}
