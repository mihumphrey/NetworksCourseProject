/*
	C socket server example
*/

#include <stdio.h>
#include <string.h>	//strlen
#include <sys/socket.h>
#include <arpa/inet.h>	//inet_addr
#include <unistd.h>	//write
#include <stdlib.h>
#include <time.h>

#define MESSAGE_LEN 21
#define SEC_TO_US(sec) ((sec)*1000000)
#define NS_TO_US(ns)    ((ns)/1000)


uint64_t expected = 0;
uint16_t numLost = 0;
uint64_t latency = 0;
uint64_t getAverageLatency(uint64_t current, uint64_t numPackets, uint64_t new);

#define ASSERT(arg, err) \
			if (!(arg)) {\
				fprintf(stderr,"\033[31mError, %s: \n\t", err);\
				perror("");\
				fprintf(stderr, "exiting...\n");\
				printf("\033[0m");\
				exit(1);\
			}

typedef struct __Packet__ {
    char message[MESSAGE_LEN];
    uint64_t time;
    uint64_t seqnum; 
} Packet;

int main(int argc , char *argv[]) {
	int socket_desc , client_sock , c , read_size;
	struct sockaddr_in server , client;
	char buff[sizeof(Packet)];
	
	//Create socket
	socket_desc = socket(AF_INET , SOCK_STREAM , 0);
	ASSERT(socket_desc != -1, "could not create socket")
	
	//Prepare the sockaddr_in structure
	server.sin_family = AF_INET;
	server.sin_addr.s_addr = INADDR_ANY;
	server.sin_port = htons( 8888 );
	
	//Bind
	ASSERT(bind(socket_desc,(struct sockaddr *)&server , sizeof(server)) >= 0, "could not bind socket")
	
	//Listen
	listen(socket_desc , 3);
	
	//Accept and incoming connection
	c = sizeof(struct sockaddr_in);
	
	//accept connection from an incoming client
	client_sock = accept(socket_desc, (struct sockaddr *)&client, (socklen_t*)&c);
	ASSERT(client_sock >= 0, "failed to accept connection")	
	//Receive a message from client
	while ((read_size = read(client_sock , buff , sizeof(Packet))) > 0 ){
		//Send the message back to client
		Packet *p = malloc(sizeof(Packet));
		memcpy(p->message, buff, MESSAGE_LEN);
		memcpy(&p->time, buff + MESSAGE_LEN, sizeof(uint64_t));
		memcpy(&p->seqnum, buff + MESSAGE_LEN + sizeof(uint64_t), sizeof(uint64_t));
		printf("FROM CLIENT: %s\n", p->message);
		printf("SEQNUM: %ld\n", p->seqnum);
		struct timespec ts;
		timespec_get(&ts, TIME_UTC);
		uint64_t us = SEC_TO_US((uint64_t)ts.tv_sec) + NS_TO_US((uint64_t)ts.tv_nsec);
		if (expected != p->seqnum) {
			printf("PACKET LOSS ON PACKET: %ld\n", expected);
			numLost++;
		}

		uint64_t newLatency = (us - p->time);
		printf("LATENCY: %ld us\n", newLatency);
		latency = getAverageLatency(latency, expected, newLatency);
		expected++;
	}
	
	if (read_size == 0) {
		puts("Client disconnected");
		fflush(stdout);
		printf("Average latency: %ld us\n", latency);
		printf("Packet loss: %d of %ld --> %f%%\n", numLost, expected, (float)numLost/expected);
	}
	else if (read_size == -1) {
		perror("recv failed");
	}
	
	return 0;
}

uint64_t getAverageLatency(uint64_t current, uint64_t numPackets, uint64_t new) {
	current *= numPackets;
	current += new;
	current /= numPackets + 1;

	return current;
}