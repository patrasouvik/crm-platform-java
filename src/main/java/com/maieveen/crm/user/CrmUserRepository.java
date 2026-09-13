package com.maieveen.crm.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CrmUserRepository extends JpaRepository<CrmUser, Long> {

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByPhone(String phone);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    boolean existsByPhoneAndIdNot(String phone, Long id);

    @Query("select u from CrmUser u where lower(u.firstName) like lower(concat('%', :q, '%')) " +
           "or lower(u.lastName) like lower(concat('%', :q, '%')) " +
           "or lower(u.email) like lower(concat('%', :q, '%')) " +
           "or u.phone like concat('%', :q, '%') order by u.id desc")
    List<CrmUser> search(@Param("q") String query);
}
