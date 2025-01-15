package jadelab1;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import java.net.*;
import java.nio.file.attribute.AclEntryPermission;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class MenuAgent extends Agent {
	private MenuAgentGui myGui;
	private Map<String, Integer> score = new HashMap<>();
	private AID[] playerAgents; // list of known player agents

	protected void setup () {
		// Before starting the game, we need to identify our players
		if (findPlayers()) {
			System.out.println("Players are set the Game can start");
			score.put(playerAgents[0].getLocalName(), 0);
			score.put(playerAgents[1].getLocalName(), 0);
			score.put("ties", 0);
			myGui = new MenuAgentGui(this);
			myGui.display();
		} else {
			System.out.println("Players are not set, unable to start the game");
			doDelete();
		}
	}

	private boolean findPlayers() {
		System.out.println(getAID().getLocalName() + ": Looking for players");

		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType("player");
		template.addServices(sd);
		int retries = 5; // Retry 5 times

		for (int attempt = 0; attempt < retries; attempt++) {
		System.out.println("Attempt " + attempt);
			try {
			DFAgentDescription[] result = DFService.search(this, template);
			System.out.println(getAID().getLocalName() + ": Found the following player agents:" + result.length);
			if (result.length < 2) {
				System.out.println("Not enough players found. The game requires 2 players.");
				Thread.sleep(1000); // Wait 1 second before retrying
				continue;
			}
			playerAgents = new AID[2]; //Limit the number of players to 2
			// NOTE: If there is more than 2 players, he will always pick the 2 players the most recently loaded
			for (int i = 0; i < 2; ++i) {
				playerAgents[i] = result[i].getName();
				System.out.println("Found player " + (i+1) + ": " + playerAgents[i].getLocalName());
			}
			return true;
		} catch (FIPAException | InterruptedException e) {
			e.printStackTrace();
		}
		}
		System.out.println("Failed to find enough players after retries.");
		return false;
	}
	protected void takeDown() {
		if (myGui != null){
			myGui.dispose();
		}
		System.out.println("Menu agent " + getAID().getLocalName() + " terminated.");
	}

	public Map<String, Integer> getScore() {
		return score;
	}

	public String getPlayerAgentName(int index) {
		return playerAgents[index].getLocalName();
	}

	public void playRound() {
		System.out.println("==== Now Starting a Round ====");
		addBehaviour(new ControllerBehaviour());
	}

	private class ControllerBehaviour extends Behaviour {
		private Map<String, String> playerActions = new HashMap<>();
		private int step = 0;
		private int repliesCnt = 0;
		private MessageTemplate mt;
		private long start = System.currentTimeMillis(); //Used to track time, check if player is taking too long

		enum Results {
			FAILURE,
			P1_WINS,
			P2_WINS,
			DRAW
		}
		private Results result;

		public void action() {
			switch (step) {
				case 0: //Step 0: Send a CFP to all players

					ACLMessage cfp = new ACLMessage(ACLMessage.CFP); // Solicit player for it's action

					//Add players to the list of receivers
					for (AID playerAgent : playerAgents) {
						cfp.addReceiver(playerAgent);
					}

					// Set Message details
					cfp.setConversationId("RPS-game");
					cfp.setReplyWith("cfp" + System.currentTimeMillis());
					myAgent.send(cfp);

					//Prepare the template to get proposals
					mt = MessageTemplate.and(
							MessageTemplate.MatchConversationId("RPS-game"),
							MessageTemplate.MatchInReplyTo(cfp.getReplyWith())
					);

					System.out.println("CFP sent to all found players :" + playerAgents.length);
					step = 1;
					break;

				case 1: //Step 1: Receive and Wait for the players' actions
					ACLMessage reply = myAgent.receive(mt);
					if (reply != null) {
						if (reply.getPerformative() == ACLMessage.PROPOSE) {
							if (reply.getContent() != null && repliesCnt < playerAgents.length) {
								playerActions.put(reply.getSender().getLocalName(), reply.getContent());
								repliesCnt++;
							}
							if (repliesCnt == playerAgents.length) {
								System.out.println("Received all proposals");
								step = 2;
							}
						}
					} else {
						long end = System.currentTimeMillis();
						if (end - start >= 10000) {
							System.out.println("Time has expired. The game is canceled due to lack of response from a player");
							step = 4;
						} else {
							block(1000); // Wait for 1 second before checking again
						}
					}
					break;

				case 2: // Step 2: Determine the winner thanks to the players' actions
					//Checking the players' submitted actions

					//If the players did not respond properly, the game is canceled
					if (playerActions.get(playerAgents[0].getLocalName()) == null || playerActions.get(playerAgents[1].getLocalName()) == null) {
						System.out.println("One of the players did not respond. The game is canceled.");

						//Define Result as a failure to start the game
						result = Results.FAILURE;
						step = 3;
						break;

					} else {

						for (int i = 0; i < 2; i++){
							if ( !playerActions.get(playerAgents[i].getLocalName()).equals("rock") && !playerActions.get(playerAgents[i].getLocalName()).equals("paper") && !playerActions.get(playerAgents[i].getLocalName()).equals("scissors")) {
								System.out.println("Player" + (i+1) + " did not respond properly. The game is canceled.");

								//Define Result as a failure to start the game
								result = Results.FAILURE;
								step = 3; 
								break;
							}
						}
						if (playerActions.get(playerAgents[0].getLocalName()).equals(playerActions.get(playerAgents[1].getLocalName()))) {
							System.out.println("It's a tie! Both players played " + playerActions.get(playerAgents[0].getLocalName()));

							// Define the result as a draw
							score.put("ties", score.get("ties") + 1);
							result = Results.DRAW;

						} else if (playerActions.get(playerAgents[0].getLocalName()).equals("rock") && playerActions.get(playerAgents[1].getLocalName()).equals("scissors") ||
								playerActions.get(playerAgents[0].getLocalName()).equals("paper") && playerActions.get(playerAgents[1].getLocalName()).equals("rock") ||
								playerActions.get(playerAgents[0].getLocalName()).equals("scissors") && playerActions.get(playerAgents[1].getLocalName()).equals("paper")) {
									// TODO Is there a better approach to this?
							System.out.println(playerAgents[0].getLocalName() + " wins!");

							// Define the result as player 1 wins
							score.put(playerAgents[0].getLocalName(), score.get(playerAgents[0].getLocalName()) + 1);
							result = Results.P1_WINS;

						} else {
							System.out.println(playerAgents[1].getLocalName() + " wins!");

							// Define the result as player 2 wins
							score.put(playerAgents[1].getLocalName(), score.get(playerAgents[1].getLocalName()) + 1);
							result = Results.P2_WINS;

						}
						myGui.updateScore();
						step = 3; //TODO Define a score limit to the game
						break;

					}
					
				case 3: // Step 3: Send the result to players
					if (result == null) {
						System.out.println("Error: Result not defined");
						step = 4;
						break;
					}
					for (int i = 0; i < 2; i++) {
						int messageType;
						String conversationIdPrefix;
						switch (result){
							case DRAW:
								messageType = ACLMessage.INFORM;
								conversationIdPrefix = "inform";
								break;
							case P1_WINS:
								if (i == 0) {
									messageType = ACLMessage.ACCEPT_PROPOSAL;
									conversationIdPrefix = "accept";
								} else {
									messageType = ACLMessage.REJECT_PROPOSAL;
									conversationIdPrefix = "reject";
								}
								break;
							case P2_WINS:
								if (i == 1) {
									messageType = ACLMessage.ACCEPT_PROPOSAL;
									conversationIdPrefix = "accept";
								} else {
									messageType = ACLMessage.REJECT_PROPOSAL;
									conversationIdPrefix = "reject";
								}
								break;
							default:
								// In case of failure we send the info to both players
								// even to the player that did not cause the error
								// perhaps adapt message content to inform the player of the error
								// if the error is caused by the player maybe the message can cause the player to reset??
								messageType = ACLMessage.FAILURE;
								conversationIdPrefix = "failure";
								break;
						}
						ACLMessage message = new ACLMessage(messageType);
						message.addReceiver(playerAgents[i]);
						message.setConversationId(conversationIdPrefix + System.currentTimeMillis());
						//Here adapt content if error message perhaps
						message.setContent(playerActions.get(playerAgents[1-i].getLocalName()));
						myAgent.send(message);
					}
					step = 4;
					break;
				}

		}

		public boolean done() { // PLACEHOLDER to finish the behaviour
			//process terminates here if the game is canceled or if the game is finished
			return step == 4;
		}
	}
}



