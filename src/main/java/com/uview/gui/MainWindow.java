package com.uview.gui;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.uview.App;
import com.uview.core.SettingsManager;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
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
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.TransferHandler;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.filechooser.FileNameExtensionFilter;

/** The main application window for UView. */
public class MainWindow extends JFrame {

  private final SettingsManager settingsManager = new SettingsManager();
  private final JTabbedPane tabbedPane;
  private JMenuItem saveMenuItem;
  private JMenuItem saveAsMenuItem;
  private JMenuItem closeMenuItem;
  private JMenuItem extractAllMenuItem;
  private JMenu openRecentMenu;
  private JLabel statusLabel;
  private JLabel fileCountLabel;
  private JLabel packageSizeLabel;
  private JLabel memoryUsageLabel;

  /** Constructs the main window and initializes its components. */
  public MainWindow() {
    super("UView");
    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // We will handle the close operation
    setSize(800, 600);
    setLocationRelativeTo(null);

    addWindowListener(
        new WindowAdapter() {
          @Override
          public void windowClosing(WindowEvent e) {
            handleExit();
          }
        });

    setJMenuBar(createMenuBar());

    tabbedPane = new JTabbedPane();
    tabbedPane.addChangeListener(e -> updateState());

    setTransferHandler(new FileDropHandler());

    add(tabbedPane, BorderLayout.CENTER);
    add(createStatusBar(), BorderLayout.SOUTH);
    updateState();
  }

  private class FileDropHandler extends TransferHandler {
    @Override
    public boolean canImport(TransferSupport support) {
      if (!support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
        return false;
      }
      try {
        Transferable t = support.getTransferable();
        List<File> files = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
        for (File file : files) {
          if (file.isFile() && file.getName().toLowerCase().endsWith(".unitypackage")) {
            return true; // Accept drop if at least one unitypackage is present
          }
        }
      } catch (Exception e) {
        return false;
      }
      return false;
    }

    @Override
    public boolean importData(TransferSupport support) {
      if (!canImport(support)) {
        return false;
      }
      try {
        Transferable t = support.getTransferable();
        List<File> files = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
        for (File file : files) {
          if (file.isFile() && file.getName().toLowerCase().endsWith(".unitypackage")) {
            openPackage(file);
          }
        }
        return true;
      } catch (Exception e) {
        return false;
      }
    }
  }

  private JPanel createStatusBar() {
    JPanel statusBar = new JPanel();
    statusBar.setLayout(new BoxLayout(statusBar, BoxLayout.X_AXIS));
    statusBar.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

    statusLabel = new JLabel("Ready");
    fileCountLabel = new JLabel("");
    packageSizeLabel = new JLabel("");
    memoryUsageLabel = new JLabel("");

    statusBar.add(statusLabel);
    statusBar.add(Box.createHorizontalGlue());
    statusBar.add(fileCountLabel);
    statusBar.add(Box.createRigidArea(new Dimension(15, 0)));
    statusBar.add(packageSizeLabel);
    statusBar.add(Box.createRigidArea(new Dimension(15, 0)));
    statusBar.add(memoryUsageLabel);

    new Timer(3000, e -> updateMemoryUsage()).start();
    updateMemoryUsage();

    return statusBar;
  }

  private void updateMemoryUsage() {
    Runtime runtime = Runtime.getRuntime();
    long usedMemory = runtime.totalMemory() - runtime.freeMemory();
    memoryUsageLabel.setText(String.format("Mem: %s", formatSize(usedMemory)));
  }

