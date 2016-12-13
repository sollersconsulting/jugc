package eu.sollers.odata.jugc.user.bootstrap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import eu.sollers.odata.jugc.user.entity.User;
import eu.sollers.odata.jugc.user.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class Initializer implements ApplicationListener<ContextRefreshedEvent> {
    @Autowired
    private UserRepository repo;

    @Autowired
    PasswordEncoder enc;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        log.info("Adding Test Users");

        repo.save(User.builder().username("admin").mail("mail@ma.il").password(enc.encode("secret")).build());
        repo.save(User.builder().username("tester").mail("test@tester.pl").password(enc.encode("secret")).build());
        repo.save(User.builder().username("cowboy").password(enc.encode("secret")).build());
        repo.save(User.builder().username("jug").mail("jugc@jug.c").password(enc.encode("secret")).build());
    }
}