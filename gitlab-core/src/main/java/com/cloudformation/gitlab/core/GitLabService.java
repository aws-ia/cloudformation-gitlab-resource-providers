package com.cloudformation.gitlab.core;


import java.util.List;
import java.util.Optional;

public interface GitLabService<T, TP> {

    /**
     * Returns the concrete object that was created or throws an exception
     * @param data
     * @return
     */
    T create(TP data);

    /**
     * Returns the concrete object that was updated or throws an exception
     * @param data
     * @return
     */
    void update(TP data);

    /**
     * Returns the list of all items of a certain type
     * @return
     */
    List<T> list();

    /**
     * This is the only one requiring {@code Optional} because this is searching for something that might not be there
     * @param id
     * @return
     */
    Optional<T> getById(Integer id);

    /**
     * Verifies that the connection can be established by querying the current user
     * @return
     */
    boolean verifyConnection();

    /**
     * Deletes the object and returns, throws an exception containing the reason why no deletion happened
     * @param id
     */
    void deleteById(Integer id);

}
