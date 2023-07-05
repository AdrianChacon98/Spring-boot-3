package kafkaeventdriven.kafkaapp.enums;

public enum Authorities {

    USER_READ("user:read"),
    USER_WRITE("user:write"),

    ADMIN_READ("admin:read"),
    ADMIN_WRITE("admin:write");


    private final String authority;

    Authorities(String authority){
        this.authority=authority;
    }

    public String getAuthority(){
        return authority;
    }

}
