package com.uview.gui;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.uview.App;
import com.uview.core.PackageManager;
import com.uview.core.SettingsManager;
import com.uview.gui.tree.TreeEntry;
import com.uview.io.PackageIO;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

/** The main application window for UView. */
public class MainWindow extends JFrame {

  private final PackageIO packageIO = new PackageIO();
  private final PackageManager packageManager = new PackageManager(packageIO);
  private final SettingsManager settingsManager = new SettingsManager();
  private final JTree tree;
  private final DefaultTreeModel treeModel;
  private File currentFile;
  private JMenuItem saveMenuItem;
  private JMenuItem closeMenuItem;
  private JMenu openRecentMenu;
  private JLabel statusLabel;
  private JLabel packageSizeLabel;
  private JLabel memoryUsageLabel;

  /** Constructs the main window and initializes its components. */
  public MainWindow() {
    super("UView");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(800, 600);
    setLocationRelativeTo(null);

    setJMenuBar(createMenuBar());

    DefaultMutableTreeNode root = new DefaultMutableTreeNode("No package loaded.");
    treeModel = new DefaultTreeModel(root);
    tree = new JTree(treeModel);
    tree.setRootVisible(false);
    tree.setCellRenderer(new FileTypeTreeCellRenderer());
    tree.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mousePressed(MouseEvent e) {
            if (SwingUtilities.isRightMouseButton(e)) {
              int row = tree.getClosestRowForLocation(e.getX(), e.getY());
              if (row != -1) {
                tree.setSelectionRow(row);
                createPopupMenu().show(e.getComponent(), e.getX(), e.getY());
              }
            }
            if (e.getClickCount() == 2) {
              handleDoubleClick();
            }
          }
        });

    add(new JScrollPane(tree), BorderLayout.CENTER);
    add(createStatusBar(), BorderLayout.SOUTH);
    updateState();
  }

  private JPanel createStatusBar() {
    JPanel statusBar = new JPanel();
    statusBar.setLayout(new BoxLayout(statusBar, BoxLayout.X_AXIS));
    statusBar.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

    statusLabel = new JLabel("Ready");
    packageSizeLabel = new JLabel("");
    memoryUsageLabel = new JLabel("");

    statusBar.add(statusLabel);
    statusBar.add(Box.createHorizontalGlue());
    statusBar.add(packageSizeLabel);
    statusBar.add(Box.createRigidArea(new Dimension(10, 0)));
    statusBar.add(memoryUsageLabel);

    // Timer to update memory usage every second
    new Timer(1000, e -> updateMemoryUsage()).start();
    updateMemoryUsage(); // Initial update

    return statusBar;
  }

  private void updateMemoryUsage() {
    Runtime runtime = Runtime.getRuntime();
    long usedMemory = runtime.totalMemory() - runtime.freeMemory();
    memoryUsageLabel.setText(String.format("Mem: %s", formatSize(usedMemory)));
  }

  private static String formatSize(long bytes) {
    if (bytes < 1024) {
      return bytes + " B";
    }
    int exp = (int) (Math.log(bytes) / Math.log(1024));
    char pre = "KMGTPE".charAt(exp - 1);
    return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
  }

  private JMenuBar createMenuBar() {
    JMenuBar menuBar = new JMenuBar();

    // --- File Menu ---
    JMenu fileMenu = new JMenu("File");
    menuBar.add(fileMenu);

    JMenuItem newMenuItem = new JMenuItem("New Package");
    newMenuItem.addActionListener(e -> newPackage());
    fileMenu.add(newMenuItem);

    JMenuItem openMenuItem = new JMenuItem("Open...");
    openMenuItem.addActionListener(e -> openFile());
    fileMenu.add(openMenuItem);

    openRecentMenu = new JMenu("Open Recent");
    fileMenu.add(openRecentMenu);

    closeMenuItem = new JMenuItem("Close");
    closeMenuItem.addActionListener(e -> closePackage());
    fileMenu.add(closeMenuItem);

    fileMenu.addMenuListener(
        new MenuListener() {
          @Override
          public void menuSelected(MenuEvent e) {
            populateRecentFilesMenu();
          }

          @Override
          public void menuDeselected(MenuEvent e) {}

          @Override
          public void menuCanceled(MenuEvent e) {}
        });

    fileMenu.add(new JSeparator());

    saveMenuItem = new JMenuItem("Save");
    saveMenuItem.addActionListener(e -> saveFile());
    fileMenu.add(saveMenuItem);

    JMenuItem saveAsMenuItem = new JMenuItem("Save As...");
    saveAsMenuItem.addActionListener(e -> saveFileAs());
    fileMenu.add(saveAsMenuItem);

    fileMenu.add(new JSeparator());

    JMenuItem exitMenuItem = new JMenuItem("Exit");
    exitMenuItem.addActionListener(e -> System.exit(0));
    fileMenu.add(exitMenuItem);

    // --- Help Menu ---
    JMenu helpMenu = new JMenu("Help");
    menuBar.add(helpMenu);

    JMenuItem aboutMenuItem = new JMenuItem("About UView");
    aboutMenuItem.addActionListener(e -> showAboutDialog());
    helpMenu.add(aboutMenuItem);

    return menuBar;
  }

  private void showAboutDialog() {
    String version = getAppVersion();
    String title = "About UView";
    String message =
        "<html><body style='width: 300px;'>"
            + "<h2>UView</h2>"
            + "<p><b>Version:</b> "
            + version
            + "</p>"
            + "<p>A desktop tool for viewing and modifying Unity packages.</p>"
            + "<p><b>GitHub:</b> <a href='https://github.com/habedi/uview'>https://github.com/habedi/uview</a></p>"
            + "</body></html>";

    java.net.URL iconUrl = App.class.getResource("/logo.svg");
    Icon aboutIcon = (iconUrl != null) ? new FlatSVGIcon(iconUrl).derive(64, 64) : null;

    // We use a JLabel with HTML to make the link clickable
    JLabel messageLabel = new JLabel(message);
    messageLabel.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            try {
              Desktop.getDesktop().browse(new java.net.URI("https://github.com/habedi/uview"));
            } catch (Exception ex) {
              // Silently fail if browser can't be opened
            }
          }
        });

    JOptionPane.showMessageDialog(
        this, messageLabel, title, JOptionPane.INFORMATION_MESSAGE, aboutIcon);
  }

  private String getAppVersion() {
    try (InputStream is =
        App.class.getResourceAsStream("/META-INF/maven/com.uview/uview/pom.properties")) {
      if (is == null) {
        return "N/A";
      }
      Properties props = new Properties();
      props.load(is);
      return props.getProperty("version", "N/A");
    } catch (IOException e) {
      return "N/A";
    }
  }

  private void populateRecentFilesMenu() {
    openRecentMenu.removeAll();
    List<File> recentFiles = settingsManager.getRecentFiles();
    if (recentFiles.isEmpty()) {
      openRecentMenu.setEnabled(false);
      return;
    }

    openRecentMenu.setEnabled(true);
    for (File file : recentFiles) {
      JMenuItem menuItem = new JMenuItem(file.getAbsolutePath());
      menuItem.addActionListener(e -> loadPackageInBackground(file));
      openRecentMenu.add(menuItem);
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

    JMenuItem extractAllMenuItem = new JMenuItem("Extract All...");
    extractAllMenuItem.setEnabled(currentFile != null);
    extractAllMenuItem.addActionListener(e -> extractAllAssets());
    popup.add(extractAllMenuItem);

    if (isNodeSelected) {
      DefaultMutableTreeNode selectedNode =
          (DefaultMutableTreeNode) selectionPath.getLastPathComponent();
      viewMenuItem.setEnabled(selectedNode.getUserObject() instanceof TreeEntry.AssetEntry);
    } else {
      viewMenuItem.setEnabled(false);
    }

    return popup;
  }

  private void newPackage() {
    if (confirmClose()) {
      packageManager.createNew();
      currentFile = null;
      refreshTree();
      updateState();
      statusLabel.setText("New package created");
      packageSizeLabel.setText("");
    }
  }

  private void openFile() {
    if (!confirmClose()) {
      return;
    }
    JFileChooser chooser = new JFileChooser();
    chooser.setCurrentDirectory(settingsManager.getLastDirectory());
    chooser.setFileFilter(new FileNameExtensionFilter("Unity Package", "unitypackage"));
    if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
      File selectedFile = chooser.getSelectedFile();
      settingsManager.setLastDirectory(selectedFile.getParentFile());
      loadPackageInBackground(selectedFile);
    }
  }

  private boolean saveFile() {
    if (currentFile == null) {
      return saveFileAs();
    } else {
      savePackageInBackground(currentFile);
      return true;
    }
  }

  private boolean saveFileAs() {
    JFileChooser chooser = new JFileChooser();
    chooser.setCurrentDirectory(settingsManager.getLastDirectory());
    chooser.setFileFilter(new FileNameExtensionFilter("Unity Package", "unitypackage"));
    if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
      File selectedFile = chooser.getSelectedFile();
      if (!selectedFile.getName().endsWith(".unitypackage")) {
        selectedFile =
            new File(selectedFile.getParentFile(), selectedFile.getName() + ".unitypackage");
      }
      currentFile = selectedFile;
      settingsManager.setLastDirectory(selectedFile.getParentFile());
      savePackageInBackground(currentFile);
      return true;
    }
    return false;
  }

  private void closePackage() {
    if (confirmClose()) {
      packageManager.createNew();
      currentFile = null;
      refreshTree();
      updateState();
      statusLabel.setText("Package closed");
      packageSizeLabel.setText("");
    }
  }

  private boolean confirmClose() {
    if (!packageManager.isModified()) {
      return true;
    }
    int result =
        JOptionPane.showConfirmDialog(
            this,
            "The current package has unsaved changes. Do you want to save them?",
            "Unsaved Changes",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.WARNING_MESSAGE);

    switch (result) {
      case JOptionPane.YES_OPTION:
        return saveFile();
      case JOptionPane.NO_OPTION:
        return true;
      case JOptionPane.CANCEL_OPTION:
      default:
        return false;
    }
  }

  private void addFile() {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setCurrentDirectory(settingsManager.getLastDirectory());
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

    if (userObject instanceof TreeEntry entry) {
      packageManager.removeAsset(entry.getFullPath());
      refreshTree();
      updateState();
    }
  }

  private void extractSelected() {
    TreePath selectionPath = tree.getSelectionPath();
    if (selectionPath == null) {
      return;
    }

    DefaultMutableTreeNode selectedNode =
        (DefaultMutableTreeNode) selectionPath.getLastPathComponent();
    Object userObject = selectedNode.getUserObject();

    if (!(userObject instanceof TreeEntry entry)) {
      return;
    }

    JFileChooser chooser = new JFileChooser();
    chooser.setCurrentDirectory(settingsManager.getLastDirectory());
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
      Path outputDir = chooser.getSelectedFile().toPath();
      settingsManager.setLastDirectory(outputDir.toFile());
      extractInBackground(entry, outputDir);
    }
  }

  private void extractAllAssets() {
    if (packageManager.getAssets().isEmpty()) {
      return;
    }
    JFileChooser chooser = new JFileChooser();
    chooser.setCurrentDirectory(settingsManager.getLastDirectory());
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
      Path outputDir = chooser.getSelectedFile().toPath();
      settingsManager.setLastDirectory(outputDir.toFile());
      extractInBackground(new TreeEntry.DirectoryEntry(""), outputDir);
    }
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
      AssetViewerFrame viewer = new AssetViewerFrame(this, entry.asset());
      viewer.setVisible(true);
    }
  }

  private void loadPackageInBackground(File file) {
    setWorking(true, "Loading " + file.getName() + "...");
    currentFile = file;
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
                this,
                () -> {
                  statusLabel.setText("Loaded " + file.getName());
                  packageSizeLabel.setText(String.format("Size: %s", formatSize(file.length())));
                  settingsManager.addRecentFile(file);
                },
                "Error loading file.");
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
                this,
                () -> {
                  statusLabel.setText("Saved " + file.getName());
                  packageSizeLabel.setText(String.format("Size: %s", formatSize(file.length())));
                },
                "Error saving file.");
          }
        };
    worker.execute();
  }

  private void extractInBackground(TreeEntry entry, Path outputDir) {
    setWorking(true, "Extracting assets...");
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
            handleTaskCompletion(
                this,
                () -> {
                  statusLabel.setText("Extraction complete");
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
      worker.get();
      refreshTree();
      updateState();
      onSuccess.run();
    } catch (Exception ex) {
      showError(errorPrefix + " " + ex.getCause().getMessage());
      statusLabel.setText(errorPrefix);
    } finally {
      String finalStatus;
      if (currentFile != null) {
        finalStatus = "Loaded " + currentFile.getName();
      } else {
        finalStatus = "Ready";
      }
      setWorking(false, finalStatus);
    }
  }

  private void refreshTree() {
    DefaultMutableTreeNode root = TreeModelBuilder.build(packageManager.getAssets());
    treeModel.setRoot(root);
    if (currentFile == null) {
      DefaultMutableTreeNode placeholderRoot = new DefaultMutableTreeNode("No package loaded.");
      treeModel.setRoot(placeholderRoot);
      tree.setRootVisible(true);
    } else {
      tree.setRootVisible(false);
      for (int i = 0; i < tree.getRowCount(); i++) {
        tree.expandRow(i);
      }
    }
  }

  private void updateState() {
    boolean hasFile = currentFile != null;
    boolean isModified = packageManager.isModified();

    saveMenuItem.setEnabled(isModified);
    closeMenuItem.setEnabled(hasFile);

    String title = "UView";
    if (hasFile) {
      title += " - " + currentFile.getName();
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
