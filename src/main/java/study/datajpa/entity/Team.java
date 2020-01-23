package study.datajpa.entity;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of = {"id", "name"})
public class Team {

    @Id @GeneratedValue
    @Column(name = "team_id")
    private Long id;
    private String name;

    @OneToMany(mappedBy = "team")  //둘다 세팅을 걸면 한쪽에 mappedBy로 주인을 정해줘야함 양방향 (FK없는 쪽에 걸어주라)
    private List<Member> members = new ArrayList<>();

    public Team(String name) {
        this.name = name;
    }
}
