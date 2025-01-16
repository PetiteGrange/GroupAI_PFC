package jadelab1;

import javax.swing.*;

import jadelab1.PlayerAgent.Strategy;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class PlayerAgentGui extends JFrame {
    private PlayerAgent myAgent;

    private JLabel rockProbLabel;
    private JLabel paperProbLabel;
    private JLabel scissorsProbLabel;

    private JComboBox<Strategy> strategyComboBox;

    public PlayerAgentGui(PlayerAgent a) {
        super(a.getLocalName());
        myAgent = a;

        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        // Title label at the top
        JLabel title = new JLabel("Action Probabilities");
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2; // Span two columns
        p.add(title, gbc);

        //TODO Limiter les décimales à 4

        // Label for rock
        gbc.gridwidth = 1; // Reset grid width
        gbc.gridx = 0;
        gbc.gridy = 1;
        p.add(new JLabel("Rock:"), gbc);
        gbc.gridx = 1;
        rockProbLabel = new JLabel(String.format("%.2f", a.getProbabilities().get("rock")));
        rockProbLabel.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(rockProbLabel, gbc);

        // Label for paper
        gbc.gridx = 0;
        gbc.gridy = 2;
        p.add(new JLabel("Paper:"), gbc);
        gbc.gridx = 1;
        paperProbLabel = new JLabel(String.format("%.2f", a.getProbabilities().get("paper")));
        paperProbLabel.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(paperProbLabel, gbc);

        // Label for scissors
        gbc.gridx = 0;
        gbc.gridy = 3;
        p.add(new JLabel("Scissors:"), gbc);
        gbc.gridx = 1;
        scissorsProbLabel = new JLabel(String.format("%.2f", a.getProbabilities().get("scissors")));
        scissorsProbLabel.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(scissorsProbLabel, gbc);

        // Combobox for strategy
        strategyComboBox = new JComboBox<>(Strategy.values());
        strategyComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Strategy selectedStrategy = (Strategy) strategyComboBox.getSelectedItem();
                myAgent.setStrategy(selectedStrategy.name());
            }
        });
        gbc.gridx = 0;
        gbc.gridy = 4;
        p.add(strategyComboBox, gbc);

        // Add panel to frame
        getContentPane().add(p);
        this.setSize(250, 220);
    }

    public void updateProbabilities() {
        System.out.println("Updating probabilities: rock = " + myAgent.getProbabilities().get("rock") + ", paper = " + myAgent.getProbabilities().get("paper") + ", scissors = " + myAgent.getProbabilities().get("scissors"));
        rockProbLabel.setText(String.valueOf(myAgent.getProbabilities().get("rock")));
        paperProbLabel.setText(String.valueOf(myAgent.getProbabilities().get("paper")));
        scissorsProbLabel.setText(String.valueOf(myAgent.getProbabilities().get("scissors")));
    }

    public void display() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int centerX = (int)screenSize.getWidth() / 2;
        int centerY = (int)screenSize.getHeight() / 2;
        setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
        setVisible(true);
    }
}
