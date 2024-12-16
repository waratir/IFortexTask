package com.ifortex.bookservice.service.impl;

import com.ifortex.bookservice.dto.SearchCriteria;
import com.ifortex.bookservice.model.Book;
import com.ifortex.bookservice.model.Member;
import com.ifortex.bookservice.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class AppStartupRunner implements CommandLineRunner {

    @Autowired
    private MyMemberServiceImpl memberService;

    @Override
    public void run(String... args) throws Exception {

        List<Member> member = memberService.findMembers();
        for (Member memb: member){
            System.out.println( memb.getName());

        }
    }
}
