#ifndef SERVER_H
#define SERVER_H


#include <stdio.h>
#include <string.h>	//strlen
#include <sys/socket.h>
#include <arpa/inet.h>	//inet_addr
#include <unistd.h>	//write
#include <stdlib.h>
#include <time.h>
#include <pthread.h>

#define MESSAGE_LEN 21
#define SEC_TO_US(sec) ((sec)*1000000)
#define NS_TO_US(ns)    ((ns)/1000)

#define PORT 8888

#define ASSERT(arg, err) \
			if (!(arg)) {\
				fprintf(stderr,"\033[31mError, %s: \n\t", err);\
				perror("");\
				fprintf(stderr, "exiting...\n");\
				printf("\033[0m");\
				exit(1);\
			}

typedef struct __ANDROID__ {
	int data;
	struct __ANDROID__ *next;
} Android;

typedef struct __Packet__ {
    char message[MESSAGE_LEN];
    uint64_t time;
    uint64_t seqnum; 
} Packet;
Android *head;


void broadcast(Android *head, Packet *p);
void *handleAndroid(void *args);
void push(Android **head, Android *new);
void *handlePi(void *args);



#endif