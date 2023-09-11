public class HttpServerRequest {

    private String file = null;
    private String host = null;
    private boolean done = false;
    private int line = 0;

    public boolean isDone() { return done; }
    public String getFile() { return file; }
    public String getHost() { return host; }

    public void process(String in)
    {

        if(line == 0){
            //Splits the get request into three parts
            String parts[] = in.split(" ");
            //Checks for the get request and correct length
            if(parts[0].trim().compareTo("GET") == 0 && parts.length >= 2){
                file = parts[1].substring(1);
                if(file.isEmpty()){
                    file = "index.html";
                }
            }
        }
        //Checks for the host header
        if(in.startsWith("Host: ")){
            host = in.substring(6);
        }

        //Checks for the end of the header
        if (in == null || in.isEmpty()) {
            done = true;
        }
        //System.out.println("Current Line: " + line + " Host: " + host + " file: " + file + " Done Status: " + done);
        line++;
    }
}
