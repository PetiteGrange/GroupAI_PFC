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
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
			ACLMessage message = agent.receive(mt);
			if (message != null)
			{
				String content = msg.getContent();
	      		ACLMessage reply = msg.createReply();

			} else {
				block();
			}
		}
	}
}
