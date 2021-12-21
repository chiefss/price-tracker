package org.devel.pricetracker.application.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

public class AbstractDao<T, I> extends SimpleJpaRepository<T, I> {

    @Getter
    @Autowired
    private JPAQueryFactory query;

    @Getter
    private final EntityManager entityManager;

    public AbstractDao(Class<T> entityClass, EntityManager entityManager) {
        super(JpaEntityInformationSupport.getEntityInformation(entityClass, entityManager), entityManager);
        this.entityManager = entityManager;
    }
}
