package com.example.ServiceBooking.auth;



import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    List<User> findByRole(Role role);

    long countByRole(Role role);
    long countByCity(String city);
    long countByRoleAndCity(Role role, String city);
    Page<User> findByRole(Role role, Pageable pageable);
    Page<User> findByRoleAndCity(Role role, String city, Pageable pageable);

    boolean existsByEmail(String email);


    // helper query for name search
    @Query("select u.id from User u where lower(u.name) like lower(concat('%', :name, '%'))")
    List<Long> findUserIdsByNameLike(@Param("name") String name);
}


