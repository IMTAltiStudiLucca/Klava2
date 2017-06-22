/*
 * Created on 21Apr.,2017
 */
package klava.new_communication;

import java.io.Serializable;

public class IPAddress implements Serializable {
    
    /** */
    private static final long serialVersionUID = -7009506563955107854L;
    private String ip;
    private int port;
    
    public IPAddress(String ip, int port) {
        this.setIp(ip);
        this.setPort(port);
    }
    public String returnFullAddress(){
        return getIp() + ":" + String.valueOf(getPort());
    }
    
    public String getIp() {
        return ip;
    }
    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }
    public void setPort(int port) {
        this.port = port;
    }
    
    public static IPAddress parseAddress(String ipAndPort)
    {
        if(ipAndPort.indexOf(":") > 0)
        {
            String ip = ipAndPort.substring(0, ipAndPort.indexOf(":"));
            String portStr = ipAndPort.substring(ipAndPort.indexOf(":") + 1, ipAndPort.length());
            Integer port = Integer.valueOf(portStr);
            return new IPAddress(ip, port);
        }
        else 
            return null;
    }
}