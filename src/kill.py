#!/bin/python

import os
import time
nombreDeServeur = 15
i = 0

while(i<20) :
    
    os.system("fuser -fk " + str(5000+4*i+1)+"/udp &")
    os.system("fuser -fk " + str(5000+4*i+2)+"/udp &")
    os.system("fuser -fk " + str(5000+4*i)+"/tcp &")
    i=i+1