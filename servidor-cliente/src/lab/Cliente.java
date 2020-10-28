/**
 * 
 */
package lab;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * @author user
 *
 */

import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


import data.Packet;
import data.PacketHandler;
 
public class Cliente extends Thread {
	private static ThreadedUDPClient client;
	
	public Cliente (){
		
	}
	
    public static void main(String[] args) {
 
    	client = new ThreadedUDPClient("localhost", 1338);
    	client.receive(new PacketHandler() {
    		
			@Override
			public void process(Packet packet) {
				
				String data = new String(packet.getData());
				System.out.println("Recibiendo: ");
				
				System.out.println(packet);
				
				if(new String(packet.getData()).trim().equals("Fin")) {
					
					client.impr();
					
				}
				if(new String(packet.getData()).trim().equals("OK")) {
					
					System.out.println("Conexión Exitosa");
					
				}
			
			}
			
		});
		System.out.println("Estableciendo Conexión...");
		client.send("Preparado".getBytes());
		
		try {
			sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		client.send("Recibido".getBytes());
		
		 
    }
 
}
