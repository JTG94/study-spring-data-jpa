package study.datajpa.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback(false)
class MemberRepositoryTest {

    @Autowired MemberRepository memberRepository;
    @Autowired TeamRepository teamRepository;
    @PersistenceContext
    private EntityManager em;

    @Test
    public void testMember() {
        //System.out.println("memberRepository = " + memberRepository.getClass());

        Member member = new Member("memberA");
        Member savedMember = memberRepository.save(member);

        //옵셔널로 가져온다 -> 있을수도있고 없을수도있어서 java 8에서 제공하는 Optional로 반환
        Member findMember = memberRepository.findById(savedMember.getId()).get();
        //이렇게 쓰면 좋은방법은 아닌데 일단 쓰자 원래는 값이 있을떄 없을때 나눠서 적어줄것

        //core.api 사용해야 assertThat 이 있음
        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        assertThat(findMember).isEqualTo(member);

        //p6spy build.gradle에서 추가해준 것은 운영환경에서는 고려해봐야한다. 많이는 아니지만 성능을 깎아먹음
    }


    @Test
    public void basicCRUD() {
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberRepository.save(member1);
        memberRepository.save(member2);

        Member findMember1 = memberRepository.findById(member1.getId()).get();
        Member findMember2 = memberRepository.findById(member2.getId()).get();

        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);


        //리스트 조회 검증 findAll
        List<Member> all = memberRepository.findAll();
        assertThat(all.size()).isEqualTo(2);

        //카운트 검증
        long count = memberRepository.count();
        assertThat(count).isEqualTo(2);



        //삭제 검증
        memberRepository.delete(member1);
        memberRepository.delete(member2);

