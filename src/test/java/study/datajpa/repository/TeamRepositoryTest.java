package study.datajpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.entity.Team;

import static org.assertj.core.api.Assertions.*;

//필수적인 SpringBootTest 와 Transactional을 어노테이션한다음
@SpringBootTest
@Transactional
class TeamRepositoryTest {

    //먼저 EntitiyManager을 주입받음 -> Entity를 컨트롤 하기위해
    @Autowired TeamJpaRepository teamJpaRepository;

    @Test
    public void testTeam() {
        Team team = new Team("Team1");
        Team savedTeam = teamJpaRepository.save(team);

        Team findTeam = teamJpaRepository.find(savedTeam.getId());

        assertThat(findTeam.getId()).isEqualTo(team.getId());
        assertThat(findTeam.getName()).isEqualTo(team.getName());

        assertThat(findTeam).isEqualTo(team);


    }

}