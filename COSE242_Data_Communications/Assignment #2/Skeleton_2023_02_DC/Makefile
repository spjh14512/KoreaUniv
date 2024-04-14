SOURCES= \
		main.c \
		sw_node.c \
		sw_interface.c \
		sw_eth.c \
		my_message_buf.c \

# This file may be used as an input for make(3)
# Remove the lines below to convert it into a pure .am file
TARGET = DC_Assignment2
OBJS=${SOURCES:.c=.o}

all: $(TARGET)

$(TARGET): ${OBJS}
	$(CC) $(CFLAGS) -o $(TARGET) ${OBJS} -lpthread -lrt

.SUFFIXES:
.SUFFIXES: .c .o

.c.o:
	$(CC) $(CFLAGS) -o $@ -c $<

clean:
	rm -f $(TARGET)
	rm -f $(OBJS)
