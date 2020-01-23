package study.datajpa.entity;


import lombok.*;


import javax.persistence.*;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of = {"id", "username", "age"}) // 객체를 바로찍을때 출력되게끔 하기 위해
//team 을 안넣어주는 이유는 출력을 할때 연관관계를 타서 출력을 하게 되므로 무한루프에 빠짐
//가급적이면 연관관계 필드는 Tostring에서 제외하는게 좋다.
@NamedQuery(
        name = "Member.findByUsername",
        query = "select m from Member m where m.username = :username"
)
@NamedEntityGraph(name = "Member.all", attributeNodes = @NamedAttributeNode("team"))
public class Member {

    @Id @GeneratedValue
    @Column(name = "member_id") //Entity는 식별 클래스가있어 id로 쓰지만 디비에는 member_id로 저장하기 위해 지정
    private Long id;
    private String username;
    private int age;

    //ManyToOne은 fetch 디폴트가 EAGER라 LAZY로 필수로 바꿔줄것
    //EAGER가 걸려있으면 성능최적하기 어려워진다.
    //지연로딩 : 멤버를 조회할때 딱 멤버만 조회 팀은 가짜 객체로 가지고있다가
    //팀의 값을 정말 사용할때(실제 안의 값을 볼때) 그떄 팀으로 쿼리 날림
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id") //FK 명
    private Team team;







    //기본생성자가 있어야지 밑에 생성자를 만들수있음(JPA이기때문에?) 대신 아무대서나 생성
    //못하게 protected 걸어줌.
    //JPA 표준 스택에서 Entity는 반드시 기본생성자가 있어야한다(파라미터 없는)
    //private으로 만들면안되고 protected까지 열어놔야한다.
    //->JPA가 프록시 기술을 사용하는데 이때 사용함 private으로 하면 사용못해서 에러남
//    protected Member() {
//
//    }
    //지우고 맨위에 어노테이션으로 @NoArgsConstructor(access = AccessLevel.PROTECTED) 적어주면 해결 (롬복사용)

    public Member(String username) {
        this.username = username;
    }

    public Member(String username, int age, Team team) {
        this.username = username;
        this.age = age;

        //파라미터로 너머오는 team이 null일경우를 처리해줘야됨 그런데 일단은 무시쪽으로 작성
        if( team != null) {
            changeTeam(team);
        }
    }

    public Member(String username, int age) {
        this.username = username;
        this.age = age;
    }

//    //Setter를 쓰지말고 이런식으로 메서드를 생성해서 하도록 하자
//    public void changeUsername(String username) {
//        this.username = username;
//    }

    //연관관계를 세팅하는 메서드를 만들어줘야한다.
    //멤버가 팀을 변경할 경우
    public  void changeTeam(Team team) {
        this.team = team;
        //팀에가서도 팀에있는 멤버에도 세팅을 걸어줘야함
        //객체이기 때문에 내쪽만 바꾸는게 아니라 반대쪽 사이드도 바꿔준다.
        team.getMembers().add(this);
    }
}
