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
	private double[] probabilities; // probabilities for {rock, paper, scissors}
	private Map<String, Integer> opponentChoices; // used to track opponent's choices

	protected void setup () {
		// Initialize probabilities and opponent tracking
		double rock = Math.random();
		double paper = Math.random() * (1 - rock);
		double scissors = 1 - rock - paper;
		probabilities = new double[]{rock, paper, scissors};

		// Define the memory of the player agent
		opponentChoices = new HashMap<>();

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

	public double[] getProbabilities() {
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
			ACLMessage message = myAgent.receive();

			if (message != null) {
				// If the received message is a request
				if (message.getPerformative() == ACLMessage.CFP) {
					String content = message.getContent(); // Not sure if it is useful for now.

					// Construction of the reply
					ACLMessage reply = message.createReply();
					reply.setPerformative(ACLMessage.PROPOSE);
					reply.setContent("rock"); // Placeholder for now, only playing rock
					reply.setConversationId(message.getConversationId());
					reply.setReplyWith(message.getReplyWith());
					myAgent.send(reply);

					System.out.println(getAID().getLocalName() + ": has sent what he played.");

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
	}
}
