package com.twilio.callforwarding.repositories;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public abstract class Repository<T> {

    protected final EntityManager em;
    protected final Class<T> entityType;

    public Repository(Class<T> entity) {

        entityType = entity;

        Map<String, String> properties = getProperties();
        em = Persistence
                .createEntityManagerFactory("callforwarding", properties)
                .createEntityManager();
    }

    /**
     * Returns all instances of the type.
     *
     * @return all entities
     */
    @SuppressWarnings("unchecked")
    public Iterable<T> findAll() {
        Query query = em.createQuery(
                String.format("SELECT e FROM %s e", entityType.getSimpleName()));

        return query.getResultList();
    }

    /**
     * Returns the number of instances of the type.
     *
     * @return entities count
     */
    @SuppressWarnings("unchecked")
    public long count() {
        Query query = em.createQuery(
                String.format("SELECT count(e) FROM %s e", entityType.getSimpleName()));

        return (long) query.getSingleResult();
    }

    /**
     * Retrieves an entity by its id.
     *
     * @param id   The given id.
     *
     * @return the entity with the given id
     */
    public T find(long id) {
        T entity = em.find(entityType, id);
        em.refresh(entity);

        return entity;
    }

    /**
     * Saves a given entity. Use the returned instance for further operations.
     *
     * @param entity   The entity
     *
     * @return the saved entity
     */
    public T create(T entity) {
        getTransaction().begin();
        em.persist(entity);
        getTransaction().commit();

        return entity;
    }

    /**
     * Saves a list of entities. Does not return the saved entities
     *
     * @param batch  The list of entities
     *
     * @return the saved entity
     */
    public void bulkCreate(List<T> batch) {
        EntityTransaction transaction = em.getTransaction();
        transaction.begin();
        batch.stream().forEach(e -> em.persist(e));
        em.flush();
        transaction.commit();
    }

    /**
     * Updates a given entity. Use the returned instance for further operations.
     *
     * @param entity   The entity
     *
     * @return the updated entity
     */
    public T update(T entity) {
        getTransaction().begin();
        T updatedEntity = em.merge(entity);
        getTransaction().commit();

        return updatedEntity;
    }

    /**
     * Deletes the entity.
     *
     * @param entity   The entity
     */
    public void delete(T entity) {
        getTransaction().begin();
        em.remove(entity);
        getTransaction().commit();
    }

    private EntityTransaction getTransaction() {
        return em.getTransaction();
    }

    private Map<String, String> getProperties() {
        Map<String, String> env = System.getenv();
        Map<String, String> config = new HashMap<>();
        for (String key : env.keySet()) {
            if (key.contains("JDBC_URL")) {
                config.put("javax.persistence.jdbc.url", env.get(key));
            }

            if (key.contains("DB_USER")) {
                config.put("javax.persistence.jdbc.user", env.get(key));
            }

            if (key.contains("DB_PASSWORD")) {
                config.put("javax.persistence.jdbc.password", env.get(key));
            }
        }

        return config;
    }


}