  private static String formatSize(long bytes) {
    if (bytes < 1024) return bytes + " B";
    int exp = (int) (Math.log(bytes) / Math.log(1024));
    char pre = "KMGTPE".charAt(exp - 1);
    return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
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

    openRecentMenu = new JMenu("Open Recent");
    fileMenu.add(openRecentMenu);

    closeMenuItem = new JMenuItem("Close");
    closeMenuItem.addActionListener(e -> closePackage());
    fileMenu.add(closeMenuItem);

    fileMenu.addMenuListener(
        new MenuListener() {
          public void menuSelected(MenuEvent e) {
            populateRecentFilesMenu();
          }

          public void menuDeselected(MenuEvent e) {}

          public void menuCanceled(MenuEvent e) {}
        });

    fileMenu.add(new JSeparator());

    saveMenuItem = new JMenuItem("Save");
    saveMenuItem.addActionListener(e -> saveFile());
    fileMenu.add(saveMenuItem);

    saveAsMenuItem = new JMenuItem("Save As...");
    saveAsMenuItem.addActionListener(e -> saveFileAs());
    fileMenu.add(saveAsMenuItem);

    fileMenu.add(new JSeparator());

    extractAllMenuItem = new JMenuItem("Extract All...");
    extractAllMenuItem.addActionListener(e -> extractAll());
    fileMenu.add(extractAllMenuItem);

    fileMenu.add(new JSeparator());

    JMenuItem exitMenuItem = new JMenuItem("Exit");
    exitMenuItem.addActionListener(e -> handleExit());
    fileMenu.add(exitMenuItem);

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
            + "<p>A tool for viewing and modifying Unity packages.</p>"
            + "<p><b>GitHub:</b> <a href='https://github.com/pixel-clover/uview'>https://github.com/pixel-clover/uview</a></p>"
            + "</body></html>";

    java.net.URL iconUrl = App.class.getResource("/logo.svg");
    Icon aboutIcon = (iconUrl != null) ? new FlatSVGIcon(iconUrl).derive(64, 64) : null;

    JLabel messageLabel = new JLabel(message);
    messageLabel.addMouseListener(
        new MouseAdapter() {
          public void mouseClicked(MouseEvent e) {
            try {
              Desktop.getDesktop().browse(new java.net.URI("https://github.com/habedi/uview"));
            } catch (Exception ex) {
              // Ignore
            }
          }
        });

    JOptionPane.showMessageDialog(
        this, messageLabel, title, JOptionPane.INFORMATION_MESSAGE, aboutIcon);
  }

  private String getAppVersion() {
    try (InputStream is =
        App.class.getResourceAsStream("/META-INF/maven/com.uview/uview/pom.properties")) {
      if (is == null) return "N/A";
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
      menuItem.addActionListener(e -> openPackage(file));
      openRecentMenu.add(menuItem);
    }
  }

  private JFileChooser createFileChooser(String title) {
    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle(title);
    chooser.setCurrentDirectory(settingsManager.getLastDirectory());
    chooser.setPreferredSize(new Dimension(1024, 768));
    return chooser;
  }

  private void newPackage() {
    openPackage(null);
  }

