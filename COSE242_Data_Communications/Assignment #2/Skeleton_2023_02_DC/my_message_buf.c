#include <stdlib.h>
#include <string.h>

#include "my_message_buf.h"

struct my_message_buf* message_buf_create(uint32_t allocation_size) {
    struct my_message_buf* new_buf = (struct my_message_buf*)malloc(sizeof(struct my_message_buf));
    memset(new_buf, 0, sizeof(struct my_message_buf));

    new_buf->m_buf = malloc(allocation_size);
    new_buf->m_buf_end = new_buf->m_buf + allocation_size;
    new_buf->m_allocated_size = allocation_size;
    new_buf->m_ptr = new_buf->m_buf + new_buf->m_allocated_size;

    return new_buf;
}

void message_buf_free(struct my_message_buf* mbuf) {
    if(mbuf->m_buf != NULL) {
        free(mbuf->m_buf);
    }

    free(mbuf);
}

bool message_buf_can_push(struct my_message_buf* mbuf, uint32_t size) {
    if((mbuf->m_ptr - size) < mbuf->m_buf) {
        return false;
    }

    return true;
}

bool message_buf_push(struct my_message_buf* mbuf, uint32_t size) {
    if(!message_buf_can_push(mbuf, size)) {
        return false;
    }

    mbuf->m_ptr -= size;

    return true;
}

bool message_buf_can_pull(struct my_message_buf* mbuf, uint32_t size) {
    if((mbuf->m_ptr + size) > mbuf->m_buf_end) {
        return false;
    }

    return true;
}

bool message_buf_pull(struct my_message_buf* mbuf, uint32_t size) {
    if(!message_buf_can_pull(mbuf, size)) {
        return false;
    }

    mbuf->m_ptr += size;

    return true;
}

int message_buf_get_data_length(struct my_message_buf* mbuf) {
    return (mbuf->m_buf_end - mbuf->m_ptr);
}
