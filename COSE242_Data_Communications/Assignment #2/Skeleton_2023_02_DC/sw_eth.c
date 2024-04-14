#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <arpa/inet.h>

#include "sw_eth.h"

#define ENTRY_NUM 16
static struct ethtype_entry ethtype_entries[ENTRY_NUM];

void register_ethtype(uint16_t ethtype, void (*func)(struct my_message_buf*)) {
    int index = ethtype % ENTRY_NUM;
    ethtype_entries[index].ethtype = ethtype;
    ethtype_entries[index].func = func;
}

struct ethtype_entry* find_ethtype(uint16_t ethtype) {
    int index = ethtype % ENTRY_NUM;
    if(ethtype_entries[index].ethtype == ethtype) {
        return &ethtype_entries[index];
    }

    return NULL;
}

int add_eth_header(struct my_message_buf* buf, uint8_t* dst_mac, uint8_t* src_mac, uint16_t eth_type) {
    struct eth_header eh;

    if(!message_buf_push(buf, sizeof(struct eth_header))) {
        return -1;
    }

    memcpy(eh.dst_mac, dst_mac, 6);
    memcpy(eh.src_mac, src_mac, 6);
    eh.eth_type = htons(eth_type);

    memcpy(buf->m_ptr, &eh, sizeof(struct eth_header));

    return 0;
    
}

int process_eth(struct my_message_buf* buf) {
    struct eth_header* eth_h = (struct eth_header*)buf->m_ptr;
    message_buf_pull(buf, sizeof(struct eth_header));

    uint16_t ethtype = ntohs(eth_h->eth_type);
    struct ethtype_entry* ee = find_ethtype(ethtype);

    if(ee != NULL && ee->func != NULL) {
        ee->func(buf);
    }

    return 0;
}