package eu.sollers.odata.jugc.user.repository;

import org.springframework.data.repository.CrudRepository;

import eu.sollers.odata.jugc.user.entity.User;

public interface UserRepository extends CrudRepository<User, Long> {
    public User findByUsername(String name);
}
