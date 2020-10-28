from socket import *
import cv2
import pickle
import struct
import threading

class Canal(threading.Thread):
    def __init__(self,address,port,file_name,divisiones):
        threading.Thread.__init__(self)
        self.multicast_addr = (address,port)
        self.sock = socket(AF_INET, SOCK_DGRAM)
        self.ttl = struct.pack('b', 1)
        self.sock.setsockopt(IPPROTO_IP, IP_MULTICAST_TTL, self.ttl)
        self.video = file_name
        self.div = divisiones

    def run(self):
        cap = cv2.VideoCapture(self.video)
        if (cap.isOpened()== False):
            print("Error opening video stream or file")
        while(cap.isOpened()):
            ret, frame = cap.read()
            f = len(frame)//self.div
            if ret:
                for i in range(self.div):
                    frameaux = frame[i*f:(i+1)*f]
                    frameaux = pickle.dumps(frameaux)
                    self.sock.sendto(frameaux,self.multicast_addr)
            else:
                break
        cap.release()
        self.sock.close()

opciones = ['canal1.avi','canal2.avi','canal3.avi']
for i in range(len(opciones)):
    t = Canal('224.0.0.1',3000+i,opciones[i],20)
    t.start()
