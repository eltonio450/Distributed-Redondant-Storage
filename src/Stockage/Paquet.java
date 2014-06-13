package Stockage;


import RelationsPubliques.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.LinkedList;

public class Paquet {

  long id ;
  
  //pour rétablir un paquet manquant : si on a power 1, c'est à nous de rétablir le paquet.
  int power ;
  String pathOnDisk;
  ArrayList<Machine> otherHosts ;
  Machine owner ;
  
  Paquet(long Id, int p , Machine proprio) {
    id = Id ;
    power = p ;
    owner = proprio ;
  }
  
  public void putPower(int p){
    power = p ;
  }
  
  public void putOtherHosts(ArrayList<Machine> liste){
    int n = liste.size() ;
    ArrayList<Machine> l = new ArrayList<Machine>(5) ;
    for (int j=0; j< n; j++){
      l.add(liste.get(j)) ;
    }
    otherHosts = l ;
  }
  

  public static LinkedList<ArrayList<Paquet>> fileToPaquets(String path){  //TODO
    //doit decouper un fichier en liste de groupes de (4+1) paquets
    // doit initialiser les champs : id, power et proprio
    return null ;
  }
  
  

  public FileInputStream bufferLecture(){
	  try {

		return new FileInputStream(pathOnDisk);
	} catch (Exception e) {
		System.out.println("Problème dans la lecture du fichier.");

	}
	  return null;
  }
  
public FileOutputStream bufferEcriture(){

	  try {

		  File file = new File(pathOnDisk);
		  if(file.exists())
			  file.delete();
		  file.createNewFile();
		return new FileOutputStream(file);
		
		
	} catch (Exception e) {
		System.out.println("Problème dans l'écriture du fichier.");

		
	}
	  return null;
  }
  
}
