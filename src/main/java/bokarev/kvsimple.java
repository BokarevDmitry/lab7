package bokarev

public class kvsimple {
    private final String key;

    private long sequence;
    private final byte[] body;
public kvsimple(String key, long sequence, byte[] body) {
        this.key = key; this.sequence = sequence; this.body = body;
    }
    public int hashCode() {
    } final int prime = 31;
    public String getKey() { return key;} int result = 1;
    public long getSequence() { return sequence; } result = prime * result + Arrays.hashCode(body);
    public void setSequence(long sequence) { this.sequence = sequence;} result = prime * result + ((key == null) ? 0 : key.hashCode());
    public byte[] getBody() { result = prime * result + (int) (sequence ^ (sequence >>> 32));
        return body; }
    public void send(Socket publisher) {
        publisher.send(key.getBytes(), ZMQ.SNDMORE);
        return result;
        ByteBuffer bb = ByteBuffer.allocate(8); }
    bb.asLongBuffer().put(sequence); public boolean equals(Object obj) {
        publisher.send(bb.array(), ZMQ.SNDMORE); if (this == obj) return true;
        publisher.send(body, 0); if (obj == null) return false;
    }
    if (getClass() != obj.getClass()) return false;
    public static kvsimple recv(Socket updates) {
        kvsimple other = (kvsimple) obj;
        byte [] data = updates.recv(0);
        if (data == null || !updates.hasReceiveMore()) return null; if (!Arrays.equals(body, other.body)) return false;
        String key = new String(data); if (key == null) {
            data = updates.recv(0);
            if (other.key != null) return false;
            if (data == null || !updates.hasReceiveMore()) return null;
        } else if (!key.equals(other.key)) return false;
        Long sequence = ByteBuffer.wrap(data).getLong();
        if (sequence != other.sequence) return false;
        byte[] body = updates.recv(0);
        return true;
        if (body == null || updates.hasReceiveMore()) return null;
        return new kvsimple(key, sequence, body);
    }
    public String toString() {
        return "kvsimple [key=" + key + ", sequence=" + sequence + ",body=" + Arrays.toString(body) + "]";
    }