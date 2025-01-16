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

	protected enum Strategy {
		RANDOM, // Randomly choose between rock, paper, scissors
		ROCK, // Chooses rock 80% of the time, paper 10%, scissors 10%
		PAPER, // Chooses paper 80% of the time, rock 10%, scissors 10%
		SCISSORS, // Chooses scissors 80% of the time, rock 10%, paper 10%
		ADAPTATIVE // Our own strategy
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
		displayResponse("See you");
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
			case ADAPTATIVE:
				break;
			default:
				System.out.println("ERROR: Unknown strategy! Defaulting to RANDOM");
				probabilities.put("rock", 1.0 / 3);
                probabilities.put("paper", 1.0 / 3);
                probabilities.put("scissors", 1.0 / 3);
                break;
        }
		myGui.updateProbabilities();
		System.out.println("Strategy set to: " + strategy);
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
		
		private void normalizeProbabilities(Map<String, Double> probabilities) {
		    double total = probabilities.values().stream().mapToDouble(Double::doubleValue).sum();
		    if (total > 0) {
		        probabilities.replaceAll((key, value) -> value / total);
		    } else {
		        // Handle case where total is 0 (fallback to equal probabilities)
		        int size = probabilities.size();
		        probabilities.replaceAll((key, value) -> 1.0 / size);
		    }
		}


	    private String findMostFrequentMove(Map<String, Integer> choices) {
	        String mostFrequentMove = "rock";
	        int maxCount = 0;

	        for (Map.Entry<String, Integer> entry : choices.entrySet()) {
	            if (entry.getValue() > maxCount) {
	                maxCount = entry.getValue();
	                mostFrequentMove = entry.getKey();
	            }
	        }
	        return mostFrequentMove;
	    }
	    
	    private void updateProbabilities() {
	    	
	        String mostFrequentMove = findMostFrequentMove(opponentChoices);
	        
	        double increase = 0.01; 
	        double decrease = 0.005; 
	        
	        switch (mostFrequentMove) {
	            case "scissors":
	                probabilities.put("rock", Math.min(probabilities.get("rock") + increase, 1.0));
	                probabilities.put("paper", Math.max(probabilities.get("paper") - decrease, 0.0));
	                probabilities.put("scissors", Math.max(probabilities.get("scissors") - decrease, 0.0));
	                break;

	            case "rock":
	                probabilities.put("paper", Math.min(probabilities.get("paper") + increase, 1.0));
	                probabilities.put("rock", Math.max(probabilities.get("rock") - decrease, 0.0));
	                probabilities.put("scissors", Math.max(probabilities.get("scissors") - decrease, 0.0));
	                break;

	            case "paper":
	                probabilities.put("scissors", Math.min(probabilities.get("scissors") + increase, 1.0));
	                probabilities.put("rock", Math.max(probabilities.get("rock") - decrease, 0.0));
	                probabilities.put("paper", Math.max(probabilities.get("paper") - decrease, 0.0));
	                break;
	        }
	        
	        normalizeProbabilities(probabilities);
	    }
	    
	    private void play() {
	    	double random = Math.random();
        	double cumulativeProbability = 0.0;
        	for (Map.Entry<String, Double> entry : probabilities.entrySet()) {
            	cumulativeProbability += entry.getValue();
            	if (random < cumulativeProbability) {
                	return entry.getKey();
            	}
        	}
        	break;
	    }


	    public String calculatePlayerAction() {
	        switch (currentStrategy) {
	            case RANDOM:play();
	            case ROCK: play();
	            case PAPER: play();
	            case SCISSORS:play();
	            
	            case ADAPTATIVE:
	                if (turnCounter < RANDOM_TURNS) {
	                    play();
	                }

	                updateProbabilities();
	                myGui.updateProbabilities();

	                double strategySelector = Math.random();

	                if (strategySelector < 0.4) {
	                    random = Math.random();
	                    cumulativeProbability = 0.0;
	                    for (Map.Entry<String, Double> entry : probabilities.entrySet()) {
	                        cumulativeProbability += entry.getValue();
	                        if (random < cumulativeProbability) {
	                            return entry.getKey();
	                        }
	                    }
	                } else if (strategySelector < 0.7) {
	                    String mostFrequentMove = findMostFrequentMove(opponentChoices);
	                    switch (mostFrequentMove) {
	                        case "rock": return "paper";
	                        case "paper": return "scissors";
	                        case "scissors": return "rock";
	                    }
	                } else {
	                    String myMostFrequentMove = findMostFrequentMove(myChoices);
	                    switch (myMostFrequentMove) {
	                        case "rock": return "scissors";
	                        case "paper": return "rock";
	                        case "scissors": return "paper";
	                    }
	                }
	                break;
	        }

	        return "rock"; // Default fallback
	    }
	}

}