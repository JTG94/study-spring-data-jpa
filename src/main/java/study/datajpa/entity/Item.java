package study.datajpa.entity;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDateTime;

/**
 * 새로운 엔티티인가 아닌가를 구별하는 방법
 * 예를들어 @Id @GeneratedValue 같은경우는 당연히 디비에 값이 없으므로
 * Spring Data Jpa save 구현체를 보면 객체가 널인지 확인하고 널이면 persist 아니면 merge를 실행한다.
 * merge 같은경우에는 셀렉트로 디비를 한번더 조회하고 그다음 작업을 하므로 비효율적이다.
 * 객체를 생성할때 PK값을 AutoIncrements가 아닌 직접 넣어줄경우 PK값이 있다고 판단하여 merge를 실행하는데
 * 이때 Persistable<String>을 상속받아 getId와 isNew를 오버라이드하여 구현해주면 된다.
 * 자주 사용하는 방법으로는 createdDate를 하나 만들어서 이값이 널인경우가 새로운 객체라고 판단하여
 * 해주는 방법으로 사용하면 된다.
 */

@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item implements Persistable<String> {

    @Id
    private String id;

    @CreatedDate
    private LocalDateTime createdDate;

    public Item(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return createdDate==null;
    }
}
