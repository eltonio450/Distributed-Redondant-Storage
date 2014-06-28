#!/bin/python

import os
nombreDeServeur = 4
i = 0

#os.system("killall java")
# d : identifiant debug
# p : port du serveur
# t : signifie qu'il est la tête de réseau
# I : première ip à contacter
# P : premier port à contacter

#Le premier serveur est une tête du serveur.
os.system("fuser -fk " + str(5000)+"/tcp")
os.system("fuser -fk " + str(5000)+"/udp")

ligne = " -d " + str(0) + " -p " + str(5000) + " -t "
os.chdir("../bin")
os.system("java Main " + ligne +"&")
#print(ligne)
#sleep(1)

while (i < nombreDeServeur-1) :
    i=i+1
    ligne = " -d " + str(i) + " -p " + str(5000 + 4*i) + " -I 127.0.0.1" + " -P 5000" 
    os.system("fuser -fk " + str(5000+4*i)+"/udp")
    os.system("fuser -fk " + str(5000+4*i)+"/tcp")
    
    os.system("java Main " + ligne +"&")
    #os.system("pwd")
    #print(ligne)
