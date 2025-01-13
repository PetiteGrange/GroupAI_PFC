package jadelab1;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import javax.swing.*;
import java.util.Map;
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

			// If the recieved message is a request
			if (message.getPerformative() == ACLMessage.CFP) {
				if (message != null) {
					String content = message.getContent(); //Not sure if it is usefull for now.


					// Construction of the reply
	
					ACLMessage reply = message.createReply();
					reply.setPerformative(ACLMessage.INFORM);
	
					reply.setContent("rock"); //Placeholder for now, only playing rock
	
				} else {
					block();
				}
			} else if (message.getPerformative() == ACLMessage.INFORM) {
				// If the recieved message is an inform, PLACEHOLDER FOR NOW
				if (message != null) {
					String content = message.getContent();
					//opponentChoices.put(content, opponentChoices.getOrDefault(content, 0) + 1);
				} else {
					block();
				}
			} else if (message.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
				// If the recieved message is an inform, PLACEHOLDER FOR NOW
				if (message != null) {
					String content = message.getContent();
					//opponentChoices.put(content, opponentChoices.getOrDefault(content, 0) + 1);
				} else {
					block();
				}
			} else if (message.getPerformative() == ACLMessage.REFUSE) {
				// If the recieved message is an inform, PLACEHOLDER FOR NOW
				if (message != null) {
					String content = message.getContent();
					//opponentChoices.put(content, opponentChoices.getOrDefault(content, 0) + 1);
				} else {
					block();
				}
			} else {
				block();
			}
			
		}
	}
}
