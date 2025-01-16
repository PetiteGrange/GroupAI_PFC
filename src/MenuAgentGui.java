package jadelab1;

import javax.swing.*;
import java.awt.*;

public class MenuAgentGui extends JFrame {
    private MenuAgent myAgent;

    private JLabel player1Label;
    private JLabel tielabel;
    private JLabel player2label;
    private JSpinner roundsSpinner;
    MenuAgentGui(MenuAgent a) {
        super(a.getLocalName());
        myAgent = a;

        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        // Title label at the top
        JLabel title = new JLabel("Game Menu");
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2; // Span two columns
        p.add(title, gbc);

        // Spinner for the number of rounds
        gbc.gridy = 1;
        gbc.gridwidth = 1; // Reset grid width
        gbc.gridx = 0;
        p.add(new JLabel("Rounds to play:"), gbc);

        roundsSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 15, 1)); // Default: 1, Min: 1, Max: 15, Step: 1
        gbc.gridx = 1;
        p.add(roundsSpinner, gbc);

        // Play button
        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.gridwidth = 2; // Span two columns
        JButton playButton = new JButton("Play");
        playButton.addActionListener(e -> {
            myAgent.setRoundsToPlay((int) roundsSpinner.getValue());
            myAgent.playRound();
        });
        playButton.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(playButton, gbc);

        // Label for player1
        gbc.gridwidth = 1; // Reset grid width
        gbc.gridx = 0;
        gbc.gridy = 3;
        p.add(new JLabel(myAgent.getPlayerAgentName(0) + " Victories:"), gbc);
        gbc.gridx = 1;
        player1Label = new JLabel(String.valueOf(a.getScore().get(myAgent.getPlayerAgentName(0))));
        player1Label.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(player1Label, gbc);

        // Label for tie
        gbc.gridx = 0;
        gbc.gridy = 4;
        p.add(new JLabel("Ties:"), gbc);
        gbc.gridx = 1;
        tielabel = new JLabel(String.valueOf(a.getScore().get("ties")));
        tielabel.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(tielabel, gbc);

        // Label for scissors
        gbc.gridx = 0;
        gbc.gridy = 5;
        p.add(new JLabel(myAgent.getPlayerAgentName(1)+ " Victories:"), gbc);
        gbc.gridx = 1;
        player2label = new JLabel(String.valueOf(a.getScore().get(myAgent.getPlayerAgentName(1))));
        player2label.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(player2label, gbc);

        // Add panel to frame
        getContentPane().add(p);
        this.setSize(250, 220);
    }

    public void updateScore() {
        player1Label.setText(String.valueOf(myAgent.getScore().get(myAgent.getPlayerAgentName(0))));
        tielabel.setText(String.valueOf(myAgent.getScore().get("ties")));
        player2label.setText(String.valueOf(myAgent.getScore().get(myAgent.getPlayerAgentName(1))));
    }

    public void dispose() {
    }

    public void display() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int centerX = (int)screenSize.getWidth() / 2;
        int centerY = (int)screenSize.getHeight() / 2;
        setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
        setVisible(true);
    }
}
