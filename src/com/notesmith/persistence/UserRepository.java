package com.notesmith.persistence;

import com.notesmith.exception.PersistenceException;
import com.notesmith.model.User;

public interface UserRepository {
    User findByUsername(String username) throws PersistenceException;
    void createUser(String username, String passwordHash) throws PersistenceException;
}
