package StartUpRoutine;

import java.net.InetAddress;
import java.net.UnknownHostException;

import Stockage.Machine;
import Utilitaires.Global;
import cli.java.org.apache.commons.cli.CommandLine;
import cli.java.org.apache.commons.cli.CommandLineParser;
import cli.java.org.apache.commons.cli.GnuParser;
import cli.java.org.apache.commons.cli.HelpFormatter;
import cli.java.org.apache.commons.cli.MissingArgumentException;
import cli.java.org.apache.commons.cli.MissingOptionException;
import cli.java.org.apache.commons.cli.OptionBuilder;
import cli.java.org.apache.commons.cli.Options;
import cli.java.org.apache.commons.cli.ParseException;
import cli.java.org.apache.commons.cli.PosixParser;

public class IniServer {
	@SuppressWarnings("static-access")
	public static void iniServer(String[] args) {

		String ip = "";
		try {
			ip = InetAddress.getLocalHost().getHostAddress().toString();
		}
		catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("Votre IP est : " + ip);
		Options options = new Options();
		options.addOption("h", "help", false, "prints the help content");
		options.addOption(OptionBuilder.withArgName("serveur").hasArg().withDescription("ip du serveur 0").create("I"));
		options.addOption(OptionBuilder.withArgName("port du serveur").hasArg().withDescription("port du serveur 0").create("P"));
		options.addOption(OptionBuilder.withArgName("debug").hasArg()
				.withDescription("Active le mode debuggage. Doit etre suivi d'un identifiant unique sur la machine").create("d"));
		options.addOption(OptionBuilder.withArgName("port").hasArg().withDescription("port de fonctionnement").create("p"));
		options.addOption(OptionBuilder.withArgName("first node").withDescription("si le serveur est le premier").create("t"));

		try {
			// Etape 2: Analyse de la ligne de commande
			CommandLineParser parser = new GnuParser();
			CommandLine cmd = parser.parse(options, args);

			if (cmd.hasOption("p")) {
				Global.CLIENTPRPORT = Integer.parseInt((cmd.getOptionValue("p")));
				Global.SERVERPRPORT = Global.CLIENTPRPORT + 1;
				Global.TCP_PORT = Global.SERVERPRPORT + 1;
			}
			if (cmd.hasOption("t")){
				Global.FIRST_IP="none#";
				System.out.println("Je suis le serveur de base !");
			}
			if (cmd.hasOption("I"))
				Global.FIRST_IP = cmd.getOptionValue("I");
			if (cmd.hasOption("P"))
				Global.FIRST_PORT = Integer.parseInt(cmd.getOptionValue("P"));
			if (cmd.hasOption("d")) {
				Global.NOM = Integer.parseInt(cmd.getOptionValue("d"));
				Global.DEBUG = true;
			}
			if (cmd.hasOption("h")) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("Server", options);
			}
		}
		catch (MissingOptionException e) {
			// vérifie si l'option -h est présente
			boolean help = false;
			try {
				Options helpOptions = new Options();
				helpOptions.addOption("h", "help", false, "prints the help content");
				CommandLineParser parser = new PosixParser();
				CommandLine line = parser.parse(helpOptions, args);
				if (line.hasOption("h"))
					help = true;
			}
			catch (Exception ex) {
			}
			if (!help)
				System.err.println(e.getMessage());
			// Et oui commons-cli permet aussi d'affiche l'aide
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("Server", options);
			System.exit(1);
		}
		catch (MissingArgumentException e) {
			System.err.println(e.getMessage());
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("Server", options);
			System.exit(1);
		}
		catch (ParseException e) {
			System.err.println("Error while parsing the command line: " + e.getMessage());
			System.exit(1);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		// Attention : a supprimer pour avoir la vraie IP à l'initiatisation :
		try{
			 //String currentDir = System.getProperty("user.dir");
				Global.MYSELF = new Machine(InetAddress.getLocalHost().getHostAddress().toString(), Global.CLIENTPRPORT);

		}
		catch (Exception e) {
		}
	}
}
