#ifndef __SW_ETH_H__
#define __SW_ETH_H__

#include <stdint.h>
#include <stddef.h>
#include <stdbool.h>

#include "my_message_buf.h"

#pragma pack(push, 1)
struct eth_header {
    uint8_t dst_mac[6];
    uint8_t src_mac[6];
    uint16_t eth_type;
};
#pragma pack(pop)

struct ethtype_entry {
    uint16_t ethtype;
    void (*func)(struct my_message_buf*);
};

void register_ethtype(uint16_t ethtype, void (*func)(struct my_message_buf*));
struct ethtype_entry* find_ethtype(uint16_t ethtype);

int add_eth_header(struct my_message_buf* buf, uint8_t* dst_mac, uint8_t* src_mac, uint16_t eth_type);
int process_eth(struct my_message_buf* buf);

#endif