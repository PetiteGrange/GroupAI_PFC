package jadelab1;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import java.net.*;
import java.io.*;

public class MenuAgent extends Agent {
	private MenuAgentGui myGui;
	private int[] score; // scores for {player1, tie, player2}

	protected void setup () {
		score = new int[]{0, 0, 0};
		myGui = new MenuAgentGui(this);
		myGui.display();
	}
	protected void takeDown() {
		myGui.dispose();
		System.out.println("Menu agent " + getAID().getLocalName() + " terminated.");
	}

	public int[] getScore() {
		return score;
	}

	public void playRound() {
		System.out.println("==== Now Starting a Round ====");

		// TODO UPDATE THE LIST OF KNOWN PLAYERS
	}

}

	private class ControllerBehaviour extends Behaviour {
		int step = 0;


		public void action() {
			switch (step) {
				case 0: // Cette partie consiste à envoyer un message à l'agent joueur pour lui demander de jouer
					
					break;

				case 1: // Cette partie consiste à recevoir le message de l'agent joueur
					
					break;

				case 2: // Cette partie consiste à déterminer le gagnant
					
					break;
			
				default: // PLACEHOLDER
					break;
			}
		}

		
		public boolean done() { // PLACEHOLDER to finish the behaviour
			return false;
		}
	}



