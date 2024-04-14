#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <time.h>
#include <signal.h>
#include <arpa/inet.h>

#include <pthread.h>

#include "my_message_buf.h"

#include "sw_eth.h"
#include "sw_interface.h"

#include "sw_node.h"

#define MY_PROTO_ETHTYPE 0xfffe

static bool m_receiving_available = true;
static bool m_send_available = true;

static uint32_t m_recv_count = 0;

static int msg_count = 0;
static int enqueued_num = 0;
static int queue_curr = 0;

#define QUEUE_LENGTH 16
static struct my_message_buf* queue[16];
static pthread_mutex_t queue_mutex;

static pthread_t send_pthread_t = -1;

static void* send_thread() {
    int ret;
    while(true) {
        if(!m_send_available || enqueued_num == 0) {
            sched_yield();
            continue;
        }

        pthread_mutex_lock(&queue_mutex);
        struct my_message_buf* to_be_sent = queue[queue_curr];
        ret = ii_send(to_be_sent, MY_PROTO_ETHTYPE);
        if(ret > 0) {
            message_buf_free(to_be_sent);
            queue[queue_curr] = NULL;
            m_send_available = false;
            queue_curr = (queue_curr+1) % QUEUE_LENGTH;
            enqueued_num--;
            printf("Message has been transmitted. Wait ACK\n");
        }
        pthread_mutex_unlock(&queue_mutex);
    }
}

bool enqueue(struct my_message_buf* buf) {
    if(enqueued_num > 16) {
        printf("Try again later\n");
        return false;
    }

    pthread_mutex_lock(&queue_mutex);
    int index = (queue_curr + enqueued_num) % QUEUE_LENGTH;
    queue[index] = buf;
    enqueued_num++;
    pthread_mutex_unlock(&queue_mutex);

    return true;
}

static timer_t ack_go_timer_id;
void schedule_ack_go_handler(int sig, siginfo_t* si, void* uc) {
    m_receiving_available = true;
    m_recv_count = 0;

    printf("Change the STOP state to GO.\n");
    send_ack_go();
}

void create_ack_go_timer() {
    int ret;
    struct sigevent evt;
    struct sigaction sa;
    int signo = SIGRTMIN;

    sa.sa_flags = SA_SIGINFO;
    sa.sa_sigaction = schedule_ack_go_handler;
    sigemptyset(&sa.sa_mask);

    ret=sigaction(signo, &sa, NULL);
    if(ret < 0) {
        printf("Create ACK Go Timer Failed\n");
        return;
    }

    evt.sigev_notify = SIGEV_SIGNAL;
    evt.sigev_signo = signo;
    evt.sigev_value.sival_ptr = NULL;
    timer_create(CLOCK_REALTIME, &evt, &ack_go_timer_id);
}

// Schedule to send ACK : GO after "time_in_ms" milliseconds
void schedule_ack_go(int time_in_ms) {
    struct itimerspec ts;
    memset(&ts, 0, sizeof(struct itimerspec));

    ts.it_value.tv_sec = time_in_ms / 1000;
    ts.it_value.tv_nsec = (time_in_ms % 1000) * 1000000;

    int ret = timer_settime(ack_go_timer_id, 0, &ts, NULL);
    if(ret == -1) {
        printf("Fail to timer_settimer()\n");
        return;
    }
}

// Send Assignment #2 Data
// Parameters (data : string to be sent, size : the length of string including '\0')
void send_data(uint8_t* data, uint32_t size) {
    int ret;

    struct my_message_buf* buf = message_buf_create(1500);
    int total_size = sizeof(struct my_data) + size;

    struct my_data* md = (struct my_data*)malloc(total_size);
    memset(md, 0, total_size);

    /* Fill the fields of Assignment #2 Data */
    md->m_header.m_type = HEADER_TYPE_DATA;
    md->m_length = htonl(size);
    memcpy(md->m_data, data, size);

    message_buf_push(buf, total_size);
    memcpy(buf->m_ptr, md, total_size);

    free(md);

    enqueue(buf);
}

// Send Assignment #2 ACK
// Parameters (ack_type : ACK_TYPE_GO or ACK_TYPE_STOP)
void send_ack(uint8_t ack_type) {
    struct my_message_buf* buf = message_buf_create(1500);
    int total_size = sizeof(struct my_ack);

    struct my_ack* ma = (struct my_ack*)malloc(total_size);
    memset(ma, 0, total_size);

    /* Fill the fields of Assignment #2 ACK */
    ma->m_header.m_type = HEADER_TYPE_ACK;
    ma->m_ack_type = ack_type;

    message_buf_push(buf, total_size);
    memcpy(buf->m_ptr, ma, total_size);

    ii_send(buf, MY_PROTO_ETHTYPE);
}

void send_ack_go() {
    printf("Send ACK : GO\n");
    send_ack(ACK_TYPE_GO);
}

void send_ack_stop() {
    printf("Send ACK : STOP\n");
    send_ack(ACK_TYPE_STOP);
}

// Process the received Assignment #2 message (Data or ACK)
void process_my_sw_proto(struct my_message_buf* buf) {
    // m_receiving_available (true) - I'm able to receive data
    // m_receiving_available (true -> false) - I should notify this fact to the sender
    // m_receiving_available (false) - I'm not able to receive data

    // m_send_available (true) - I'm able to send data (I received ACK-GO before)
    // m_send_available (false) - I'm not able to send data - (I sent a data, but I have not received ACK)
    //                                                      - (I received ACK-STOP before)
    uint8_t* pdu = buf->m_ptr;
    struct my_header* mh = (struct my_header*)pdu;

    if(mh->m_type == HEADER_TYPE_DATA) {
        struct my_data* md = (struct my_data*)pdu;

        if(m_receiving_available) {
            printf("DATA has been received, Length : %d, %s\n", ntohl(md->m_length), md->m_data);
            m_recv_count++;
            if(m_recv_count >= 2) { // Receiving should be stopped
                m_receiving_available = false;

                send_ack_stop();
                schedule_ack_go(2000);
            } else {
                send_ack_go();
            }
        } else {
            printf("Discard DATA\n");
        }
        
    }

    if(mh->m_type == HEADER_TYPE_ACK) {
        struct my_ack* ma = (struct my_ack*)pdu;

        if(ma->m_ack_type == ACK_TYPE_GO) {
            /* Fill the blank, change "m_send_available" to true? false?*/
            m_send_available = true;
            printf("ACK : GO has been received\n");
        }

        if(ma->m_ack_type == ACK_TYPE_STOP) {
            /* Fill the blank, change "m_send_available" to true? false?*/
            m_send_available = false;
            printf("ACK : STOP has been received\n");
        }
    }

    message_buf_free(buf);
}

int init_my_proto() {
    pthread_mutex_init(&queue_mutex, NULL);
    if(pthread_create(&send_pthread_t, NULL, &send_thread, NULL) != 0) {
        printf("Init : Send Thread Error\n");
        return -1;
    }

    memset(queue, 0, sizeof(struct my_message_buf*)*QUEUE_LENGTH);

    register_ethtype(MY_PROTO_ETHTYPE, process_my_sw_proto);
    create_ack_go_timer();
}

void exit_my_proto() {
    pthread_cancel(send_pthread_t);

    for(int i = 0; i < QUEUE_LENGTH; i++) {
        if(queue[i] != NULL) {
            message_buf_free(queue[i]);
        }
    }
}


void send_msg() {
    static int msg_cnt = 0;
    uint8_t msg_buf[128];

    sprintf(msg_buf, "Hello, %d", msg_cnt++);

    send_data(msg_buf, strlen(msg_buf)+1);
}