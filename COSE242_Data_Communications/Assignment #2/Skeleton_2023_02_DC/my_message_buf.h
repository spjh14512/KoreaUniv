#ifndef __MY_MESSAGE_BUF_H__
#define __MY_MESSAGE_BUF_H__

#include <stdint.h>
#include <stddef.h>
#include <stdbool.h>

struct my_message_buf {
    uint8_t* m_buf;
    uint8_t* m_buf_end;
    uint32_t m_allocated_size;
    uint8_t* m_ptr;
};

struct my_message_buf* message_buf_create(uint32_t allocation_size);
void message_buf_free(struct my_message_buf* mbuf);

bool message_buf_can_push(struct my_message_buf* mbuf, uint32_t size);
bool message_buf_push(struct my_message_buf* mbuf, uint32_t size);

bool message_buf_can_pull(struct my_message_buf* mbuf, uint32_t size);
bool message_buf_pull(struct my_message_buf* mbuf, uint32_t size);

int message_buf_get_data_length(struct my_message_buf* mbuf);
#endif