package com.ifortex.bookservice.service.impl;

import com.ifortex.bookservice.model.Book;
import com.ifortex.bookservice.model.Member;
import com.ifortex.bookservice.service.MemberService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Primary
public class MyMemberServiceImpl implements MemberService {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Member findMember() {
        TypedQuery<Member> query = entityManager.createQuery("SELECT m FROM Member m JOIN FETCH m.borrowedBooks", Member.class);
        List<Member> members = query.getResultList();
        Member selectedMember = null;
        LocalDateTime oldestPublicationDate = LocalDateTime.MAX;

        for (Member member : members) {
            Optional<Book> oldestRomanceBook = member.getBorrowedBooks().stream()
                    .filter(book -> book.getGenres().contains("Romance"))
                    .min(Comparator.comparing(Book::getPublicationDate));

            if (oldestRomanceBook.isPresent() &&
                    (oldestRomanceBook.get().getPublicationDate().isBefore(oldestPublicationDate) ||
                            (oldestRomanceBook.get().getPublicationDate().isEqual(oldestPublicationDate) &&
                                    (selectedMember == null || member.getMembershipDate().isAfter(selectedMember.getMembershipDate()))))) {

                selectedMember = member;
                oldestPublicationDate = oldestRomanceBook.get().getPublicationDate();
            }
        }
        return selectedMember;
    }

    @Override
    public List<Member> findMembers() {
        TypedQuery<Member> query = entityManager.createQuery("SELECT m FROM Member m" +
                " WHERE m.borrowedBooks IS EMPTY OR SIZE(m.borrowedBooks) = 0" +
                " AND YEAR(m.membershipDate) = 2023", Member.class);

        return query.getResultList();
    }
}
