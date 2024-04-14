#ifndef __SW_NODE_H__
#define __SW_NODE_H__

#include <stdint.h>
#include <stddef.h>
#include <stdbool.h>

enum {
    HEADER_TYPE_DATA,
    HEADER_TYPE_ACK,
};

enum {
    ACK_TYPE_GO,
    ACK_TYPE_STOP,
    ACK_TYPE_NACK,
};

#pragma pack(push, 1)
struct my_header {
    uint8_t m_type;
};

struct my_data {
    struct my_header m_header;
    uint32_t m_length;
    uint8_t m_data[0];
};

struct my_ack {
    struct my_header m_header;
    uint8_t m_ack_type;
};

#pragma pack(pop)

void send_data(uint8_t* data, uint32_t size);
void send_ack(uint8_t ack_type);
void send_ack_go();
void send_ack_stop();
void process_my_sw_proto(struct my_message_buf* buf);

int init_my_proto();
void exit_my_proto();
void send_msg();

#endif