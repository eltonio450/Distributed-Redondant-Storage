#!/bin/python

import os
import time
import socket
nombreDeServeur = 7
i = 0

ip = socket.gethostbyname(socket.gethostname())

print(ip)
#os.system("killall java")
# d : identifiant debug
# p : port du serveur
# t : signifie qu'il est la tete de reseau
# I : premieOkre ip a contacter
# P : premier port a contacter

#Le premier serveur est une tete du serveur.
os.system("fuser -fk " + str(5000)+"/tcp")
os.system("fuser -fk " + str(5000)+"/udp")
time.sleep(3)
ligne = " -d " + str(0) + " -p " + str(5000) + " -t "
os.chdir("../bin")
os.system("java Main " + ligne +"&\n")
#print(ligne)
time.sleep(1)

#while(i<10) :
#   i=i+1
#   os.system("fuser -fk " + str(5000+4*i+1)+"/udp")
#    os.system("fuser -fk " + str(5000+4*i+2)+"/udp")
#    os.system("fuser -fk " + str(5000+4*i)+"/tcp")
    
i=0
while (i < nombreDeServeur-1) :
    time.sleep(1)
    i=i+1
    ligne = " -d " + str(i) + " -p " + str(5000 + 4*i) + " -I "+ ip + " -P 5000" 
    #os.system("fuser -fk " + str(5000+4*i+1)+"/udp")
    #os.system("fuser -fk " + str(5000+4*i+2)+"/udp")
    #os.system("fuser -fk " + str(5000+4*i)+"/tcp")
   
    os.system("java Main " + ligne +"&\n")
    #os.system("pwd")
    #print(ligne)
    
#time.sleep(10)
#os.system("./kill.py")
