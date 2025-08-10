package io.github.pixelclover.uview.gui;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * A panel to be displayed when no tabs are open, welcoming the user and providing quick actions.
 */
public class WelcomePanel extends JPanel {

  /**
   * Constructs a new welcome panel.
   *
   * @param newPackageAction The action to run when the "New Package" button is clicked.
   * @param openFileAction The action to run when the "Open..." button is clicked.
   */
  public WelcomePanel(Runnable newPackageAction, Runnable openFileAction) {
    setLayout(new BorderLayout());
    setBorder(new EmptyBorder(20, 20, 20, 20));

    // Logo and Title
    JPanel headerPanel = new JPanel();
    headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
    headerPanel.setAlignmentX(CENTER_ALIGNMENT);

    FlatSVGIcon logo = new FlatSVGIcon("logo.svg", 128, 128);
    JLabel logoLabel = new JLabel(logo);
    logoLabel.setAlignmentX(CENTER_ALIGNMENT);
    headerPanel.add(logoLabel);

    headerPanel.add(Box.createRigidArea(new Dimension(0, 20)));

    JLabel titleLabel = new JLabel("Welcome to UView");
    titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.BOLD, 24));
    titleLabel.setAlignmentX(CENTER_ALIGNMENT);
    headerPanel.add(titleLabel);

    JLabel subtitleLabel = new JLabel("A tool for viewing and modifying Unity packages.");
    subtitleLabel.setAlignmentX(CENTER_ALIGNMENT);
    headerPanel.add(subtitleLabel);

    add(headerPanel, BorderLayout.CENTER);

    // Action Buttons
    JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
    JButton newButton = new JButton("New Package");
    newButton.addActionListener(e -> newPackageAction.run());
    actionPanel.add(newButton);

    JButton openButton = new JButton("Open...");
    openButton.addActionListener(e -> openFileAction.run());
    actionPanel.add(openButton);

    add(actionPanel, BorderLayout.SOUTH);
  }
}
