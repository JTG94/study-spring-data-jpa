package study.datajpa.entity;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback(false)
class MemberTest {

    @PersistenceContext
    EntityManager em;

    //팀이랑 멤버랑 연관관계 잘 맺어서 잘 저장되는지 테스트
    @Test
    public void testEntity() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1",10,teamA);
        Member member2 = new Member("member2",20,teamA);
        Member member3 = new Member("member3",30,teamB);
        Member member4 = new Member("member4",40,teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
        //persist하면 바로 디비에 쿼리날리는것이아니라 JPA영속성컨텍스트
        //다 모아놓고 flush하눈 순간 강제로 쿼리를 다 디비로 날림
        em.flush();
        //디비에 쿼리 다날리고 JPA 영속성 컨텍스트의 캐시를 다 날려버림
        em.clear();

        //확인 jpql작성 (assert로 검증해도 되지만 눈으로 보고싶어서)
        List<Member> members = em.createQuery("select m from Member m", Member.class).getResultList();

        for (Member member : members) {
            System.out.println("member = " + member);
            System.out.println("-> member.team = " + member.getTeam());

        }


    }


}