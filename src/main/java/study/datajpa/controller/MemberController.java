package study.datajpa.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.repository.MemberRepository;

import javax.annotation.PostConstruct;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberRepository memberRepository;

    /**
     *
     * 이 아이디가 어차피 PK가 들어간 것이므로 "도메인 클래스 컨버터"를 쓸수 있다.
     * 스프링이 중간에서 파라미터 결과로 인젝션 해 줌
     * 도메인 클래스 컨버터를 쓸 경우에는 반드시 조회용으로만 사용해야된다
     * -> 트랜잭션이 없는 상황에서 사용한 것이기 떄문에 영속성컨텍스트가 애매하다.
     *
     */
    @GetMapping("/members2/{id}") // 요새 pathvariable 기반으로 많이 짠다.
    public String findMember2(@PathVariable("id") Member member) {
        return member.getUsername();
    }

    @GetMapping("/members/{id}") // 요새 pathvariable 기반으로 많이 짠다.
    public String findMember(@PathVariable("id") Long id) {
        return memberRepository.findById(id).get().getUsername();
    }

    /**
     *
     * 웹에서도 페이징과 정렬을 지원
     * 파라미터로 마지막에 pageable만 넘겨주면 됨
     * 이렇게 하면 /members?page=0하면 20개만 꺼내옴 자동으로
     * URL에 /members?page=0&size=3 하면 한페이지에 3개만 불러옴
     * URL에 /members?page=0&size=3&sort=id,desc 하면 id순으로 정렬함 추가로 정렬조건 붙일수있음
     *
     * 디폴트가 20개인데 바꿀수있다
     * 1. 글로벌 설정 yml에 세팅
     *  data:
     *     web:
     *       pageable:
     *         default-page-size: 10
     *         max-page-size: 2000
     *
     * 2. 특정부분에만 적용하고 싶을 경우
     * @PageableDefault 어노테이션 사용
     *
     * 엔티티는 DTO를 가급적 보지않는게 좋다? DTO는 엔티티봐도 괜찮다.
     *
     * 만약 DTO 생성자로 엔티티를 받아서 할경우에는 MemberDto::new 처럼 메서드 레퍼런스를 써서
     * 코드를 간략화 할 수 있다.
     *
     * 스프링은 페이지가 0부터 시작 1부터 시작하려면?
     * 두가지방법이있다.
     * 1. Page를 새로 정의 해서 하는 법 (궁극적인 방법)
     * 2. yml에다가 one-indexed-parameters: true 추가 -> but 한계가 있다.
     *    -> 밑에 추가로 전달되는 json 데이터와 맞지않음 0을 베이스로 보내기때문
     * 3. 가급적 0으로 그냥 쓰는것이 좋음 깔끔끔     *
     *
     */
    @GetMapping("/members")
    public Page<MemberDto> list(@PageableDefault(size=5) Pageable pageable) {
        //현재 엔티티를 반환하고있는데 그러면 안된다. 특히 API에서  DTO로 변환 하도록
        Page<Member> page = memberRepository.findAll(pageable);
        Page<MemberDto> map = page.map(MemberDto::new);
        return map;
    }

    //@PostConstruct
    public  void init() {
        for(int i =0 ; i< 100; i++) {
            memberRepository.save(new Member("user" + i, i));
        }
    }
}
