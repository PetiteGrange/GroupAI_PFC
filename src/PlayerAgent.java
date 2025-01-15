package jadelab1;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import javax.swing.*;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;

public class PlayerAgent extends Agent {
	private PlayerAgentGui myGui;
	private Map<String, Double> probabilities = new HashMap<>(); // probabilities of playing rock, paper, scissors
	private Map<String, Integer> opponentChoices = new HashMap<>(); // used to track opponent's choices
	private Map<String, Integer> myChoices = new HashMap<>(); //used to track our choices for bluff strategy
	private int turnCounter = 0; // counts the number of turns played
    private static final int RANDOM_TURNS = 10; // number of turns to play randomly

	protected void setup () {
		// Initialize probabilities and opponent tracking
		double rock = Math.random();
		double paper = Math.random() * (1 - rock);
		double scissors = 1 - rock - paper;
		probabilities.put("rock", rock);
		probabilities.put("paper", paper);
		probabilities.put("scissors", scissors);

		// Define the player agent in the DF and define the service
		DFAgentDescription dfd = new DFAgentDescription();
    	dfd.setName(getAID());
    	ServiceDescription sd = new ServiceDescription();
    	sd.setType("player");
    	sd.setName("JADE-rock-paper-scissors");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		// Register the GUI
		myGui = new PlayerAgentGui(this);
		myGui.display();
		addBehaviour(new GameBehaviour());
	}

	public Map<String, Double> getProbabilities() {
		return probabilities;
	}

	protected void takeDown() {
		displayResponse("See you");
	}
	public void displayResponse(String message) {
		JOptionPane.showMessageDialog(null,message,"Message",JOptionPane.PLAIN_MESSAGE);
	}

	private class GameBehaviour extends CyclicBehaviour {
		public void action() {
			//MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
			ACLMessage message = myAgent.receive();
			if (message != null) {
				// If the received message is a request
				if (message.getPerformative() == ACLMessage.CFP) {
					// Construction of the reply
					ACLMessage reply = message.createReply();
					reply.setPerformative(ACLMessage.PROPOSE);
					// Calculating player action
					String move = calculatePlayerAction();
					
					myChoices.put(move, myChoices.getOrDefault(move, 0) + 1);
					
					//Preparing the reply
					reply.setContent(move);
					reply.setConversationId(message.getConversationId());
					reply.setInReplyTo(message.getReplyWith());
					myAgent.send(reply);

					turnCounter++;
					
					System.out.println(getAID().getLocalName() + ": has sent what he played. He played: " + move);
				} else if (message.getPerformative() == ACLMessage.INFORM) {
					//Handling the result of the game
					String content = message.getContent();
					System.out.println(myAgent.getLocalName() + " received this message: " + content);
					//Parse string to get info about opponent action + result
					opponentChoices.put(content, opponentChoices.getOrDefault(content, 0) + 1); //Hashmap needs to have each move possible with the numbers of time they were played
					
				} else {
					block();
				}
			} else {
				block();
			}
		}

	    // Method to find the most frequent move of the opponent
    	private String findMostFrequentMove() {
        	return opponentChoices.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("rock"); // Default to "rock" if no moves are recorded
    	}

		// Method to adapt probabilities based on the opponent's most frequent move
		private void adaptProbabilities() {
			String mostFrequentMove = findMostFrequentMove();
			double adjustmentFactor = 0.1; // Factor to adjust probabilities dynamically
	
			switch (mostFrequentMove) {
				case "rock":
					probabilities.put("rock", Math.max(0.1, probabilities.get("rock") - adjustmentFactor));
					probabilities.put("paper", Math.min(0.8, probabilities.get("paper") + adjustmentFactor)); // Paper beats rock
					probabilities.put("scissors", Math.max(0.1, probabilities.get("scissors") - adjustmentFactor));
					break;
				case "paper":
					probabilities.put("rock", Math.min(0.8, probabilities.get("rock") + adjustmentFactor)); // Rock beats paper
					probabilities.put("paper", Math.max(0.1, probabilities.get("paper") - adjustmentFactor));
					probabilities.put("scissors", Math.max(0.1, probabilities.get("scissors") - adjustmentFactor));
					break;
				case "scissors":
					probabilities.put("rock", Math.max(0.1, probabilities.get("rock") - adjustmentFactor));
					probabilities.put("paper", Math.max(0.1, probabilities.get("paper") - adjustmentFactor));
					probabilities.put("scissors", Math.min(0.8, probabilities.get("scissors") + adjustmentFactor)); // Scissors beat paper
					break;
			}
		}

	    private String calculatePlayerAction() {
			double rand = Math.random();
			if (rand < probabilities.get("rock")) {
				return "rock";
			} else if (rand < probabilities.get("rock") + probabilities.get("paper")) {
				return "paper";
			} else {
				return "scissors";
			}
		}
	}
}
