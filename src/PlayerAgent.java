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

	protected enum Strategy {
		RANDOM, // Randomly choose between rock, paper, scissors
		ROCK, // Chooses rock 80% of the time, paper 10%, scissors 10%
		PAPER, // Chooses paper 80% of the time, rock 10%, scissors 10%
		SCISSORS, // Chooses scissors 80% of the time, rock 10%, paper 10%
		ADAPTATIVE // Our own strategy
	}

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
		displayResponse(getAID().getLocalName() + ": See you");
	}
	public void displayResponse(String message) {
		JOptionPane.showMessageDialog(null,message,"Message",JOptionPane.PLAIN_MESSAGE);
	}

	public void setStrategy(String strategy) {
        // Set the probabilities based on the strategy
        switch (Strategy.valueOf(strategy)) {
            case RANDOM:
                probabilities.put("rock", 1.0 / 3);
                probabilities.put("paper", 1.0 / 3);
                probabilities.put("scissors", 1.0 / 3);
                break;
            case ROCK:
                probabilities.put("rock", 0.8);
                probabilities.put("paper", 0.1);
                probabilities.put("scissors", 0.1);
                break;
            case PAPER:
                probabilities.put("rock", 0.1);
                probabilities.put("paper", 0.8);
                probabilities.put("scissors", 0.1);
                break;
            case SCISSORS:
                probabilities.put("rock", 0.1);
                probabilities.put("paper", 0.1);
                probabilities.put("scissors", 0.8);
                break;
			case ADAPTATIVE:
				//TODO To merge with the strategy
				break;
			default:
				System.out.println("ERROR: Unknown strategy! Defaulting to RANDOM");
				probabilities.put("rock", 1.0 / 3);
                probabilities.put("paper", 1.0 / 3);
                probabilities.put("scissors", 1.0 / 3);
                break;
        }
		myGui.updateProbabilities();
		System.out.println(getAID().getLocalName() + ": Strategy set to: " + strategy);
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
					//Preparing the reply
					reply.setContent(move);
					reply.setConversationId(message.getConversationId());
					reply.setInReplyTo(message.getReplyWith());
					myAgent.send(reply);
					System.out.println(getAID().getLocalName() + ": has sent what he played. He played: " + move);
				} else if (message.getPerformative() == ACLMessage.INFORM) {
					//Handling the result of the game
					String content = message.getContent();
					System.out.println(myAgent.getLocalName() + " received this message: " + content);
					//Parse string to get info about opponent action + result

				} else {
					block();
				}
			} else {
				block();
			}
		}

		public String calculatePlayerAction() {
			double random = Math.random();
			double cumulativeProbability = 0.0;

			for (Map.Entry<String, Double> entry : probabilities.entrySet()) {
				cumulativeProbability += entry.getValue();
				if (random < cumulativeProbability) {
					return entry.getKey();
				}
			}
			// Fallback (shouldn't happen if probabilities sum to 1)
			throw new IllegalStateException("Probabilities do not sum to 1!");
		}
	}
}
