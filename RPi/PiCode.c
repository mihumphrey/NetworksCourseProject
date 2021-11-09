#include <pigpio.h>
#include <stdio.h>
#include <stdbool.h>
#include <stdlib.h>
#include <string.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <time.h>

#define NUM_SENSORS 2
#define SENSOR_1 22
#define SENSOR_2 23
#define WET 0
#define DRY 1
#define DELAY 1

#define MESSAGE_LEN 21

#define PORT 8888

#define SEC_TO_US(sec) ((sec)*1000000)
#define NS_TO_US(ns)    ((ns)/1000)



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

void getInput(uint8_t sensors[NUM_SENSORS], bool levels[NUM_SENSORS]);
void checkLevels(bool levels[NUM_SENSORS], int server, uint64_t *seq);
int connectToServer();
void pingServer(int server, uint64_t *seq);

uint64_t seqnum = 0;

int main() {
    int server = connectToServer();
    uint8_t sensors[NUM_SENSORS] = {SENSOR_1, SENSOR_2};
    bool levels[NUM_SENSORS] = {WET}; 
    uint64_t seq = 0;
    gpioInitialise();
    while (1) {
        getInput(sensors, levels);
        checkLevels(levels, server, &seq);
        sleep(DELAY);
    }
    close(server);
    return 0;
}

void getInput(uint8_t sensors[NUM_SENSORS], bool levels[NUM_SENSORS]) {
    for (uint8_t i = 0; i < NUM_SENSORS; i++) {
        if (gpioRead(sensors[i]) == DRY) {
            levels[i] = DRY;
            printf("SENSOR %d IS DRY\n", sensors[i]);
        } else {
            levels[i] = WET;
        }
    }
}

void checkLevels(bool levels[NUM_SENSORS], int server, uint64_t *seq) {
    bool allDry = true;
    for (uint8_t i = 0; i < NUM_SENSORS; i++) {
        if (levels[i] == WET) {
            allDry = false;
            break;
        }
    }
    if (allDry) {
        printf("all dry\n");
        pingServer(server, seq);
    }
}

int connectToServer() {
    int sockfd;
    struct sockaddr_in servaddr;
   
    sockfd = socket(AF_INET, SOCK_STREAM, 0);
    ASSERT(sockfd != -1, "client failed to create socket");
    printf("Socket successfully created..\n");
    bzero(&servaddr, sizeof(servaddr));
   
    servaddr.sin_family = AF_INET;
    servaddr.sin_addr.s_addr = inet_addr("138.67.190.221");
    servaddr.sin_port = htons(PORT);
   
    ASSERT(connect(sockfd, (struct sockaddr*)&servaddr, sizeof(servaddr)) == 0, "failed to connect to server");
    printf("connected to the server..\n");

    return sockfd;
}

void pingServer(int server, uint64_t *seq) {
    Packet *p = malloc(sizeof(Packet));
    memcpy(p->message, "PLANT NEEDS WATERING", MESSAGE_LEN);

    struct timespec ts;
    timespec_get(&ts, TIME_UTC);
    uint64_t us = SEC_TO_US((uint64_t)ts.tv_sec) + NS_TO_US((uint64_t)ts.tv_nsec);

    p->time = us;
    p->seqnum = seqnum;
    seqnum++;
    char buff[sizeof(Packet)];

    printf("SENDING PACKET WITH SEQNUM %ld AND TIME %ld\n", p->seqnum, p->time);

    memcpy(buff, p->message, MESSAGE_LEN);
    memcpy(buff + MESSAGE_LEN, &p->time, sizeof(uint64_t));
    memcpy(buff + MESSAGE_LEN + sizeof(uint64_t), &p->seqnum, sizeof(uint64_t));
    
    strcpy(buff, "PLANT NEEDS WATERING");
    write(server, buff, sizeof(buff));
    printf("WROTE: %s TO CLIENT\n", buff);
}
