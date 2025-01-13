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
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
			ACLMessage message = myAgent.receive(mt);
			if (message != null) {
				// If the received message is a request
				if (message.getPerformative() == ACLMessage.CFP) {
					String content = message.getContent(); // Not sure if it is useful for now.
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




					// TODO Handle the result of the game
				} else if (message.getPerformative() == ACLMessage.INFORM) {
					// If the received message is an inform, PLACEHOLDER FOR NOW
					String content = message.getContent();
					System.out.println(getAID().getLocalName() + ": INFORM is not handled yet.");

				} else if (message.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
					// If the received message is an accept proposal, it means the player won
					String content = message.getContent();
					System.out.println(getAID().getLocalName() + ": ACCEPT_PROPOSAL is not handled yet.");

				} else if (message.getPerformative() == ACLMessage.REJECT_PROPOSAL) {
					// If the received message is a reject proposal, it means the player lost
					String content = message.getContent();
					System.out.println(getAID().getLocalName() + ": REJECT_PROPOSAL is not handled yet.");

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
