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
	private String lastOpponentMove = null;

	protected enum Strategy {
		ONESHOT, // Generate probabilities once and play accordingly
		RANDOM, // Randomly choose between rock, paper, scissors
		ROCK, // Chooses rock 80% of the time, paper 10%, scissors 10%
		PAPER, // Chooses paper 80% of the time, rock 10%, scissors 10%
		SCISSORS, // Chooses scissors 80% of the time, rock 10%, paper 10%
		ADAPTATIVE, // Our own strategy
    	SHORT_ADAPTATIVE, // Our own strategy, we reset the saved opponent choices after a certain number of turns
		REACTIVE // Plays the counter move to the opponent's last move
	}
	
	private Strategy currentStrategy = Strategy.RANDOM;

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
		currentStrategy = Strategy.valueOf(strategy);
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
			case ONESHOT:
				double rock = Math.random();
				double paper = Math.random() * (1 - rock);
				double scissors = 1 - rock - paper;
				probabilities.put("rock", rock);
				probabilities.put("paper", paper);
				probabilities.put("scissors", scissors);
				break;
			case ADAPTATIVE, SHORT_ADAPTATIVE:
				break;
			case REACTIVE:
				probabilities.put("rock", 1.0 / 3);
                probabilities.put("paper", 1.0 / 3);
                probabilities.put("scissors", 1.0 / 3);
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

					lastOpponentMove = content;

					//Parse string to get info about opponent action + result
					opponentChoices.put(content, opponentChoices.getOrDefault(content, 0) + 1); //Hashmap needs to have each move possible with the numbers of time they were played
					
				} else {
					block();
				}
			} else {
				block();
			}
		}
		
		private String calculatePlayerAction() {
            switch (currentStrategy) {
                case RANDOM:
				case ONESHOT:
                case ROCK:
                case PAPER:
                case SCISSORS:
                    return selectBasedOnProbabilities();
				case REACTIVE:
					return calculateReactiveMove();
        case ADAPTATIVE, SHORT_ADAPTATIVE:
            if (turnCounter < RANDOM_TURNS) {
                return selectBasedOnProbabilities();
            }
            // Reset the opponent choices after a certain number of turns
            if (currentStrategy == Strategy.SHORT_ADAPTATIVE && turnCounter % 10 == 0) {
                opponentChoices.clear();
            }

            updateProbabilities();
            myGui.updateProbabilities();

            double strategySelector = Math.random();
            if (strategySelector < 0.4) {
                return selectBasedOnProbabilities();
            } else if (strategySelector < 0.7) {
                return counterOpponentMostFrequent();
            } else {
                return counterOwnMostFrequent();
            }

        default:
            return "rock"; // Fallback
            }
        }

        private String selectBasedOnProbabilities() {
            double random = Math.random();
            double cumulativeProbability = 0.0;
            for (Map.Entry<String, Double> entry : probabilities.entrySet()) {
                cumulativeProbability += entry.getValue();
                if (random < cumulativeProbability) {
                    return entry.getKey();
                }
            }
            return "rock"; // Fallback
        }

		private String calculateReactiveMove() {
			if (lastOpponentMove == null) {
				// If no last move, default to random
				System.out.println(myAgent.getLocalName() + ": REACTIVE Playing random move");
				return selectBasedOnProbabilities();
			}
			switch (lastOpponentMove) {
				case "rock":
					return "paper"; // Paper beats rock
				case "paper":
					return "scissors"; // Scissors beat paper
				case "scissors":
					return "rock"; // Rock beats scissors
				default:
					System.out.println(myAgent.getLocalName() + ": REACTIVE Playing random move because incorrect move received");
					return selectBasedOnProbabilities(); // Default to random if unknown move
			}
		}

        private String counterOpponentMostFrequent() {
            String mostFrequent = findMostFrequentMove(opponentChoices);
            switch (mostFrequent) {
                case "rock": return "paper";
                case "paper": return "scissors";
                case "scissors": return "rock";
                default: return "rock";
            }
        }

        private String counterOwnMostFrequent() {
            String mostFrequent = findMostFrequentMove(myChoices);
            switch (mostFrequent) {
                case "rock": return "scissors";
                case "paper": return "rock";
                case "scissors": return "paper";
                default: return "rock";
            }
        }

        private String findMostFrequentMove(Map<String, Integer> choices) {
            return choices.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("rock");
        }

        private void updateProbabilities() {
            String mostFrequent = findMostFrequentMove(opponentChoices);
            switch (mostFrequent) {
                case "rock":
                    adjustProbabilities("paper", "scissors", "rock");
                    break;
                case "paper":
                    adjustProbabilities("scissors", "rock", "paper");
                    break;
                case "scissors":
                    adjustProbabilities("rock", "paper", "scissors");
                    break;
            }
        }

        private void adjustProbabilities(String increaseKey, String decreaseKey1, String decreaseKey2) {
            double increase = 0.05, decrease = 0.025;
            probabilities.put(increaseKey, Math.min(probabilities.get(increaseKey) + increase, 1.0));
            probabilities.put(decreaseKey1, Math.max(probabilities.get(decreaseKey1) - decrease, 0.0));
            probabilities.put(decreaseKey2, Math.max(probabilities.get(decreaseKey2) - decrease, 0.0));
            normalizeProbabilities();
        }

        private void normalizeProbabilities() {
            double total = probabilities.values().stream().mapToDouble(Double::doubleValue).sum();
            probabilities.replaceAll((key, value) -> value / total);
        }
	}
}