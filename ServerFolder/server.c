/*
	C socket server example
*/

#include <stdio.h>
#include <string.h>	//strlen
#include <sys/socket.h>
#include <arpa/inet.h>	//inet_addr
#include <unistd.h>	//write
#include <stdlib.h>

#define MESSAGE_LEN 21

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
    time_t time;
    uint64_t seqnum; 
} Packet;

int main(int argc , char *argv[])
{
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
	while ((read_size = read(client_sock , buff , 256)) > 0 ){
		//Send the message back to client
		Packet *p = malloc(sizeof(Packet));
		memcpy(p->message, buff, MESSAGE_LEN);
		memcpy(p->time, buff + MESSAGE_LEN, sizeof(time_t));
		memcpy(p->seqnum, buff + MESSAGE_LEN + sizeof(time_t), sizeof(uint64_t));
		printf("FROM CLIENT: %s\n", p->message);
		printf("TIME: %ld\n", p->time);
		printf("SEQNUM: %ld\n", p->seqnum);
	}
	
	if (read_size == 0) {
		puts("Client disconnected");
		fflush(stdout);
	}
	else if (read_size == -1) {
		perror("recv failed");
	}
	
	return 0;
}