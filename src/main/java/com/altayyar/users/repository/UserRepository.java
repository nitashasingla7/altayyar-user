package com.altayyar.users.repository;

import org.springframework.data.repository.Repository;

import com.altayyar.users.model.User;

/**
 * Spring Data JPA repository for {@link User}. It extends spring's {@link Repository} and provides default
 * implementation.
 * 
 * @author nitasha
 */

public interface UserRepository extends Repository<User,String>
{

    /**
     * Saves the given user entity.
     *
     * @param user entity to save
     * @return saved entity
     */
    User save(User user);

    /**
     * Finds the user entity by given userId
     * 
     * @param userId
     * @return user entity
     */
    User findByUserId(String userId);

}
