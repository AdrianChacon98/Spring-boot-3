package kafkaeventdriven.kafkaapp.enums;

import com.google.common.collect.Sets;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Set;
import java.util.stream.Collectors;

public enum Roles {
    ROLE_USER(Sets.newHashSet(Authorities.USER_READ,Authorities.USER_WRITE)),
    ROLE_ADMIN(Sets.newHashSet(Authorities.ADMIN_READ,Authorities.ADMIN_WRITE));

    private Set<Authorities> authorities;

    Roles(Set<Authorities> authorities){
        this.authorities=authorities;
    }

    public Set<Authorities> getAuthorities(){
        return authorities;
    }

    public Set<GrantedAuthority> getGrantedAuthorities(){

        Set<GrantedAuthority> grantedAuthorities = authorities.stream()
                .map(authority->new SimpleGrantedAuthority(authority.getAuthority()))
                .collect(Collectors.toSet());

        grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_"+this.name()));


        return grantedAuthorities;

    }



}
