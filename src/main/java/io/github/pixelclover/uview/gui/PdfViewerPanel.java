package io.github.pixelclover.uview.gui;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.swing.*;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

/** A panel that displays a PDF document with page navigation controls. */
public class PdfViewerPanel extends JPanel {

  private final PDDocument document;
  private final PDFRenderer renderer;
  private final JLabel pageLabel = new JLabel();
  private final JButton prevButton = new JButton("< Prev");
  private final JButton nextButton = new JButton("Next >");
  private final JLabel statusLabel = new JLabel();
  private final JTextField pageInputField = new JTextField(4);
  private int currentPage = 0;

  /**
   * Constructs a PdfViewerPanel.
   *
   * @param pdfData The byte array containing the PDF data.
   * @throws IOException If the PDF data cannot be loaded.
   */
  public PdfViewerPanel(byte[] pdfData) throws IOException {
    super(new BorderLayout());

    // Load the document and create a renderer
    this.document = Loader.loadPDF(pdfData);
    this.renderer = new PDFRenderer(document);

    // Main display area for the rendered page
    pageLabel.setHorizontalAlignment(JLabel.CENTER);
    add(new JScrollPane(pageLabel), BorderLayout.CENTER);

    // Navigation and status controls panel
    JPanel controlsPanel = new JPanel();
    controlsPanel.setLayout(new BoxLayout(controlsPanel, BoxLayout.X_AXIS));

    // Left-aligned navigation
    JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    navPanel.add(prevButton);
    navPanel.add(nextButton);

    // Right-aligned page jump
    JPanel jumpPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    jumpPanel.add(new JLabel("Go to page:"));
    jumpPanel.add(pageInputField);

    controlsPanel.add(navPanel);
    controlsPanel.add(statusLabel);
    controlsPanel.add(jumpPanel);
    add(controlsPanel, BorderLayout.SOUTH);

    setupActionListeners();
    updatePage(0); // Display the first page initially
  }

  private void setupActionListeners() {
    prevButton.addActionListener(e -> updatePage(currentPage - 1));
    nextButton.addActionListener(e -> updatePage(currentPage + 1));

    pageInputField.addKeyListener(
        new KeyAdapter() {
          @Override
          public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
              jumpToPage();
            }
          }
        });
  }

  private void jumpToPage() {
    try {
      int pageNum = Integer.parseInt(pageInputField.getText());
      // Convert 1-based user input to 0-based index
      updatePage(pageNum - 1);
    } catch (NumberFormatException e) {
      JOptionPane.showMessageDialog(
          this, "Please enter a valid page number.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
    }
  }

  private void updatePage(int newPageIndex) {
    if (newPageIndex < 0 || newPageIndex >= document.getNumberOfPages()) {
      return; // Page index is out of bounds
    }
    currentPage = newPageIndex;

    // Render the page in a background thread to keep the UI responsive
    SwingUtilities.invokeLater(
        () -> {
          try {
            BufferedImage image = renderer.renderImageWithDPI(currentPage, 150); // 150 DPI
            pageLabel.setIcon(new ImageIcon(image));
            statusLabel.setText("Page " + (currentPage + 1) + " of " + document.getNumberOfPages());
            pageInputField.setText(String.valueOf(currentPage + 1));

            // Enable/disable navigation buttons
            prevButton.setEnabled(currentPage > 0);
            nextButton.setEnabled(currentPage < document.getNumberOfPages() - 1);
          } catch (IOException e) {
            pageLabel.setText("Failed to render page: " + e.getMessage());
          }
        });
  }

  /**
   * Closes the underlying PDDocument to free up resources. This must be called when the panel is no
   * longer needed.
   *
   * @throws IOException if the document cannot be closed.
   */
  public void close() throws IOException {
    if (document != null) {
      document.close();
    }
  }
}
