package de.uniluebeck.itm.FlashLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.cli.*;

public class Main {
	
	private static double version = 0.1;
	
	public static void main(String[] args) throws IOException {
		// create Options object
		Option help_option = new Option( "help", "print this message" );
		Option version_option = new Option( "version", "print the version information" );
		
		Options options = new Options();
		
		options.addOption(help_option);
		options.addOption(version_option);
		
		// add options for FlashLoader
		options.addOption("file", true, "Enth�lt das Programm, das geflasht werden soll");

		// for help statement
		HelpFormatter formatter = new HelpFormatter();

		CommandLineParser parser = new GnuParser();
		
		BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));
		String last_line = buffer.readLine();
		args = last_line.split(" ");
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.out.println("Diese Option gibt es nicht.");
		}
		if(cmd != null){
			//standard-options
			if(cmd.hasOption("help")){
				System.out.println("Aufrufbeispiele:");
				System.out.println("Flashloader: flash -port x -file programm.bin");
				System.out.println("");
				formatter.printHelp("help", options);
			}
			if(cmd.hasOption("version")){
				System.out.println(version);
			}
			
			//der FlashLoader
			if(args[0].equals("flash")) {
				System.out.println("starte FlashLoader...");
				
				String port = cmd.getOptionValue("port");
				String server = cmd.getOptionValue("server");
				String file = cmd.getOptionValue("file");
				
				FlashLoader flashLoader = new FlashLoader();
				flashLoader.setPort(port);
				flashLoader.setServer(server);
				flashLoader.setFile(file);
				flashLoader.flash();	
				
			}else if(args[0].equals("readmac")) {
				System.out.println("starte FlashLoader...");
				
				FlashLoader flashLoader = new FlashLoader();
				flashLoader.readmac();	
				
			}else if(args[0].equals("writemac")) {
				System.out.println("starte FlashLoader...");
				
				FlashLoader flashLoader = new FlashLoader();
				flashLoader.writemac();	
				
			}else if(args[0].equals("reset")) {
				System.out.println("starte FlashLoader...");
				
				FlashLoader flashLoader = new FlashLoader();
				flashLoader.reset();	
				
			}
		}
		buffer.close();
	}
}