  private void openFile() {
    JFileChooser chooser = createFileChooser("Open Unity Package");
    chooser.setFileFilter(new FileNameExtensionFilter("Unity Package", "unitypackage"));
    if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
      File selectedFile = chooser.getSelectedFile();
      settingsManager.setLastDirectory(selectedFile.getParentFile());
      openPackage(selectedFile);
    }
  }

  private void openPackage(File file) {
    setWorking(true, "Loading...");
    PackageViewPanel newPanel = new PackageViewPanel(this, file, settingsManager);
    newPanel.loadPackage(
        () -> {
          int newIndex = tabbedPane.getTabCount();
          tabbedPane.addTab(null, newPanel);
          tabbedPane.setTabComponentAt(newIndex, new ButtonTabComponent(tabbedPane));
          tabbedPane.setSelectedIndex(newIndex);
          if (file != null) {
            settingsManager.addRecentFile(file);
          }
          updateState();
          setWorking(false, null);
        });
  }

  private PackageViewPanel getCurrentPanel() {
    if (tabbedPane.getSelectedIndex() == -1) {
      return null;
    }
    return (PackageViewPanel) tabbedPane.getSelectedComponent();
  }

  private void saveFile() {
    PackageViewPanel currentPanel = getCurrentPanel();
    if (currentPanel == null) return;
    if (currentPanel.getPackageFile() == null) {
      saveFileAs();
    } else {
      savePackageInBackground(currentPanel, currentPanel.getPackageFile());
    }
  }

  private void saveFileAs() {
    PackageViewPanel currentPanel = getCurrentPanel();
    if (currentPanel == null) return;

    JFileChooser chooser = createFileChooser("Save Unity Package As...");
    chooser.setFileFilter(new FileNameExtensionFilter("Unity Package", "unitypackage"));
    if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
      File selectedFile = chooser.getSelectedFile();
      if (!selectedFile.getName().endsWith(".unitypackage")) {
        selectedFile =
            new File(selectedFile.getParentFile(), selectedFile.getName() + ".unitypackage");
      }
      settingsManager.setLastDirectory(selectedFile.getParentFile());
      savePackageInBackground(currentPanel, selectedFile);
    }
  }

  private boolean confirmAndSaveChanges() {
    PackageViewPanel currentPanel = getCurrentPanel();
    if (currentPanel == null || !currentPanel.getPackageManager().isModified()) {
      return true; // No changes to save, proceed.
    }

    int result =
        JOptionPane.showConfirmDialog(
            this,
            "The package '"
                + currentPanel.getTabTitle().replace("*", "")
                + "' has unsaved changes. Do you want to save them?",
            "Unsaved Changes",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.WARNING_MESSAGE);

    if (result == JOptionPane.CANCEL_OPTION) {
      return false; // User cancelled the action.
    }
    if (result == JOptionPane.YES_OPTION) {
      saveFile(); // Save the changes.
    }
    return true; // Proceed with closing (either saved or chose not to).
  }

  void closePackage() {
    if (confirmAndSaveChanges()) {
      int selectedIndex = tabbedPane.getSelectedIndex();
      if (selectedIndex != -1) {
        tabbedPane.remove(selectedIndex);
      }
    }
  }

  private void handleExit() {
    // Iterate through all tabs and check for unsaved changes.
    for (int i = 0; i < tabbedPane.getTabCount(); i++) {
      tabbedPane.setSelectedIndex(i);
      if (!confirmAndSaveChanges()) {
        return; // If user cancels at any point, abort the exit.
      }
    }
    dispose(); // Close the window
    System.exit(0); // Terminate the application
  }

  private void savePackageInBackground(PackageViewPanel panel, File file) {
    setWorking(true, "Saving " + file.getName() + "...");
    SwingWorker<Void, Void> worker =
        new SwingWorker<>() {
          @Override
          protected Void doInBackground() throws Exception {
            panel.getPackageManager().savePackage(file);
            return null;
          }

          @Override
          protected void done() {
            panel.setPackageFile(file); // Update the file reference
            updateState();
            setWorking(false, null);
          }
        };
    worker.execute();
  }

  private void extractAll() {
    PackageViewPanel currentPanel = getCurrentPanel();
    if (currentPanel == null) return;

    JFileChooser chooser = createFileChooser("Select Directory to Extract All Assets");
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

    if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
      Path outputDir = chooser.getSelectedFile().toPath();
      settingsManager.setLastDirectory(outputDir.toFile());
      extractAllInBackground(currentPanel, outputDir);
    }
  }

  private void extractAllInBackground(PackageViewPanel panel, Path outputDir) {
    setWorking(true, "Extracting all assets...");
    SwingWorker<Void, Void> worker =
        new SwingWorker<>() {
          @Override
          protected Void doInBackground() throws Exception {
            // Extract all assets, stripping the root "Assets/" prefix for a clean output
            panel
                .getPackageManager()
                .extractAssets(panel.getPackageManager().getAssets(), outputDir, "Assets/");
            return null;
          }

          @Override
          protected void done() {
            setWorking(false, "Extraction complete.");
            try {
              get(); // Check for exceptions
              Desktop.getDesktop().open(outputDir.toFile());
            } catch (Exception ex) {
              JOptionPane.showMessageDialog(
                  MainWindow.this,
                  "Extraction failed: " + ex.getCause().getMessage(),
                  "Error",
                  JOptionPane.ERROR_MESSAGE);
            }
          }
        };
    worker.execute();
  }

  void updateState() {
    PackageViewPanel currentPanel = getCurrentPanel();
    boolean hasPanel = currentPanel != null;

    saveMenuItem.setEnabled(hasPanel && currentPanel.getPackageManager().isModified());
    saveAsMenuItem.setEnabled(hasPanel);
    closeMenuItem.setEnabled(hasPanel);
    extractAllMenuItem.setEnabled(hasPanel);

    if (hasPanel) {
      int selectedIndex = tabbedPane.getSelectedIndex();
      tabbedPane.setTitleAt(selectedIndex, currentPanel.getTabTitle());

      File file = currentPanel.getPackageFile();
      int fileCount = currentPanel.getPackageManager().getAssets().size();
      fileCountLabel.setText(String.format("Files: %,d", fileCount));

      if (file != null) {
        setTitle("UView - " + file.getName());
        statusLabel.setText("Loaded '" + file.getName() + "'");
        packageSizeLabel.setText(String.format("Size: %s", formatSize(file.length())));
      } else {
        setTitle("UView - New Package");
        statusLabel.setText("New Package");
        packageSizeLabel.setText("");
      }
    } else {
      setTitle("UView");
      statusLabel.setText("Ready");
      fileCountLabel.setText("");
      packageSizeLabel.setText("");
    }
  }

  private void setWorking(boolean working, String status) {
    setCursor(working ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
    if (status != null) {
      statusLabel.setText(status);
    }
  }
}
