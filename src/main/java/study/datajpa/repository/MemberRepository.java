package study.datajpa.repository;

//Data jpa는 항상 리포지토리를 인터페이스로 만들어야함 중요.

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;

import java.util.List;
import java.util.Optional;

//extends JpaRepository를 해줘야하고 제너릭은 타입과, 키의 타입을 넣어주면 됨
//JpaRepository를 상속받아야하는데 인터페이스끼리 상속은 extends 고로 JpaRepository는 인터페이스
//Spring Data jpa = 인터페이스만 만들어주면  구현체를 Spring Data jpa가 다 만들어서 넣어준다.
//구현체는 SimpleJpaRepository JpaRepository에서 찾아보면 된다.
public interface MemberRepository extends JpaRepository<Member, Long>, JpaSpecificationExecutor<Member> {
    //구현체가 없는데 어떻게 동작을 하나? 인터페이스를 상속받았을 뿐인데
    // 테스트에서 sout(memberRepository.getClass())로 찍어보면
    //memberRepository = class com.sun.proxy.$Proxy106 정보가 뜬다
    // -> 스프링이 인터페이스를 보고 프록시 객체 그러니까 스프링 data jpa가 구현클래스를 만들어서 꽂아버린 것
    // 구현체를 스프링데이터jpa가 만들어서 주입을 해주는 것이다.
    // 인터페이스만 개발자가 해놓으면 구현체는 스프링 data jpa가 해줌

    //상상할수 있는 모든 공통의 기능은 넣어두었지만 그외의 특화된 메서드는 어떻게 구현하냐(예를들어 이름으로 검색)
    //인터페이스인데.. 단지 하나만 구현하고 싶은건데 클래스를 만들고 MemberRepository를 Implements 하는 순간
    //모든 것을 구현해야한다.
    //List<Member> findByUsername(String username); //구현하지않아도 이게 동작한다;; 쿼리메서드 기능

    //도메인에 특화된 문제를 어떻게 해야할까 -> 쿼리메소드기능 사용 3가지방법
    //1. 메소드이름으로 쿼리를 제공하는 기능
    // 심각한 문제는 이름이 길어질수있따. -> 2개까지는 쓰는데 그 이상은 다른방법 JPQL작성하는법도 기능제공
    // 짧은 쿼리들은 이것을 사용하는게 좋다. 복잡하면 다른 방법으로.
    List<Member> findByUsernameAndAgeGreaterThan(String username, int age);

    //2. JPA NamedQuery 실무에서 쓸일 없음
    /**
     * @NamedQuery(
     *         name = "Member.findByUsername",
     *         query = "select m from Member m where m.username = :username"
     * )
     * 엔티티에다가 작성해 줄수있다. 제일 상단
     * 그후에 리포지토리 클래스에서 이름을 호출해서사용하면 됨
     */
    @Query(name = "Member.findByUsername") //이거 없이도 실행이되는데 관례가 있다 .Member.findUsername을 찾아버림
    //먼저 네임드 쿼리를 찾고 없으면 메서드이름으로 쿼리 생성(1번)을 함 순서가 있음
    //실무에서 거의 사용을 안하는 이유는 (엔티티에 있는 것도 별로(따로 빼는방법이 있지만)
    //그다음 나오는 기능이 너무막강해서
    //장점 : 일반 JPQL은 문자열이라 where절에 이상한 문자를 쳐도 오류가 안나서 실행되어서
    // 고객이 사용할때 오류가 남
    // 하지만 네임드쿼리는 애플리케이션 실행시점에 파싱을 해봄으로써 이 시점에 오류를 날려줌
    // 기본적으로 정적쿼리기 때문에 미리 파싱을 해볼수가있음
    List<Member> findByUsername(@Param("username") String username);

    //3. 네임드쿼리의 장점을 다 가지고있으면서도 리포지토리에 JQPL 쿼리를 작성할수있는 방법
    //실무에서 많이 씀
    //이름이 없는 네임드쿼리라 생각하면 된다.
    //정적 쿼리라 파싱을 해서 sql을 다 만들어놓는데 오타나면 잡아줌
    //간단한것은 1번 사용 복잡한것은 3번을 사용하고 메서드이름 간단하게 작성
    //동적쿼리는 querydsl사용 -> 가장 깔끔하고 유지보수성에도 좋음
    @Query("select m from Member m where m.username = :username and m.age = :age")
    List<Member> findUser(@Param("username") String username, @Param("age") int age);


    /**
     * DTO 조회 ex 사용자 이름 리스트만 다가져오고 싶을경우
     * 이름,나이 팀 등 복잡한 것을 가지고 오고싶을경우는 DTO를 사용해야함
     */
    @Query("select m.username from Member m")
    List<String> findUsernameList();


    //DTO로 조회할 경우 반드시 JPQL에 new 연산을 써줘야함 생성자로 매칭 그래서 생성자 필요
    @Query("select new study.datajpa.dto.MemberDto(m.id, m.username, t.name) from Member m join m.team t")
    List<MemberDto> findMemberDto();

