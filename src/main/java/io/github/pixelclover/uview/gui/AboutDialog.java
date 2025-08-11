package io.github.pixelclover.uview.gui;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import io.github.pixelclover.uview.App;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Properties;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class AboutDialog extends JDialog {

  public AboutDialog(JFrame owner) {
    super(owner, "About UView", true);
    setLayout(new BorderLayout());

    JPanel contentPanel = new JPanel();
    contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
    contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    // Logo and Title
    FlatSVGIcon logo = new FlatSVGIcon("logo.svg", 64, 64);
    JLabel logoLabel = new JLabel(logo);
    logoLabel.setAlignmentX(CENTER_ALIGNMENT);
    contentPanel.add(logoLabel);

    contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));

    JLabel titleLabel = new JLabel("UView");
    titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.BOLD, 20));
    titleLabel.setAlignmentX(CENTER_ALIGNMENT);
    contentPanel.add(titleLabel);

    JLabel versionLabel = new JLabel("Version: " + getAppVersion());
    versionLabel.setAlignmentX(CENTER_ALIGNMENT);
    contentPanel.add(versionLabel);

    contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));

    // Description
    JTextArea description =
        new JTextArea("A desktop tool for viewing and modifying Unity packages.");
    description.setEditable(false);
    description.setLineWrap(true);
    description.setWrapStyleWord(true);
    description.setBackground(getBackground());
    description.setAlignmentX(CENTER_ALIGNMENT);
    description.setMaximumSize(new Dimension(300, Integer.MAX_VALUE));
    contentPanel.add(description);

    contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));

    // GitHub Link
    JLabel githubLabel =
        new JLabel("<html><a href=''>https://github.com/pixel-clover/uview</a></html>");
    githubLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    githubLabel.setAlignmentX(CENTER_ALIGNMENT);
    githubLabel.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            try {
              Desktop.getDesktop().browse(new URI("https://github.com/pixel-clover/uview"));
            } catch (Exception ex) {
              // Ignore
            }
          }
        });
    contentPanel.add(githubLabel);

    contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));

    // Close Button
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton closeButton = new JButton("Close");
    closeButton.addActionListener(e -> dispose());
    buttonPanel.add(closeButton);

    add(contentPanel, BorderLayout.CENTER);
    add(buttonPanel, BorderLayout.SOUTH);

    pack();
    setResizable(false);
    setLocationRelativeTo(owner);
  }

  private String getAppVersion() {
    try (InputStream is =
        App.class.getResourceAsStream(
            "/META-INF/maven/io.github.pixelclover/uview/pom.properties")) {
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
}
