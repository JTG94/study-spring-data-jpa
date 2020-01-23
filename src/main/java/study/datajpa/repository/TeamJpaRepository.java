package study.datajpa.repository;

import org.springframework.stereotype.Repository;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
//어노테이션은 두가지를 한다 1. 컴포넌트 스캔 2. JPA예외를 스프링에서 공통적으로 처리할수있는 예외로 변환하는 기능까지 포함
@Repository
public class TeamJpaRepository {

    @PersistenceContext //JPA의 EntityManager 인젝션해주는 어노테이션
    private EntityManager em;

    //저장
    public Team save(Team team) {
        em.persist(team);
        return team;
    }

    //삭제
    public void delete(Team team) {
        em.remove(team);
    }

    //전체 조회
    public List<Team> findAll() {
        return em.createQuery("select t from Team t", Team.class).getResultList();
    }

    //단건 조회
   public Optional<Team> findById(Long id) {
        Team team = em.find(Team.class, id);
        return Optional.ofNullable(team);
   }

   public long count() {
        return em.createQuery("select count(t) from Team t", Long.class)
                .getSingleResult();
   }

    public Team find(Long id) {
        return em.find(Team.class, id);
    }
}
