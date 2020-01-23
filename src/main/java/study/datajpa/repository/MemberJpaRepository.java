package study.datajpa.repository;

import org.springframework.stereotype.Repository;
import study.datajpa.entity.Member;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@Repository
public class MemberJpaRepository {

    //스프링 부트 컨테이너가 JPA에 있는 영속성컨텍스트 EntityManager를 가져옴
    //얘를 넣어주면 JPA가 알아서 디비에 Entity에 맞는 인서트 조회를 함
    @PersistenceContext
    private EntityManager em;

    //저장
    public Member save(Member member) {
        em.persist(member);
        return member;
    }

    //public void update(Member member)
    // 왜 업데이트 메서드는 없냐 ->
    // JPA는 기본적으로 Entity를 변경을 할때 변경감지 기능으로 데이터를 바꾼다.
    // 그래서 update가 별도로 필요없다.


    //삭제
    public void delete(Member member) {
        em.remove(member);
    }

    //전체 조회나 where조건 검색은 jpa가 제공하는 JPQL을 사용해야한다.
    //JPQL은 테이블 대상이 아닌 객체를 대상으로하는 쿼리
    // from의 Member는 Entity이다. 뒤에오는 Member.class는 반환타입을 적어주는 것
    //SQL로 번역되어 데이터베이스에서 값을 가져오는 것
    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class).getResultList();
    }

    //옵셔널로 조회
    public Optional<Member> findById(Long id) {
        Member member = em.find(Member.class, id);
        //member가 널일수도있고 아닐수도있으니 이걸 옵션으로 감싸서 밖에다 제공하는 것
        return Optional.ofNullable(member);
    }

    //count 쿼리
    public long count() {
        //count가 숫자 Long타입으로 나온다/ 단건인 경우에 getSingleResult();
        return em.createQuery("select count(m) from Member m", Long.class).getSingleResult();
    }

    //단건 조회
    public Member find(Long id) {
        return em.find(Member.class, id);
    }

    //순수 JPA 특정나이보다 많은 멤버 조회 JPQL 작성해야됨
    public List<Member> findByUsernameAndAgeGreaterThen(String username, int age) {
        return em.createQuery("select m from Member m where m.username = :username and m.age > :age")
                .setParameter("username", username)
                .setParameter("age", age)
                .getResultList();
    }

    public List<Member> findByUsername(String username) {
        return em.createNamedQuery("Member.findByUsername", Member.class)
                .setParameter("username", username)
                .getResultList();

    }

    /**
     * 검색 조건: 나이가 10살
     * 정렬 조건: 이름으로 내림차순
     * 페이징 조건: 첫 번째 페이지, 페이지당 보여줄 데이터는 3건
     * 순수 JPA 버전
     */
    public List<Member> findByPage(int age, int offset, int limit) {
        return em.createQuery("select m from Member m where m.age = :age order by m.username desc")
                .setParameter("age", age)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    //페이징 쿼리를 작성할때 현재 몇번쨰 페이지인지 표현해야하므로 totalCount를 가져옴
    public long totalCount(int age) {
        return  em.createQuery("select count(m) from Member m where m.age = :age", Long.class)
                .setParameter("age", age)
                .getSingleResult();
    }

    //회원의 나이를 전체 수정 (벌크 수정) / 더티체킹같은경우는 한건에 의해서만 해주므로
    public int bulkAgePlus(int age) {
        return em.createQuery("update Member m set m.age = m.age + 1 where m.age >= :age")
                .setParameter("age", age)
                .executeUpdate();

    }

}