        long deletedCount = memberRepository.count();
        assertThat(deletedCount).isEqualTo(0);
    }

    @Test
    public void findByUsernameAndAgeGreaterThen() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("AAA", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        //Username은 이퀄, And는 and조건 Age GreaterThan은 >를 의미함 구현체를 구현하지않아도 됨
        List<Member> result = memberRepository.findByUsernameAndAgeGreaterThan("AAA", 15);
        assertThat(result.get(0).getUsername()).isEqualTo("AAA");
        assertThat(result.get(0).getAge()).isEqualTo(20);
        assertThat(result.size()).isEqualTo(1);


    }

    @Test
    public void testNamedQuery() {
        Member m1 = new Member("AAA",10);
        Member m2 = new Member("BBB", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findByUsername("AAA");
        Member findMember = result.get(0);
        assertThat(findMember).isEqualTo(m1);
    }

    //인터페이스 리포지토리에 직접 JPQL작성하여 하는 방법 테스트
    @Test
    public void testQuery() {
        Member m1 = new Member("AAA",10);
        Member m2 = new Member("BBB", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findUser("AAA", 10);
        assertThat(result.get(0)).isEqualTo(m1);

    }

    @Test
    public void findUsernameList() {
        Member m1 = new Member("AAA",10);
        Member m2 = new Member("BBB", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<String> usernameList = memberRepository.findUsernameList();
        for (String s : usernameList) {
            System.out.println("s = " + s);
        }

    }

    //DTO 조회 테스트
    @Test
    public void findMemberDto() {
        Team team = new Team("teamA");
        teamRepository.save(team);

        Member m1 = new Member("AAA",10);
        m1.setTeam(team);
        memberRepository.save(m1);

        List<MemberDto> memberDto = memberRepository.findMemberDto();
        for (MemberDto dto : memberDto) {
            System.out.println("dto = " + dto); //출력되는이유는 @Data에 Tostring 이런게 다있어서 가능
        }
    }


    @Test
    public void findByNames() {
        Member m1 = new Member("AAA",10);
        Member m2 = new Member("BBB", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findByNames(Arrays.asList("AAA", "BBB"));
        for (Member member : result) {
            System.out.println("member = " + member);
        }


    }

    //반환타입 테스트
    @Test
    public void returnType() {
        Member m1 = new Member("AAA",10);
        Member m2 = new Member("BBB", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        //List<Member> aaa = memberRepository.findListByUsername("AAA");
        //Member findMember = memberRepository.findMemberByUsername("AAA");
        //Optional<Member> aaa = memberRepository.findOptionalByUsername("AAA");

        /**
         * 주의 컬렉션을 반환할때는 Null이 아니라 빈컬렉션을 반환하므로 주의
         * 그러므로 리스트는 그냥 받으면 됨
         * 단건조회에서는 문제 발생 단건은 NUll을 반환
         *
         * ****null로 넘어오는 이유 : 순수 JPA는 하나를 조회했을때 없으면 NoResultException을 터뜨리는데
         * Spring Data Jpa는 없는데 왜 Exception을 터뜨려 그냥 null 반환하면되지 Exception발생하면
         * 개발자가 try catch해야해서 불편 -> 자기가 try catch를 감싸가지고 null을 반환해버림
         * ***그냥 Optional을 사용하는 것이 좋다.
         * Optional은 당연히 없을수있다는 가정으로 클라이언트가 그 코드에 대해서 책임을 져야하니
         * 클라이언트에 처리가 넘어가는거
         * ***디비에 조회를 하는데 있을수도있고 없을수도있으면 Optional사용
         * ***한건 조회인데 결과가 두개이상일경우 : 예외가 터짐 IncorrectResultSizeDataAccessException
         */
        //List<Member> result = memberRepository.findListByUsername("asdfadsfa");
        Optional<Member> findMember = memberRepository.findOptionalByUsername("afoijaodfj");
        System.out.println("findMember = " + findMember);



    }

    //Spring Data Jpa 페이징 테스트
    @Test
    public void paging() {
        //given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));

        //page 1 offset =0 , limit = 10, page2 -> offset=10, limit 10 요렇게 계산해줘야함 원래는
        int age = 10;
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));



        //when
        Page<Member> page = memberRepository.findByAge(age, pageRequest);
        //long totalCount = memberRepository.totalCount(age);
                //토탈카운트를 가져오는 코드를 작성안해도 됨
                //-> 반환타입이 Page면 알아서 토탈카운트 쿼리까지 같이 날림

        /**
         * 위의 page를 그대로 api 로 반환하면 절대안됨 (엔티티 자체를 외부노출하면 만약 엔티티가바뀌면
         * api 스펙이 다 바뀌기때문에 장애발생 --> 쉽게 DTO로 변환하는 방법
         */
        Page<MemberDto> toMap = page.map(m -> new MemberDto(m.getId(), m.getUsername(), null));


        //then
        List<Member> content = page.getContent(); //실제 데이터 보고싶으면 getContent()
       //long totalElements = page.getTotalElements();

        assertThat(content.size()).isEqualTo(3);
        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getNumber()).isEqualTo(0);  // 페이지 번호가져올 수 있음
        assertThat(page.getTotalPages()).isEqualTo(2); //토탈 페이지
        assertThat(page.isFirst()).isTrue();
        assertThat(page.hasNext()).isTrue(); //다음 페이지가 있냐
    }


    /**
     * Spring Data Jpa를 활용하여 벌크 수정하는 기능 테스트
     */
    @Test
    public void bulkUpdate() {
        //given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 19));
        memberRepository.save(new Member("member3", 20));
        memberRepository.save(new Member("member4", 21));
        memberRepository.save(new Member("member5", 40));

        //when
        int resultCount = memberRepository.bulkAgePlus(20);
    
        List<Member> result = memberRepository.findByUsername("member5");
        Member member5 = result.get(0);
        System.out.println("member5 = " + member5);
        //then
        assertThat(resultCount).isEqualTo(3);
    }


    /**
     * 엔티티 그래프 및 지연로딩 (fetch join)
     */
    @Test
    public void findMemberLazy() {
        //given
        //member1 -> teamA
        //member2 -> teamB

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        teamRepository.save(teamA);
        teamRepository.save(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamB);
        memberRepository.save(member1);
        memberRepository.save(member2);

        em.flush();
        em.clear();
        //영속성 컨텍스트 날림

        //when N + 1 문제 1이 처음날린쿼리 N은 그 쿼리기반의 결과 갯수
        //네트워크를 N + 1 만큼 타기때문에 성능이 빠를수가 없다.
        //fetch join으로 해결
        //여기서 객체그래프라는 표현을 쓰는데 연관관계가 있는것을 한번에 join해서 select로 다 가져옴
        //이때는 Team에는 진짜 Entity 객체와 값들이 세팅되어 있따. getClass로 확인해보면 Entity.Team
        //지연로딩이기 때문에 멤버만디비에서 긁어온다.
        List<Member> members = memberRepository.findEntityGraphByUsername("member1");

        for (Member member : members) {
            System.out.println("member.getUsername() = " + member.getUsername());
            //getClass() null를 세팅할수없으니 프록시 기술을 이용하여 가짜 객체를 생성하는중
            System.out.println("member.getTeam().getClass() = " + member.getTeam().getClass());
            //getName 등의 진짜 값을 원할경우에 그제서야 쿼리를 날려 값을 가져옴
            System.out.println("member.getTeam().getName() = " + member.getTeam().getName());


        }

    }

    @Test
    public void specBaisc() throws Exception {
        //given
        Team teamA = new Team("teamA");
        em.persist(teamA);

        Member m1 = new Member("m1", 0, teamA);
        Member m2 = new Member("m2", 0, teamA);
        em.persist(m1);
        em.persist(m2);

        em.flush();
        em.clear();
        //when
        Specification<Member> spec = MemberSpec.username("m1").and(MemberSpec.teamName("teamA"));
        List<Member> result = memberRepository.findAll(spec);


        //then
        assertThat(result.size()).isEqualTo(1);

    }

    //이너조인만 가능하고 아우터 조인은 안된다.
    @Test
    public void queryByExample() throws Exception {
        //given
        Team teamA = new Team("teamA");
        em.persist(teamA);

        Member m1 = new Member("m1", 0, teamA);
        Member m2 = new Member("m2", 0, teamA);
        em.persist(m1);
        em.persist(m2);

        em.flush();
        em.clear();

        //when
        //Probe
        Member member = new Member("m1");
        Team team = new Team("teamA");
        member.setTeam(team);

        ExampleMatcher matcher = ExampleMatcher.matching().withIgnorePaths("age");

        Example<Member> example = Example.of(member,matcher);

        List<Member> result = memberRepository.findAll(example);

        assertThat(result.get(0).getUsername()).isEqualTo("m1");

        //then

    }

    //한계 : 조인이 들어가는순간 root Entity는 최적화되지만 두번째는 모든 필드를 가져옴
    // 프로젝션 대상이 root엔티티이면 유용하다.

    @Test
    public void projections() throws Exception {
        //given
        Team teamA = new Team("teamA");
        em.persist(teamA);

        Member m1 = new Member("m1", 0, teamA);
        Member m2 = new Member("m2", 0, teamA);
        em.persist(m1);
        em.persist(m2);

        em.flush();
        em.clear();

        //when
        List<NestedClosedProjections> result = memberRepository.findProjectionsByUsername("m1",NestedClosedProjections.class);

        for (NestedClosedProjections nestedClosedProjections : result) {
            String username = nestedClosedProjections.getUsername();
            System.out.println("username = " + username);
            String teamName = nestedClosedProjections.getTeam().getName();
            System.out.println("teamName = " + teamName);

        }
        //then

    }

    @Test
    public void nativeQuery() throws Exception {
        //given
        Team teamA = new Team("teamA");
        em.persist(teamA);

        Member m1 = new Member("m1", 0, teamA);
        Member m2 = new Member("m2", 0, teamA);
        em.persist(m1);
        em.persist(m2);

        em.flush();
        em.clear();

        //when
        Page<MemberProjection> result = memberRepository.findByNativeProjection(PageRequest.of(0,10));
        List<MemberProjection> content = result.getContent();

        for (MemberProjection memberProjection : content) {
            System.out.println("memberProjection.getUsername() = " + memberProjection.getUsername());
            System.out.println("memberProjection.getTeamName() = " + memberProjection.getTeamName());
        }

        //then

    }






}