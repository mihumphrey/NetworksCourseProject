
#include "server.h"

#define PRINT(...) printf(__VA_ARGS__)
#define DEBUG(...) printf(__VA_ARGS__)
//#define DEBUG(...)


void push(Android **head, Android *new) {
	DEBUG("IN PUSH\n");
    new->next = *head;
    (*head) = new;
	DEBUG("AFTER PUSH\n");
}

void broadcast(Android *head, Packet *p) {
	char buff[sizeof(Packet)];
	DEBUG("Writing to device(s)\n");	
	memcpy(buff, p->message, MESSAGE_LEN);
    memcpy(buff + MESSAGE_LEN, &p->time, sizeof(uint64_t));
    memcpy(buff + MESSAGE_LEN + sizeof(uint64_t), &p->seqnum, sizeof(uint64_t));
	memcpy(buff + MESSAGE_LEN + sizeof(uint64_t) + sizeof(uint64_t), &p->plantnum, sizeof(uint64_t));
	while (head != NULL) { 
		DEBUG("IN BROADCAST LOOP\n");
		sleep(1);
		if (head->data != -1) {
			DEBUG("\t Writing to %d\n", head->data);
			ASSERT(write(head->data, buff, sizeof(buff)) != -1, "invalid write");
		}
		head = head->next;
	}
	DEBUG("Wrote to device(s)\n");
}

void *handleAndroid(void *args) {
	DEBUG("in handle android\n");
	int client_fd = *((int *)args);

	// push this connection onto list of all connections
	Android *new = malloc(sizeof(Android));
	new->data = client_fd;
	new->next = NULL;
	push(&head, new);

	while (1) {
		// handle android schtuff here

		// currently empty infinite loop, meaning keep connection alive, 
		// but do not directly interact with individual server as it is created
	}

	return NULL;
}

void *handlePi(void *args) {
	DEBUG("in handle pi\n");
	int client_fd = *((int *)args);
	int read_size;
	char buff[sizeof(Packet)];
	//Receive a message from RPi
	while ((read_size = read(client_fd , buff , sizeof(Packet))) > 0 ) {
		Packet *p = malloc(sizeof(Packet));
		memcpy(p->message, buff, MESSAGE_LEN);
		memcpy(&p->time, buff + MESSAGE_LEN, sizeof(uint64_t));
		memcpy(&p->seqnum, buff + MESSAGE_LEN + sizeof(uint64_t), sizeof(uint64_t));
		memcpy(buff + MESSAGE_LEN + sizeof(uint64_t) + sizeof(uint64_t), &p->plantnum, sizeof(uint64_t));
		broadcast(head, p);
		DEBUG("FROM CLIENT: %s\n", p->message);
		free(p);
	}
	return NULL;
}

int main(int argc , char *argv[]) {
	int socket_desc , client_sock , c;
	struct sockaddr_in server , client;

	head = malloc(sizeof(Android));
	head->data = -1;
	head ->next = NULL;


	//delete this stupid if shit ... just put it in here to minimize the socket creation in IDE.
	if (1) {
		PRINT("Creating Socket\n");
		//Create socket
		socket_desc = socket(AF_INET , SOCK_STREAM , 0);
		ASSERT(socket_desc != -1, "could not create socket")
		PRINT("Socket Created\n");
		
		PRINT("Socket Prep\n");
		//Prepare the sockaddr_in structure
		server.sin_family = AF_INET;
		server.sin_addr.s_addr = INADDR_ANY;
		server.sin_port = htons(PORT);
		PRINT("Socket Prep Done\n");

		PRINT("Binding\n");	
		//Bind
		ASSERT(bind(socket_desc,(struct sockaddr *)&server , sizeof(server)) >= 0, "could not bind socket")
		PRINT("Bound\n");

		//Listen
		ASSERT(listen(socket_desc , 3) == 0, "Could Not Listen");
		PRINT("Listening\n");
		
		//Accept and incoming connection
		c = sizeof(client);
	}
	pthread_t pids[51];
	int thread_num = 0;
	char initialBuff[1];
	while (1) {
		DEBUG("In while loop\n");
		client_sock = accept(socket_desc, (struct sockaddr *)&client, (socklen_t*)&c);
		DEBUG("after accept\n");
		ASSERT(client_sock >= 0, "failed to accept connection")
		bzero(initialBuff, sizeof(initialBuff));
		ASSERT(read(client_sock, initialBuff, sizeof(initialBuff)) != -1, "INVALID READ"); 
		DEBUG("INITIAL: %s\n", initialBuff);
		if (strncmp(initialBuff, "0", 1) == 0) {
			pthread_create(&pids[thread_num++], NULL, handlePi, (void *)&client_sock);
		} else {	
			pthread_create(&pids[thread_num++], NULL, handleAndroid, (void *)&client_sock);
		}
	}

	while (head != NULL) {
		Android *tmp = head->next;
		free(head);
		head = tmp;
	}
	return 0;
}
