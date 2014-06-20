package Stockage;


import RelationsPubliques.*;
import Utilitaires.Global;
import Utilitaires.Utilitaires;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class Paquet {

  long id ;
  //pour rétablir un paquet manquant : si on a power 1, c'est à nous de rétablir le paquet.
  int power ;
  boolean enLecture;
  String pathOnDisk;
  FileChannel fichier;
  ArrayList<Machine> otherHosts ;
  Machine owner ;
  Lock isUsed = new ReentrantLock();
  
  Paquet(long Id, int p , Machine proprio) {
    id = Id ;
    power = p ;
    owner = proprio ;
    pathOnDisk=owner.toString()+"/data/"+ id;
    	try {
			fichier = FileChannel.open(FileSystems.getDefault().getPath(pathOnDisk), StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
  
  


  public void envoyerPaquet(SocketChannel s) throws IOException{
    //we assume connection has already started
    ByteBuffer buffer = Utilitaires.createBufferForPaquetInformation(id,power,owner);  //already flipped
    s.write(buffer) ;
    isUsed.lock();
    try {
      long length = fichier.size() ;
      fichier.transferTo(0, length, s) ;
    }
    finally {
      isUsed.unlock();
    }
  }
  
  public static Paquet createPaquetFromBuffer(ByteBuffer b){
    //buffer is flipped
    long id = 0;
    int power = 0 ;
    String IpAdresse = "" ;
    int port = 0 ;
    //TODO : � voir avec Utilitaires
    Machine owner = new Machine(IpAdresse,port) ;
    return new Paquet(id,power,owner) ;
  }
  
  public static Paquet recoitPaquet(SocketChannel s) throws IOException{
  //we assume connection has already started
    ByteBuffer buffer = ByteBuffer.allocateDirect(Global.BUFFER_LENGTH);
    buffer.clear() ;
    s.read(buffer) ;
    buffer.flip() ;
    Paquet p = createPaquetFromBuffer(buffer) ;
    p.isUsed.lock() ;
    try {
     fichier.transferFrom(0,Global.MAXIMUM_SIZE,s) ;
    }
    finally {
      p.isUsed.unlock() ;
    }
    return p ;
  }
	
  /*public boolean nextByteBuffer(ByteBuffer aRemplir){
	  
	  int n = aRemplir.capacity();
	  int i = 0;
	  
	  aRemplir.clear();
	  
	  byte car = 'a';
	  while(i<n && car != -1)
	  {
		  try {
			car = (byte) input.read();
		} catch (IOException e) {
			System.out.println("Cela n'arrivera jamais...");
			e.printStackTrace();
		}
		  if(car == -1)
			  return false;
		  i++;
	  }
	  aRemplir.flip();
	  return true;
  }*/
  
  
}
