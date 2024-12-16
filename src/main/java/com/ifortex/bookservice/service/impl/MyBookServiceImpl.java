package com.ifortex.bookservice.service.impl;

import com.ifortex.bookservice.dto.SearchCriteria;
import com.ifortex.bookservice.model.Book;
import com.ifortex.bookservice.service.BookService;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Primary
public class MyBookServiceImpl implements BookService {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Map<String, Long> getBooks() {
        String sql = "SELECT unnest(b.genres) AS genre, COUNT(b) FROM Book b GROUP BY unnest(b.genres) ORDER BY COUNT(b) DESC";
        Query query = entityManager.createQuery(sql);
        List<Object[]> results = query.getResultList();

        Map<String, Long> genreCountMap = new LinkedHashMap<>();

        for (Object[] result : results) {
            String genre = (String) result[0];
            Long count = (Long) result[1];
            genreCountMap.put(genre, count);
        }

        return genreCountMap;
    }

    @Override
    public List<Book> getAllByCriteria(SearchCriteria searchCriteria) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Book> cq = cb.createQuery(Book.class);
        Root<Book> book = cq.from(Book.class);

        List<Predicate> predicates = new ArrayList<>();

        if (StringUtils.isNotBlank(searchCriteria.getTitle())) {
            predicates.add(cb.like(book.get("title"), "%" + searchCriteria.getTitle() + "%"));
        }
        if (StringUtils.isNotBlank(searchCriteria.getAuthor())) {
            predicates.add(cb.like(book.get("author"), "%" + searchCriteria.getAuthor() + "%"));
        }
        if (StringUtils.isNotBlank(searchCriteria.getGenre())) {
            predicates.add(cb.equal(book.get("genre"), searchCriteria.getGenre()));
        }
        if (StringUtils.isNotBlank(searchCriteria.getDescription())) {
            predicates.add(cb.like(book.get("description"), "%" + searchCriteria.getDescription() + "%"));
        }
        if (searchCriteria.getYear() != null) {
            Expression<Integer> yearExpression = cb.function("DATE_PART", Integer.class, cb.literal("year"), book.get("publicationDate"));
            predicates.add(cb.equal(yearExpression, searchCriteria.getYear()));
        }

        if (predicates.isEmpty()) {
            return entityManager.createQuery(cq).getResultList();
        }

        cq.where(predicates.toArray(new Predicate[0]));
        cq.orderBy(cb.asc(book.get("publicationDate")));

        TypedQuery<Book> query = entityManager.createQuery(cq);
        return query.getResultList();
    }
}

