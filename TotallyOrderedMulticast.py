from __future__ import division
import sys
import socket
import random
import threading

# Message Format = MsgId,Value
# Msg 1 - Send logical clock to others if this is TD
# Msg 2 - Respond to Msg 1 i.e. subtract received value with your value and send back to TD
# Msg 3 - Send new logical clocks to others if this is TD after finding (average)

client_port_list = [7000, 5000, 6000]
logical_clock = float(random.randint(2,20))
print "This system logical clock counter is : " ,logical_clock

def listener(name,host,port):
	try:
		global logical_clock
		s = socket.socket()
		s.bind((host,port))
		s.listen(5)
		
		while True:
			conn, address = s.accept()			
			print "Client connected ip:<" + str(address) + ">"
			data = conn.recv(1024) # Message i.e. the (ID, value)
			print ('Message received', data)
			initialmsg = data.split(",")
			if (len(initialmsg) < 2):# if invalid message sent
				print ("Invalid Message : ", initialmsg)
				conn.close()
				return

			msg_id = int(initialmsg[0])
			msg_value = float(initialmsg[1])

			if msg_id == 1:
				msg_value = logical_clock - msg_value
				msg = '2,'+str(msg_value)
				conn.send(msg)
			elif msg_id == 3:
				logical_clock = logical_clock + msg_value
				print ("Synchronised clock is : " , logical_clock)
			else:
				print ("Invalid Message ID : ", msg_id)
			conn.close()

  	except (KeyboardInterrupt, SystemExit):
  		print("Closing app.. Bye Bye")
  		s.close()
  		sys.exit(0)

if __name__ == "__main__":
	host = "127.0.0.1"
	this_port = int(sys.argv[1])

	# Start thread for this system in DS. This is to handle messages from other nodes in DS
	try:
		if (this_port in client_port_list):
			t = threading.Thread(target = listener, args = ("YoYo", host, this_port))
			t.start()

			while True:
				# Get raw input if TD and then send Msg 1 to all
				timedaemon_flag = raw_input("Do you want this node to be time daemon (y/n) : ")
				timedaemon_flag = timedaemon_flag.lower()

				if timedaemon_flag == "y":
					# First send logical clock to all other nodes and get response (subtracted value)
					counter_total= 0
					temp_clocks = []
					#print "Time Daemon" , logical_clock
					msg = '1,'+str(logical_clock)
					for port in client_port_list:
						if this_port != port:
							s = socket.socket()
							s.connect((host,port))
							s.send(msg)
							ack = s.recv(1024)
							initialmsg = ack.split(",")
							#print (initialmsg)
							msg_id = int(initialmsg[0])
							msg_value = float(initialmsg[1])
							if msg_id == 2:
								#print "Logical clock difference : " , msg_value
								temp_clocks.append(msg_value)
								counter_total = counter_total + msg_value
							else:
								print "Invalid Response"
							s.close()

					# Calculate average value
					clock_avg = counter_total / len(client_port_list)

					# Send the new logical clock value to others
					temp_clock_index = 0
					for port in client_port_list:
						if this_port != port:
							s = socket.socket()
							s.connect((host,port))
							if temp_clocks[temp_clock_index]<0:
								temp_clocks[temp_clock_index] = abs(temp_clocks[temp_clock_index])
							elif temp_clocks[temp_clock_index]>0:
								temp_clocks[temp_clock_index] = -(temp_clocks[temp_clock_index])
							time_difference = clock_avg + temp_clocks[temp_clock_index] 
							timedaemon_avg = "3,"+str(time_difference)
							s.send(timedaemon_avg)
							temp_clock_index += 1
							s.close()
						else:
							print "Synchronised clock is : " , logical_clock + clock_avg
		else:
		 	print "Enter a port number in the range ", client_port_list
	except (KeyboardInterrupt, SystemExit):
  		print("Closing app.. Bye Bye")
  		sys.exit(0)
		