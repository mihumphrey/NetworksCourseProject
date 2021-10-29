PIFILES := $(wildcard RPi/*.c)
PIHFILES := $(wildcard RPi/*.h)

SERVERFILES := $(wildcard Server/*.c)
SERVERHFILES := $(wildcard Server/*.h)

all: pi server

pi: $(PIFILES) $(PIHFILES) 
		gcc -Wall -lpthread -lpigpio -Ofast -o pi $(PIFILES)

server: $(SERVERFILES) $(SERVERHFILES)
		gcc -Wall -Ofast -o server $(SERVERFILES)

zip: 
		make clean >/dev/null
		zip -r project.zip Server RPi Android README

clean:
		rm -rfv server pi *.zip

.PHONY: all system pi server zip clean
