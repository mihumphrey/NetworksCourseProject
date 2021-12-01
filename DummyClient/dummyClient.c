#include <stdio.h>
#include <stdbool.h>
#include <stdlib.h>
#include <string.h>
#include <arpa/inet.h>
#include <unistd.h>

#define NUM_SENSORS 2
#define SENSOR_1 22
#define SENSOR_2 23
#define WET 0
#define DRY 1
#define DELAY 1

#define PORT 8888

#define MESSAGE_LEN 21


typedef struct __Packet__ {
    char message[MESSAGE_LEN];
    uint64_t time;
    uint64_t seqnum; 
} Packet;

#define ASSERT(arg, err) \
			if (!(arg)) {\
				fprintf(stderr,"\033[31mError, %s: \n\t", err);\
				perror("");\
				fprintf(stderr, "exiting...\n");\
				printf("\033[0m");\
				exit(1);\
			}

void getInput(uint8_t sensors[NUM_SENSORS], bool levels[NUM_SENSORS]);
void checkLevels(bool levels[NUM_SENSORS], int server);
int connectToServer();
void pingServer(int server);

int main() {
    int server = connectToServer();
    write(server, "0", 1);
    
    bool yes = 0;
    Packet *p;
    sleep(1);
    while (1) {
        if (!yes)
            pingServer(server);
        if (yes) {
            char buff[sizeof(Packet)];
            read(server, buff, sizeof(buff));
            p = malloc(sizeof(Packet));
            memcpy(p->message, buff, MESSAGE_LEN);
            memcpy(&p->time, buff + MESSAGE_LEN, sizeof(uint64_t));
            memcpy(&p->seqnum, buff + MESSAGE_LEN + sizeof(uint64_t), sizeof(uint64_t));
        }
        if (yes) {
            printf("Received packet from server: \n");
            printf("\tMESSAGE: %s\n", p->message);
            printf("\tTIME: %lu\n", p->time);
            printf("\tMESSAGE: %lu\n", p->seqnum);
        }
        sleep(10);
    }
    return 0;
}

int connectToServer() {
    int sockfd;
    struct sockaddr_in servaddr;
   
    sockfd = socket(AF_INET, SOCK_STREAM, 0);
    ASSERT(sockfd != -1, "client failed to create socket");
    printf("Socket successfully created..\n");
    bzero(&servaddr, sizeof(servaddr));
   
   	printf("Server Address Setup\n"); 

    servaddr.sin_family = AF_INET;
    servaddr.sin_addr.s_addr = inet_addr("127.0.0.1");
    servaddr.sin_port = htons(PORT);
    printf("%d\n", ntohs(servaddr.sin_port));
   
    ASSERT(connect(sockfd, (struct sockaddr*)&servaddr, sizeof(servaddr)) == 0, "failed to connect to server\n");
    printf("connected to the server..\n");

    return sockfd;
}

void pingServer(int server) {
    Packet *p = malloc(sizeof(Packet));
	memcpy(p->message, "PLANT NEEDS WATERING", MESSAGE_LEN);
	p->seqnum = 240;
	p->time = 25;
	char buff[sizeof(Packet)];
	printf("Writing to device\n");	
	memcpy(buff, p->message, MESSAGE_LEN);
    memcpy(buff + MESSAGE_LEN, &p->time, sizeof(uint64_t));
    memcpy(buff + MESSAGE_LEN + sizeof(uint64_t), &p->seqnum, sizeof(uint64_t));
    write(server, buff, sizeof(buff));
    printf("Wrote to device\n");
    //printf("WROTE: %s TO CLIENT\n", buff);
}
