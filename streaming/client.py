from socket import *
import cv2
import numpy as np
import time
import pickle
import threading
from tkinter import *

class Cliente(threading.Thread):

    def __init__(self):
        threading.Thread.__init__(self)
        self.multicast_addr = '224.0.0.1'
        self.bind_addr = '0.0.0.0'
        self.port = 3000
        self.canales = []
        self.puertos = [3000,3001,3002]
        self.inicializarSockets()
        self.sock = self.canales[0]
        self.play = True
        self.t0 = time.time()
        self.frame_idx = 0

    def inicializarSockets(self):
        indice_ports = 0
        for i in range(len(self.puertos)):
            i = socket(AF_INET, SOCK_DGRAM)
            membership = inet_aton(self.multicast_addr) + inet_aton(self.bind_addr)
            i.setsockopt(IPPROTO_IP, IP_ADD_MEMBERSHIP, membership)
            i.setsockopt(SOL_SOCKET, SO_REUSEADDR, 1)
            i.bind((self.bind_addr, self.puertos[indice_ports]))
            self.canales.append(i)
            indice_ports += 1

    def cambiarCanal1(self):
        self.port = 3000
        self.sock = self.canales[0]

    def cambiarCanal2(self):
        self.port = 3001
        self.sock = self.canales[1]

    def cambiarCanal3(self):
        self.port = 3002
        self.sock = self.canales[2]

    def pausa(self):
        self.play = not self.play
        if b3["text"] == "Pausa":
            b3.configure(bg="green", text="Seguir reproduciendo")
        else:
            b3.configure(bg="red", text="Pausa")

    #Tamaño de un frame 691200 bytes + info adicional (dimensiones del frame)
    ##tamaño del frame entre 20 = 34717
    def run(self):
        buf = 34717
        div = 20
        while True:
            img = None #img es cada frame
            b = False
            for i in range(div):
                data, server = self.sock.recvfrom(buf)
                array = pickle.loads(data)
                if b:
                    img = np.concatenate((img, array), axis=0)
                else:
                    img = array.copy()
                    b=True
            if self.play:
                cv2.imshow('frame', img)
            k = cv2.waitKey(1)
            if k & 0xFF == ord('q'):
                print("El cliente se va")
                self.sock.close()
                break
            self.frame_idx += 1

            if self.frame_idx == 30:
                t1 = time.time()
                sys.stdout.write('\r Framerate : {:.2f} frames/s.     '.format(30 / (t1 - t0)))
                sys.stdout.flush()
                t0 = t1
            self.frame_idx = 0
    cv2.destroyAllWindows()

master = Tk()
master.minsize(520, 160)
master.geometry("320x100")
y = Cliente()
b = Button(master, text="Canal 1", command=y.cambiarCanal1, height=3, width=20)
b1 = Button(master, text="Canal 2", command=y.cambiarCanal2, height=3, width=20)
b2 = Button(master, text="Canal 3", command=y.cambiarCanal3, height=3, width=20)
b3 = Button(master, text="Pausa", command=y.pausa, height=3, width=20, bg="red")
b.place(x=20, y=20)
b1.place(x=180, y=20)
b2.place(x=340, y=20)
b3.place(x=180, y=85)
y.start()
mainloop()