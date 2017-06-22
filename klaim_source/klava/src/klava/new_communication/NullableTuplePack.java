/*
 * Created on 5May,2017
 */
package klava.new_communication;

public class NullableTuplePack 
{
    private TuplePack packet;

    public NullableTuplePack()
    {
        packet = null;
    }
    
    public NullableTuplePack(TuplePack packet)
    {
        this.packet = packet;
    }
    
    public TuplePack getPacket() {
        return packet;
    }
    public void setPacket(TuplePack packet) {
        this.packet = packet;
    }
}
