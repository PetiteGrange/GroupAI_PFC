package jadelab1;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import javax.swing.*;
import java.util.Map;
import java.util.Objects;
import java.util.HashMap;
import java.util.Random;

public class PlayerAgent extends Agent {
	private PlayerAgentGui myGui;
	private Map<String, Double> probabilities = new HashMap<>(); // probabilities of playing rock, paper, scissors
	private Map<String, Integer> opponentChoices = new HashMap<>(); // used to track opponent's choices
	private Map<String, String> counter = new HashMap<String, String >(); // used to track how to counter choice
	private int roundCounter = 0;

	protected void setup () {
		// Initialize probabilities and opponent tracking
		double rock = Math.random();
		double paper = Math.random() * (1 - rock);
		double scissors = 1 - rock - paper;
		double sum = rock + paper + scissors;
		probabilities.put("rock", rock / sum);
		probabilities.put("paper", paper / sum);
		probabilities.put("scissors", scissors / sum);

		// Initialize counter
		counter.put("rock", "paper");
		counter.put("paper", "scissors");
		counter.put("scissors", "rock");

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
		try	{
			DFService.deregister(this);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
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
					// Calculating player action, the action is either random, based on the player's own probabilities, or based on the opponent's choices
					int random = new Random().nextInt(3);
					String move;
					if (random == 0){
						System.out.println(myAgent.getLocalName() + ": Random move");
						random = new Random().nextInt(3);
						switch (random) {
							case 0:
								move = "rock";
								break;
							case 1:
								move = "paper";
								break;
							case 2:
								move = "scissors";
								break;
							default:
								throw new IllegalStateException("Unexpected value: " + random);
						}
					} else if (random == 1 && !opponentChoices.isEmpty()) {
						System.out.println(myAgent.getLocalName() + ": Counter most played move");
						move = counter.get(findMostPlayed());
					} else {
						System.out.println(myAgent.getLocalName() + ": Calculate move based on probabilities");
						move = calculatePlayerAction();
					}
					//Preparing the reply
					reply.setContent(move);
					reply.setConversationId(message.getConversationId());
					reply.setInReplyTo(message.getReplyWith());
					myAgent.send(reply);
					System.out.println(getAID().getLocalName() + ": has sent what he played. He played: " + move);
				} else if (message.getPerformative() == ACLMessage.INFORM) {
					//Handling the result of the game
					String content = message.getContent();
					if (!content.equals("rock") && !content.equals("paper") && !content.equals("scissors")) {
						System.out.println("Invalid entry");
						block();
					} else {
						opponentChoices.put(content, opponentChoices.getOrDefault(content, 0) + 1);
						roundCounter++;
						//update probabilities every 5 rounds
						if (roundCounter == 5){
							updateProbabilities();
							roundCounter = 0;
							opponentChoices.clear();
						}
					}

				} else {
					block();
				}
			} else {
				block();
			}
		}

		//We calculate the player's action based on the probabilities
		public String calculatePlayerAction() {
			// Ensure probabilities are normalized
			double total = probabilities.values().stream().mapToDouble(Double::doubleValue).sum();
			Map<String, Double> normalizedProbabilities = new HashMap<>();
			probabilities.forEach((key, value) -> normalizedProbabilities.put(key, value / total));

			// Randomly select a move based on probabilities
			double random = Math.random();
			double cumulativeProbability = 0.0;

			for (Map.Entry<String, Double> entry : normalizedProbabilities.entrySet()) {
				cumulativeProbability += entry.getValue();
				if (random < cumulativeProbability) {
					return entry.getKey();
				}
			}

			// Fallback: return the last key (shouldn't happen if probabilities sum to 1)
			return normalizedProbabilities.keySet().iterator().next();
		}

		//Find opponent most played move
		public String findMostPlayed() {
			String mostPlayed = "";
			int max = 0;
			for (Map.Entry<String, Integer> entry : opponentChoices.entrySet()) {
				if (entry.getValue() > max) {
					max = entry.getValue();
					mostPlayed = entry.getKey();
				}
			}
			//add fallback for code security
			if (opponentChoices.isEmpty()){
				return "rock"; // default move
			}
			return mostPlayed;
		}

		//Update the probabilities based on the opponent's choices
		public void updateProbabilities() {
			// Find the opponent's most played move
			String mostPlayed = findMostPlayed();
			if (!Objects.equals(mostPlayed, "")) {
				// Counter move to the most played move
				String counterMove = counter.get(mostPlayed);

				// Adjust probabilities proportionally
				double adjustment = 0.1; // Total adjustment to be distributed
				double counterIncrement = probabilities.get(counterMove) + adjustment;
				double remainingAdjustment = adjustment;

				// Distribute the adjustment across other moves proportionally
				for (String move : probabilities.keySet()) {
					if (!move.equals(counterMove)) {
						double currentProb = probabilities.get(move);
						double decrease = currentProb * adjustment; // Reduce proportionally
						remainingAdjustment -= decrease;
						probabilities.put(move, Math.max(0, currentProb - decrease));
					}
				}

				// Apply the remaining adjustment to the counter move
				probabilities.put(counterMove, probabilities.get(counterMove) + remainingAdjustment);

				// Normalize probabilities to ensure they sum to 1
				double total = probabilities.values().stream().mapToDouble(Double::doubleValue).sum();
				probabilities.replaceAll((key, value) -> value / total);

				// Update the GUI with the new probabilities
				myGui.updateProbabilities();
			}
		}

	}
}
