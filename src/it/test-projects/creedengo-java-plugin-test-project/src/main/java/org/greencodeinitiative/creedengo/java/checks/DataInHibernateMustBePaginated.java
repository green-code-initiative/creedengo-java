package org.greencodeinitiative.creedengo.java.checks;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

// L'interface doit être package-private (pas "public") pour rester dans ce fichier
@Repository
interface DataInHibernateMustBePaginated extends JpaRepository<Object, Long> {

    List<Object> findAll(); // Noncompliant {{Hibernate queries must be paginated to avoid excessive data loading}}

    @Query(value = "SELECT * FROM users", nativeQuery = true)
    List<Object> loadAll(); // Noncompliant {{Hibernate queries must be paginated to avoid excessive data loading}}

    @Query("SELECT u FROM User u")
    List<Object> getUsers(); // Noncompliant {{Hibernate queries must be paginated to avoid excessive data loading}}

    Page<Object> findAll(Pageable pageable); // OK

    @Query(value = "SELECT * FROM users", countQuery = "SELECT count(*) FROM users", nativeQuery = true)
    Page<Object> findAllNative(Pageable pageable); // OK
}

