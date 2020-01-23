package study.datajpa.repository;

import org.springframework.beans.factory.annotation.Value;

public interface UsernameOnly {

    @Value("#{target.username + '  ' + target.age}") //Open Projection 지원 일단 다 퍼올린다음에 애플리케이션에서 조절

    String getUsername(); //Projection 정확히 매칭되면 셀렉트절 최적화 가능
}
