package org.example;

public class Request{
    String type;
    String path;
    String protocol;
    Object object = new Object();
    public Request(String type, String path, String protocol){
        this.type = type;
        this.path =path;
        this.protocol = protocol;
    }
    @Override
    public boolean equals(Object obj){
        Request rec = (Request) obj;
        //System.out.println(rec.type);
        //System.out.println(this.type);
        //System.out.println(rec.path);
        //System.out.println(this.path);
        if(rec.type.equals(this.type)&& rec.path.equals(this.path)&& rec.protocol.equals(this.protocol)){
            return true;
        }return false;
    }
    @Override
    public String toString(){
        return "type"+type+" path"+path+" protocol"+protocol;
    }
    @Override
    public int hashCode(){
        int hash = this.type.hashCode()+this.path.hashCode()+this.protocol.hashCode();
        return hash;
    }
}
