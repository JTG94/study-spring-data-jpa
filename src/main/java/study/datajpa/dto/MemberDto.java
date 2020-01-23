package study.datajpa.dto;

import lombok.Data;
import study.datajpa.entity.Member;

@Data //DTO는 @Data를 사용해도 되지만 Entitiy에서는 지양할 것
public class MemberDto {

    //조회하고 싶은 대상 작성
    private Long id;
    private String username;
    private String teamName;

    public MemberDto(Long id, String username, String teamName) {
        this.id = id;
        this.username = username;
        this.teamName = teamName;
    }

    //엔티티를 참조해서 DTO 하는법
    public MemberDto(Member member) {
        this.id = member.getId();
        this.username = member.getUsername();
    }
}
