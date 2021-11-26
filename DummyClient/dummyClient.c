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
    while (1) {
        pingServer(server);
        sleep(1);
    }
    /*uint8_t sensors[NUM_SENSORS] = {SENSOR_1, SENSOR_2};
    bool levels[NUM_SENSORS] = {WET};
    gpioInitialise();
    while (1) {
        getInput(sensors, levels);
        checkLevels(levels, server);
        sleep(DELAY);
    }
    close(server);*/
    return 0;
}

/*void getInput(uint8_t sensors[NUM_SENSORS], bool levels[NUM_SENSORS]) {
    for (uint8_t i = 0; i < NUM_SENSORS; i++) {
        if (gpioRead(sensors[i]) == DRY) {
            levels[i] = DRY;
            printf("SENSOR %d IS DRY\n", sensors[i]);
        } else {
            levels[i] = WET;
        }
    }
}

void checkLevels(bool levels[NUM_SENSORS], int server) {
    bool allDry = true;
    for (uint8_t i = 0; i < NUM_SENSORS; i++) {
        if (levels[i] == WET) {
            allDry = false;
            break;
        }
    }
    if (allDry) {
        printf("all dry\n");
        pingServer(server);
    }
}*/

int connectToServer() {
    int sockfd;
    struct sockaddr_in servaddr;
   
    sockfd = socket(AF_INET, SOCK_STREAM, 0);
    ASSERT(sockfd != -1, "client failed to create socket");
    printf("Socket successfully created..\n");
    bzero(&servaddr, sizeof(servaddr));
   
   	printf("Server Address Setup\n"); 

    servaddr.sin_family = AF_INET;
    servaddr.sin_addr.s_addr = inet_addr("209.97.232.75");
    servaddr.sin_port = htons(PORT);

    printf("Server Address Setup Done\n");

    printf("connecting to server\n");
   
    ASSERT(connect(sockfd, (struct sockaddr*)&servaddr, sizeof(servaddr)) == 0, "failed to connect to server\n");
    printf("connected to the server..\n");

    return sockfd;
}

void pingServer(int server) {
    char buff[64];
    strcpy(buff, "Ya Bitch\n");
    write(server, buff, sizeof(buff));
    printf("WROTE: %s TO CLIENT\n", buff);
}