    @Query("select m from Member m where m.username in :names")
    List<Member> findByNames(@Param("names") List<String> names);

    //유연한 반환타입
    List<Member> findListByUsername(String username); //컬렉션
    Member findMemberByUsername(String username); //단건
    Optional<Member> findOptionalByUsername(String username); //단건의 Optional


    /**
     * Spring Data Jpa의 페이징
     * page = totalCount 쿼리 같이 날림 slice = totalCount 쿼리 안날림( 더보기 버튼으로 계속 가져오는경우)
     *
     */
    @Query(value = "select m from Member m left join m.team",
            countQuery = "select count(m.username) from Member m") //카운트 쿼리 분리 하기 굳이 조인이 필요없으니까
    Page<Member> findByAge(int age, Pageable pageable);


    /**
     * Spring data Jpa를 이용한 벌크 수정
     * @Modifying(clearAutomatically = true) : 이런 벌크성 업데이트에서는 주의할 점이 있다.
     *              Jpa는 영속성컨텍스트 개념이있어 Entity들이 관리가 되는데
     *              벌크 연산은 그런것을 다 무시하고 DB에 다이렉트로 쿼리를 날림
     *              저장 후 업데이트해주면 저장시의 데이터가 영속성컨텍스트로 남아있기때문에
     *              반영된것을 받아오지못함
     *              -> 해결하려면 벌크연산한 후에 영속성컨텍스트를 다 날리면된다.(Modifying 옵션이 없을경우에는)
     *                 em.clear;
     *                 cf) 벌크연산 후 Api가 끝나면 상관없는데 같은 트랜잭션에서 그다음 로직이 있으면 주의해야함
     *                      jpql적으면 실행먼저 하고 그다음 jpql 실행됨
     *                 cf) 직접 순수한 JDBC,Mybatis랑 섞어 사용하는 경우도 마찬가지
     *                     -> JDBC, Mybatis가 직접 쿼리날리는것을 JPA는 인식을 못하기 때문 영속성컨텍스트의 내용과 안맞음
     *                       Mybatis에서 쿼리를 날리기전에 flush해주고?                 */

    @Modifying(clearAutomatically = true)   //이걸 해줘야 executeUpdate()를 함 없으면 getSingleResult나 getResultList()를 호출
    @Query("update Member m set m.age = m.age + 1 where m.age >= :age")
    int bulkAgePlus(@Param("age") int age);

    /**
     * N + 1문제를 막기위해 fetch join을 이용하는데
     * 이는 JPQL를 직접 작성해야 된다는 번거로움이 있다.
     * 미리 구현된 Spring Data Jpa의 findAll을 오버라이드 하여
     * 엔티티그래프를 이용하면 멤버를 조회하는 FindAll이면서도 관련된 연관관계의 데이터까지 전부 가져올수있다.
     */
    @Query("select m from Member m left join fetch m.team")
    List<Member> findMemberFetchJoin();

    //1. Entity그래프만 사용
    @Override
    @EntityGraph(attributePaths = {"team"})
    List<Member> findAll();

    //2. JPQL를 짯는데 엔티티그래프를 사용할수도있음
    @EntityGraph(attributePaths = {"team"})
    @Query("select m from Member m")
    List<Member> findMemberEntityGraph();

    // 메서드이름 기반 쿼리로 엔티티그래프 사용하는 방법
    @EntityGraph(attributePaths = {"team"})
    List<Member> findEntityGraphByUsername(@Param("username") String username);

    //NamedEntityGraph 사용 하는 방법 : Member 엔티티에 작성해주고 그걸 사용하면 됨.
    @EntityGraph("Member.all")
    List<Member> findEntityGraph2ByUsername(@Param("username") String username);
    /**
     * 복잡한 쿼리는 JPQL 사용
     * 간단한건데 JPQL 사용하기 싫으면 이러한 EntityGraph 등을 이용하자 
     */

    /**
     * Jpa Criteria를 이용한 Specification 기능 ( 실무에서는 잘사용하지않음 해석하기 어려움 )
     * JpaSpecificationExecutor<Member>를 상속받으면 됨.
     */


    <T> List<T> findProjectionsByUsername(@Param("username") String username, Class<T> type);


    /**
     * 네이티브 쿼리
     * 한계가 많다 : Member data를 셀렉트에 다 찍어야된다.
     * 반환타입이 몇가지 지원이안된다.
     * 정렬 정상 동작하지 않을 수 있따(믿지말고 직접 처리)
     * JPQL처럼 애플리케이션 로딩 시점에 문법 확인 불가
     * 동적 쿼리불가
     * -> 네이티브 SQL을 DTO로 조회할때는 JDBCTempleate이나 Mybatis를 사용
     */
    @Query(value = "select * from member where username = ?", nativeQuery = true)
    Member findByNativeQuery(String username);

    @Query(value = "select m.member_id as id,m.username,t.name as teamName" +
            " from member m left join team t",countQuery = "select count(*) from member",nativeQuery = true)
    Page<MemberProjection> findByNativeProjection(Pageable pageable);
}
