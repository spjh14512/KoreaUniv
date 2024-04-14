#ifndef __SW_INTERFACE_H__
#define __SW_INTERFACE_H__

#include <stdint.h>
#include <stddef.h>
#include <stdbool.h>

#include "my_message_buf.h"

struct interface_info {
    uint8_t m_mac[6];

    int m_sock;
    void (*recv_func)(struct my_message_buf*);
};

void init_interface_info(uint8_t* dev_name);
void exit_interface_info();

int ii_send(struct my_message_buf* buf, uint16_t ethtype);

#endif
