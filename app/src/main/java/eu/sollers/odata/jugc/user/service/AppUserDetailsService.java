package eu.sollers.odata.jugc.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import eu.sollers.odata.jugc.user.repository.UserRepository;

@Service
public class AppUserDetailsService implements UserDetailsService {
    @Autowired
    UserRepository repo;

    @Override
    public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException {
        return repo.findByUsername(name).extractAuthenticatedUser();
    }
}
