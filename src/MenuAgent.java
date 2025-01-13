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
			takeDown();
		}
	}

	private boolean findPlayers() {
		System.out.println(getAID().getLocalName() + ": Looking for players");

		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType("player");
		template.addServices(sd);

		// Try to search for players using the template
		try {
			DFAgentDescription[] result = DFService.search(this, template);
			System.out.println(getAID().getLocalName() + ": Found the following player agents:");
			if (result.length < 2) {
				System.out.println("Not enough players found. The game requires 2 players.");
				playerAgents = null;
				return false;
			}
			playerAgents = new AID[2]; //Limit the number of players to 2
			for (int i = 0; i < 2; ++i) {
				playerAgents[i] = result[i].getName();
				System.out.println("Found player " + i + ": " + playerAgents[i].getLocalName());
			}
		} catch (FIPAException fe) {
			fe.printStackTrace();
			playerAgents = null;
			return false;
		}
		if (playerAgents.length == 2) {
			return true;
		} else {
			return false;
		}
	}
	protected void takeDown() {
		myGui.dispose();
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


		public void action() {
			switch (step) {
				case 0: //Step 0: Send a CFP to all players
					//call for action to found players
					ACLMessage cfp = new ACLMessage(ACLMessage.CFP); // Solicit player for it's action
					//Add players to the list of receivers
					for (AID playerAgent : playerAgents) {
						cfp.addReceiver(playerAgent);
					}
					cfp.setConversationId("RPS-game");
					cfp.setReplyWith("cfp" + System.currentTimeMillis());
					myAgent.send(cfp);
					mt = MessageTemplate.and(
							MessageTemplate.MatchConversationId("RPS-game"),
							MessageTemplate.MatchInReplyTo(cfp.getReplyWith())
					);
//					mt = MessageTemplate.MatchConversationId("RPS-game");
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
							step = 3;
						} else {
							block(1000); // Wait for 1 second before checking again
						}
					}
					break;

				case 2: // Step 2: Determine the winner thanks to the players' actions
					//Checking the players' submitted actions
					if (playerActions.get(playerAgents[0].getLocalName()) == null || playerActions.get(playerAgents[1].getLocalName()) == null) {
						System.out.println("One of the players did not respond properly. The game is canceled.");
						step = 3;
						break;
					}
					for (int i = 0; i < 2; i++){
						if ( !playerActions.get(playerAgents[i].getLocalName()).equals("rock") && !playerActions.get(playerAgents[i].getLocalName()).equals("paper") && !playerActions.get(playerAgents[i].getLocalName()).equals("scissors")) {
							System.out.println("Player" + i + " did not respond properly. The game is canceled.");
							step = 3; //TODO Find another way to finish the game, perhaps send a message to the player who hasn't responded properly
							break;
						}
					}
					if (playerActions.get(playerAgents[0].getLocalName()).equals(playerActions.get(playerAgents[1].getLocalName()))) {
						System.out.println("It's a tie! Both players played " + playerActions.get(playerAgents[0].getLocalName()));
						score.put("ties", score.get("ties") + 1);
					} else if (playerActions.get(playerAgents[0].getLocalName()).equals("rock") && playerActions.get(playerAgents[1].getLocalName()).equals("scissors") ||
							playerActions.get(playerAgents[0].getLocalName()).equals("paper") && playerActions.get(playerAgents[1].getLocalName()).equals("rock") ||
							playerActions.get(playerAgents[0].getLocalName()).equals("scissors") && playerActions.get(playerAgents[1].getLocalName()).equals("paper")) {
						System.out.println(playerAgents[0].getLocalName() + " wins!");
						score.put(playerAgents[0].getLocalName(), score.get(playerAgents[0].getLocalName()) + 1);
					} else {
						System.out.println(playerAgents[1].getLocalName() + " wins!");
						score.put(playerAgents[1].getLocalName(), score.get(playerAgents[1].getLocalName()) + 1);
					}
					myGui.updateScore();
					step = 3; //TODO Define a score limit to the game
					break;
				case 3: // Step 3: Send the result to players
					//TODO send the result to the players
					

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



