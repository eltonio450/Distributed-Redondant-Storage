import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import RelationsPubliques.*;

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


public class Main{


	public static void main(String[] args)  {
		//System.out.println("Hello World !");

		

		//Etape 1: Définition des options
		Options options = new Options();
		options.addOption("h", "help", false, "prints the help content");
		options.addOption(OptionBuilder
				.withArgName("serveur")
				.hasArg()
				.withDescription("ip du serveur 0")
				.create("s"));
		options.addOption(OptionBuilder
				.withArgName("debug")
				.withDescription("active le mode debuggage")
				.create("d"));
		options.addOption(OptionBuilder
				.withArgName("port")
				.hasArg()
				.withDescription("port de fonctionnement")
				.create("p"));

		try{
			//Etape 2: Analyse de la ligne de commande
			CommandLineParser parser = new GnuParser();
			CommandLine cmd = parser.parse(options, args);

			//Etape 3: Récupération et traitement des résultat
			//InputStream in = new FileInputStream(cmd.getOptionValue("i"));
			//Par défaut écrit la sortie sur la sortie standard
			if(cmd.hasOption("d")){
				Global.CLIENTPRPORT = Integer.parseInt((cmd.getOptionValue("d")));
				Global.SERVERPRPORT = Global.CLIENTPRPORT++;
			}
				
			if(cmd.hasOption("s"))
				Global.FIRSTIP = cmd.getOptionValue("s");
			if(cmd.hasOption("d"))
				Global.DEBUG = true;
			if(cmd.hasOption("h")){
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp( "Server" , options );
			}
		}
		catch(MissingOptionException e){
			//vérifie si l'option -h est présente
			boolean help = false;
			try{
				Options helpOptions = new Options();
				helpOptions.addOption("h", "help", false, "prints the help content");
				CommandLineParser parser = new PosixParser();
				CommandLine line = parser.parse(helpOptions, args);
				if(line.hasOption("h")) help = true;
			}
			catch(Exception ex){ }
			if(!help) System.err.println(e.getMessage());
			//Et oui commons-cli permet aussi d'affiche l'aide
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp( "Server" , options );
			System.exit(1);
		} catch(MissingArgumentException e){
			System.err.println(e.getMessage());
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp( "Server" , options );
			System.exit(1);
		} catch(ParseException e){
			System.err.println("Error while parsing the command line: "+e.getMessage());
			System.exit(1);
		} catch(Exception e){
			e.printStackTrace();
		}


		//variables de diagnostic





		//Main.main(arguments);

		//try{
		//portTexte = args[0];
		//} catch(Exception e) {portTexte = "5040";}

		//int port = Integer.parseInt(portTexte);

		//System.out.println("Port de fonctionnement : " + port);
	}

}
