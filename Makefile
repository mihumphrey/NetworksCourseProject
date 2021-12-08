PIFILES := $(wildcard RPi/*.c)
PIHFILES := $(wildcard RPi/*.h)

DUMMYFILES := $(wildcard DummyClient/*.c)
DUMDUMMYHFILES := $(wildcard DummyClient/*.h)

SERVERFILES := $(wildcard ServerFolder/*.c)
SERVERHFILES := $(wildcard ServerFolder/*.h)

all: pi server

pi: $(PIFILES) $(PIHFILES) 
		gcc -lpthread -lpigpio -o pi $(PIFILES)

dummy: $(DUMMYFILES) $(DUMMYHFILES) 
		gcc -lpthread -o dummy $(DUMMYFILES)		

server: $(SERVERFILES) $(SERVERHFILES)
		gcc -Wall -Ofast -o server $(SERVERFILES) -lpthread

zip: 
		make clean >/dev/null
		zip -r project.zip Server RPi Android README

clean:
		rm -rfv server pi *.zip

.PHONY: all system pi server zip clean
