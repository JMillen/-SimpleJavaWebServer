import java.net.*;
import java.io.*;
import java.util.*;
 
public class HttpServer{
    private ArrayList<HttpServerSession> sessions;

    public static void main(String args[]){
        //Sever starting message
        System.out.println("Web server starting");

        HttpServer server = new HttpServer();
        server.start_server(); 
    }

    public void start_server(){ 
        try{
            //Listen on port 51333 for incoming connections
            ServerSocket ss = new ServerSocket(51333);
            sessions = new ArrayList<HttpServerSession>();

            //Loop and accept new connections as they arrive
            while(true){
                Socket s = ss.accept();
                HttpServerSession session = new HttpServerSession(s);
                sessions.add(session);

                //Start the session thread to handle the connection
                session.start();

                System.out.println("Connection received from: " + s.getInetAddress());
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}

class HttpServerSession extends Thread{
    private Socket s;
    private BufferedOutputStream bos;
    
    public HttpServerSession(Socket s){
        this.s = s;
    }

    private boolean println(BufferedOutputStream bos, String s){
        String news = s + "\r\n";
        byte[] array = news.getBytes();
        try{
            bos.write(array, 0, array.length);
            return true;
        }catch(IOException e){
            e.printStackTrace();
            return false;
        }
    }

    //Entry point into the server session
    public void run(){
        
        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));

            bos = new BufferedOutputStream(s.getOutputStream());

            HttpServerRequest request = new HttpServerRequest();            

            String requestLine;
            //While isDone() is false loop through the header
            while(!request.isDone()){
                requestLine = reader.readLine();
                request.process(requestLine);
                System.out.println("Header: " + requestLine);
            }

            //Use the parsed information from HttpServerRequest
            String file = request.getFile();
            String host = request.getHost();
            System.out.println("File: " + file + " " + "Host: " + host);
            
            //Checks if the host or file is null when creating the file path
            String filePath;
            if (host != null && file != null) {
                //host = host.replace(":", "_"); //For Windows OS
                filePath = host + "/" + file;
            } else if (file != null) {
                filePath = "localhost:51333/" + file;
            } else{
                System.out.println("Error: file and host are null");
                filePath = null;
            }

            System.out.println("FilePath: " + filePath);

            //Create a file object to see if the file exsists
            File fileObject = new File(filePath);

            //Try/Catch loop for running the requested file
            try{
                if(!fileObject.exists()){

                    //404 message for file not found
                    println(bos, "HTTP/1.1 404 Not Found");
                    println(bos, "Content-Type: text/html");
                    println(bos, "");
        
                    //Give the user a basic 404 file not found HTML page
                    FileInputStream fileInputStream = new FileInputStream("404.html");

                    byte[] buffer = new byte[1000];
                    int bytesRead;
                    while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                        bos.write(buffer, 0, bytesRead);
                    }

                    //Close the fileInputStream
                    fileInputStream.close();
                    //Flush the output stream
                    bos.flush();
                    //Close socket
                    s.close();
                }else{
                FileInputStream fileInputStream = new FileInputStream(filePath);

                //Checks for the requested file type
                //Then sends the file content type in the response
                String contentType;
                if(file.endsWith(".html")){
                    contentType = "text/html";
                }else if(file.endsWith(".jpeg")){
                    contentType = "image/jpg";
                }else{
                    contentType = "text/plain";
                }

                //Print HTTP header response
                println(bos, "HTTP/1.1 200 OK");
                //Print the content type
                println(bos, "Content-Type: " + contentType);
                //Send empty line to show end of header
                println(bos, "");

                //Read and send file content
                byte[] buffer = new byte[1000];
                int bytesRead;
                while((bytesRead = fileInputStream.read(buffer)) != -1){
                    bos.write(buffer, 0, bytesRead);
                    //Thread.sleep(1000);
                }

                //Close the fileInputStream
                fileInputStream.close();
                //Flush the output stream
                bos.flush();
                //Close socket
                s.close();
            }
            }catch(FileNotFoundException e){
                //404 message for file not found
                println(bos, "HTTP/1.1 404 Not Found");
                println(bos, "");
                e.printStackTrace();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}