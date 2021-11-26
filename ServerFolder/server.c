/*
	C socket server example
*/

#include <stdio.h>
#include <string.h>	//strlen
#include <sys/socket.h>
#include <arpa/inet.h>	//inet_addr
#include <unistd.h>	//write
#include <stdlib.h>

#define ASSERT(arg, err) \
			if (!(arg)) {\
				fprintf(stderr,"\033[31mError, %s: \n\t", err);\
				perror("");\
				fprintf(stderr, "exiting...\n");\
				printf("\033[0m");\
				exit(1);\
			}

int main(int argc , char *argv[])
{
	printf("Starting\n");
	int socket_desc , client_sock , c , read_size;
	struct sockaddr_in server , client;
	char client_message[256];
	
	printf("Creating Socket\n");
	//Create socket
	socket_desc = socket(AF_INET , SOCK_STREAM , 0);
	ASSERT(socket_desc != -1, "could not create socket")
	printf("Socket Created\n");
	
	printf("Socket Prep\n");
	//Prepare the sockaddr_in structure
	server.sin_family = AF_INET;
	server.sin_addr.s_addr = INADDR_ANY;
	server.sin_port = htons( 8888 );
	printf("Socket Prep Done\n");

	printf("Binding\n");	
	//Bind
	ASSERT(bind(socket_desc,(struct sockaddr *)&server , sizeof(server)) >= 0, "could not bind socket")
	printf("Bound\n");

	//Listen
	ASSERT(listen(socket_desc , 3) == 0, "Could Not Listen");
	printf("Listening\n");
	
	//Accept and incoming connection
	c = sizeof(client);
	
	//accept connection from an incoming client
	client_sock = accept(socket_desc, (struct sockaddr *)&client, (socklen_t*)&c);
	ASSERT(client_sock >= 0, "failed to accept connection")	
	//Receive a message from client
	while ((read_size = read(client_sock , client_message , 256)) > 0 ){
		//Send the message back to client
		printf("FROM CLIENT: %s\n", client_message);
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
