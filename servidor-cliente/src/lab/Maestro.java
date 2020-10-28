/**
 * 
 */
package lab;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.bouncycastle.util.encoders.Hex;

import data.Packet;
import data.PacketHandler;

/**
 * @author user
 *
 */
public class Maestro {

	private static ServerSocket ss;	
	private static final String MAESTRO = "MAESTRO: ";
	private static final int numeroThreads = 25;
	private static int cont=1;
	static Object o=new Object();
	static Object p=new Object();
	public static String log = "";
	public static String ruta = "./data/log_";
	private static ThreadedUDPServer server;
	public static String send="";
	public static long diff = 0;
	/**
	 * @param args
	 */
	public static void main(String[] args)throws Exception {
		// TODO Auto-generated method stub

		server= new ThreadedUDPServer(1338);
		Scanner in = new Scanner(System.in);
		System.out.println("�A cuantos clientes desea enviar el archivo?");
		int clientes= in.nextInt();
		String archivo;
		System.out.println("Elija un archivo para enviar (1 o 2):");
		System.out.println("1.Archivo de texto 250MB");
		System.out.println("2.Archivo de texto 100MB");
		archivo = in.nextLine();
		
		String tim = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss").format(Calendar.getInstance().getTime());
		ruta+= tim + ".txt";
		
		server.receive(new PacketHandler() {

			@Override
			public void process(Packet packet) {
				String data = new String(packet.getData()).trim();
				Date t1 = Calendar.getInstance().getTime();
				String time = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss").format(Calendar.getInstance().getTime());
				log+=time+"\n";

				if(data.equals("Preparado")) {
					ThreadedUDPServer.CLIENTS.add(packet.getConnection());
					server.send(new Packet("OK".getBytes(), packet.getAddr(), packet.getPort()));
					System.out.println("Recibiendo: ");
					System.out.println(new String(packet.getData()).trim());
					data = new String(packet.getData()).trim();


					if(server.CLIENTS.size()==clientes) {

						try {

							if(archivo.equals("1")) {

								send="./data/Archivo1.txt";
								log+="Archivo Enviado: archivo1 \r\n";
							}else {
								send="./data/Archivo2.txt";
								log+="Archivo Enviado: archivo2 \r\n";
							}
							File sendFile = new File(send); 
							byte[] bytesArray = new byte[(int) sendFile.length()]; 
							MessageDigest digest;
							try {
								digest = MessageDigest.getInstance("SHA-256");
								byte[] hash = digest.digest(bytesArray);
								String sha256hex = new String(Hex.encode(hash));
								log+="Hash:"+sha256hex;
							} catch (NoSuchAlgorithmException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}


							FileInputStream fis = new FileInputStream(sendFile);
							BufferedInputStream bis = new BufferedInputStream(fis);
							bis.read(bytesArray,0,bytesArray.length);
							System.out.println(bytesArray.length);

							for(int i=0;i<bytesArray.length;i+=1024) {
								byte[] temp=Arrays.copyOfRange(bytesArray, i, i+1024);

								server.broadcast(temp);
								log+="Paquete: "+i;
							}

						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				if(data.equals("Recibido")) {
					server.send(new Packet("Fin".getBytes(), packet.getAddr(), packet.getPort()));
					System.out.println(new String(packet.getData()).trim());
					log+="Estado: "+data;
					data = new String(packet.getData()).trim();
					log+="Estado: "+data;
				}
				Date t2 = Calendar.getInstance().getTime();
				 diff = t2.getTime()-t1.getTime();
			}
			
		});

		
		log(diff);
	}

	public static void reply(Packet packet) {
		server.broadcast(new String(packet.toString()).getBytes());
	}
	public static void dormir() {
		try {
			synchronized(o) {
				synchronized(p) {

					p.notify();
				}
				cont++;
				o.wait();
			}


		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void log(long diff) {
		try {
			System.out.println(log);
			log+="Tiempo de env�o: "+(diff / (1000 * 60 * 60 * 24))+"\r\n";
			log+="Archivo recibido exitosamente \r\n";
			FileWriter fw1 = new FileWriter(new File(ruta),true);
			fw1.write(log + "\r");
			fw1.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
