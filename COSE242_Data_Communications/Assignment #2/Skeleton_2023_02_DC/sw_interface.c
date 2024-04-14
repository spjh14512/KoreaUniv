#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <arpa/inet.h>
#include <netinet/in.h>
#include <net/if.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <sys/ioctl.h>
#include <linux/if_packet.h>
#include <net/ethernet.h>
#include <pthread.h>
#include <unistd.h>

#include "my_message_buf.h"
#include "sw_eth.h"

#include "sw_interface.h"

static struct interface_info ii;
static pthread_t ii_recv_thread_t;

#define RECEIVING_MAX_LEN 1500
static void* ii_recv_thread(void* data) {
    uint8_t receiving[RECEIVING_MAX_LEN];
    int recv_len;

    while(true) {
        recv_len = recv(ii.m_sock, receiving, RECEIVING_MAX_LEN, 0);
        if(recv_len <= 0) {
            continue;
        }

        struct my_message_buf* mbuf = message_buf_create(recv_len);
        message_buf_push(mbuf, recv_len);
        memcpy(mbuf->m_ptr, receiving, recv_len);

        process_eth(mbuf);
    }
}

void init_interface_info(uint8_t* dev_name) {
    memset(&ii, 0, sizeof(struct interface_info));

    ii.m_sock = socket(PF_PACKET, SOCK_RAW, htons(ETH_P_ALL));
    struct sockaddr_ll sock_addr;
    memset(&sock_addr, 0, sizeof(sock_addr));

    sock_addr.sll_family = AF_PACKET;
    sock_addr.sll_ifindex = if_nametoindex(dev_name);
    sock_addr.sll_protocol = htons(ETH_P_ALL);

    struct ifreq s;

    strcpy(s.ifr_name, dev_name);
    if(ioctl(ii.m_sock, SIOCGIFHWADDR, &s) < 0) {
        printf("HW Address Error\n");
    }

    memcpy(ii.m_mac, s.ifr_addr.sa_data, 6);

    if(bind(ii.m_sock, (struct sockaddr*)&sock_addr, sizeof(sock_addr)) < 0) {
        printf("Socket Bind Error\n");
        return;
    }

    if(pthread_create(&ii_recv_thread_t, NULL, &ii_recv_thread, NULL) != 0) {
        printf("Init : II Recv Thread Error\n");
        return;
    }
}

void exit_interface_info() {
    pthread_cancel(ii_recv_thread_t);
    close(ii.m_sock);
}

int ii_send(struct my_message_buf* buf, uint16_t ethtype) {
    int ret;
    
    if(buf == NULL)
        return -1;

    uint8_t broadcast[6] = {0xff, 0xff, 0xff, 0xff, 0xff, 0xff};

    add_eth_header(buf, broadcast, ii.m_mac, ethtype);

    ret = send(ii.m_sock, buf->m_ptr, message_buf_get_data_length(buf), 0);

    return ret;
}

