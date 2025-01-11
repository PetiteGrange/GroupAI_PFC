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
		opponentChoices = new HashMap<>();
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
		MyAgent myAgent;
		public MyCyclicBehaviour(MyAgent myAgent) {
			this.myAgent = myAgent;
		}

		public void action() {
			ACLMessage message = agent.receive();

			// If the recieved message is a request
			if (message.getPerformative() == ACLMessage.REQUEST) {
				if (message != null) {
					String content = msg.getContent(); //Not sure if it is usefull for now.


					// Construction of the reply
	
					ACLMessage reply = msg.createReply();
					reply.setPerformative(ACLMessage.INFORM);
	
					reply.setContent("rock"); //Placeholder for now, only playing rock
	
				} else {
					block();
				}
			} else if (message.getPerformative() == ACLMessage.INFORM) {
				// If the recieved message is an inform, PLACEHOLDER FOR NOW
				if (message != null) {
					String content = msg.getContent();
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
