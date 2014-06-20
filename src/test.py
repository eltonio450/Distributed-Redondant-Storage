nombreDeServeur = 4
i = 0

# d : identifiant debug
# p : port du serveur
# t : signifie qu'il est la tête de réseau
# I : première ip à contacter
# P : premier port à contacter

#Le premier serveur est une tête du serveur.
ligne = " -d " + str(0) + " -p " + str(5000) + " -t "
print(ligne)

while (i < nombreDeServeur) :
    i=i+1
    ligne = " -d " + str(i) + " -p " + str(5000 + 4*i) + " -I 127.0.0.1" + " -P 5000" 
    print(ligne)
