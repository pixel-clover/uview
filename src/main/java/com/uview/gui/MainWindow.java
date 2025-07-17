package com.uview.gui;

import com.uview.core.PackageManager;
import com.uview.io.PackageIO;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Path;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

/** The main application window for UView. */
public class MainWindow extends JFrame {

  private final PackageIO packageIO = new PackageIO();
  private final PackageManager packageManager = new PackageManager(packageIO);
  private final JTree tree;
  private final DefaultTreeModel treeModel;
  private final JLabel statusLabel;
  private File currentFile;
  private JMenuItem saveMenuItem;

  /** Constructs the main window and initializes its components. */
  public MainWindow() {
    super("UView");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(800, 600);
    setLocationRelativeTo(null);

    JMenuBar menuBar = createMenuBar();
    setJMenuBar(menuBar);

    DefaultMutableTreeNode root = new DefaultMutableTreeNode("No package loaded.");
    treeModel = new DefaultTreeModel(root);
    tree = new JTree(treeModel);
    tree.setCellRenderer(new FileTypeTreeCellRenderer());
    tree.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mousePressed(MouseEvent e) {
            if (SwingUtilities.isRightMouseButton(e)) {
              int row = tree.getClosestRowForLocation(e.getX(), e.getY());
              tree.setSelectionRow(row);
              createPopupMenu().show(e.getComponent(), e.getX(), e.getY());
            }
          }
        });

    statusLabel = new JLabel("Ready.");
    statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

    add(new JScrollPane(tree), BorderLayout.CENTER);
    add(statusLabel, BorderLayout.SOUTH);
    updateState();
  }

  private JMenuBar createMenuBar() {
    JMenuBar menuBar = new JMenuBar();
    JMenu fileMenu = new JMenu("File");
    menuBar.add(fileMenu);

    JMenuItem newMenuItem = new JMenuItem("New Package");
    newMenuItem.addActionListener(e -> newPackage());
    fileMenu.add(newMenuItem);

    JMenuItem openMenuItem = new JMenuItem("Open...");
    openMenuItem.addActionListener(e -> openFile());
    fileMenu.add(openMenuItem);

    saveMenuItem = new JMenuItem("Save");
    saveMenuItem.addActionListener(e -> saveFile());
    fileMenu.add(saveMenuItem);

    JMenuItem saveAsMenuItem = new JMenuItem("Save As...");
    saveAsMenuItem.addActionListener(e -> saveFileAs());
    fileMenu.add(saveAsMenuItem);

    return menuBar;
  }

  private JPopupMenu createPopupMenu() {
    JPopupMenu popup = new JPopupMenu();
    TreePath selectionPath = tree.getSelectionPath();
    boolean isNodeSelected = selectionPath != null;

    JMenuItem addMenuItem = new JMenuItem("Add File...");
    addMenuItem.addActionListener(e -> addFile());
    popup.add(addMenuItem);

    JMenuItem removeMenuItem = new JMenuItem("Remove");
    removeMenuItem.setEnabled(isNodeSelected);
    removeMenuItem.addActionListener(e -> removeSelectedAsset());
    popup.add(removeMenuItem);

    popup.add(new JSeparator());

    JMenuItem extractMenuItem = new JMenuItem("Extract All...");
    extractMenuItem.setEnabled(currentFile != null);
    extractMenuItem.addActionListener(e -> extractAllAssets());
    popup.add(extractMenuItem);

    return popup;
  }

  private void newPackage() {
    packageManager.createNew();
    currentFile = null;
    refreshTree();
    updateState();
    statusLabel.setText("New package created.");
  }

  private void openFile() {
    JFileChooser chooser = new JFileChooser();
    chooser.setFileFilter(new FileNameExtensionFilter("Unity Package", "unitypackage"));
    if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
      currentFile = chooser.getSelectedFile();
      loadPackageInBackground(currentFile);
    }
  }

  private void saveFile() {
    if (currentFile == null) {
      saveFileAs();
    } else {
      savePackageInBackground(currentFile);
    }
  }

  private void saveFileAs() {
    JFileChooser chooser = new JFileChooser();
    chooser.setFileFilter(new FileNameExtensionFilter("Unity Package", "unitypackage"));
    if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
      File selectedFile = chooser.getSelectedFile();
      if (!selectedFile.getName().endsWith(".unitypackage")) {
        selectedFile =
            new File(selectedFile.getParentFile(), selectedFile.getName() + ".unitypackage");
      }
      currentFile = selectedFile;
      savePackageInBackground(currentFile);
    }
  }

  private void addFile() {
    JFileChooser fileChooser = new JFileChooser();
    if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
      Path sourceFile = fileChooser.getSelectedFile().toPath();
      String assetPath =
          JOptionPane.showInputDialog(this, "Enter the asset path (e.g., Assets/MyFile.txt):");
      if (assetPath != null && !assetPath.trim().isEmpty()) {
        try {
          packageManager.addAsset(sourceFile, assetPath);
          refreshTree();
          updateState();
        } catch (Exception ex) {
          showError("Error adding file: " + ex.getMessage());
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

    if (userObject instanceof com.uview.gui.tree.TreeEntry entry) {
      packageManager.removeAsset(entry.getFullPath());
      refreshTree();
      updateState();
    }
  }

  private void extractAllAssets() {
    if (packageManager.getAssets().isEmpty()) {
      return;
    }

    JFileChooser chooser = new JFileChooser();
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
      Path outputDir = chooser.getSelectedFile().toPath();
      extractAllAssetsInBackground(outputDir);
    }
  }

  private void loadPackageInBackground(File file) {
    setWorking(true, "Loading " + file.getName() + "...");
    SwingWorker<Void, Void> worker =
        new SwingWorker<>() {
          @Override
          protected Void doInBackground() throws Exception {
            packageManager.loadPackage(file);
            return null;
          }

          @Override
          protected void done() {
            handleTaskCompletion(
                this, () -> statusLabel.setText("Loaded " + file.getName()), "Error loading file.");
          }
        };
    worker.execute();
  }

  private void savePackageInBackground(File file) {
    setWorking(true, "Saving " + file.getName() + "...");
    SwingWorker<Void, Void> worker =
        new SwingWorker<>() {
          @Override
          protected Void doInBackground() throws Exception {
            packageManager.savePackage(file);
            return null;
          }

          @Override
          protected void done() {
            handleTaskCompletion(
                this, () -> statusLabel.setText("Saved " + file.getName()), "Error saving file.");
          }
        };
    worker.execute();
  }

  private void extractAllAssetsInBackground(Path outputDir) {
    setWorking(true, "Extracting assets...");
    SwingWorker<Void, Void> worker =
        new SwingWorker<>() {
          @Override
          protected Void doInBackground() throws Exception {
            packageManager.extractAssets(packageManager.getAssets(), outputDir);
            return null;
          }

          @Override
          protected void done() {
            handleTaskCompletion(
                this,
                () -> {
                  statusLabel.setText("Extraction complete.");
                  try {
                    Desktop.getDesktop().open(outputDir.toFile());
                  } catch (Exception ex) {
                    showError("Could not open output directory.");
                  }
                },
                "Extraction failed.");
          }
        };
    worker.execute();
  }

  private void handleTaskCompletion(
      SwingWorker<?, ?> worker, Runnable onSuccess, String errorPrefix) {
    try {
      worker.get(); // Check for exceptions from doInBackground
      refreshTree();
      updateState();
      onSuccess.run();
    } catch (Exception ex) {
      showError(errorPrefix + " " + ex.getCause().getMessage());
      statusLabel.setText(errorPrefix);
    } finally {
      setWorking(false, null);
    }
  }

  private void refreshTree() {
    String rootName = (currentFile != null) ? currentFile.getName() : "New Package";
    DefaultMutableTreeNode root = TreeModelBuilder.build(packageManager.getAssets(), rootName);
    treeModel.setRoot(root);
    for (int i = 0; i < tree.getRowCount(); i++) {
      tree.expandRow(i);
    }
  }

  private void updateState() {
    boolean isModified = packageManager.isModified();
    saveMenuItem.setEnabled(isModified);
    String title = "UView";
    if (currentFile != null) {
      title += " - " + currentFile.getName();
    } else {
      title += " - New Package";
    }
    if (isModified) {
      title += "*";
    }
    setTitle(title);
  }

  private void setWorking(boolean working, String status) {
    setCursor(working ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
    if (status != null) {
      statusLabel.setText(status);
    }
  }

  private void showError(String message) {
    JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
  }
}
