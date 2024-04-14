#include <stdio.h>
#include <stdlib.h>
#include <signal.h>
#include <unistd.h>

#include "sw_interface.h"
#include "sw_node.h"

void sigint(int signum) {
    exit_my_proto();
    exit_interface_info();

    exit(0);
}

int main(int argc, char* args[]) {
    init_interface_info("enp0s3");
    init_my_proto();

    sleep(5);
    signal(SIGINT, sigint);

    for(int i = 0; i < 16; i++) {
        send_msg();
    }

    while(1);

    return 0;
}